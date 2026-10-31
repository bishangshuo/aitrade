package com.aitrade.common.utils;

import java.util.concurrent.CancellationException;

/** 绑定当前AI任务的取消令牌和HTTP中止动作，未绑定的旧业务调用保持原行为。 */
public final class AiTaskExecutionContext {

    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    /** 创建工具类。 */
    private AiTaskExecutionContext() {
    }

    /**
     * 为当前执行线程绑定任务上下文。
     *
     * @param taskId 对外任务编号
     * @param activeCheck 校验数据库任务仍可执行的动作
     * @return 当前线程的任务作用域
     */
    static Scope bind(String taskId, Runnable activeCheck) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("当前线程已经绑定AI任务");
        }
        Scope scope = new Scope(taskId, activeCheck);
        CURRENT.set(scope);
        return scope;
    }

    /**
     * 取得当前执行线程绑定的任务编号。
     *
     * @return 任务编号；普通业务线程返回null
     */
    public static String getTaskId() {
        Scope scope = CURRENT.get();
        return scope == null ? null : scope.taskId;
    }

    /** 校验当前任务未取消；未绑定任务的业务线程不增加取消逻辑。 */
    public static void ensureActive() {
        Scope scope = CURRENT.get();
        if (scope != null) {
            scope.ensureActive();
        }
    }

    /**
     * 判断当前作用域是否已收到用户取消信号，不访问数据库。
     *
     * @return 已收到取消信号时返回true；普通业务线程返回false
     */
    public static boolean isCancellationRequested() {
        Scope scope = CURRENT.get();
        if (scope == null) {
            return false;
        }
        synchronized (scope) {
            return scope.cancelled;
        }
    }

    /**
     * 进入任务收尾阶段，清除用户取消产生的中断标志以允许获取数据库连接。
     * 取消令牌仍然有效，禁止继续业务；服务关闭产生的中断标志保持不变。
     */
    public static void beginCleanup() {
        Scope scope = CURRENT.get();
        if (scope != null) {
            synchronized (scope) {
                scope.finishing = true;
                scope.abortAction = null;
                if (scope.cancelled) {
                    Thread.interrupted();
                }
            }
        }
    }

    /**
     * 注册当前阻塞请求的中止动作，取消与请求建立并发时仍可正确中止。
     *
     * @param abortAction HTTP请求的中止动作
     */
    public static void registerAbortAction(Runnable abortAction) {
        Scope scope = CURRENT.get();
        if (scope != null) {
            scope.registerAbortAction(abortAction);
        }
    }

    /** 清除已经结束的HTTP请求中止动作。 */
    public static void clearAbortAction() {
        Scope scope = CURRENT.get();
        if (scope != null) {
            synchronized (scope) {
                scope.abortAction = null;
            }
        }
    }

    /** 单次任务执行作用域，关闭时清除任务线程的取消中断标志。 */
    public static final class Scope implements AutoCloseable {

        private final String taskId;
        private final Thread thread = Thread.currentThread();
        private final Runnable activeCheck;
        private boolean cancelled;
        private boolean closed;
        private boolean finishing;
        private Runnable abortAction;

        /**
         * 创建单次执行作用域。
         *
         * @param taskId 对外任务编号
         * @param activeCheck 数据库状态检查动作
         */
        private Scope(String taskId, Runnable activeCheck) {
            this.taskId = taskId;
            this.activeCheck = activeCheck;
        }

        /** 校验本地取消令牌、线程中断和数据库中的任务状态。 */
        private void ensureActive() {
            ensureNotCancelled();
            activeCheck.run();
            ensureNotCancelled();
        }

        /** 校验取消信号，防止HTTP返回或重试时继续处理。 */
        private synchronized void ensureNotCancelled() {
            if (cancelled || closed || finishing || thread.isInterrupted()) {
                throw new CancellationException("AI任务已取消或执行已中断");
            }
        }

        /**
         * 为当前请求安装中止动作；已取消时先关闭请求再拒绝执行。
         *
         * @param action HTTP请求中止动作
         */
        private synchronized void registerAbortAction(Runnable action) {
            if (cancelled || closed || finishing || thread.isInterrupted()) {
                action.run();
                throw new CancellationException("AI任务已取消");
            }
            abortAction = action;
        }

        /** 中止当前HTTP请求并中断执行线程；已结束的作用域不影响复用线程。 */
        synchronized void cancel() {
            if (closed || cancelled) {
                return;
            }
            cancelled = true;
            if (finishing) {
                // 业务已退出，迟到的跨实例取消通知不能再中断收尾SQL。
                return;
            }
            try {
                if (abortAction != null) {
                    abortAction.run();
                }
            } finally {
                thread.interrupt();
            }
        }

        /** 释放线程绑定，避免取消一个任务后影响下一条Stream任务。 */
        @Override
        public synchronized void close() {
            closed = true;
            abortAction = null;
            if (CURRENT.get() == this) {
                CURRENT.remove();
                if (cancelled) {
                    Thread.interrupted();
                }
            }
        }
    }
}

