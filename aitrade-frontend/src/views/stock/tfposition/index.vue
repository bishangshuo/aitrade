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
      <el-form-item label="總持倉股數" prop="totalVolume">
        <el-input
          v-model="queryParams.totalVolume"
          placeholder="请输入總持倉股數"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="可用股數" prop="availableVolume">
        <el-input
          v-model="queryParams.availableVolume"
          placeholder="请输入可用股數"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="持倉成本價" prop="costPrice">
        <el-input
          v-model="queryParams.costPrice"
          placeholder="请输入持倉成本價"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="當前最新價/收盤價" prop="currentPrice">
        <el-input
          v-model="queryParams.currentPrice"
          placeholder="请输入當前最新價/收盤價"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="當前持倉市值" prop="marketValue">
        <el-input
          v-model="queryParams.marketValue"
          placeholder="请输入當前持倉市值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="浮動盈虧金額" prop="unrealizedPnl">
        <el-input
          v-model="queryParams.unrealizedPnl"
          placeholder="请输入浮動盈虧金額"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="浮動盈虧比例" prop="unrealizedPnlPct">
        <el-input
          v-model="queryParams.unrealizedPnlPct"
          placeholder="请输入浮動盈虧比例"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="建倉日期" prop="openDate">
        <el-date-picker clearable
          v-model="queryParams.openDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择建倉日期">
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
          v-hasPermi="['stock:tfposition:add']"
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
          v-hasPermi="['stock:tfposition:edit']"
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
          v-hasPermi="['stock:tfposition:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:tfposition:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tfpositionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主鍵 ID" align="center" prop="id" />
      <el-table-column label="賬戶編號" align="center" prop="accountNo" />
      <el-table-column label="股票代碼" align="center" prop="symbol" />
      <el-table-column label="股票名稱" align="center" prop="stockName" />
      <el-table-column label="總持倉股數" align="center" prop="totalVolume" />
      <el-table-column label="可用股數" align="center" prop="availableVolume" />
      <el-table-column label="持倉成本價" align="center" prop="costPrice" />
      <el-table-column label="當前最新價/收盤價" align="center" prop="currentPrice" />
      <el-table-column label="當前持倉市值" align="center" prop="marketValue" />
      <el-table-column label="浮動盈虧金額" align="center" prop="unrealizedPnl" />
      <el-table-column label="浮動盈虧比例" align="center" prop="unrealizedPnlPct" />
      <el-table-column label="建倉日期" align="center" prop="openDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.openDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:tfposition:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:tfposition:remove']"
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

    <!-- 添加或修改回測當前持倉对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="賬戶編號" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="请输入賬戶編號" />
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
            <el-form-item label="總持倉股數" prop="totalVolume">
              <el-input v-model="form.totalVolume" placeholder="请输入總持倉股數" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="可用股數" prop="availableVolume">
              <el-input v-model="form.availableVolume" placeholder="请输入可用股數" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="持倉成本價" prop="costPrice">
              <el-input v-model="form.costPrice" placeholder="请输入持倉成本價" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="當前最新價/收盤價" prop="currentPrice">
              <el-input v-model="form.currentPrice" placeholder="请输入當前最新價/收盤價" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="當前持倉市值" prop="marketValue">
              <el-input v-model="form.marketValue" placeholder="请输入當前持倉市值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="浮動盈虧金額" prop="unrealizedPnl">
              <el-input v-model="form.unrealizedPnl" placeholder="请输入浮動盈虧金額" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="浮動盈虧比例" prop="unrealizedPnlPct">
              <el-input v-model="form.unrealizedPnlPct" placeholder="请输入浮動盈虧比例" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="建倉日期" prop="openDate">
              <el-date-picker clearable
                v-model="form.openDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择建倉日期">
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
import { listTfposition, getTfposition, delTfposition, addTfposition, updateTfposition } from "@/api/stock/tfposition"

export default {
  name: "Tfposition",
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
      // 回測當前持倉表格数据
      tfpositionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountNo: null,
        symbol: null,
        stockName: null,
        totalVolume: null,
        availableVolume: null,
        costPrice: null,
        currentPrice: null,
        marketValue: null,
        unrealizedPnl: null,
        unrealizedPnlPct: null,
        openDate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        accountNo: [
          { required: true, message: "賬戶編號不能为空", trigger: "blur" }
        ],
        symbol: [
          { required: true, message: "股票代碼不能为空", trigger: "blur" }
        ],
        stockName: [
          { required: true, message: "股票名稱不能为空", trigger: "blur" }
        ],
        totalVolume: [
          { required: true, message: "總持倉股數不能为空", trigger: "blur" }
        ],
        availableVolume: [
          { required: true, message: "可用股數不能为空", trigger: "blur" }
        ],
        costPrice: [
          { required: true, message: "持倉成本價不能为空", trigger: "blur" }
        ],
        currentPrice: [
          { required: true, message: "當前最新價/收盤價不能为空", trigger: "blur" }
        ],
        marketValue: [
          { required: true, message: "當前持倉市值不能为空", trigger: "blur" }
        ],
        openDate: [
          { required: true, message: "建倉日期不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回測當前持倉列表 */
    getList() {
      this.loading = true
      listTfposition(this.queryParams).then(response => {
        this.tfpositionList = response.rows
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
        symbol: null,
        stockName: null,
        totalVolume: null,
        availableVolume: null,
        costPrice: null,
        currentPrice: null,
        marketValue: null,
        unrealizedPnl: null,
        unrealizedPnlPct: null,
        openDate: null,
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
      this.title = "添加回測當前持倉"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTfposition(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回測當前持倉"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTfposition(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTfposition(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回測當前持倉编号为"' + ids + '"的数据项？').then(function() {
        return delTfposition(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/tfposition/export', {
        ...this.queryParams
      }, `tfposition_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
