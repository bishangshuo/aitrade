<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="賬戶編號" prop="accountNo">
        <el-input
          v-model="queryParams.accountNo"
          placeholder="请输入賬戶編號"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="業務發生日期" prop="tradeDate">
        <el-date-picker clearable
          v-model="queryParams.tradeDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择業務發生日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="變動金額" prop="amount">
        <el-input
          v-model="queryParams.amount"
          placeholder="请输入變動金額"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="變動後的可用現金餘額" prop="balanceAfter">
        <el-input
          v-model="queryParams.balanceAfter"
          placeholder="请输入變動後的可用現金餘額"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="關聯的訂單編號/流水號" prop="relatedOrderId">
        <el-input
          v-model="queryParams.relatedOrderId"
          placeholder="请输入關聯的訂單編號/流水號"
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
          v-hasPermi="['stock:tffundflow:add']"
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
          v-hasPermi="['stock:tffundflow:edit']"
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
          v-hasPermi="['stock:tffundflow:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:tffundflow:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tffundflowList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主鍵 ID" align="center" prop="id" />
      <el-table-column label="賬戶編號" align="center" prop="accountNo" />
      <el-table-column label="業務發生日期" align="center" prop="tradeDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.tradeDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="業務類型：DEPOSIT-入金, WITHDRAW-出金, BUY-買入扣款, SELL-賣出回款, COMMISSION-傭金, TAX-印花稅" align="center" prop="bizType" />
      <el-table-column label="變動金額" align="center" prop="amount" />
      <el-table-column label="變動後的可用現金餘額" align="center" prop="balanceAfter" />
      <el-table-column label="關聯的訂單編號/流水號" align="center" prop="relatedOrderId" />
      <el-table-column label="備註說明" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:tffundflow:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:tffundflow:remove']"
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

    <!-- 添加或修改回測資金流水明細对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="賬戶編號" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="请输入賬戶編號" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="業務發生日期" prop="tradeDate">
              <el-date-picker clearable
                v-model="form.tradeDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择業務發生日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="變動金額" prop="amount">
              <el-input v-model="form.amount" placeholder="请输入變動金額" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="變動後的可用現金餘額" prop="balanceAfter">
              <el-input v-model="form.balanceAfter" placeholder="请输入變動後的可用現金餘額" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="關聯的訂單編號/流水號" prop="relatedOrderId">
              <el-input v-model="form.relatedOrderId" placeholder="请输入關聯的訂單編號/流水號" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="備註說明" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入備註說明" />
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
import { listTffundflow, getTffundflow, delTffundflow, addTffundflow, updateTffundflow } from "@/api/stock/tffundflow"

export default {
  name: "Tffundflow",
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
      // 回測資金流水明細表格数据
      tffundflowList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountNo: null,
        tradeDate: null,
        bizType: null,
        amount: null,
        balanceAfter: null,
        relatedOrderId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        accountNo: [
          { required: true, message: "賬戶編號不能为空", trigger: "blur" }
        ],
        tradeDate: [
          { required: true, message: "業務發生日期不能为空", trigger: "blur" }
        ],
        bizType: [
          { required: true, message: "業務類型：DEPOSIT-入金, WITHDRAW-出金, BUY-買入扣款, SELL-賣出回款, COMMISSION-傭金, TAX-印花稅不能为空", trigger: "change" }
        ],
        amount: [
          { required: true, message: "變動金額不能为空", trigger: "blur" }
        ],
        balanceAfter: [
          { required: true, message: "變動後的可用現金餘額不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回測資金流水明細列表 */
    getList() {
      this.loading = true
      listTffundflow(this.queryParams).then(response => {
        this.tffundflowList = response.rows
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
        accountNo: null,
        tradeDate: null,
        bizType: null,
        amount: null,
        balanceAfter: null,
        relatedOrderId: null,
        remark: null,
        createTime: null
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
      this.title = "添加回測資金流水明細"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTffundflow(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回測資金流水明細"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTffundflow(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTffundflow(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回測資金流水明細编号为"' + ids + '"的数据项？').then(function() {
        return delTffundflow(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/tffundflow/export', {
        ...this.queryParams
      }, `tffundflow_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
