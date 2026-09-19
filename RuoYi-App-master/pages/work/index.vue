<template>
  <view class="work-container">
    <!-- ============ 基础资料 ============ -->
    <uni-section title="基础资料" type="line" sub-title="低频配置 · 增删改查">
      <view class="grid-body">
        <uni-grid :column="4" :showBorder="false" :square="false" @change="onGridChange">
          <uni-grid-item :index="0">
            <view class="grid-item-box">
              <view class="grid-icon blue">
                <uni-icons type="bars" size="26" color="#409eff"></uni-icons>
              </view>
              <text class="text">药品类别</text>
            </view>
          </uni-grid-item>
          <uni-grid-item :index="1">
            <view class="grid-item-box">
              <view class="grid-icon green">
                <uni-icons type="staff-filled" size="26" color="#67c23a"></uni-icons>
              </view>
              <text class="text">供应商信息</text>
            </view>
          </uni-grid-item>
          <uni-grid-item :index="2">
            <view class="grid-item-box">
              <view class="grid-icon orange">
                <uni-icons type="heart-filled" size="26" color="#e6a23c"></uni-icons>
              </view>
              <text class="text">药品信息</text>
            </view>
          </uni-grid-item>
        </uni-grid>
      </view>
    </uni-section>

    <!-- ============ 库存业务 ============ -->
    <uni-section title="库存业务" type="line" sub-title="日常高频操作">
      <view class="grid-body">
        <uni-grid :column="4" :showBorder="false" :square="false" @change="onStockGridChange">
          <uni-grid-item :index="0">
            <view class="grid-item-box">
              <view class="grid-icon purple">
                <uni-icons type="wallet-filled" size="26" color="#9b59b6"></uni-icons>
              </view>
              <text class="text">药品库存</text>
            </view>
          </uni-grid-item>
          <uni-grid-item :index="1">
            <view class="grid-item-box">
              <view class="grid-icon cyan">
                <uni-icons type="settings-filled" size="26" color="#17a2b8"></uni-icons>
              </view>
              <text class="text">库存盘点</text>
            </view>
          </uni-grid-item>
          <uni-grid-item :index="2">
            <view class="grid-item-box">
              <view class="grid-icon blue">
                <uni-icons type="list" size="26" color="#409eff"></uni-icons>
              </view>
              <text class="text">库存流水</text>
            </view>
          </uni-grid-item>
          <uni-grid-item :index="3">
            <view class="grid-item-box">
              <view class="grid-icon red">
                <uni-icons type="gear-filled" size="26" color="#f56c6c"></uni-icons>
              </view>
              <text class="text">过期药品</text>
            </view>
          </uni-grid-item>
        </uni-grid>
      </view>
    </uni-section>

    <!-- ============ 智能助手 ============ -->
    <uni-section title="智能助手" type="line" sub-title="库存、效期、报表，随口问一句">
      <view class="ai-entry" @click="goPage('/pages/work/ai/index')">
        <view class="ai-icon">
          <uni-icons type="chat-filled" size="28" color="#fff"></uni-icons>
        </view>
        <view class="ai-content">
          <text class="ai-title">AI 智能问答</text>
          <text class="ai-sub">库存、效期、报表，随时可问</text>
        </view>
        <uni-icons type="right" size="18" color="rgba(255,255,255,0.85)"></uni-icons>
      </view>
    </uni-section>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    /** 基础资料宫格 */
    onGridChange(e) {
      const routes = [
        '/pages/work/category/index', // 0 药品类别
        '/pages/work/supplier/index', // 1 供应商信息
        '/pages/work/info/index'      // 2 药品信息
      ]
      this.goPage(routes[e.detail.index])
    },

    /** 库存业务宫格 */
    onStockGridChange(e) {
      const routes = [
        '/pages/work/stock/index',    // 0 药品库存
        '/pages/work/check/index',    // 1 库存盘点
        '/pages/work/flow/index',     // 2 库存流水（新增）
        '/pages/work/clean/index'     // 3 过期药品
      ]
      this.goPage(routes[e.detail.index])
    },

    goPage(url) {
      if (!url) return
      uni.navigateTo({
        url,
        fail: (err) => {
          console.error('跳转失败', url, err)
          uni.showToast({ title: '页面未注册：' + url, icon: 'none', duration: 3000 })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
page {
  background-color: #f5f7fa;
  min-height: 100%;
}

.work-container {
  padding-bottom: 40rpx;
}

.grid-body {
  background: #fff;
  margin: 0 20rpx;
  border-radius: 16rpx;
  padding: 10rpx 0;
}

.grid-item-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
}

.grid-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;

  &.blue { background: rgba(64, 158, 255, 0.12); }
  &.green { background: rgba(103, 194, 58, 0.12); }
  &.orange { background: rgba(230, 162, 60, 0.12); }
  &.purple { background: rgba(155, 89, 182, 0.12); }
  &.cyan { background: rgba(23, 162, 184, 0.12); }
  &.red { background: rgba(245, 108, 108, 0.12); }
}

.text {
  text-align: center;
  font-size: 24rpx;
  color: #303133;
  margin-top: 6rpx;
}

/* AI 入口卡片 */
.ai-entry {
  display: flex;
  align-items: center;
  margin: 0 20rpx;
  padding: 32rpx 28rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.ai-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.ai-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.ai-title {
  font-size: 30rpx;
  font-weight: 600;
}

.ai-sub {
  font-size: 22rpx;
  opacity: 0.85;
  margin-top: 6rpx;
}
</style>