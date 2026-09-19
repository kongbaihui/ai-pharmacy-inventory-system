<template>
  <view class="home-page">
    <!-- ==================== 1. 医院 Banner ==================== -->
    <view class="banner">
      <view class="banner-top">
        <view class="hospital-info">
          <view class="hospital-logo">医</view>
          <view class="hospital-name">
            <text class="name-main">AI 医院药品管理系统</text>
            <text class="name-sub">药品进销存 · 移动端</text>
          </view>
        </view>
        <view class="notify-icon">
          <text class="iconfont icon-bell">🔔</text>
          <view v-if="totalWarnCount > 0" class="notify-badge">{{ totalWarnCount }}</view>
        </view>
      </view>
    </view>

    <!-- ==================== 2. 核心数据概览 ==================== -->
    <view class="stat-grid">
      <view class="stat-card">
        <view class="stat-icon blue">💊</view>
        <view class="stat-content">
          <text class="stat-value">{{ medTotal }}</text>
          <text class="stat-label">药品总数</text>
        </view>
      </view>
      <view class="stat-card">
        <view class="stat-icon purple">📦</view>
        <view class="stat-content">
          <text class="stat-value">{{ stockTotal }}</text>
          <text class="stat-label">库存总量</text>
        </view>
      </view>
      <view class="stat-card">
        <view class="stat-icon red">⚠️</view>
        <view class="stat-content">
          <text class="stat-value" :class="{ danger: stockWarnCount > 0 }">{{ stockWarnCount }}</text>
          <text class="stat-label">库存预警</text>
        </view>
      </view>
      <view class="stat-card">
        <view class="stat-icon orange">⏰</view>
        <view class="stat-content">
          <text class="stat-value" :class="{ warning: expireWarnCount > 0 }">{{ expireWarnCount }}</text>
          <text class="stat-label">效期预警</text>
        </view>
      </view>
    </view>

    <!-- ==================== 3. 库存概况 ==================== -->
    <view class="section-card">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📊</text>
          <text>库存概况</text>
        </view>
        <text class="section-more" @click="goStock">查看全部 ›</text>
      </view>

      <view v-if="stockOverview.length === 0" class="empty-tip">
        <text>暂无库存数据</text>
      </view>

      <view v-else class="stock-list">
        <view
          v-for="item in stockOverview"
          :key="item.medId"
          class="stock-item"
        >
          <view class="stock-info">
            <text class="stock-name">{{ item.medName }}</text>
            <text class="stock-detail">{{ item.totalQty }} / 限 {{ item.stockMin }}~{{ item.stockMax }}</text>
          </view>
          <view class="stock-bar-wrapper">
            <view
              class="stock-bar"
              :class="item.warnType"
              :style="{ width: item.barPercent + '%' }"
            ></view>
          </view>
          <text class="stock-status" :class="item.warnType">{{ item.warnText }}</text>
        </view>
      </view>
    </view>

    <!-- ==================== 4. 效期概况 ==================== -->
    <view class="section-card">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">⏳</text>
          <text>效期概况</text>
        </view>
        <text class="section-more" @click="goClean">查看全部 ›</text>
      </view>

      <view class="expire-distribution">
        <view
          v-for="seg in expireSegments"
          :key="seg.key"
          class="dist-segment"
          :style="{ width: seg.percent + '%', background: seg.color }"
        ></view>
      </view>
      <view class="expire-legend">
        <view v-for="seg in expireSegments" :key="seg.key" class="legend-item">
          <view class="legend-dot" :style="{ background: seg.color }"></view>
          <text class="legend-text">{{ seg.label }} {{ seg.value }}</text>
        </view>
      </view>

      <view v-if="expireList.length === 0" class="empty-tip">
        <text>暂无临期或过期药品</text>
      </view>
      <view v-else class="expire-list">
        <view
          v-for="item in expireList"
          :key="item.cleanId || item.batchId"
          class="expire-item"
        >
          <view class="expire-dot" :class="item.level"></view>
          <view class="expire-info">
            <text class="expire-name">{{ getMedName(item.medId) }}</text>
            <text class="expire-batch">批号 {{ item.batchNo || '-' }}</text>
          </view>
          <view class="expire-right">
            <text class="expire-qty">{{ item.cleanQty || item.remainQty || 0 }} 件</text>
            <text class="expire-status" :class="item.level">{{ item.statusText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ==================== 5. 出入库流水概况 ==================== -->
    <view class="section-card">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📈</text>
          <text>近期流水</text>
        </view>
        <text class="section-more" @click="goFlow">查看全部 ›</text>
      </view>

      <view class="trend-chart">
        <view
          v-for="(day, idx) in flowTrend"
          :key="idx"
          class="trend-col"
        >
          <view class="trend-bars">
            <view
              class="trend-bar in"
              :style="{ height: day.inHeight + 'rpx' }"
            ></view>
            <view
              class="trend-bar out"
              :style="{ height: day.outHeight + 'rpx' }"
            ></view>
          </view>
          <text class="trend-label">{{ day.label }}</text>
        </view>
      </view>
      <view class="trend-legend">
        <view class="legend-item">
          <view class="legend-dot" style="background:#67C23A"></view>
          <text class="legend-text">入库</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot" style="background:#F56C6C"></view>
          <text class="legend-text">出库</text>
        </view>
      </view>

      <view v-if="recentFlows.length === 0" class="empty-tip">
        <text>暂无流水记录</text>
      </view>
      <view v-else class="flow-list">
        <view
          v-for="item in recentFlows"
          :key="item.flowId"
          class="flow-item"
        >
          <view class="flow-icon" :class="getFlowClass(item.flowType)">
            {{ getFlowIcon(item.flowType) }}
          </view>
          <view class="flow-info">
            <text class="flow-name">{{ getMedName(item.medId) }}</text>
            <text class="flow-time">{{ formatTime(item.flowTime) }}</text>
          </view>
          <text class="flow-qty" :class="item.changeQty > 0 ? 'in' : 'out'">
            {{ item.changeQty > 0 ? '+' : '' }}{{ item.changeQty }}
          </text>
        </view>
      </view>
    </view>

    <!-- ==================== 6. AI 快捷咨询入口 ==================== -->
    <view class="ai-entry" @click="goAiChat">
      <view class="ai-icon">🤖</view>
      <view class="ai-content">
        <text class="ai-title">有问题？问 AI</text>
        <text class="ai-sub">库存、效期、报表，随口问一句</text>
      </view>
      <view class="ai-arrow">›</view>
    </view>

    <view class="safe-bottom"></view>
  </view>
</template>

<script>
import { listInfo } from '@/api/system/info'
import { listStock } from '@/api/system/stock'
import { listFlow } from '@/api/system/flow'
import { listClean } from '@/api/system/clean'

export default {
  name: 'HomePage',
  data() {
    return {
      userName: '张药师',
      medList: [],
      medMap: {},
      stockList: [],
      flowList: [],
      cleanList: [],
      loading: false
    }
  },
  computed: {
    medTotal() {
      return this.medList.length
    },
    stockTotal() {
      return this.stockList.reduce((sum, s) => sum + (Number(s.totalQty) || 0), 0)
    },
    stockWarnCount() {
      let count = 0
      this.stockList.forEach(s => {
        const info = this.medMap[s.medId]
        if (!info) return
        const qty = Number(s.totalQty) || 0
        const min = Number(info.stockMin) || 0
        const max = Number(info.stockMax) || 0
        if ((min > 0 && qty < min) || (max > 0 && qty > max)) count++
      })
      return count
    },
    expireWarnCount() {
      return this.cleanList.filter(c =>
        c.cleanStatus === '0' || c.cleanStatus === '1'
      ).length
    },
    totalWarnCount() {
      return this.stockWarnCount + this.expireWarnCount
    },
    stockOverview() {
      const list = []
      this.stockList.forEach(s => {
        const info = this.medMap[s.medId]
        if (!info) return
        const qty = Number(s.totalQty) || 0
        const min = Number(info.stockMin) || 0
        const max = Number(info.stockMax) || 0
        const rangeMax = max > 0 ? max : (min > 0 ? min * 2 : qty || 1)

        let warnType = 'normal'
        let warnText = '正常'
        if (min > 0 && qty < min) {
          warnType = 'low'
          warnText = '不足'
        } else if (max > 0 && qty > max) {
          warnType = 'high'
          warnText = '积压'
        }

        let barPercent = Math.min(100, Math.round((qty / rangeMax) * 100))
        if (barPercent < 4) barPercent = 4

        if (warnType !== 'normal') {
          list.push({
            medId: s.medId,
            medName: info.medName,
            totalQty: qty,
            stockMin: min,
            stockMax: max,
            warnType,
            warnText,
            barPercent
          })
        }
      })
      list.sort((a, b) => (a.warnType === 'low' ? -1 : 1))
      return list.slice(0, 5)
    },
    expireSegments() {
      const counts = { normal: 0, nearExpire: 0, expired: 0 }
      this.cleanList.forEach(c => {
        if (c.cleanStatus === '1') counts.expired++
        else if (c.cleanStatus === '0') counts.nearExpire++
        else counts.normal++
      })
      const total = counts.normal + counts.nearExpire + counts.expired || 1
      return [
        { key: 'normal', label: '正常', value: counts.normal, color: '#67C23A', percent: (counts.normal / total) * 100 },
        { key: 'nearExpire', label: '临期', value: counts.nearExpire, color: '#E6A23C', percent: (counts.nearExpire / total) * 100 },
        { key: 'expired', label: '过期', value: counts.expired, color: '#F56C6C', percent: (counts.expired / total) * 100 }
      ]
    },
    expireList() {
      return this.cleanList
        .filter(c => c.cleanStatus === '0' || c.cleanStatus === '1')
        .slice(0, 5)
        .map(c => {
          const isExpired = c.cleanStatus === '1'
          return {
            cleanId: c.cleanId,
            medId: c.medId,
            batchNo: c.batchNo,
            cleanQty: c.cleanQty,
            level: isExpired ? 'expired' : 'nearExpire',
            statusText: isExpired ? '已过期' : '待处理'
          }
        })
    },
    flowTrend() {
      const days = []
      const today = new Date()
      today.setHours(0, 0, 0, 0)

      for (let i = 6; i >= 0; i--) {
        const d = new Date(today.getTime() - i * 86400000)
        days.push({
          date: d,
          label: `${d.getMonth() + 1}/${d.getDate()}`,
          in: 0,
          out: 0
        })
      }

      this.flowList.forEach(f => {
        if (!f.flowTime) return
        const t = new Date(f.flowTime)
        t.setHours(0, 0, 0, 0)
        const idx = days.findIndex(d => d.date.getTime() === t.getTime())
        if (idx === -1) return
        const qty = Number(f.changeQty) || 0
        if (qty > 0) days[idx].in += qty
        else days[idx].out += Math.abs(qty)
      })

      const maxVal = Math.max(1, ...days.map(d => Math.max(d.in, d.out)))

      return days.map(d => ({
        ...d,
        inHeight: Math.max(4, Math.round((d.in / maxVal) * 100)),
        outHeight: Math.max(4, Math.round((d.out / maxVal) * 100))
      }))
    },
    recentFlows() {
      return this.flowList.slice(0, 5)
    }
  },
  onLoad() {
    this.loadAllData()
  },
  onPullDownRefresh() {
    this.loadAllData().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadAllData() {
      this.loading = true
      try {
        const [medRes, stockRes, flowRes, cleanRes] = await Promise.all([
          listInfo({ pageNum: 1, pageSize: 1000 }).catch(() => ({ rows: [] })),
          listStock({ pageNum: 1, pageSize: 1000 }).catch(() => ({ rows: [] })),
          listFlow({ pageNum: 1, pageSize: 200 }).catch(() => ({ rows: [] })),
          listClean({ pageNum: 1, pageSize: 100 }).catch(() => ({ rows: [] }))
        ])

        this.medList = medRes.rows || []
        this.stockList = stockRes.rows || []
        this.flowList = flowRes.rows || []
        this.cleanList = cleanRes.rows || []

        const map = {}
        this.medList.forEach(m => {
          map[m.medId] = {
            medName: m.medName,
            stockMin: m.stockMin,
            stockMax: m.stockMax
          }
        })
        this.medMap = map
      } catch (e) {
        console.error('加载首页数据失败', e)
      } finally {
        this.loading = false
      }
    },
    getMedName(medId) {
      if (medId == null || medId === '') return '-'
      const info = this.medMap[medId]
      return info ? info.medName : ('药品' + medId)
    },
    getFlowIcon(type) {
      const map = { '1': '↑', '2': '↓', '3': '↩', '4': '⟳', '5': '×' }
      return map[type] || '·'
    },
    getFlowClass(type) {
      const map = {
        '1': 'flow-in',
        '2': 'flow-out',
        '3': 'flow-return',
        '4': 'flow-check',
        '5': 'flow-clean'
      }
      return map[type] || 'flow-default'
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      const pad = n => (n < 10 ? '0' + n : n)
      const now = new Date()
      const isToday =
        d.getFullYear() === now.getFullYear() &&
        d.getMonth() === now.getMonth() &&
        d.getDate() === now.getDate()
      if (isToday) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
      return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    goStock() {
      uni.navigateTo({ url: '/pages/work/stock/index' })
    },
    goClean() {
      uni.navigateTo({ url: '/pages/work/clean/index' })
    },
    goFlow() {
      uni.navigateTo({ url: '/pages/work/flow/index' })
    },
    goAiChat() {
      uni.navigateTo({ url: '/pages/work/ai/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

/* ==================== Banner（收紧高度） ==================== */
.banner {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 60rpx 32rpx 40rpx;
  color: #fff;
  border-radius: 0 0 32rpx 32rpx;
}

.banner-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hospital-info {
  display: flex;
  align-items: center;
}

.hospital-logo {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 600;
  margin-right: 20rpx;
}

.hospital-name {
  display: flex;
  flex-direction: column;
}

.name-main {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.name-sub {
  font-size: 22rpx;
  opacity: 0.85;
  margin-top: 6rpx;
}

.notify-icon {
  position: relative;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;

  .notify-badge {
    position: absolute;
    top: -4rpx;
    right: -4rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background: #f56c6c;
    border-radius: 16rpx;
    font-size: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 3rpx solid #409eff;
  }
}

/* ==================== 数据概览 ==================== */
.stat-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 0 24rpx;
  margin-top: 24rpx;
}

.stat-card {
  width: calc(50% - 12rpx);
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 24rpx;

  &:nth-child(odd) {
    margin-right: 24rpx;
  }
}

.stat-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.blue { background: rgba(64, 158, 255, 0.12); }
  &.purple { background: rgba(155, 89, 182, 0.12); }
  &.red { background: rgba(245, 108, 108, 0.12); }
  &.orange { background: rgba(230, 162, 60, 0.12); }
}

.stat-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 44rpx;
  font-weight: 600;
  color: #303133;
  line-height: 1.1;

  &.danger { color: #f56c6c; }
  &.warning { color: #e6a23c; }
}

.stat-label {
  font-size: 24rpx;
  color: #909399;
  margin-top: 8rpx;
}

/* ==================== 通用区块卡片 ==================== */
.section-card {
  background: #fff;
  border-radius: 20rpx;
  margin: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;

  .title-icon {
    font-size: 32rpx;
    margin-right: 12rpx;
  }
}

.section-more {
  font-size: 24rpx;
  color: #409eff;
}

.empty-tip {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #c0c4cc;
}

/* ==================== 库存概况 ==================== */
.stock-list {
  display: flex;
  flex-direction: column;
}

.stock-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f3f5;

  &:last-child { border-bottom: none; }
}

.stock-info {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.stock-name {
  font-size: 28rpx;
  color: #303133;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.stock-detail {
  font-size: 22rpx;
  color: #909399;
  flex-shrink: 0;
}

.stock-bar-wrapper {
  height: 16rpx;
  background: #f2f3f5;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 10rpx;
}

.stock-bar {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.3s;

  &.low { background: linear-gradient(90deg, #f56c6c, #f78989); }
  &.high { background: linear-gradient(90deg, #e6a23c, #ebb563); }
  &.normal { background: linear-gradient(90deg, #67c23a, #85ce61); }
}

.stock-status {
  font-size: 22rpx;
  font-weight: 500;

  &.low { color: #f56c6c; }
  &.high { color: #e6a23c; }
  &.normal { color: #67c23a; }
}

/* ==================== 效期概况 ==================== */
.expire-distribution {
  display: flex;
  height: 24rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: #f2f3f5;
  margin-bottom: 20rpx;
}

.dist-segment {
  height: 100%;
  transition: width 0.3s;
}

.expire-legend {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f2f3f5;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #606266;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.expire-list {
  display: flex;
  flex-direction: column;
}

.expire-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f3f5;

  &:last-child { border-bottom: none; }
}

.expire-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.nearExpire { background: #e6a23c; }
  &.expired { background: #f56c6c; }
}

.expire-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.expire-name {
  font-size: 28rpx;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.expire-batch {
  font-size: 22rpx;
  color: #909399;
  margin-top: 6rpx;
}

.expire-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.expire-qty {
  font-size: 26rpx;
  color: #303133;
  font-weight: 500;
}

.expire-status {
  font-size: 22rpx;
  margin-top: 6rpx;

  &.nearExpire { color: #e6a23c; }
  &.expired { color: #f56c6c; }
}

/* ==================== 流水概况 ==================== */
.trend-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200rpx;
  padding: 20rpx 0;
  margin-bottom: 12rpx;
  border-bottom: 1rpx solid #f2f3f5;
}

.trend-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.trend-bars {
  display: flex;
  align-items: flex-end;
  gap: 4rpx;
  height: 140rpx;
}

.trend-bar {
  width: 16rpx;
  border-radius: 4rpx 4rpx 0 0;

  &.in { background: #67c23a; }
  &.out { background: #f56c6c; }
}

.trend-label {
  font-size: 20rpx;
  color: #909399;
  margin-top: 10rpx;
}

.trend-legend {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  padding: 16rpx 0 24rpx;
  border-bottom: 1rpx solid #f2f3f5;
}

.flow-list {
  display: flex;
  flex-direction: column;
}

.flow-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f3f5;

  &:last-child { border-bottom: none; }
}

.flow-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
  color: #fff;
  font-weight: 600;

  &.flow-in { background: #67c23a; }
  &.flow-out { background: #f56c6c; }
  &.flow-return { background: #e6a23c; }
  &.flow-check { background: #409eff; }
  &.flow-clean { background: #909399; }
  &.flow-default { background: #c0c4cc; }
}

.flow-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.flow-name {
  font-size: 28rpx;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-time {
  font-size: 22rpx;
  color: #909399;
  margin-top: 6rpx;
}

.flow-qty {
  font-size: 30rpx;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 16rpx;

  &.in { color: #67c23a; }
  &.out { color: #f56c6c; }
}

/* ==================== AI 入口 ==================== */
.ai-entry {
  display: flex;
  align-items: center;
  margin: 24rpx;
  padding: 32rpx 28rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.ai-icon {
  width: 88rpx;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.ai-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.ai-title {
  font-size: 32rpx;
  font-weight: 600;
}

.ai-sub {
  font-size: 24rpx;
  opacity: 0.85;
  margin-top: 8rpx;
}

.ai-arrow {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.7);
  flex-shrink: 0;
  line-height: 1;
}

/* ==================== 安全区 ==================== */
.safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>