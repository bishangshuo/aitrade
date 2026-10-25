<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="賬戶編號/策略標識" prop="accountNo">
        <el-input
          v-model="queryParams.accountNo"
          placeholder="请输入賬戶編號/策略標識"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="初始資金" prop="initialCapital">
        <el-input
          v-model="queryParams.initialCapital"
          placeholder="请输入初始資金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="可用現金" prop="availableCash">
        <el-input
          v-model="queryParams.availableCash"
          placeholder="请输入可用現金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="凍結現金" prop="frozenCash">
        <el-input
          v-model="queryParams.frozenCash"
          placeholder="请输入凍結現金"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="持倉市值" prop="marketValue">
        <el-input
          v-model="queryParams.marketValue"
          placeholder="请输入持倉市值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="總資產" prop="totalAssets">
        <el-input
          v-model="queryParams.totalAssets"
          placeholder="请输入總資產"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="累計盈虧金額" prop="totalPnl">
        <el-input
          v-model="queryParams.totalPnl"
          placeholder="请输入累計盈虧金額"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="累計盈虧比例" prop="totalPnlPct">
        <el-input
          v-model="queryParams.totalPnlPct"
          placeholder="请输入累計盈虧比例"
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
          v-hasPermi="['tfstock:tfaccount:add']"
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
          v-hasPermi="['tfstock:tfaccount:edit']"
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
          v-hasPermi="['tfstock:tfaccount:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tfstock:tfaccount:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tfaccountList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主鍵 ID" align="center" prop="id" />
      <el-table-column label="賬戶編號/策略標識" align="center" prop="accountNo" />
      <el-table-column label="初始資金" align="center" prop="initialCapital" />
      <el-table-column label="可用現金" align="center" prop="availableCash" />
      <el-table-column label="凍結現金" align="center" prop="frozenCash" />
      <el-table-column label="持倉市值" align="center" prop="marketValue" />
      <el-table-column label="總資產" align="center" prop="totalAssets" />
      <el-table-column label="累計盈虧金額" align="center" prop="totalPnl" />
      <el-table-column label="累計盈虧比例" align="center" prop="totalPnlPct" />
      <el-table-column label="狀態：1-正常，0-凍結" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tfstock:tfaccount:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tfstock:tfaccount:remove']"
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

    <!-- 添加或修改回測虛擬賬戶对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="賬戶編號/策略標識" prop="accountNo">
              <el-input v-model="form.accountNo" placeholder="请输入賬戶編號/策略標識" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="初始資金" prop="initialCapital">
              <el-input v-model="form.initialCapital" placeholder="请输入初始資金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="可用現金" prop="availableCash">
              <el-input v-model="form.availableCash" placeholder="请输入可用現金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="凍結現金" prop="frozenCash">
              <el-input v-model="form.frozenCash" placeholder="请输入凍結現金" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="持倉市值" prop="marketValue">
              <el-input v-model="form.marketValue" placeholder="请输入持倉市值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="總資產" prop="totalAssets">
              <el-input v-model="form.totalAssets" placeholder="请输入總資產" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="累計盈虧金額" prop="totalPnl">
              <el-input v-model="form.totalPnl" placeholder="请输入累計盈虧金額" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="累計盈虧比例" prop="totalPnlPct">
              <el-input v-model="form.totalPnlPct" placeholder="请输入累計盈虧比例" />
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
import { listTfaccount, getTfaccount, delTfaccount, addTfaccount, updateTfaccount } from "@/api/stock/tfaccount"

export default {
  name: "Tfaccount",
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
      // 回測虛擬賬戶表格数据
      tfaccountList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountNo: null,
        initialCapital: null,
        availableCash: null,
        frozenCash: null,
        marketValue: null,
        totalAssets: null,
        totalPnl: null,
        totalPnlPct: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        accountNo: [
          { required: true, message: "賬戶編號/策略標識不能为空", trigger: "blur" }
        ],
        initialCapital: [
          { required: true, message: "初始資金不能为空", trigger: "blur" }
        ],
        availableCash: [
          { required: true, message: "可用現金不能为空", trigger: "blur" }
        ],
        totalAssets: [
          { required: true, message: "總資產不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询回測虛擬賬戶列表 */
    getList() {
      this.loading = true
      listTfaccount(this.queryParams).then(response => {
        this.tfaccountList = response.rows
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
        initialCapital: null,
        availableCash: null,
        frozenCash: null,
        marketValue: null,
        totalAssets: null,
        totalPnl: null,
        totalPnlPct: null,
        status: null,
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
      this.title = "添加回測虛擬賬戶"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTfaccount(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回測虛擬賬戶"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTfaccount(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTfaccount(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除回測虛擬賬戶编号为"' + ids + '"的数据项？').then(function() {
        return delTfaccount(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tfstock/tfaccount/export', {
        ...this.queryParams
      }, `tfaccount_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
