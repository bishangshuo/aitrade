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
      <el-form-item label="訂單編號" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入訂單編號"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="股票代碼" prop="symbol">
        <el-input
          v-model="queryParams.symbol"
          placeholder="请输入股票代碼"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="股票名稱" prop="stockName">
        <el-input
          v-model="queryParams.stockName"
          placeholder="请输入股票名稱"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易方向：BUY-買入, SELL-賣出" prop="tradeSide">
        <el-input
          v-model="queryParams.tradeSide"
          placeholder="请输入交易方向：BUY-買入, SELL-賣出"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成交價格" prop="tradePrice">
        <el-input
          v-model="queryParams.tradePrice"
          placeholder="请输入成交價格"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成交股數" prop="tradeVolume">
        <el-input
          v-model="queryParams.tradeVolume"
          placeholder="请输入成交股數"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成交總金額" prop="tradeAmount">
        <el-input
          v-model="queryParams.tradeAmount"
          placeholder="请输入成交總金額"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易傭金" prop="commission">
        <el-input
          v-model="queryParams.commission"
          placeholder="请输入交易傭金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="印花稅" prop="tax">
        <el-input
          v-model="queryParams.tax"
          placeholder="请输入印花稅"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平倉實現盈虧" prop="realizedPnl">
        <el-input
          v-model="queryParams.realizedPnl"
          placeholder="请输入平倉實現盈虧"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成交時間" prop="tradeTime">
        <el-date-picker clearable
          v-model="queryParams.tradeTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择成交時間">
        </el-date-picker>
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
          v-hasPermi="['stock:tftradeexecution:add']"
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
          v-hasPermi="['stock:tftradeexecution:edit']"
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
          v-hasPermi="['stock:tftradeexecution:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:tftradeexecution:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tftradeexecutionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主鍵 ID" align="center" prop="id" />
      <el-table-column label="賬戶編號" align="center" prop="accountNo" />
      <el-table-column label="訂單編號" align="center" prop="orderId" />
      <el-table-column label="股票代碼" align="center" prop="symbol" />
      <el-table-column label="股票名稱" align="center" prop="stockName" />
      <el-table-column label="交易方向：BUY-買入, SELL-賣出" align="center" prop="tradeSide" />
      <el-table-column label="成交價格" align="center" prop="tradePrice" />
      <el-table-column label="成交股數" align="center" prop="tradeVolume" />
      <el-table-column label="成交總金額" align="center" prop="tradeAmount" />
      <el-table-column label="交易傭金" align="center" prop="commission" />
      <el-table-column label="印花稅" align="center" prop="tax" />
      <el-table-column label="平倉實現盈虧" align="center" prop="realizedPnl" />
      <el-table-column label="成交時間" align="center" prop="tradeTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.tradeTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:tftradeexecution:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:tftradeexecution:remove']"
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

    <!-- 添加或修改回測歷史成交交割單对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="賬戶編號" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="请输入賬戶編號" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="訂單編號" prop="orderId">
              <el-input v-model="form.orderId" placeholder="请输入訂單編號" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="股票代碼" prop="symbol">
              <el-input v-model="form.symbol" placeholder="请输入股票代碼" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="股票名稱" prop="stockName">
              <el-input v-model="form.stockName" placeholder="请输入股票名稱" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易方向：BUY-買入, SELL-賣出" prop="tradeSide">
              <el-input v-model="form.tradeSide" placeholder="请输入交易方向：BUY-買入, SELL-賣出" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交價格" prop="tradePrice">
              <el-input v-model="form.tradePrice" placeholder="请输入成交價格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交股數" prop="tradeVolume">
              <el-input v-model="form.tradeVolume" placeholder="请输入成交股數" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交總金額" prop="tradeAmount">
              <el-input v-model="form.tradeAmount" placeholder="请输入成交總金額" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易傭金" prop="commission">
              <el-input v-model="form.commission" placeholder="请输入交易傭金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="印花稅" prop="tax">
              <el-input v-model="form.tax" placeholder="请输入印花稅" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="平倉實現盈虧" prop="realizedPnl">
              <el-input v-model="form.realizedPnl" placeholder="请输入平倉實現盈虧" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交時間" prop="tradeTime">
              <el-date-picker clearable
                v-model="form.tradeTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择成交時間">
              </el-date-picker>
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
import { listTftradeexecution, getTftradeexecution, delTftradeexecution, addTftradeexecution, updateTftradeexecution } from "@/api/stock/tftradeexecution"

export default {
  name: "Tftradeexecution",
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
      // 回測歷史成交交割單表格数据
      tftradeexecutionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountNo: null,
        orderId: null,
        symbol: null,
        stockName: null,
        tradeSide: null,
        tradePrice: null,
        tradeVolume: null,
        tradeAmount: null,
        commission: null,
        tax: null,
        realizedPnl: null,
        tradeTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        accountNo: [
          { required: true, message: "賬戶編號不能为空", trigger: "blur" }
        ],
        orderId: [
          { required: true, message: "訂單編號不能为空", trigger: "blur" }
        ],
        symbol: [
          { required: true, message: "股票代碼不能为空", trigger: "blur" }
        ],
        stockName: [
          { required: true, message: "股票名稱不能为空", trigger: "blur" }
        ],
        tradeSide: [
          { required: true, message: "交易方向：BUY-買入, SELL-賣出不能为空", trigger: "blur" }
        ],
        tradePrice: [
          { required: true, message: "成交價格不能为空", trigger: "blur" }
        ],
        tradeVolume: [
          { required: true, message: "成交股數不能为空", trigger: "blur" }
        ],
        tradeAmount: [
          { required: true, message: "成交總金額不能为空", trigger: "blur" }
        ],
        tradeTime: [
          { required: true, message: "成交時間不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回測歷史成交交割單列表 */
    getList() {
      this.loading = true
      listTftradeexecution(this.queryParams).then(response => {
        this.tftradeexecutionList = response.rows
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
        orderId: null,
        symbol: null,
        stockName: null,
        tradeSide: null,
        tradePrice: null,
        tradeVolume: null,
        tradeAmount: null,
        commission: null,
        tax: null,
        realizedPnl: null,
        tradeTime: null,
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
      this.title = "添加回測歷史成交交割單"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTftradeexecution(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回測歷史成交交割單"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTftradeexecution(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTftradeexecution(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回測歷史成交交割單编号为"' + ids + '"的数据项？').then(function() {
        return delTftradeexecution(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/tftradeexecution/export', {
        ...this.queryParams
      }, `tftradeexecution_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
