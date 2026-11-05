<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="T+1高开幅度上限" prop="t1HighOpenRatio">
        <el-input
          v-model="queryParams.t1HighOpenRatio"
          placeholder="请输入T+1高开幅度上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="持仓天数" prop="holdDays">
        <el-input
          v-model="queryParams.holdDays"
          placeholder="请输入持仓天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="止损幅度" prop="stopLossRatio">
        <el-input
          v-model="queryParams.stopLossRatio"
          placeholder="请输入止损幅度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="止盈方法:1固定幅度，2动态幅度" prop="stopProfitMethod">
        <el-input
          v-model="queryParams.stopProfitMethod"
          placeholder="请输入止盈方法:1固定幅度，2动态幅度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算" prop="stopProfitRatio">
        <el-input
          v-model="queryParams.stopProfitRatio"
          placeholder="请输入止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['stock:strategytradeconfig:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['stock:strategytradeconfig:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['stock:strategytradeconfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:strategytradeconfig:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="strategytradeconfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="T+1高开幅度上限" align="center" prop="t1HighOpenRatio" />
      <el-table-column label="持仓天数" align="center" prop="holdDays" />
      <el-table-column label="止损幅度" align="center" prop="stopLossRatio" />
      <el-table-column label="止盈方法:1固定幅度，2动态幅度" align="center" prop="stopProfitMethod" />
      <el-table-column label="止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算" align="center" prop="stopProfitRatio" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:strategytradeconfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:strategytradeconfig:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改交易策略参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="T+1高开幅度上限" prop="t1HighOpenRatio">
              <el-input v-model="form.t1HighOpenRatio" placeholder="请输入T+1高开幅度上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="持仓天数" prop="holdDays">
              <el-input v-model="form.holdDays" placeholder="请输入持仓天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="止损幅度" prop="stopLossRatio">
              <el-input v-model="form.stopLossRatio" placeholder="请输入止损幅度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="止盈方法:1固定幅度，2动态幅度" prop="stopProfitMethod">
              <el-input v-model="form.stopProfitMethod" placeholder="请输入止盈方法:1固定幅度，2动态幅度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算" prop="stopProfitRatio">
              <el-input v-model="form.stopProfitRatio" placeholder="请输入止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStrategytradeconfig, getStrategytradeconfig, delStrategytradeconfig, addStrategytradeconfig, updateStrategytradeconfig } from "@/api/stock/strategytradeconfig"

export default {
  name: "Strategytradeconfig",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 交易策略参数配置表格数据
      strategytradeconfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        t1HighOpenRatio: null,
        holdDays: null,
        stopLossRatio: null,
        stopProfitMethod: null,
        stopProfitRatio: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询交易策略参数配置列表 */
    getList() {
      this.loading = true
      listStrategytradeconfig(this.queryParams).then(response => {
        this.strategytradeconfigList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        t1HighOpenRatio: null,
        holdDays: null,
        stopLossRatio: null,
        stopProfitMethod: null,
        stopProfitRatio: null,
        createTime: null,
        updateTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加交易策略参数配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getStrategytradeconfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改交易策略参数配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStrategytradeconfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStrategytradeconfig(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除交易策略参数配置编号为"' + ids + '"的数据项？').then(function() {
        return delStrategytradeconfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/strategytradeconfig/export', {
        ...this.queryParams
      }, `strategytradeconfig_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
