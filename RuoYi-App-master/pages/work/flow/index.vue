<template>
  <view class="page">
    <!-- 搜索栏：药品名称 + 业务类型 -->
    <view class="search-bar">
      <uni-search-bar
        v-model="queryParams.medId"
        placeholder="药品ID筛选"
        radius="100"
        cancelButton="none"
        @confirm="handleQuery"
        @clear="handleQuery"
      />
    </view>

    <!-- 业务类型快捷筛选 -->
    <scroll-view scroll-x class="type-tabs">
      <view
        v-for="tab in typeTabs"
        :key="tab.value"
        class="type-tab"
        :class="{ active: queryParams.flowType === tab.value }"
        @click="handleTypeChange(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </scroll-view>

    <!-- 流水列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="list.length === 0" class="empty">
        <uni-icons type="list" size="50" color="#c0c4cc"></uni-icons>
        <text class="empty-text">暂无流水记录</text>
      </view>

      <view v-for="item in list" :key="item.flowId" class="card">
        <view class="card-top">
          <view class="flow-icon" :class="flowClass(item.flowType)">
            {{ flowIcon(item.flowType) }}
          </view>
          <view class="card-main">
            <text class="card-title">{{ getMedName(item.medId) }}</text>
            <text class="card-sub">
              {{ flowTypeText(item.flowType) }} · 操作人 {{ item.operator || '-' }}
            </text>
          </view>
          <text class="change-qty" :class="item.changeQty > 0 ? 'in' : 'out'">
            {{ item.changeQty > 0 ? '+' : '' }}{{ item.changeQty }}
          </text>
        </view>

        <view class="card-info">
          <view class="info-item">
            <text class="label">变动前</text>
            <text class="value">{{ item.beforeQty }}</text>
          </view>
          <view class="info-item">
            <text class="label">变动后</text>
            <text class="value">{{ item.afterQty }}</text>
          </view>
          <view class="info-item">
            <text class="label">业务单号</text>
            <text class="value ellipsis">{{ item.bizNo || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="label">批次ID</text>
            <text class="value">{{ item.batchId || '-' }}</text>
          </view>
        </view>

        <view class="card-bottom">
          <text class="time">{{ formatTime(item.flowTime) }}</text>
          <text v-if="item.remark" class="remark ellipsis">{{ item.remark }}</text>
        </view>
      </view>

      <view v-if="list.length && finished" class="no-more">— 没有更多了 —</view>
    </scroll-view>
  </view>
</template>

<script>
import { listFlow } from '@/api/system/flow'
import { listInfo } from '@/api/system/info'

export default {
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medId: null,
        flowType: null,
        operator: null,
        flowTime: null
      },
      list: [],
      total: 0,
      finished: false,
      medMap: {},
      typeTabs: [
        { value: null, label: '全部' },
        { value: '1', label: '入库' },
        { value: '2', label: '出库' },
        { value: '3', label: '退库' },
        { value: '4', label: '盘点调整' },
        { value: '5', label: '过期清理' }
      ]
    }
  },
  onLoad() {
    this.loadMedMap().then(() => this.getList(true))
  },
  onPullDownRefresh() {
    this.getList(true).finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    /* ---------------- 数据加载 ---------------- */

    /** 拉药品映射，把 medId 换成药品名 */
    async loadMedMap() {
      try {
        const res = await listInfo({ pageNum: 1, pageSize: 1000 })
        const map = {}
        ;(res.rows || []).forEach(m => {
          map[m.medId] = m.medName
        })
        this.medMap = map
      } catch (e) {
        this.medMap = {}
      }
    },

    async getList(reset = false) {
      if (reset) {
        this.queryParams.pageNum = 1
        this.finished = false
      }
      try {
        const res = await listFlow(this.queryParams)
        const rows = res.rows || []
        this.list = reset ? rows : [...this.list, ...rows]
        this.total = res.total
        if (this.list.length >= res.total) {
          this.finished = true
        }
      } catch (e) {
        if (reset) this.list = []
      }
    },

    loadMore() {
      if (this.finished) return
      this.queryParams.pageNum++
      this.getList()
    },

    /* ---------------- 筛选 ---------------- */

    handleQuery() {
      this.getList(true)
    },

    handleTypeChange(type) {
      if (this.queryParams.flowType === type) return
      this.queryParams.flowType = type
      this.getList(true)
    },

    /* ---------------- 展示辅助 ---------------- */

    getMedName(id) {
      if (id == null) return '-'
      return this.medMap[id] || ('药品' + id)
    },

    flowIcon(type) {
      return { '1': '↑', '2': '↓', '3': '↩', '4': '⟳', '5': '×' }[String(type)] || '·'
    },

    flowClass(type) {
      return {
        '1': 'flow-in',
        '2': 'flow-out',
        '3': 'flow-return',
        '4': 'flow-check',
        '5': 'flow-clean'
      }[String(type)] || 'flow-default'
    },

    flowTypeText(type) {
      return {
        '1': '入库',
        '2': '出库',
        '3': '退库',
        '4': '盘点调整',
        '5': '过期清理'
      }[String(type)] || '其他'
    },

    formatTime(t) {
      if (!t) return '-'
      const d = new Date(t)
      const pad = n => (n < 10 ? '0' + n : n)
      const now = new Date()
      const isToday =
        d.getFullYear() === now.getFullYear() &&
        d.getMonth() === now.getMonth() &&
        d.getDate() === now.getDate()
      if (isToday) return `今天 ${pad(d.getHours())}:${pad(d.getMinutes())}`
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
page {
  background: #f5f7fa;
}

.page {
  min-height: 100vh;
  padding-bottom: 40rpx;
  display: flex;
  flex-direction: column;
}

.search-bar {
  background: #fff;
  padding: 8rpx 0;
  flex-shrink: 0;
}

/* 顶部横向滑动的业务类型筛选 */
.type-tabs {
  white-space: nowrap;
  background: #fff;
  padding: 12rpx 0 20rpx;
  flex-shrink: 0;

  .type-tab {
    display: inline-block;
    padding: 10rpx 28rpx;
    margin-left: 16rpx;
    font-size: 26rpx;
    color: #606266;
    background: #f5f7fa;
    border-radius: 32rpx;

    &:first-child {
      margin-left: 24rpx;
    }

    &.active {
      background: #409eff;
      color: #fff;
      font-weight: 500;
    }
  }
}

.list-scroll {
  flex: 1;
  padding: 20rpx 0;
}

/* ============ 卡片 ============ */
.card {
  background: #fff;
  margin: 0 20rpx 16rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.card-top {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.flow-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
  margin-right: 20rpx;

  &.flow-in { background: #67c23a; }
  &.flow-out { background: #f56c6c; }
  &.flow-return { background: #e6a23c; }
  &.flow-check { background: #409eff; }
  &.flow-clean { background: #909399; }
  &.flow-default { background: #c0c4cc; }
}

.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-sub {
  font-size: 24rpx;
  color: #909399;
  margin-top: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.change-qty {
  font-size: 36rpx;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 16rpx;

  &.in { color: #67c23a; }
  &.out { color: #f56c6c; }
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 12rpx 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  width: 50%;
  font-size: 24rpx;
  padding: 6rpx 0;

  .label {
    color: #909399;
    margin-right: 8rpx;
    flex-shrink: 0;
  }

  .value {
    color: #303133;
    flex: 1;
    min-width: 0;
  }

  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f2f3f5;

  .time {
    font-size: 22rpx;
    color: #909399;
    flex-shrink: 0;
  }

  .remark {
    font-size: 22rpx;
    color: #c0c4cc;
    margin-left: 20rpx;
    flex: 1;
    min-width: 0;
    text-align: right;
  }
}

/* ============ 空态 / 到底提示 ============ */
.empty {
  padding: 200rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;

  .empty-text {
    font-size: 26rpx;
    color: #c0c4cc;
    margin-top: 20rpx;
  }
}

.no-more {
  text-align: center;
  padding: 24rpx;
  font-size: 22rpx;
  color: #c0c4cc;
}
</style>