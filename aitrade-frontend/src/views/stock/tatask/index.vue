<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="启动起始时间" prop="startBeginTime">
        <el-date-picker clearable
          v-model="queryParams.startBeginTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择启动起始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="启动截止时间" prop="startEndTime">
        <el-date-picker clearable
          v-model="queryParams.startEndTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择启动截止时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="任务状态" clearable style="width: 100px">
          <el-option label="未启动" value="0"></el-option>
          <el-option label="正在进行" value="1"></el-option>
          <el-option label="已经完成" value="2"></el-option>
        </el-select>
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
          v-hasPermi="['stock:tatask:add']"
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
          v-hasPermi="['stock:tatask:edit']"
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
          v-hasPermi="['stock:tatask:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:tatask:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tataskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="启动起始时间" align="center" prop="startBeginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startBeginTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="启动截止时间" align="center" prop="startEndTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startEndTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务完成时间" align="center" prop="finishTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.finishTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务状态" align="center" prop="status" >
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0">未启动</el-tag>
          <el-tag v-if="scope.row.status === 1">正在进行</el-tag>
          <el-tag v-if="scope.row.status === 2">已经完成</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:tatask:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:tatask:remove']"
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

    <!-- 添加或修改任务规划对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="启动截止时间" prop="startTime">
              <el-date-picker
                v-model="startTimeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="启动起始时间"
                end-placeholder="启动截止时间"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="editding">
            <el-form-item label="任务完成时间" prop="finishTime">
              <el-date-picker clearable
                v-model="form.finishTime"
                type="date"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="editding">
            <el-form-item label="任务状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">未启动</el-radio>
                <el-radio :label="1">正在进行</el-radio>
                <el-radio :label="2">已经完成</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
import { listTatask, getTatask, delTatask, addTatask, updateTatask } from "@/api/stock/tatask"

export default {
  name: "Tatask",
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
      // 任务规划表格数据
      tataskList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      editding: true,
      open: false,
      startTimeRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        startBeginTime: null,
        startEndTime: null,
        finishTime: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        startBeginTime: [
          { required: true, message: "任务启动起始时间不能为空", trigger: "blur" }
        ],
        startEndTime: [
          { required: true, message: "任务启动截止时间，一个任务如果截止时间已经过期，则不再被启动不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "任务状态0未启动1正在进行2已经完成不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  watch: {
    startTimeRange(val) {
      if (val) {
        this.form.startBeginTime = val[0]
        this.form.startEndTime = val[1]
      } else {
        this.form.startBeginTime = null
        this.form.startEndTime = null
      }
    }
  },
  methods: {
    /** 查询任务规划列表 */
    getList() {
      this.loading = true
      listTatask(this.queryParams).then(response => {
        this.tataskList = response.rows
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
        startBeginTime: null,
        startEndTime: null,
        finishTime: null,
        status: null,
        createTime: null,
        updateTime: null,
        remark: null
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
      this.editding = false
      this.open = true
      this.title = "添加任务规划"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTatask(id).then(response => {
        this.form = response.data
        this.editding = true
        this.open = true
        this.title = "修改任务规划"
        this.startTimeRange = [this.form.startBeginTime, this.form.startEndTime]
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTatask(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTatask(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除任务规划编号为"' + ids + '"的数据项？').then(function() {
        return delTatask(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/tatask/export', {
        ...this.queryParams
      }, `tatask_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
