<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置版本名称" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置版本名称"
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
      <el-form-item label="最小K线数要求" prop="minKlineCount">
        <el-input
          v-model="queryParams.minKlineCount"
          placeholder="请输入最小K线数要求"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="VCP回溯天数" prop="vcpLookbackDays">
        <el-input
          v-model="queryParams.vcpLookbackDays"
          placeholder="请输入VCP回溯天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="VCP最大振幅限制" prop="vcpMaxAmplitude">
        <el-input
          v-model="queryParams.vcpMaxAmplitude"
          placeholder="请输入VCP最大振幅限制"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="VCP最小涨幅" prop="vcpMinChangeRate">
        <el-input
          v-model="queryParams.vcpMinChangeRate"
          placeholder="请输入VCP最小涨幅"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="VCP最小放量倍数" prop="vcpMinVolRatio">
        <el-input
          v-model="queryParams.vcpMinVolRatio"
          placeholder="请输入VCP最小放量倍数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="VCP突破前高回溯天数" prop="vcpHighLookback">
        <el-input
          v-model="queryParams.vcpHighLookback"
          placeholder="请输入VCP突破前高回溯天数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="均线粘合极差比率上限" prop="coilMaxDiffRatio">
        <el-input
          v-model="queryParams.coilMaxDiffRatio"
          placeholder="请输入均线粘合极差比率上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="空中加油回踩MA20容忍度" prop="airRefuelMaTolerance">
        <el-input
          v-model="queryParams.airRefuelMaTolerance"
          placeholder="请输入空中加油回踩MA20容忍度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="MACD零轴贴近极限" prop="macdNearZeroLimit">
        <el-input
          v-model="queryParams.macdNearZeroLimit"
          placeholder="请输入MACD零轴贴近极限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数N" prop="kdjN">
        <el-input
          v-model="queryParams.kdjN"
          placeholder="请输入KDJ参数N"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数M1" prop="kdjM1">
        <el-input
          v-model="queryParams.kdjM1"
          placeholder="请输入KDJ参数M1"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ参数M2" prop="kdjM2">
        <el-input
          v-model="queryParams.kdjM2"
          placeholder="请输入KDJ参数M2"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ强势区间下限" prop="kdjMinStrong">
        <el-input
          v-model="queryParams.kdjMinStrong"
          placeholder="请输入KDJ强势区间下限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="KDJ强势区间上限" prop="kdjMaxStrong">
        <el-input
          v-model="queryParams.kdjMaxStrong"
          placeholder="请输入KDJ强势区间上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI周期" prop="rsiPeriod">
        <el-input
          v-model="queryParams.rsiPeriod"
          placeholder="请输入RSI周期"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI下限" prop="rsiMin">
        <el-input
          v-model="queryParams.rsiMin"
          placeholder="请输入RSI下限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="RSI上限" prop="rsiMax">
        <el-input
          v-model="queryParams.rsiMax"
          placeholder="请输入RSI上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OBV创新高回溯天数" prop="obvLookbackDays">
        <el-input
          v-model="queryParams.obvLookbackDays"
          placeholder="请输入OBV创新高回溯天数"
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
      <el-form-item label="量价爆发力权重" prop="swVolumeWeight">
        <el-input
          v-model="queryParams.swVolumeWeight"
          placeholder="请输入量价爆发力权重"
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
      <el-form-item label="技术动能权重" prop="swMomentumWeight">
        <el-input
          v-model="queryParams.swMomentumWeight"
          placeholder="请输入技术动能权重"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="形态品质权重" prop="swPatternWeight">
        <el-input
          v-model="queryParams.swPatternWeight"
          placeholder="请输入形态品质权重"
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
      <el-form-item label="MACD强红柱得分" prop="swMacdHighScore">
        <el-input
          v-model="queryParams.swMacdHighScore"
          placeholder="请输入MACD强红柱得分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="MACD一般红柱得分" prop="swMacdMidScore">
        <el-input
          v-model="queryParams.swMacdMidScore"
          placeholder="请输入MACD一般红柱得分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="形态预设得分" prop="swPatternDefaultScore">
        <el-input
          v-model="queryParams.swPatternDefaultScore"
          placeholder="请输入形态预设得分"
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
          v-hasPermi="['stock:strongbreakoutconfig:add']"
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
          v-hasPermi="['stock:strongbreakoutconfig:edit']"
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
          v-hasPermi="['stock:strongbreakoutconfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:strongbreakoutconfig:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="strongbreakoutconfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置主键ID" align="center" prop="id" />
      <el-table-column label="配置版本名称" align="center" prop="configName" />
      <el-table-column label="策略编码" align="center" prop="strategyCode" />
      <el-table-column label="策略名称" align="center" prop="strategyName" />
      <el-table-column label="最小K线数要求" align="center" prop="minKlineCount" />
      <el-table-column label="VCP回溯天数" align="center" prop="vcpLookbackDays" />
      <el-table-column label="VCP最大振幅限制" align="center" prop="vcpMaxAmplitude" />
      <el-table-column label="VCP最小涨幅" align="center" prop="vcpMinChangeRate" />
      <el-table-column label="VCP最小放量倍数" align="center" prop="vcpMinVolRatio" />
      <el-table-column label="VCP突破前高回溯天数" align="center" prop="vcpHighLookback" />
      <el-table-column label="均线粘合极差比率上限" align="center" prop="coilMaxDiffRatio" />
      <el-table-column label="空中加油回踩MA20容忍度" align="center" prop="airRefuelMaTolerance" />
      <el-table-column label="MACD零轴贴近极限" align="center" prop="macdNearZeroLimit" />
      <el-table-column label="KDJ参数N" align="center" prop="kdjN" />
      <el-table-column label="KDJ参数M1" align="center" prop="kdjM1" />
      <el-table-column label="KDJ参数M2" align="center" prop="kdjM2" />
      <el-table-column label="KDJ强势区间下限" align="center" prop="kdjMinStrong" />
      <el-table-column label="KDJ强势区间上限" align="center" prop="kdjMaxStrong" />
      <el-table-column label="RSI周期" align="center" prop="rsiPeriod" />
      <el-table-column label="RSI下限" align="center" prop="rsiMin" />
      <el-table-column label="RSI上限" align="center" prop="rsiMax" />
      <el-table-column label="OBV创新高回溯天数" align="center" prop="obvLookbackDays" />
      <el-table-column label="最小获利盘比例" align="center" prop="chipMinProfitRatio" />
      <el-table-column label="量价爆发力权重" align="center" prop="swVolumeWeight" />
      <el-table-column label="筹码面权重" align="center" prop="swChipWeight" />
      <el-table-column label="技术动能权重" align="center" prop="swMomentumWeight" />
      <el-table-column label="形态品质权重" align="center" prop="swPatternWeight" />
      <el-table-column label="单峰密集加分" align="center" prop="swSinglePeakBonus" />
      <el-table-column label="MACD强红柱得分" align="center" prop="swMacdHighScore" />
      <el-table-column label="MACD一般红柱得分" align="center" prop="swMacdMidScore" />
      <el-table-column label="形态预设得分" align="center" prop="swPatternDefaultScore" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['stock:strongbreakoutconfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:strongbreakoutconfig:remove']"
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

    <!-- 添加或修改主升浪策略回测参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="配置版本名称" prop="configName">
              <el-input v-model="form.configName" placeholder="请输入配置版本名称" />
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
            <el-form-item label="最小K线数要求" prop="minKlineCount">
              <el-input v-model="form.minKlineCount" placeholder="请输入最小K线数要求" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="VCP回溯天数" prop="vcpLookbackDays">
              <el-input v-model="form.vcpLookbackDays" placeholder="请输入VCP回溯天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="VCP最大振幅限制" prop="vcpMaxAmplitude">
              <el-input v-model="form.vcpMaxAmplitude" placeholder="请输入VCP最大振幅限制" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="VCP最小涨幅" prop="vcpMinChangeRate">
              <el-input v-model="form.vcpMinChangeRate" placeholder="请输入VCP最小涨幅" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="VCP最小放量倍数" prop="vcpMinVolRatio">
              <el-input v-model="form.vcpMinVolRatio" placeholder="请输入VCP最小放量倍数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="VCP突破前高回溯天数" prop="vcpHighLookback">
              <el-input v-model="form.vcpHighLookback" placeholder="请输入VCP突破前高回溯天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="均线粘合极差比率上限" prop="coilMaxDiffRatio">
              <el-input v-model="form.coilMaxDiffRatio" placeholder="请输入均线粘合极差比率上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="空中加油回踩MA20容忍度" prop="airRefuelMaTolerance">
              <el-input v-model="form.airRefuelMaTolerance" placeholder="请输入空中加油回踩MA20容忍度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="MACD零轴贴近极限" prop="macdNearZeroLimit">
              <el-input v-model="form.macdNearZeroLimit" placeholder="请输入MACD零轴贴近极限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数N" prop="kdjN">
              <el-input v-model="form.kdjN" placeholder="请输入KDJ参数N" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数M1" prop="kdjM1">
              <el-input v-model="form.kdjM1" placeholder="请输入KDJ参数M1" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ参数M2" prop="kdjM2">
              <el-input v-model="form.kdjM2" placeholder="请输入KDJ参数M2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ强势区间下限" prop="kdjMinStrong">
              <el-input v-model="form.kdjMinStrong" placeholder="请输入KDJ强势区间下限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="KDJ强势区间上限" prop="kdjMaxStrong">
              <el-input v-model="form.kdjMaxStrong" placeholder="请输入KDJ强势区间上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI周期" prop="rsiPeriod">
              <el-input v-model="form.rsiPeriod" placeholder="请输入RSI周期" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI下限" prop="rsiMin">
              <el-input v-model="form.rsiMin" placeholder="请输入RSI下限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="RSI上限" prop="rsiMax">
              <el-input v-model="form.rsiMax" placeholder="请输入RSI上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="OBV创新高回溯天数" prop="obvLookbackDays">
              <el-input v-model="form.obvLookbackDays" placeholder="请输入OBV创新高回溯天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最小获利盘比例" prop="chipMinProfitRatio">
              <el-input v-model="form.chipMinProfitRatio" placeholder="请输入最小获利盘比例" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="量价爆发力权重" prop="swVolumeWeight">
              <el-input v-model="form.swVolumeWeight" placeholder="请输入量价爆发力权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="筹码面权重" prop="swChipWeight">
              <el-input v-model="form.swChipWeight" placeholder="请输入筹码面权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="技术动能权重" prop="swMomentumWeight">
              <el-input v-model="form.swMomentumWeight" placeholder="请输入技术动能权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="形态品质权重" prop="swPatternWeight">
              <el-input v-model="form.swPatternWeight" placeholder="请输入形态品质权重" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单峰密集加分" prop="swSinglePeakBonus">
              <el-input v-model="form.swSinglePeakBonus" placeholder="请输入单峰密集加分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="MACD强红柱得分" prop="swMacdHighScore">
              <el-input v-model="form.swMacdHighScore" placeholder="请输入MACD强红柱得分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="MACD一般红柱得分" prop="swMacdMidScore">
              <el-input v-model="form.swMacdMidScore" placeholder="请输入MACD一般红柱得分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="形态预设得分" prop="swPatternDefaultScore">
              <el-input v-model="form.swPatternDefaultScore" placeholder="请输入形态预设得分" />
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
import { listStrongbreakoutconfig, getStrongbreakoutconfig, delStrongbreakoutconfig, addStrongbreakoutconfig, updateStrongbreakoutconfig } from "@/api/stock/strongbreakoutconfig"

export default {
  name: "Strongbreakoutconfig",
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
      // 主升浪策略回测参数配置表格数据
      strongbreakoutconfigList: [],
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
        vcpLookbackDays: null,
        vcpMaxAmplitude: null,
        vcpMinChangeRate: null,
        vcpMinVolRatio: null,
        vcpHighLookback: null,
        coilMaxDiffRatio: null,
        airRefuelMaTolerance: null,
        macdNearZeroLimit: null,
        kdjN: null,
        kdjM1: null,
        kdjM2: null,
        kdjMinStrong: null,
        kdjMaxStrong: null,
        rsiPeriod: null,
        rsiMin: null,
        rsiMax: null,
        obvLookbackDays: null,
        chipMinProfitRatio: null,
        swVolumeWeight: null,
        swChipWeight: null,
        swMomentumWeight: null,
        swPatternWeight: null,
        swSinglePeakBonus: null,
        swMacdHighScore: null,
        swMacdMidScore: null,
        swPatternDefaultScore: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        configName: [
          { required: true, message: "配置版本名称不能为空", trigger: "blur" }
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
    /** 查询主升浪策略回测参数配置列表 */
    getList() {
      this.loading = true
      listStrongbreakoutconfig(this.queryParams).then(response => {
        this.strongbreakoutconfigList = response.rows
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
        vcpLookbackDays: null,
        vcpMaxAmplitude: null,
        vcpMinChangeRate: null,
        vcpMinVolRatio: null,
        vcpHighLookback: null,
        coilMaxDiffRatio: null,
        airRefuelMaTolerance: null,
        macdNearZeroLimit: null,
        kdjN: null,
        kdjM1: null,
        kdjM2: null,
        kdjMinStrong: null,
        kdjMaxStrong: null,
        rsiPeriod: null,
        rsiMin: null,
        rsiMax: null,
        obvLookbackDays: null,
        chipMinProfitRatio: null,
        swVolumeWeight: null,
        swChipWeight: null,
        swMomentumWeight: null,
        swPatternWeight: null,
        swSinglePeakBonus: null,
        swMacdHighScore: null,
        swMacdMidScore: null,
        swPatternDefaultScore: null,
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
      this.title = "添加主升浪策略回测参数配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getStrongbreakoutconfig(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改主升浪策略回测参数配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStrongbreakoutconfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStrongbreakoutconfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除主升浪策略回测参数配置编号为"' + ids + '"的数据项？').then(function() {
        return delStrongbreakoutconfig(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('stock/strongbreakoutconfig/export', {
        ...this.queryParams
      }, `strongbreakoutconfig_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
