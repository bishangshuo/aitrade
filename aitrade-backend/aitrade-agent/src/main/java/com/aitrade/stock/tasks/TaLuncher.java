package com.aitrade.stock.tasks;

import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.domain.TaTask;
import com.aitrade.stock.enums.TaskStatusEnum;
import com.aitrade.stock.handler.DayTaResultListener;
import com.aitrade.stock.mapper.TaTaskMapper;
import com.aitrade.stock.service.IStockTaService;
import com.aitrade.tickflow.domain.TfStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class TaLuncher {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Autowired
    private IStockTaService stockTaService;

    @Autowired
    private TaTaskMapper taTaskMapper;

    @Scheduled(fixedDelay = 4000)
    public void start() {
        //如果正在运行中，则返回
        if (isRunning.get()) {
            log.info("任务正在运行中，返回");
            return;
        }
        //查询状态为“未开始”或“进行中”的任务
        //进行中状态，是因为中途意外或取消导致
        List<TaTask> pendingTasks = taTaskMapper.getPendingTasks();
        if(pendingTasks == null || pendingTasks.isEmpty()) {
            log.info("没有找到待运行的任务");
            isRunning.set(false);
            return;
        }
        boolean bingo = false;
        //现在时刻
        Date nowTime = new Date();
        //遍历任务列表
        for (TaTask task : pendingTasks) {
            //如果任务启动截止时间已经过期, 不论状态是“未开始”还是“进行中”
            if(task.getStartEndTime().before(nowTime)) {
                task.setStatus(TaskStatusEnum.EXPIRED.getStatus());
                taTaskMapper.updateTaTask(task);
                log.info("任务{}已过期", task.getId());
                continue;
            }
            //不然，如果任务启动截止时间未过期，即 task.getStartBeginTime <= nowTime <= task.getStartEndTime,则启动任务
            if (task.getStartBeginTime().compareTo(nowTime) <= 0
                    && nowTime.compareTo(task.getStartEndTime()) <= 0) {
                bingo = true;
                launchTask(task, isRunning);
                //只启动一个任务
                break;
            }
        }
        if(!bingo) {
            isRunning.set(false);
        }
    }

    private void launchTask(TaTask task, AtomicBoolean isRunning) {
        LocalDate klineDate = task.getStartBeginTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        stockTaService.startTA(klineDate, isRunning, new DayTaResultListener() {
            @Override
            public void onComplete() {
                log.info("Ta任务{}完成", task.getId());
            }

            @Override
            public void stockSelectResult(TfStock stock, StockPickDiagnostic diagnostic) {

            }


        });
    }
}
