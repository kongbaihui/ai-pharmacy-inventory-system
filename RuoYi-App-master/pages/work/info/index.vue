<template>
  <view class="page">
    <view class="search-bar">
      <uni-search-bar v-model="queryParams.medName" placeholder="药品通用名" radius="100" cancelButton="none" @confirm="handleQuery" @clear="handleQuery" />
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="list.length === 0" class="empty"><text class="empty-text">暂无药品信息</text></view>
      <view v-for="item in list" :key="item.medId" class="card">
        <view class="card-top">
          <text class="card-title">{{ item.medName }}</text>
          <text v-if="item.tradeName" class="trade">{{ item.tradeName }}</text>
        </view>
        <view class="card-sub">{{ item.medSpec || '-' }} · {{ item.dosageForm || '-' }} · {{ categoryName(item.categoryId) }}</view>
        <view class="card-info">
          <view class="info-item"><text class="label">编码</text><text class="value">{{ item.medCode }}</text></view>
          <view class="info-item"><text class="label">厂家</text><text class="value ellipsis">{{ item.manufacturer || '-' }}</text></view>
          <view class="info-item"><text class="label">售价</text><text class="value price">¥{{ item.salePrice }}</text></view>
        </view>
        <view class="card-bottom">
          <text class="stock-warn">库存区间 {{ item.stockMin || 0 }}~{{ item.stockMax || 0 }}</text>
          <view class="card-actions">
            <uni-icons type="compose" size="20" color="#409eff" @click="handleEdit(item)"></uni-icons>
            <uni-icons type="trash" size="20" color="#f56c6c" @click="handleDelete(item)"></uni-icons>
          </view>
        </view>
      </view>
      <view v-if="list.length && finished" class="no-more">没有更多了</view>
    </scroll-view>

    <view class="fab" @click="handleAdd"><uni-icons type="plusempty" size="26" color="#fff"></uni-icons></view>

    <uni-popup ref="formPopup" type="bottom">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">{{ form.medId ? '修改药品' : '新增药品' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item"><text class="form-label">药品编码 <text class="required">*</text></text><input class="form-input" v-model="form.medCode" /></view>
          <view class="form-item"><text class="form-label">通用名 <text class="required">*</text></text><input class="form-input" v-model="form.medName" /></view>
          <view class="form-item"><text class="form-label">商品名</text><input class="form-input" v-model="form.tradeName" /></view>
          <view class="form-item"><text class="form-label">药品分类 <text class="required">*</text></text>
            <picker :range="categoryRange" range-key="label" @change="e => form.categoryId = categoryRange[e.detail.value].value">
              <view class="form-picker">{{ categoryName(form.categoryId) || '请选择分类' }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">默认供应商ID</text><input class="form-input" v-model="form.supplierId" type="number" /></view>
          <view class="form-item"><text class="form-label">规格</text><input class="form-input" v-model="form.medSpec" /></view>
          <view class="form-item"><text class="form-label">剂型</text><input class="form-input" v-model="form.dosageForm" /></view>
          <view class="form-item"><text class="form-label">计量单位</text><input class="form-input" v-model="form.unit" /></view>
          <view class="form-item"><text class="form-label">生产厂家</text><input class="form-input" v-model="form.manufacturer" /></view>
          <view class="form-item"><text class="form-label">批准文号</text><input class="form-input" v-model="form.approvalNo" /></view>
          <view class="form-item"><text class="form-label">存储条件</text><input class="form-input" v-model="form.storageCond" /></view>
          <view class="form-item"><text class="form-label">参考进价</text><input class="form-input" v-model="form.purchasePrice" type="digit" /></view>
          <view class="form-item"><text class="form-label">零售价</text><input class="form-input" v-model="form.salePrice" type="digit" /></view>
          <view class="form-item"><text class="form-label">库存下限</text><input class="form-input" v-model="form.stockMin" type="number" /></view>
          <view class="form-item"><text class="form-label">库存上限</text><input class="form-input" v-model="form.stockMax" type="number" /></view>
          <view class="form-item"><text class="form-label">临期预警天数</text><input class="form-input" v-model="form.warnDays" type="number" /></view>
          <view class="form-item"><text class="form-label">备注</text><textarea class="form-textarea" v-model="form.remark" /></view>
        </scroll-view>
        <view class="form-footer">
          <button class="btn-cancel" @click="closePopup">取消</button>
          <button class="btn-confirm" @click="submitForm">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { listInfo, getInfo, addInfo, updateInfo, delInfo } from '@/api/system/info'
import { listCategory } from '@/api/system/category'

export default {
  data() {
    return {
      queryParams: { medName: '', pageNum: 1, pageSize: 10 },
      list: [], total: 0, finished: false,
      categoryRange: [], categoryMap: {},
      form: this.emptyForm()
    }
  },
  onLoad() { this.loadCategories(); this.getList(true) },
  onPullDownRefresh() { this.getList(true).finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return { medId: null, medCode: '', medName: '', tradeName: '', categoryId: null, supplierId: null, medSpec: '', dosageForm: '', unit: '盒', manufacturer: '', approvalNo: '', storageCond: '', purchasePrice: 0, salePrice: 0, stockMin: 0, stockMax: 0, warnDays: 90, remark: '' }
    },
    async loadCategories() {
      try {
        const res = await listCategory({ pageNum: 1, pageSize: 1000 })
        const rows = res.rows || []
        this.categoryRange = rows.map(r => ({ value: r.categoryId, label: r.categoryName }))
        const map = {}
        rows.forEach(r => { map[r.categoryId] = r.categoryName })
        this.categoryMap = map
      } catch (e) {}
    },
    categoryName(id) { return this.categoryMap[id] || '' },
    async getList(reset = false) {
      if (reset) { this.queryParams.pageNum = 1; this.finished = false }
      try {
        const res = await listInfo(this.queryParams)
        const rows = res.rows || []
        this.list = reset ? rows : [...this.list, ...rows]
        this.total = res.total
        if (this.list.length >= res.total) this.finished = true
      } catch (e) {}
    },
    loadMore() { if (!this.finished) { this.queryParams.pageNum++; this.getList() } },
    handleQuery() { this.getList(true) },
    handleAdd() { this.form = this.emptyForm(); this.$refs.formPopup.open() },
    async handleEdit(item) {
      const res = await getInfo(item.medId)
      this.form = { ...this.emptyForm(), ...res.data }
      this.$refs.formPopup.open()
    },
    closePopup() { this.$refs.formPopup.close() },
    submitForm() {
      if (!this.form.medCode || !this.form.medName || !this.form.categoryId) return uni.showToast({ title: '请填写必填项', icon: 'none' })
      const api = this.form.medId ? updateInfo : addInfo
      api(this.form).then(() => { uni.showToast({ title: '操作成功', icon: 'success' }); this.closePopup(); this.getList(true) })
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示', content: `确认删除「${item.medName}」？`,
        success: r => { if (r.confirm) delInfo(item.medId).then(() => { uni.showToast({ title: '删除成功', icon: 'success' }); this.getList(true) }) }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/mobile-form.scss';
page { background: #f5f7fa; }
.page { min-height: 100vh; padding-bottom: 140rpx; }
.search-bar { background: #fff; padding: 8rpx 0; }
.list-scroll { padding: 20rpx 0; }
.card { background: #fff; margin: 0 20rpx 16rpx; padding: 24rpx; border-radius: 16rpx; }
.card-top { display: flex; align-items: baseline; margin-bottom: 8rpx; }
.card-title { font-size: 30rpx; color: #303133; font-weight: 600; }
.trade { font-size: 22rpx; color: #409eff; margin-left: 12rpx; padding: 2rpx 10rpx; background: rgba(64,158,255,.1); border-radius: 6rpx; }
.card-sub { font-size: 24rpx; color: #909399; }
.card-info { display: flex; flex-wrap: wrap; margin: 16rpx 0 0; background: #f7f8fa; border-radius: 10rpx; padding: 12rpx 16rpx; }
.info-item { display: flex; align-items: center; width: 50%; font-size: 24rpx; padding: 6rpx 0; .label { color: #909399; margin-right: 8rpx; } .value { color: #303133; flex: 1; min-width: 0; } .value.price { color: #f56c6c; font-weight: 600; } .ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } }
.card-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f2f3f5; }
.stock-warn { font-size: 22rpx; color: #909399; }
.card-actions { display: flex; gap: 24rpx; }
.empty { padding: 200rpx 0; text-align: center; } .empty-text { font-size: 26rpx; color: #c0c4cc; }
.no-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #c0c4cc; }
.fab { position: fixed; right: 40rpx; bottom: 80rpx; width: 100rpx; height: 100rpx; background: linear-gradient(135deg, #409eff, #66b1ff); border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(64,158,255,.4); z-index: 99; }
</style>