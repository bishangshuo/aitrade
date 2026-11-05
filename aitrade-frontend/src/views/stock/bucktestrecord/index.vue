<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="回测编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入回测编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="回测名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入回测名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开始日期" prop="fromDate">
        <el-date-picker clearable
          v-model="queryParams.fromDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束日期" prop="toDate">
        <el-date-picker clearable
          v-model="queryParams.toDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="主升浪策略配置ID" prop="cnfStrongBreakoutId">
        <el-input
          v-model="queryParams.cnfStrongBreakoutId"
          placeholder="请输入主升浪策略配置ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="底部反转策略配置ID" prop="cnfBottomPullbackId">
        <el-input
          v-model="queryParams.cnfBottomPullbackId"
          placeholder="请输入底部反转策略配置ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="震荡攀升策略参数配置ID" prop="cnfPullbackLowSuckId">
        <el-input
          v-model="queryParams.cnfPullbackLowSuckId"
          placeholder="请输入震荡攀升策略参数配置ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="超跌反弹策略配置ID" prop="cnfOversoldReboundId">
        <el-input
          v-model="queryParams.cnfOversoldReboundId"
          placeholder="请输入超跌反弹策略配置ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易策略配置ID" prop="cnfTradeId">
        <el-input
          v-model="queryParams.cnfTradeId"
          placeholder="请输入交易策略配置ID"
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
          v-hasPermi="['stock:bucktestrecord:add']"
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
          v-hasPermi="['stock:bucktestrecord:edit']"
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
          v-hasPermi="['stock:bucktestrecord:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:bucktestrecord:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bucktestrecordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="回测编码" align="center" prop="code" />
      <el-table-column label="回测名称" align="center" prop="name" />
      <el-table-column label="开始日期" align="center" prop="fromDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.fromDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束日期" align="center" prop="toDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.toDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="主升浪策略配置ID" align="center" prop="cnfStrongBreakoutId" />
      <el-table-column label="底部反转策略配置ID" align="center" prop="cnfBottomPullbackId" />
      <el-table-column label="震荡攀升策略参数配置ID" align="center" prop="cnfPullbackLowSuckId" />
      <el-table-column label="超跌反弹策略配置ID" align="center" prop="cnfOversoldReboundId" />
      <el-table-column label="交易策略配置ID" align="center" prop="cnfTradeId" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:bucktestrecord:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:bucktestrecord:remove']"
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

    <!-- 添加或修改回测记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="回测编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入回测编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="回测名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入回测名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开始日期" prop="fromDate">
              <el-date-picker clearable
                v-model="form.fromDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择开始日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结束日期" prop="toDate">
              <el-date-picker clearable
                v-model="form.toDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择结束日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主升浪策略配置ID" prop="cnfStrongBreakoutId">
              <el-input v-model="form.cnfStrongBreakoutId" placeholder="请输入主升浪策略配置ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="底部反转策略配置ID" prop="cnfBottomPullbackId">
              <el-input v-model="form.cnfBottomPullbackId" placeholder="请输入底部反转策略配置ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="震荡攀升策略参数配置ID" prop="cnfPullbackLowSuckId">
              <el-input v-model="form.cnfPullbackLowSuckId" placeholder="请输入震荡攀升策略参数配置ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="超跌反弹策略配置ID" prop="cnfOversoldReboundId">
              <el-input v-model="form.cnfOversoldReboundId" placeholder="请输入超跌反弹策略配置ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易策略配置ID" prop="cnfTradeId">
              <el-input v-model="form.cnfTradeId" placeholder="请输入交易策略配置ID" />
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
import { listBucktestrecord, getBucktestrecord, delBucktestrecord, addBucktestrecord, updateBucktestrecord } from "@/api/stock/bucktestrecord"

export default {
  name: "Bucktestrecord",
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
      // 回测记录表格数据
      bucktestrecordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        code: null,
        name: null,
        fromDate: null,
        toDate: null,
        cnfStrongBreakoutId: null,
        cnfBottomPullbackId: null,
        cnfPullbackLowSuckId: null,
        cnfOversoldReboundId: null,
        cnfTradeId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        code: [
          { required: true, message: "回测编码不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "回测名称不能为空", trigger: "blur" }
        ],
        fromDate: [
          { required: true, message: "开始日期不能为空", trigger: "blur" }
        ],
        toDate: [
          { required: true, message: "结束日期不能为空", trigger: "blur" }
        ],
        cnfStrongBreakoutId: [
          { required: true, message: "主升浪策略配置ID不能为空", trigger: "blur" }
        ],
        cnfBottomPullbackId: [
          { required: true, message: "底部反转策略配置ID不能为空", trigger: "blur" }
        ],
        cnfPullbackLowSuckId: [
          { required: true, message: "震荡攀升策略参数配置ID不能为空", trigger: "blur" }
        ],
        cnfOversoldReboundId: [
          { required: true, message: "超跌反弹策略配置ID不能为空", trigger: "blur" }
        ],
        cnfTradeId: [
          { required: true, message: "交易策略配置ID不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回测记录列表 */
    getList() {
      this.loading = true
      listBucktestrecord(this.queryParams).then(response => {
        this.bucktestrecordList = response.rows
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
        code: null,
        name: null,
        fromDate: null,
        toDate: null,
        cnfStrongBreakoutId: null,
        cnfBottomPullbackId: null,
        cnfPullbackLowSuckId: null,
        cnfOversoldReboundId: null,
        cnfTradeId: null,
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
      this.title = "添加回测记录"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getBucktestrecord(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回测记录"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateBucktestrecord(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addBucktestrecord(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回测记录编号为"' + ids + '"的数据项？').then(function() {
        return delBucktestrecord(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/bucktestrecord/export', {
        ...this.queryParams
      }, `bucktestrecord_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
