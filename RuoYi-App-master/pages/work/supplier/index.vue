<template>
  <view class="page">
    <view class="search-bar">
      <uni-search-bar v-model="queryParams.supplierName" placeholder="供应商名称" radius="100" cancelButton="none" @confirm="handleQuery" @clear="handleQuery" />
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="list.length === 0" class="empty"><text class="empty-text">暂无供应商</text></view>
      <view v-for="item in list" :key="item.supplierId" class="card">
        <view class="card-top">
          <text class="card-title">{{ item.supplierName }}</text>
          <view class="tag" :class="gspTag(item.gspStatus)">{{ gspText(item.gspStatus) }}</view>
        </view>
        <view class="card-sub"><uni-icons type="contact-filled" size="14" color="#909399"></uni-icons><text>{{ item.contactPerson || '-' }} · {{ item.contactPhone || '-' }}</text></view>
        <view class="card-sub"><uni-icons type="location-filled" size="14" color="#909399"></uni-icons><text class="ellipsis">{{ item.address || '-' }}</text></view>
        <view class="card-bottom">
          <text class="credit">信用 {{ item.creditLevel || '-' }}</text>
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
          <text class="form-title">{{ form.supplierId ? '修改供应商' : '新增供应商' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item"><text class="form-label">供应商编码 <text class="required">*</text></text><input class="form-input" v-model="form.supplierCode" /></view>
          <view class="form-item"><text class="form-label">供应商名称 <text class="required">*</text></text><input class="form-input" v-model="form.supplierName" /></view>
          <view class="form-item"><text class="form-label">联系人</text><input class="form-input" v-model="form.contactPerson" /></view>
          <view class="form-item"><text class="form-label">联系电话</text><input class="form-input" v-model="form.contactPhone" /></view>
          <view class="form-item"><text class="form-label">供应商地址</text><input class="form-input" v-model="form.address" /></view>
          <view class="form-item"><text class="form-label">经营许可证号</text><input class="form-input" v-model="form.licenseNo" /></view>
          <view class="form-item"><text class="form-label">许可证有效期至</text><picker mode="date" :value="form.licenseDate" @change="e => form.licenseDate = e.detail.value"><view class="form-picker">{{ form.licenseDate || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">信用等级</text>
            <picker :range="creditRange" @change="e => form.creditLevel = creditRange[e.detail.value]">
              <view class="form-picker">{{ form.creditLevel || '请选择' }}</view>
            </picker>
          </view>
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
import { listSupplier, getSupplier, addSupplier, updateSupplier, delSupplier } from '@/api/system/supplier'

export default {
  data() {
    return {
      queryParams: { supplierName: '', pageNum: 1, pageSize: 10 },
      list: [], total: 0, finished: false,
      creditRange: ['A', 'B', 'C', 'D'],
      form: this.emptyForm()
    }
  },
  onLoad() { this.getList(true) },
  onPullDownRefresh() { this.getList(true).finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return { supplierId: null, supplierCode: '', supplierName: '', contactPerson: '', contactPhone: '', address: '', licenseNo: '', licenseDate: null, creditLevel: 'B', remark: '' }
    },
    async getList(reset = false) {
      if (reset) { this.queryParams.pageNum = 1; this.finished = false }
      try {
        const res = await listSupplier(this.queryParams)
        const rows = res.rows || []
        this.list = reset ? rows : [...this.list, ...rows]
        this.total = res.total
        if (this.list.length >= res.total) this.finished = true
      } catch (e) {}
    },
    loadMore() {
      if (this.finished) return
      this.queryParams.pageNum++
      this.getList()
    },
    gspText(s) { return ({ '0': '资质过期', '1': '资质有效', '2': '临近到期' })[s] || '未知' },
    gspTag(s) { return ({ '0': 'tag-red', '1': 'tag-green', '2': 'tag-orange' })[s] || 'tag-gray' },
    handleQuery() { this.getList(true) },
    handleAdd() { this.form = this.emptyForm(); this.$refs.formPopup.open() },
    async handleEdit(item) {
      const res = await getSupplier(item.supplierId)
      this.form = { ...this.emptyForm(), ...res.data }
      this.$refs.formPopup.open()
    },
    closePopup() { this.$refs.formPopup.close() },
    submitForm() {
      if (!this.form.supplierCode || !this.form.supplierName) return uni.showToast({ title: '请填写必填项', icon: 'none' })
      const api = this.form.supplierId ? updateSupplier : addSupplier
      api(this.form).then(() => {
        uni.showToast({ title: '操作成功', icon: 'success' })
        this.closePopup()
        this.getList(true)
      })
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示', content: `确认删除「${item.supplierName}」？`,
        success: r => { if (r.confirm) delSupplier(item.supplierId).then(() => { uni.showToast({ title: '删除成功', icon: 'success' }); this.getList(true) }) }
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
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.card-title { font-size: 30rpx; color: #303133; font-weight: 600; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 12rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; flex-shrink: 0;
  &.tag-red { background: rgba(245,108,108,.12); color: #f56c6c; }
  &.tag-green { background: rgba(103,194,58,.12); color: #67c23a; }
  &.tag-orange { background: rgba(230,162,60,.12); color: #e6a23c; }
  &.tag-gray { background: #f2f3f5; color: #909399; }
}
.card-sub { display: flex; align-items: center; font-size: 24rpx; color: #606266; margin-top: 8rpx; uni-icons { margin-right: 8rpx; } .ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; } }
.card-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f2f3f5; }
.credit { font-size: 24rpx; color: #909399; }
.card-actions { display: flex; gap: 24rpx; }
.empty { padding: 200rpx 0; text-align: center; } .empty-text { font-size: 26rpx; color: #c0c4cc; }
.no-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #c0c4cc; }
.fab { position: fixed; right: 40rpx; bottom: 80rpx; width: 100rpx; height: 100rpx; background: linear-gradient(135deg, #409eff, #66b1ff); border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(64,158,255,.4); z-index: 99; }
</style>