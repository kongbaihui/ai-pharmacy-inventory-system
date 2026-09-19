<template>
  <div class="app-container stock-check-dashboard">
    <!-- ============ 顶部筛选 ============ -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
        <el-form-item label="盘点名称" prop="checkName">
          <el-input
            v-model="queryParams.checkName"
            placeholder="请输入盘点名称"
            clearable
            @keyup.enter.native="loadData"
          />
        </el-form-item>
        <el-form-item label="盘点类型" prop="checkType">
          <el-select v-model="queryParams.checkType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="全盘" value="0" />
            <el-option label="抽盘" value="1" />
            <el-option label="重点盘点" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点状态" prop="checkStatus">
          <el-select v-model="queryParams.checkStatus" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待盘点" value="0" />
            <el-option label="盘点中" value="1" />
            <el-option label="已盘点" value="2" />
            <el-option label="已审核" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ============ 概览卡片 ============ -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.color + '1A', color: card.color }">
            <i :class="card.icon"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ============ 图表区 1：盘点盈亏柱状图 + 盘点类型饼图 ============ -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>各盘点单盈亏对比</span>
          </div>
          <div ref="barChart" class="chart chart-lg"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>盘点类型分布</span>
          </div>
          <div ref="pieChart" class="chart chart-lg"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ============ 图表区 2：药品盈亏排行 + 盈亏类型占比 ============ -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>药品盈亏数量 Top 10</span>
          </div>
          <div ref="hBarChart" class="chart chart-lg"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header" class="chart-header">
            <span>盈亏类型占比</span>
          </div>
          <div ref="pieChart2" class="chart chart-lg"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ============ 明细表 ============ -->
    <el-card shadow="never" class="table-card">
      <div slot="header" class="chart-header">
        <span>盘点明细</span>
        <el-tag size="small" type="info">共 {{ checkItemList.length }} 条</el-tag>
      </div>
      <el-table v-loading="loading" :data="checkItemList" stripe max-height="420">
        <el-table-column label="所属盘点" prop="checkName" align="center" min-width="140" />
        <el-table-column label="药品名称" align="center" min-width="160">
          <template slot-scope="scope">{{ getMedName(scope.row.medId) }}</template>
        </el-table-column>
        <el-table-column label="批次ID" prop="batchId" align="center" width="90" />
        <el-table-column label="生产批号" prop="batchNo" align="center" min-width="130" />
        <el-table-column label="账面数量" prop="bookQty" align="center" width="90" />
        <el-table-column label="实盘数量" prop="realQty" align="center" width="90" />
        <el-table-column label="盈亏数量" align="center" width="100">
          <template slot-scope="scope">
            <span
              :style="{
                color: scope.row.diffQty > 0 ? '#67C23A' : (scope.row.diffQty < 0 ? '#F56C6C' : '#909399'),
                fontWeight: 600
              }"
            >
              {{ scope.row.diffQty > 0 ? '+' + scope.row.diffQty : scope.row.diffQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="盈亏类型" align="center" width="100">
          <template slot-scope="scope">
            <el-tag :type="diffTypeTag(scope.row.diffType)" size="mini">
              {{ diffTypeText(scope.row.diffType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="盈亏原因" prop="diffReason" align="center" show-overflow-tooltip min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { listCheck, getCheck } from "@/api/system/check"
import { listInfo } from "@/api/system/info"

export default {
  name: "CheckDashboard",
  data() {
    return {
      loading: false,
      // 盘点主表数据
      checkList: [],
      // 所有盘点明细的汇总
      checkItemList: [],
      // 药品 id -> 药品名称
      medMap: {},
      queryParams: {
        checkName: null,
        checkType: null,
        checkStatus: null
      },
      // 图表实例缓存
      charts: {
        bar: null,
        pie: null,
        hBar: null,
        pie2: null
      }
    }
  },
  computed: {
    statCards() {
      let gainQty = 0
      let lossQty = 0
      this.checkItemList.forEach(item => {
        const q = Number(item.diffQty) || 0
        if (q > 0) gainQty += q
        else if (q < 0) lossQty += Math.abs(q)
      })
      return [
        { label: '盘点单总数', value: this.checkList.length, icon: 'el-icon-document-checked', color: '#409EFF' },
        { label: '盘点明细数', value: this.checkItemList.length, icon: 'el-icon-s-order', color: '#909399' },
        { label: '盘盈总量', value: '+' + gainQty, icon: 'el-icon-top', color: '#67C23A' },
        { label: '盘亏总量', value: '-' + lossQty, icon: 'el-icon-bottom', color: '#F56C6C' }
      ]
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(c => c && c.dispose())
  },
  methods: {
    /** 拉取盘点主表 + 药品列表 + 所有明细 */
    async loadData() {
      this.loading = true
      try {
        const checkParams = { pageNum: 1, pageSize: 1000 }
        if (this.queryParams.checkName) checkParams.checkName = this.queryParams.checkName
        if (this.queryParams.checkType) checkParams.checkType = this.queryParams.checkType
        if (this.queryParams.checkStatus) checkParams.checkStatus = this.queryParams.checkStatus

        const [checkRes, infoRes] = await Promise.all([
          listCheck(checkParams),
          listInfo({ pageNum: 1, pageSize: 1000 })
        ])

        const checks = checkRes.rows || []
        const meds = infoRes.rows || []

        // 建立药品 id -> name 映射
        const medMap = {}
        meds.forEach(m => { medMap[m.medId] = m.medName })
        this.medMap = medMap

        // 并发拉取每一条盘点的明细
        const detailResults = await Promise.all(
          checks.map(c => getCheck(c.checkId).catch(() => ({ data: {} })))
        )

        // 汇总所有明细，并带上所属盘点信息
        const allItems = []
        detailResults.forEach((res, idx) => {
          const check = checks[idx]
          const items = (res.data && res.data.medStockCheckItemList) || []
          items.forEach(item => {
            allItems.push({
              ...item,
              checkId: check.checkId,
              checkName: check.checkName || ('盘点' + check.checkId),
              checkType: check.checkType
            })
          })
        })

        this.checkList = checks
        this.checkItemList = allItems

        this.$nextTick(() => {
          this.renderBarChart()
          this.renderPieChart()
          this.renderHBarChart()
          this.renderPie2Chart()
        })
      } finally {
        this.loading = false
      }
    },

    /** 各盘点单盈亏对比柱状图 */
    renderBarChart() {
      const el = this.$refs.barChart
      if (!el) return
      if (this.charts.bar) this.charts.bar.dispose()
      this.charts.bar = echarts.init(el)

      const byCheck = {}
      this.checkItemList.forEach(item => {
        const key = item.checkId
        if (!byCheck[key]) {
          byCheck[key] = { name: item.checkName, gain: 0, loss: 0 }
        }
        const q = Number(item.diffQty) || 0
        if (q > 0) byCheck[key].gain += q
        else if (q < 0) byCheck[key].loss += Math.abs(q)
      })
      const list = Object.values(byCheck)

      if (list.length === 0) {
        this.charts.bar.setOption({
          title: {
            text: '暂无盘点数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14, fontWeight: 'normal' }
          }
        })
        return
      }

      this.charts.bar.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['盘盈', '盘亏'], bottom: 0 },
        grid: { left: 50, right: 24, top: 24, bottom: 50 },
        xAxis: {
          type: 'category',
          data: list.map(i => i.name),
          axisLabel: { rotate: list.length > 4 ? 30 : 0, color: '#606266' }
        },
        yAxis: {
          type: 'value',
          name: '数量',
          nameTextStyle: { color: '#909399' },
          axisLabel: { color: '#606266' },
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        series: [
          {
            name: '盘盈',
            type: 'bar',
            data: list.map(i => i.gain),
            barMaxWidth: 36,
            itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] }
          },
          {
            name: '盘亏',
            type: 'bar',
            data: list.map(i => i.loss),
            barMaxWidth: 36,
            itemStyle: { color: '#F56C6C', borderRadius: [4, 4, 0, 0] }
          }
        ]
      })
    },

    /** 盘点类型分布饼图 */
    renderPieChart() {
      const el = this.$refs.pieChart
      if (!el) return
      if (this.charts.pie) this.charts.pie.dispose()
      this.charts.pie = echarts.init(el)

      const typeMap = { '0': '全盘', '1': '抽盘', '2': '重点盘点' }
      const counts = {}
      this.checkList.forEach(c => {
        const t = c.checkType != null ? String(c.checkType) : '0'
        counts[t] = (counts[t] || 0) + 1
      })
      const data = Object.entries(counts).map(([t, v]) => ({
        name: typeMap[t] || ('类型' + t),
        value: v
      }))

      if (data.length === 0) {
        this.charts.pie.setOption({
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14, fontWeight: 'normal' }
          }
        })
        return
      }

      this.charts.pie.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, textStyle: { color: '#606266' } },
        color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C'],
        series: [{
          type: 'pie',
          radius: ['40%', '68%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          label: { formatter: '{b}\n{d}%', color: '#606266' },
          labelLine: { length: 8, length2: 10 },
          data
        }]
      })
    },

    /** 药品盈亏 Top 10 横向柱状图 */
    renderHBarChart() {
      const el = this.$refs.hBarChart
      if (!el) return
      if (this.charts.hBar) this.charts.hBar.dispose()
      this.charts.hBar = echarts.init(el)

      const agg = {}
      this.checkItemList.forEach(item => {
        const id = item.medId
        if (id == null) return
        agg[id] = (agg[id] || 0) + (Number(item.diffQty) || 0)
      })

      const list = Object.entries(agg)
        .map(([id, qty]) => ({
          name: this.medMap[id] || ('药品' + id),
          value: qty
        }))
        .sort((a, b) => Math.abs(b.value) - Math.abs(a.value))
        .slice(0, 10)
        .reverse()

      if (list.length === 0) {
        this.charts.hBar.setOption({
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14, fontWeight: 'normal' }
          }
        })
        return
      }

      this.charts.hBar.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 130, right: 40, top: 20, bottom: 30 },
        xAxis: {
          type: 'value',
          name: '盈亏数量',
          nameTextStyle: { color: '#909399' },
          axisLabel: { color: '#606266' },
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        yAxis: {
          type: 'category',
          data: list.map(i => i.name),
          axisLabel: { fontSize: 12, color: '#606266' }
        },
        series: [{
          type: 'bar',
          barMaxWidth: 22,
          data: list.map(i => ({
            value: i.value,
            itemStyle: {
              color: i.value >= 0 ? '#67C23A' : '#F56C6C',
              borderRadius: i.value >= 0 ? [0, 4, 4, 0] : [4, 0, 0, 4]
            }
          })),
          label: {
            show: true,
            position: 'right',
            formatter: '{c}',
            color: '#606266'
          }
        }]
      })
    },

    /** 盈亏类型占比饼图 */
    renderPie2Chart() {
      const el = this.$refs.pieChart2
      if (!el) return
      if (this.charts.pie2) this.charts.pie2.dispose()
      this.charts.pie2 = echarts.init(el)

      const typeMap = { '0': '正常', '1': '盘盈', '2': '盘亏' }
      const colorMap = { '0': '#909399', '1': '#67C23A', '2': '#F56C6C' }
      const counts = {}
      this.checkItemList.forEach(item => {
        const t = item.diffType != null ? String(item.diffType) : '0'
        counts[t] = (counts[t] || 0) + 1
      })

      const data = Object.entries(counts).map(([t, v]) => ({
        name: typeMap[t] || ('类型' + t),
        value: v,
        itemStyle: { color: colorMap[t] || '#409EFF' }
      }))

      if (data.length === 0) {
        this.charts.pie2.setOption({
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#999', fontSize: 14, fontWeight: 'normal' }
          }
        })
        return
      }

      this.charts.pie2.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, textStyle: { color: '#606266' } },
        series: [{
          type: 'pie',
          radius: '62%',
          center: ['50%', '45%'],
          label: { formatter: '{b}\n{d}%', color: '#606266' },
          data
        }]
      })
    },

    /** 窗口变化时重绘 */
    handleResize() {
      Object.values(this.charts).forEach(c => c && c.resize())
    },

    /** 重置查询 */
    resetQuery() {
      this.queryParams = { checkName: null, checkType: null, checkStatus: null }
      this.loadData()
    },

    /** 药品 id -> 名称 */
    getMedName(medId) {
      if (medId == null || medId === '') return ''
      return this.medMap[medId] || medId
    },

    /** 盈亏类型文案 */
    diffTypeText(t) {
      const map = { '0': '正常', '1': '盘盈', '2': '盘亏' }
      return map[t] || '正常'
    },

    /** 盈亏类型标签颜色 */
    diffTypeTag(t) {
      const map = { '0': 'info', '1': 'success', '2': 'danger' }
      return map[t] || 'info'
    }
  }
}
</script>

<style scoped>
.stock-check-dashboard .filter-card {
  margin-bottom: 16px;
}
.stock-check-dashboard .filter-card ::v-deep .el-form-item {
  margin-bottom: 0;
}
.stock-check-dashboard .stat-row {
  margin-bottom: 16px;
}
.stock-check-dashboard .stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 18px 20px;
}
.stock-check-dashboard .stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-right: 16px;
  flex-shrink: 0;
}
.stock-check-dashboard .stat-content {
  flex: 1;
  overflow: hidden;
}
.stock-check-dashboard .stat-value {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
}
.stock-check-dashboard .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.stock-check-dashboard .chart-row {
  margin-bottom: 16px;
}
.stock-check-dashboard .chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.stock-check-dashboard .chart {
  width: 100%;
  height: 320px;
}
.stock-check-dashboard .chart-lg {
  height: 340px;
}
.stock-check-dashboard .table-card {
  margin-bottom: 16px;
}
</style>