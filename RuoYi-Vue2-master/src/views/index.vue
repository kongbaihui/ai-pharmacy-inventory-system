<template>
  <div class="dashboard-page">
    <div class="dashboard-heading">
      <div><h1>药品库存工作台</h1><p>掌握药品库存动态，保障临床用药安全</p></div>
      <div class="heading-actions"><span>{{ currentDate }}</span><el-button icon="el-icon-refresh" size="small" :loading="loading" @click="loadDashboard">刷新数据</el-button></div>
    </div>

    <div class="metric-row">
      <div v-for="item in metrics" :key="item.label" class="metric-item">
        <div class="metric-icon" :class="item.tone"><svg-icon :icon-class="item.icon" /></div>
        <div><span>{{ item.label }}</span><strong>{{ formatNumber(item.value) }}</strong><small>{{ item.unit }}</small></div>
      </div>
    </div>

    <el-row :gutter="16" class="dashboard-row">
      <el-col :xs="24" :lg="16">
        <section class="data-panel chart-panel">
          <div class="panel-title"><div><h2>近 12 个月药品出入库趋势</h2><span>按已确认库存业务单统计</span></div><div class="legend"><span class="inbound">入库数量</span><span class="outbound">出库数量</span></div></div>
          <div ref="trendChart" class="chart-canvas" />
        </section>
      </el-col>
      <el-col :xs="24" :lg="8">
        <section class="data-panel chart-panel">
          <div class="panel-title"><div><h2>库存状态分布</h2><span>当前有效库存与风险批次</span></div></div>
          <div ref="statusChart" class="chart-canvas" />
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="dashboard-row">
      <el-col :xs="24" :lg="16">
        <section class="data-panel table-panel">
          <div class="panel-title"><div><h2>最近库存业务</h2><span>入库、出库和退库单据</span></div><router-link :to="{ path: '/med/business/inbound', query: { orderType: '1' } }">查看全部</router-link></div>
          <el-table :data="recentOrders" size="small">
            <el-table-column prop="orderNo" label="单据编号" min-width="175" show-overflow-tooltip />
            <el-table-column label="业务类型" width="88"><template slot-scope="scope"><span :class="['business-type', 'type-' + scope.row.orderType]">{{ typeName(scope.row.orderType) }}</span></template></el-table-column>
            <el-table-column label="业务单位" min-width="130" show-overflow-tooltip><template slot-scope="scope">{{ scope.row.supplierName || scope.row.department || '—' }}</template></el-table-column>
            <el-table-column prop="totalQty" label="数量" width="80" align="right" />
            <el-table-column prop="operator" label="经办人" width="90" />
            <el-table-column prop="orderDate" label="日期" width="105" />
            <el-table-column label="状态" width="80" align="center"><template slot-scope="scope"><el-tag :type="scope.row.orderStatus === '1' ? 'success' : 'info'" size="mini">{{ scope.row.orderStatus === '1' ? '已确认' : '草稿' }}</el-tag></template></el-table-column>
          </el-table>
          <div v-if="!recentOrders.length && !loading" class="empty-line">暂无库存业务记录</div>
        </section>
      </el-col>
      <el-col :xs="24" :lg="8">
        <section class="data-panel warning-panel">
          <div class="panel-title"><div><h2>待处理预警</h2><span>库存不足、积压与效期风险</span></div><router-link to="/med/stock/warn">查看全部</router-link></div>
          <div v-if="warnings.length" class="warning-list">
            <div v-for="item in warnings" :key="item.warnId" class="warning-item">
              <span class="warning-dot" :class="item.warnLevel === '2' ? 'danger' : 'warning'" />
              <div><strong>{{ item.medName || '药品预警' }}</strong><small>{{ item.warnContent || warnTypeName(item.warnType) }}</small></div>
              <span class="warning-qty">{{ item.currentQty == null ? '—' : item.currentQty }}</span>
            </div>
          </div>
          <div v-else class="empty-warning"><i class="el-icon-circle-check" /><span>暂无待处理库存预警</span></div>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { listInfo } from '@/api/system/info'
import { getWarnSummary, listWarn } from '@/api/system/warn'
import { getExpirySummary } from '@/api/system/batch'
import { listStockOrder } from '@/api/system/stockOrder'

export default {
  name: 'Index',
  data() {
    return { loading: false, medicineCount: 0, stockTotal: 0, warningCount: 0, nearExpiryCount: 0, expirySummary: {}, warnSummary: {}, recentOrders: [], warnings: [], allOrders: [], trendChart: null, statusChart: null }
  },
  computed: {
    currentDate() { return this.parseTime(new Date(), '{y}年{m}月{d}日') },
    metrics() {
      return [
        { label: '药品品种', value: this.medicineCount, unit: '种', icon: 'clipboard', tone: 'teal' },
        { label: '库存总量', value: this.stockTotal, unit: '件', icon: 'shopping', tone: 'blue' },
        { label: '库存预警', value: this.warningCount, unit: '条', icon: 'message', tone: 'red' },
        { label: '近效期批次', value: this.nearExpiryCount, unit: '批', icon: 'time-range', tone: 'amber' }
      ]
    }
  },
  mounted() { this.loadDashboard(); window.addEventListener('resize', this.resizeCharts) },
  beforeDestroy() { window.removeEventListener('resize', this.resizeCharts); if (this.trendChart) this.trendChart.dispose(); if (this.statusChart) this.statusChart.dispose() },
  methods: {
    loadDashboard() {
      this.loading = true
      const tasks = [
        listInfo({ pageNum: 1, pageSize: 1 }).then(res => { this.medicineCount = res.total || 0 }),
        getWarnSummary().then(res => { this.warnSummary = res.data || {}; this.warningCount = Number(this.warnSummary.unhandleCount || 0) }),
        getExpirySummary().then(res => { this.expirySummary = res.data || {}; this.nearExpiryCount = Number(this.expirySummary.nearCount || 0); this.stockTotal = Number(this.expirySummary.normalQty || 0) + Number(this.expirySummary.nearQty || 0) + Number(this.expirySummary.expiredQty || 0) }),
        listWarn({ pageNum: 1, pageSize: 6, handleStatus: '0' }).then(res => { this.warnings = res.rows || [] }),
        listStockOrder({ pageNum: 1, pageSize: 500 }).then(res => { this.allOrders = res.rows || []; this.recentOrders = this.allOrders.slice(0, 6) })
      ]
      Promise.all(tasks.map(task => task.catch(() => null))).then(() => this.$nextTick(this.renderCharts)).finally(() => { this.loading = false })
    },
    renderCharts() { this.renderTrendChart(); this.renderStatusChart() },
    renderTrendChart() {
      if (!this.$refs.trendChart) return
      this.trendChart = this.trendChart || echarts.init(this.$refs.trendChart)
      const months = [], inbound = [], outbound = [], now = new Date()
      for (let i = 11; i >= 0; i--) { const date = new Date(now.getFullYear(), now.getMonth() - i, 1); months.push({ key: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`, label: `${date.getMonth() + 1}月` }); inbound.push(0); outbound.push(0) }
      this.allOrders.filter(item => item.orderStatus === '1').forEach(item => { const index = months.findIndex(month => month.key === String(item.orderDate || '').slice(0, 7)); if (index > -1 && item.orderType === '1') inbound[index] += Number(item.totalQty || 0); if (index > -1 && item.orderType === '2') outbound[index] += Number(item.totalQty || 0) })
      this.trendChart.setOption({ color: ['#10a995', '#2f7de1'], tooltip: { trigger: 'axis' }, grid: { left: 16, right: 18, top: 26, bottom: 8, containLabel: true }, xAxis: { type: 'category', boundaryGap: false, data: months.map(item => item.label), axisLine: { lineStyle: { color: '#dce4eb' } }, axisLabel: { color: '#718397' } }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#edf1f5' } }, axisLabel: { color: '#718397' } }, series: [{ name: '入库数量', type: 'line', smooth: true, symbolSize: 6, data: inbound, areaStyle: { color: 'rgba(16,169,149,.08)' } }, { name: '出库数量', type: 'line', smooth: true, symbolSize: 6, data: outbound }] })
    },
    renderStatusChart() {
      if (!this.$refs.statusChart) return
      this.statusChart = this.statusChart || echarts.init(this.$refs.statusChart)
      const normal = Number(this.expirySummary.normalQty || 0), near = Number(this.expirySummary.nearQty || 0), expired = Number(this.expirySummary.expiredQty || 0)
      this.statusChart.setOption({ color: ['#10a995', '#f2a93b', '#e35361'], tooltip: { trigger: 'item' }, legend: { bottom: 4, itemWidth: 9, itemHeight: 9, textStyle: { color: '#617488' } }, series: [{ type: 'pie', radius: ['52%', '73%'], center: ['50%', '43%'], label: { show: false }, data: [{ name: '正常库存', value: normal }, { name: '近效期', value: near }, { name: '已过期', value: expired }] }], graphic: [{ type: 'text', left: 'center', top: '35%', style: { text: this.formatNumber(this.stockTotal), fill: '#183754', font: '600 22px sans-serif', textAlign: 'center' } }, { type: 'text', left: 'center', top: '46%', style: { text: '库存总量', fill: '#7b8b9a', font: '12px sans-serif', textAlign: 'center' } }] })
    },
    resizeCharts() { if (this.trendChart) this.trendChart.resize(); if (this.statusChart) this.statusChart.resize() },
    formatNumber(value) { return Number(value || 0).toLocaleString('zh-CN') },
    typeName(type) { return ({ '1': '入库', '2': '出库', '3': '退库' })[type] || '其他' },
    warnTypeName(type) { return ({ '0': '库存不足', '1': '库存积压', '2': '近效期', '3': '已过期' })[type] || '库存预警' }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-page{min-height:calc(100vh - 84px);padding:24px;background:#f5f7fa;color:#183754}.dashboard-heading{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:20px}.dashboard-heading h1{margin:0;font-size:27px;line-height:38px;letter-spacing:-.5px}.dashboard-heading p{margin:4px 0 0;color:#738497;font-size:14px}.heading-actions{display:flex;align-items:center;gap:14px;color:#66798d;font-size:13px}.metric-row{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:16px}.metric-item{display:flex;align-items:center;min-height:112px;padding:20px 22px;background:#fff;border:1px solid #e0e7ed;border-radius:7px}.metric-icon{display:flex;align-items:center;justify-content:center;width:50px;height:50px;margin-right:17px;border-radius:7px;font-size:25px}.metric-icon.teal{color:#079b83;background:#e6f7f4}.metric-icon.blue{color:#2f7de1;background:#eaf2fd}.metric-icon.red{color:#e35361;background:#fdecef}.metric-icon.amber{color:#df941d;background:#fff5e2}.metric-item span{display:block;margin-bottom:7px;color:#66798d;font-size:13px}.metric-item strong{color:#163858;font-size:27px;line-height:30px}.metric-item small{margin-left:6px;color:#8291a0}.dashboard-row{margin-bottom:16px}.data-panel{background:#fff;border:1px solid #e0e7ed;border-radius:7px}.chart-panel{padding:18px 20px 12px}.chart-canvas{height:310px}.panel-title{display:flex;align-items:center;justify-content:space-between;min-height:42px}.panel-title h2{margin:0;color:#1b3955;font-size:16px;line-height:24px}.panel-title span{color:#8996a4;font-size:12px}.panel-title a{color:#2f7de1;font-size:13px}.legend{display:flex;gap:18px}.legend span:before{display:inline-block;width:16px;height:3px;margin-right:7px;border-radius:2px;vertical-align:middle;content:''}.legend .inbound:before{background:#10a995}.legend .outbound:before{background:#2f7de1}.table-panel,.warning-panel{min-height:330px;padding:18px 20px}.table-panel ::v-deep .el-table:before{display:none}.table-panel ::v-deep th{background:#f5f7f9;color:#53677b;font-weight:500}.business-type{font-weight:600}.type-1{color:#079b83}.type-2{color:#2f7de1}.type-3{color:#df941d}.warning-list{margin-top:8px}.warning-item{display:flex;align-items:center;min-height:48px;border-bottom:1px solid #edf1f4}.warning-dot{width:8px;height:8px;margin-right:12px;border-radius:50%;background:#f2a93b}.warning-dot.danger{background:#e35361}.warning-item div{flex:1;min-width:0}.warning-item strong,.warning-item small{display:block;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.warning-item strong{color:#344d65;font-size:13px}.warning-item small{margin-top:3px;color:#8996a4;font-size:12px}.warning-qty{margin-left:10px;color:#e35361;font-weight:600}.empty-line,.empty-warning{display:flex;align-items:center;justify-content:center;min-height:210px;color:#9aa7b3;font-size:13px}.empty-warning{flex-direction:column;gap:10px}.empty-warning i{color:#10a995;font-size:30px}@media(max-width:1200px){.metric-row{grid-template-columns:repeat(2,1fr)}.dashboard-row .el-col{margin-bottom:16px}}@media(max-width:680px){.dashboard-page{padding:16px}.dashboard-heading{gap:12px}.heading-actions span{display:none}.metric-row{grid-template-columns:1fr}.metric-item{min-height:92px}.chart-canvas{height:260px}}
</style>
