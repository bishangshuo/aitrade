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
      <el-form-item label="趋势回溯天数" prop="trendLookbackDays">
        <el-input
          v-model="queryParams.trendLookbackDays"
          placeholder="请输入趋势回溯天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区间最高位置分位数" prop="trendMaxPositionRatio">
        <el-input
          v-model="queryParams.trendMaxPositionRatio"
          placeholder="请输入区间最高位置分位数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="均线视窗大小" prop="trendMaWindow">
        <el-input
          v-model="queryParams.trendMaWindow"
          placeholder="请输入均线视窗大小"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="前置均线偏移天数" prop="trendPrevMaOffset">
        <el-input
          v-model="queryParams.trendPrevMaOffset"
          placeholder="请输入前置均线偏移天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="均线止跌系数" prop="trendMaFlatMultiplier">
        <el-input
          v-model="queryParams.trendMaFlatMultiplier"
          placeholder="请输入均线止跌系数"
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
      <el-form-item label="最小放量倍数" prop="vpMinVolRatio">
        <el-input
          v-model="queryParams.vpMinVolRatio"
          placeholder="请输入最小放量倍数"
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
      <el-form-item label="短均线视窗" prop="vpShortMaWindow">
        <el-input
          v-model="queryParams.vpShortMaWindow"
          placeholder="请输入短均线视窗"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="长均线视窗" prop="vpLongMaWindow">
        <el-input
          v-model="queryParams.vpLongMaWindow"
          placeholder="请输入长均线视窗"
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
      <el-form-item label="KDJ超卖阈值" prop="momKdjThreshold">
        <el-input
          v-model="queryParams.momKdjThreshold"
          placeholder="请输入KDJ超卖阈值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI周期" prop="momRsiPeriod">
        <el-input
          v-model="queryParams.momRsiPeriod"
          placeholder="请输入RSI周期"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="昨日RSI上限" prop="momRsiYesterdayMax">
        <el-input
          v-model="queryParams.momRsiYesterdayMax"
          placeholder="请输入昨日RSI上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="今日RSI下限" prop="momRsiTodayMin">
        <el-input
          v-model="queryParams.momRsiTodayMin"
          placeholder="请输入今日RSI下限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="MACD背离回溯天数" prop="momMacdLookback">
        <el-input
          v-model="queryParams.momMacdLookback"
          placeholder="请输入MACD背离回溯天数"
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
      <el-form-item label="趋势得分权重" prop="swTrendWeight">
        <el-input
          v-model="queryParams.swTrendWeight"
          placeholder="请输入趋势得分权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="量价得分权重" prop="swVolumeWeight">
        <el-input
          v-model="queryParams.swVolumeWeight"
          placeholder="请输入量价得分权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="动能得分权重" prop="swMomentumWeight">
        <el-input
          v-model="queryParams.swMomentumWeight"
          placeholder="请输入动能得分权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="筹码得分权重" prop="swChipWeight">
        <el-input
          v-model="queryParams.swChipWeight"
          placeholder="请输入筹码得分权重"
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
          v-hasPermi="['stock:bottombreakoutconfig:add']"
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
          v-hasPermi="['stock:bottombreakoutconfig:edit']"
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
          v-hasPermi="['stock:bottombreakoutconfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:bottombreakoutconfig:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bottombreakoutconfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置主键ID" align="center" prop="id" />
      <el-table-column label="配置名称/版本备注" align="center" prop="configName" />
      <el-table-column label="策略编码" align="center" prop="strategyCode" />
      <el-table-column label="策略名称" align="center" prop="strategyName" />
      <el-table-column label="最小需要的K线历史天数" align="center" prop="minKlineCount" />
      <el-table-column label="趋势回溯天数" align="center" prop="trendLookbackDays" />
      <el-table-column label="区间最高位置分位数" align="center" prop="trendMaxPositionRatio" />
      <el-table-column label="均线视窗大小" align="center" prop="trendMaWindow" />
      <el-table-column label="前置均线偏移天数" align="center" prop="trendPrevMaOffset" />
      <el-table-column label="均线止跌系数" align="center" prop="trendMaFlatMultiplier" />
      <el-table-column label="最小涨幅" align="center" prop="vpMinChangeRate" />
      <el-table-column label="最小放量倍数" align="center" prop="vpMinVolRatio" />
      <el-table-column label="成交量均线视窗" align="center" prop="vpVolMaWindow" />
      <el-table-column label="短均线视窗" align="center" prop="vpShortMaWindow" />
      <el-table-column label="长均线视窗" align="center" prop="vpLongMaWindow" />
      <el-table-column label="KDJ参数N" align="center" prop="momKdjN" />
      <el-table-column label="KDJ参数M1" align="center" prop="momKdjM1" />
      <el-table-column label="KDJ参数M2" align="center" prop="momKdjM2" />
      <el-table-column label="KDJ超卖阈值" align="center" prop="momKdjThreshold" />
      <el-table-column label="RSI周期" align="center" prop="momRsiPeriod" />
      <el-table-column label="昨日RSI上限" align="center" prop="momRsiYesterdayMax" />
      <el-table-column label="今日RSI下限" align="center" prop="momRsiTodayMin" />
      <el-table-column label="MACD背离回溯天数" align="center" prop="momMacdLookback" />
      <el-table-column label="最小获利盘比例" align="center" prop="chipMinProfitRatio" />
      <el-table-column label="趋势得分权重" align="center" prop="swTrendWeight" />
      <el-table-column label="量价得分权重" align="center" prop="swVolumeWeight" />
      <el-table-column label="动能得分权重" align="center" prop="swMomentumWeight" />
      <el-table-column label="筹码得分权重" align="center" prop="swChipWeight" />
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
            v-hasPermi="['stock:bottombreakoutconfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:bottombreakoutconfig:remove']"
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

    <!-- 添加或修改底部反转策略回测参数配置对话框 -->
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
            <el-form-item label="趋势回溯天数" prop="trendLookbackDays">
              <el-input v-model="form.trendLookbackDays" placeholder="请输入趋势回溯天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="区间最高位置分位数" prop="trendMaxPositionRatio">
              <el-input v-model="form.trendMaxPositionRatio" placeholder="请输入区间最高位置分位数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="均线视窗大小" prop="trendMaWindow">
              <el-input v-model="form.trendMaWindow" placeholder="请输入均线视窗大小" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="前置均线偏移天数" prop="trendPrevMaOffset">
              <el-input v-model="form.trendPrevMaOffset" placeholder="请输入前置均线偏移天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="均线止跌系数" prop="trendMaFlatMultiplier">
              <el-input v-model="form.trendMaFlatMultiplier" placeholder="请输入均线止跌系数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小涨幅" prop="vpMinChangeRate">
              <el-input v-model="form.vpMinChangeRate" placeholder="请输入最小涨幅" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小放量倍数" prop="vpMinVolRatio">
              <el-input v-model="form.vpMinVolRatio" placeholder="请输入最小放量倍数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成交量均线视窗" prop="vpVolMaWindow">
              <el-input v-model="form.vpVolMaWindow" placeholder="请输入成交量均线视窗" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="短均线视窗" prop="vpShortMaWindow">
              <el-input v-model="form.vpShortMaWindow" placeholder="请输入短均线视窗" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="长均线视窗" prop="vpLongMaWindow">
              <el-input v-model="form.vpLongMaWindow" placeholder="请输入长均线视窗" />
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
            <el-form-item label="KDJ超卖阈值" prop="momKdjThreshold">
              <el-input v-model="form.momKdjThreshold" placeholder="请输入KDJ超卖阈值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI周期" prop="momRsiPeriod">
              <el-input v-model="form.momRsiPeriod" placeholder="请输入RSI周期" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="昨日RSI上限" prop="momRsiYesterdayMax">
              <el-input v-model="form.momRsiYesterdayMax" placeholder="请输入昨日RSI上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="今日RSI下限" prop="momRsiTodayMin">
              <el-input v-model="form.momRsiTodayMin" placeholder="请输入今日RSI下限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="MACD背离回溯天数" prop="momMacdLookback">
              <el-input v-model="form.momMacdLookback" placeholder="请输入MACD背离回溯天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小获利盘比例" prop="chipMinProfitRatio">
              <el-input v-model="form.chipMinProfitRatio" placeholder="请输入最小获利盘比例" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="趋势得分权重" prop="swTrendWeight">
              <el-input v-model="form.swTrendWeight" placeholder="请输入趋势得分权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="量价得分权重" prop="swVolumeWeight">
              <el-input v-model="form.swVolumeWeight" placeholder="请输入量价得分权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="动能得分权重" prop="swMomentumWeight">
              <el-input v-model="form.swMomentumWeight" placeholder="请输入动能得分权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="筹码得分权重" prop="swChipWeight">
              <el-input v-model="form.swChipWeight" placeholder="请输入筹码得分权重" />
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
import { listBottombreakoutconfig, getBottombreakoutconfig, delBottombreakoutconfig, addBottombreakoutconfig, updateBottombreakoutconfig } from "@/api/stock/bottombreakoutconfig"

export default {
  name: "Bottombreakoutconfig",
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
      // 底部反转策略回测参数配置表格数据
      bottombreakoutconfigList: [],
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
        trendLookbackDays: null,
        trendMaxPositionRatio: null,
        trendMaWindow: null,
        trendPrevMaOffset: null,
        trendMaFlatMultiplier: null,
        vpMinChangeRate: null,
        vpMinVolRatio: null,
        vpVolMaWindow: null,
        vpShortMaWindow: null,
        vpLongMaWindow: null,
        momKdjN: null,
        momKdjM1: null,
        momKdjM2: null,
        momKdjThreshold: null,
        momRsiPeriod: null,
        momRsiYesterdayMax: null,
        momRsiTodayMin: null,
        momMacdLookback: null,
        chipMinProfitRatio: null,
        swTrendWeight: null,
        swVolumeWeight: null,
        swMomentumWeight: null,
        swChipWeight: null,
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
    /** 查询底部反转策略回测参数配置列表 */
    getList() {
      this.loading = true
      listBottombreakoutconfig(this.queryParams).then(response => {
        this.bottombreakoutconfigList = response.rows
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
        trendLookbackDays: null,
        trendMaxPositionRatio: null,
        trendMaWindow: null,
        trendPrevMaOffset: null,
        trendMaFlatMultiplier: null,
        vpMinChangeRate: null,
        vpMinVolRatio: null,
        vpVolMaWindow: null,
        vpShortMaWindow: null,
        vpLongMaWindow: null,
        momKdjN: null,
        momKdjM1: null,
        momKdjM2: null,
        momKdjThreshold: null,
        momRsiPeriod: null,
        momRsiYesterdayMax: null,
        momRsiTodayMin: null,
        momMacdLookback: null,
        chipMinProfitRatio: null,
        swTrendWeight: null,
        swVolumeWeight: null,
        swMomentumWeight: null,
        swChipWeight: null,
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
      this.title = "添加底部反转策略回测参数配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getBottombreakoutconfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改底部反转策略回测参数配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateBottombreakoutconfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addBottombreakoutconfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除底部反转策略回测参数配置编号为"' + ids + '"的数据项？').then(function() {
        return delBottombreakoutconfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/bottombreakoutconfig/export', {
        ...this.queryParams
      }, `bottombreakoutconfig_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
