<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="账户编号" prop="accountNo">
        <el-input
          v-model="queryParams.accountNo"
          placeholder="请输入账户编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日历日期" prop="calenderDate">
        <el-date-picker clearable
          v-model="queryParams.calenderDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择日历日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="可用现金" prop="availableCash">
        <el-input
          v-model="queryParams.availableCash"
          placeholder="请输入可用现金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="冻结现金" prop="frozenCash">
        <el-input
          v-model="queryParams.frozenCash"
          placeholder="请输入冻结现金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="持仓市值" prop="marketValue">
        <el-input
          v-model="queryParams.marketValue"
          placeholder="请输入持仓市值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总资产" prop="totalAssets">
        <el-input
          v-model="queryParams.totalAssets"
          placeholder="请输入总资产"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="累计盈亏金额" prop="totalPnl">
        <el-input
          v-model="queryParams.totalPnl"
          placeholder="请输入累计盈亏金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="累计盈亏比例" prop="totalPnlPct">
        <el-input
          v-model="queryParams.totalPnlPct"
          placeholder="请输入累计盈亏比例"
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
          v-hasPermi="['stock:tfaccountdaily:add']"
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
          v-hasPermi="['stock:tfaccountdaily:edit']"
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
          v-hasPermi="['stock:tfaccountdaily:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:tfaccountdaily:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tfaccountdailyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键 ID" align="center" prop="id" />
      <el-table-column label="账户编号" align="center" prop="accountNo" />
      <el-table-column label="日历日期" align="center" prop="calenderDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.calenderDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="可用现金" align="center" prop="availableCash" />
      <el-table-column label="冻结现金" align="center" prop="frozenCash" />
      <el-table-column label="持仓市值" align="center" prop="marketValue" />
      <el-table-column label="总资产" align="center" prop="totalAssets" />
      <el-table-column label="累计盈亏金额" align="center" prop="totalPnl" />
      <el-table-column label="累计盈亏比例" align="center" prop="totalPnlPct" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:tfaccountdaily:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:tfaccountdaily:remove']"
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

    <!-- 添加或修改回测账户快照对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="账户编号" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="请输入账户编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="日历日期" prop="calenderDate">
              <el-date-picker clearable
                v-model="form.calenderDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择日历日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="可用现金" prop="availableCash">
              <el-input v-model="form.availableCash" placeholder="请输入可用现金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="冻结现金" prop="frozenCash">
              <el-input v-model="form.frozenCash" placeholder="请输入冻结现金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="持仓市值" prop="marketValue">
              <el-input v-model="form.marketValue" placeholder="请输入持仓市值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="总资产" prop="totalAssets">
              <el-input v-model="form.totalAssets" placeholder="请输入总资产" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="累计盈亏金额" prop="totalPnl">
              <el-input v-model="form.totalPnl" placeholder="请输入累计盈亏金额" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="累计盈亏比例" prop="totalPnlPct">
              <el-input v-model="form.totalPnlPct" placeholder="请输入累计盈亏比例" />
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
import { listTfaccountdaily, getTfaccountdaily, delTfaccountdaily, addTfaccountdaily, updateTfaccountdaily } from "@/api/stock/tfaccountdaily"

export default {
  name: "Tfaccountdaily",
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
      // 回测账户快照表格数据
      tfaccountdailyList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountNo: null,
        calenderDate: null,
        availableCash: null,
        frozenCash: null,
        marketValue: null,
        totalAssets: null,
        totalPnl: null,
        totalPnlPct: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        accountNo: [
          { required: true, message: "账户编号不能为空", trigger: "blur" }
        ],
        calenderDate: [
          { required: true, message: "日历日期不能为空", trigger: "blur" }
        ],
        availableCash: [
          { required: true, message: "可用现金不能为空", trigger: "blur" }
        ],
        totalAssets: [
          { required: true, message: "总资产不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回测账户快照列表 */
    getList() {
      this.loading = true
      listTfaccountdaily(this.queryParams).then(response => {
        this.tfaccountdailyList = response.rows
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
        calenderDate: null,
        availableCash: null,
        frozenCash: null,
        marketValue: null,
        totalAssets: null,
        totalPnl: null,
        totalPnlPct: null,
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
      this.title = "添加回测账户快照"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTfaccountdaily(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回测账户快照"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTfaccountdaily(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTfaccountdaily(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回测账户快照编号为"' + ids + '"的数据项？').then(function() {
        return delTfaccountdaily(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/tfaccountdaily/export', {
        ...this.queryParams
      }, `tfaccountdaily_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
