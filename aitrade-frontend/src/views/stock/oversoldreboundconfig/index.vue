<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置名称/版本备注" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置名称/版本备注"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="策略编码" prop="strategyCode">
        <el-input
          v-model="queryParams.strategyCode"
          placeholder="请输入策略编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="策略名称" prop="strategyName">
        <el-input
          v-model="queryParams.strategyName"
          placeholder="请输入策略名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最小需要的K线历史天数" prop="minKlineCount">
        <el-input
          v-model="queryParams.minKlineCount"
          placeholder="请输入最小需要的K线历史天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="短均线视窗" prop="trendMaShortWindow">
        <el-input
          v-model="queryParams.trendMaShortWindow"
          placeholder="请输入短均线视窗"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="长均线视窗" prop="trendMaLongWindow">
        <el-input
          v-model="queryParams.trendMaLongWindow"
          placeholder="请输入长均线视窗"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="20日乖离率阈值" prop="biasMaxThreshold">
        <el-input
          v-model="queryParams.biasMaxThreshold"
          placeholder="请输入20日乖离率阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI6超卖阈值" prop="momRsi6Threshold">
        <el-input
          v-model="queryParams.momRsi6Threshold"
          placeholder="请输入RSI6超卖阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI14超卖阈值" prop="momRsi14Threshold">
        <el-input
          v-model="queryParams.momRsi14Threshold"
          placeholder="请输入RSI14超卖阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数N" prop="momKdjN">
        <el-input
          v-model="queryParams.momKdjN"
          placeholder="请输入KDJ参数N"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数M1" prop="momKdjM1">
        <el-input
          v-model="queryParams.momKdjM1"
          placeholder="请输入KDJ参数M1"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数M2" prop="momKdjM2">
        <el-input
          v-model="queryParams.momKdjM2"
          placeholder="请输入KDJ参数M2"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ低位阈值" prop="momKdjLowThreshold">
        <el-input
          v-model="queryParams.momKdjLowThreshold"
          placeholder="请输入KDJ低位阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成交量均线视窗" prop="vpVolMaWindow">
        <el-input
          v-model="queryParams.vpVolMaWindow"
          placeholder="请输入成交量均线视窗"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="20日乖离率阈值" prop="vpMinVolRatio">
        <el-input
          v-model="queryParams.vpMinVolRatio"
          placeholder="请输入20日乖离率阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="锤头线下影线倍数" prop="vpHammerShadowRatio">
        <el-input
          v-model="queryParams.vpHammerShadowRatio"
          placeholder="请输入锤头线下影线倍数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最小涨幅" prop="vpMinChangeRate">
        <el-input
          v-model="queryParams.vpMinChangeRate"
          placeholder="请输入最小涨幅"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最小获利盘比例" prop="chipMinProfitRatio">
        <el-input
          v-model="queryParams.chipMinProfitRatio"
          placeholder="请输入最小获利盘比例"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="超跌乖离深度得分权重" prop="swBiasWeight">
        <el-input
          v-model="queryParams.swBiasWeight"
          placeholder="请输入超跌乖离深度得分权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="量价反转力度权重" prop="swVolumeWeight">
        <el-input
          v-model="queryParams.swVolumeWeight"
          placeholder="请输入量价反转力度权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="动能修复权重" prop="swMomentumWeight">
        <el-input
          v-model="queryParams.swMomentumWeight"
          placeholder="请输入动能修复权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="筹码面权重" prop="swChipWeight">
        <el-input
          v-model="queryParams.swChipWeight"
          placeholder="请输入筹码面权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="乖离深度得分转换倍数" prop="biasScoreMultiplier">
        <el-input
          v-model="queryParams.biasScoreMultiplier"
          placeholder="请输入乖离深度得分转换倍数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="乖离得分下限" prop="biasScoreMin">
        <el-input
          v-model="queryParams.biasScoreMin"
          placeholder="请输入乖离得分下限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="乖离得分上限" prop="biasScoreMax">
        <el-input
          v-model="queryParams.biasScoreMax"
          placeholder="请输入乖离得分上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预设动能得分" prop="swDefaultMomentumScore">
        <el-input
          v-model="queryParams.swDefaultMomentumScore"
          placeholder="请输入预设动能得分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预设筹码得分" prop="swDefaultChipScore">
        <el-input
          v-model="queryParams.swDefaultChipScore"
          placeholder="请输入预设筹码得分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单峰密集加分" prop="swSinglePeakBonus">
        <el-input
          v-model="queryParams.swSinglePeakBonus"
          placeholder="请输入单峰密集加分"
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
          v-hasPermi="['stock:oversoldreboundconfig:add']"
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
          v-hasPermi="['stock:oversoldreboundconfig:edit']"
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
          v-hasPermi="['stock:oversoldreboundconfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:oversoldreboundconfig:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="oversoldreboundconfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置主键ID" align="center" prop="id" />
      <el-table-column label="配置名称/版本备注" align="center" prop="configName" />
      <el-table-column label="策略编码" align="center" prop="strategyCode" />
      <el-table-column label="策略名称" align="center" prop="strategyName" />
      <el-table-column label="最小需要的K线历史天数" align="center" prop="minKlineCount" />
      <el-table-column label="短均线视窗" align="center" prop="trendMaShortWindow" />
      <el-table-column label="长均线视窗" align="center" prop="trendMaLongWindow" />
      <el-table-column label="20日乖离率阈值" align="center" prop="biasMaxThreshold" />
      <el-table-column label="RSI6超卖阈值" align="center" prop="momRsi6Threshold" />
      <el-table-column label="RSI14超卖阈值" align="center" prop="momRsi14Threshold" />
      <el-table-column label="KDJ参数N" align="center" prop="momKdjN" />
      <el-table-column label="KDJ参数M1" align="center" prop="momKdjM1" />
      <el-table-column label="KDJ参数M2" align="center" prop="momKdjM2" />
      <el-table-column label="KDJ低位阈值" align="center" prop="momKdjLowThreshold" />
      <el-table-column label="成交量均线视窗" align="center" prop="vpVolMaWindow" />
      <el-table-column label="20日乖离率阈值" align="center" prop="vpMinVolRatio" />
      <el-table-column label="锤头线下影线倍数" align="center" prop="vpHammerShadowRatio" />
      <el-table-column label="最小涨幅" align="center" prop="vpMinChangeRate" />
      <el-table-column label="最小获利盘比例" align="center" prop="chipMinProfitRatio" />
      <el-table-column label="超跌乖离深度得分权重" align="center" prop="swBiasWeight" />
      <el-table-column label="量价反转力度权重" align="center" prop="swVolumeWeight" />
      <el-table-column label="动能修复权重" align="center" prop="swMomentumWeight" />
      <el-table-column label="筹码面权重" align="center" prop="swChipWeight" />
      <el-table-column label="乖离深度得分转换倍数" align="center" prop="biasScoreMultiplier" />
      <el-table-column label="乖离得分下限" align="center" prop="biasScoreMin" />
      <el-table-column label="乖离得分上限" align="center" prop="biasScoreMax" />
      <el-table-column label="预设动能得分" align="center" prop="swDefaultMomentumScore" />
      <el-table-column label="预设筹码得分" align="center" prop="swDefaultChipScore" />
      <el-table-column label="单峰密集加分" align="center" prop="swSinglePeakBonus" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:oversoldreboundconfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:oversoldreboundconfig:remove']"
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

    <!-- 添加或修改超跌反弹策略回测参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="配置名称/版本备注" prop="configName">
              <el-input v-model="form.configName" placeholder="请输入配置名称/版本备注" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="策略编码" prop="strategyCode">
              <el-input v-model="form.strategyCode" placeholder="请输入策略编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="策略名称" prop="strategyName">
              <el-input v-model="form.strategyName" placeholder="请输入策略名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小需要的K线历史天数" prop="minKlineCount">
              <el-input v-model="form.minKlineCount" placeholder="请输入最小需要的K线历史天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短均线视窗" prop="trendMaShortWindow">
              <el-input v-model="form.trendMaShortWindow" placeholder="请输入短均线视窗" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="长均线视窗" prop="trendMaLongWindow">
              <el-input v-model="form.trendMaLongWindow" placeholder="请输入长均线视窗" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="20日乖离率阈值" prop="biasMaxThreshold">
              <el-input v-model="form.biasMaxThreshold" placeholder="请输入20日乖离率阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI6超卖阈值" prop="momRsi6Threshold">
              <el-input v-model="form.momRsi6Threshold" placeholder="请输入RSI6超卖阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI14超卖阈值" prop="momRsi14Threshold">
              <el-input v-model="form.momRsi14Threshold" placeholder="请输入RSI14超卖阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数N" prop="momKdjN">
              <el-input v-model="form.momKdjN" placeholder="请输入KDJ参数N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数M1" prop="momKdjM1">
              <el-input v-model="form.momKdjM1" placeholder="请输入KDJ参数M1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数M2" prop="momKdjM2">
              <el-input v-model="form.momKdjM2" placeholder="请输入KDJ参数M2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ低位阈值" prop="momKdjLowThreshold">
              <el-input v-model="form.momKdjLowThreshold" placeholder="请输入KDJ低位阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交量均线视窗" prop="vpVolMaWindow">
              <el-input v-model="form.vpVolMaWindow" placeholder="请输入成交量均线视窗" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="20日乖离率阈值" prop="vpMinVolRatio">
              <el-input v-model="form.vpMinVolRatio" placeholder="请输入20日乖离率阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="锤头线下影线倍数" prop="vpHammerShadowRatio">
              <el-input v-model="form.vpHammerShadowRatio" placeholder="请输入锤头线下影线倍数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小涨幅" prop="vpMinChangeRate">
              <el-input v-model="form.vpMinChangeRate" placeholder="请输入最小涨幅" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小获利盘比例" prop="chipMinProfitRatio">
              <el-input v-model="form.chipMinProfitRatio" placeholder="请输入最小获利盘比例" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="超跌乖离深度得分权重" prop="swBiasWeight">
              <el-input v-model="form.swBiasWeight" placeholder="请输入超跌乖离深度得分权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="量价反转力度权重" prop="swVolumeWeight">
              <el-input v-model="form.swVolumeWeight" placeholder="请输入量价反转力度权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="动能修复权重" prop="swMomentumWeight">
              <el-input v-model="form.swMomentumWeight" placeholder="请输入动能修复权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="筹码面权重" prop="swChipWeight">
              <el-input v-model="form.swChipWeight" placeholder="请输入筹码面权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="乖离深度得分转换倍数" prop="biasScoreMultiplier">
              <el-input v-model="form.biasScoreMultiplier" placeholder="请输入乖离深度得分转换倍数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="乖离得分下限" prop="biasScoreMin">
              <el-input v-model="form.biasScoreMin" placeholder="请输入乖离得分下限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="乖离得分上限" prop="biasScoreMax">
              <el-input v-model="form.biasScoreMax" placeholder="请输入乖离得分上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预设动能得分" prop="swDefaultMomentumScore">
              <el-input v-model="form.swDefaultMomentumScore" placeholder="请输入预设动能得分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预设筹码得分" prop="swDefaultChipScore">
              <el-input v-model="form.swDefaultChipScore" placeholder="请输入预设筹码得分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单峰密集加分" prop="swSinglePeakBonus">
              <el-input v-model="form.swSinglePeakBonus" placeholder="请输入单峰密集加分" />
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
import { listOversoldreboundconfig, getOversoldreboundconfig, delOversoldreboundconfig, addOversoldreboundconfig, updateOversoldreboundconfig } from "@/api/stock/oversoldreboundconfig"

export default {
  name: "Oversoldreboundconfig",
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
      // 超跌反弹策略回测参数配置表格数据
      oversoldreboundconfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configName: null,
        strategyCode: null,
        strategyName: null,
        minKlineCount: null,
        trendMaShortWindow: null,
        trendMaLongWindow: null,
        biasMaxThreshold: null,
        momRsi6Threshold: null,
        momRsi14Threshold: null,
        momKdjN: null,
        momKdjM1: null,
        momKdjM2: null,
        momKdjLowThreshold: null,
        vpVolMaWindow: null,
        vpMinVolRatio: null,
        vpHammerShadowRatio: null,
        vpMinChangeRate: null,
        chipMinProfitRatio: null,
        swBiasWeight: null,
        swVolumeWeight: null,
        swMomentumWeight: null,
        swChipWeight: null,
        biasScoreMultiplier: null,
        biasScoreMin: null,
        biasScoreMax: null,
        swDefaultMomentumScore: null,
        swDefaultChipScore: null,
        swSinglePeakBonus: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        configName: [
          { required: true, message: "配置名称/版本备注不能为空", trigger: "blur" }
        ],
        strategyCode: [
          { required: true, message: "策略编码不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询超跌反弹策略回测参数配置列表 */
    getList() {
      this.loading = true
      listOversoldreboundconfig(this.queryParams).then(response => {
        this.oversoldreboundconfigList = response.rows
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
        configName: null,
        strategyCode: null,
        strategyName: null,
        minKlineCount: null,
        trendMaShortWindow: null,
        trendMaLongWindow: null,
        biasMaxThreshold: null,
        momRsi6Threshold: null,
        momRsi14Threshold: null,
        momKdjN: null,
        momKdjM1: null,
        momKdjM2: null,
        momKdjLowThreshold: null,
        vpVolMaWindow: null,
        vpMinVolRatio: null,
        vpHammerShadowRatio: null,
        vpMinChangeRate: null,
        chipMinProfitRatio: null,
        swBiasWeight: null,
        swVolumeWeight: null,
        swMomentumWeight: null,
        swChipWeight: null,
        biasScoreMultiplier: null,
        biasScoreMin: null,
        biasScoreMax: null,
        swDefaultMomentumScore: null,
        swDefaultChipScore: null,
        swSinglePeakBonus: null,
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
      this.title = "添加超跌反弹策略回测参数配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getOversoldreboundconfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改超跌反弹策略回测参数配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateOversoldreboundconfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addOversoldreboundconfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除超跌反弹策略回测参数配置编号为"' + ids + '"的数据项？').then(function() {
        return delOversoldreboundconfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/oversoldreboundconfig/export', {
        ...this.queryParams
      }, `oversoldreboundconfig_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
