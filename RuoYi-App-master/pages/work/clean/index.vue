<template>
  <view class="page">
    <view class="search-bar">
      <uni-search-bar v-model="queryParams.batchNo" placeholder="生产批号" radius="100" cancelButton="none" @confirm="handleQuery" @clear="handleQuery" />
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="list.length === 0" class="empty"><text class="empty-text">暂无清理记录</text></view>
      <view v-for="item in list" :key="item.cleanId" class="card">
        <view class="card-top">
          <text class="card-title">{{ getMedName(item.medId) }}</text>
          <view class="tag" :class="statusTag(item.cleanStatus)">{{ statusText(item.cleanStatus) }}</view>
        </view>
        <view class="card-info">
          <view class="info-item"><text class="label">清理单号</text><text class="value">{{ item.cleanNo }}</text></view>
          <view class="info-item"><text class="label">生产批号</text><text class="value">{{ item.batchNo || '-' }}</text></view>
          <view class="info-item"><text class="label">有效期至</text><text class="value">{{ formatDate(item.expireDate) }}</text></view>
          <view class="info-item"><text class="label">清理数量</text><text class="value red">{{ item.cleanQty }}</text></view>
          <view class="info-item"><text class="label">清理方式</text><text class="value">{{ cleanTypeText(item.cleanType) }}</text></view>
          <view class="info-item"><text class="label">申请人</text><text class="value">{{ item.cleanUser || '-' }}</text></view>
        </view>
        <view v-if="item.cleanReason" class="card-reason"><text class="reason-text">{{ item.cleanReason }}</text></view>
        <view class="card-bottom">
          <text class="time">申请 {{ formatDate(item.cleanTime) }}</text>
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
          <text class="form-title">{{ form.cleanId ? '修改清理单' : '新增清理单' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item"><text class="form-label">清理单号 <text class="required">*</text></text><input class="form-input" v-model="form.cleanNo" /></view>
          <view class="form-item"><text class="form-label">药品 <text class="required">*</text></text>
            <picker :range="medRange" range-key="label" @change="e => form.medId = medRange[e.detail.value].value">
              <view class="form-picker">{{ getMedName(form.medId) || '请选择药品' }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">批次ID</text><input class="form-input" v-model="form.batchId" type="number" /></view>
          <view class="form-item"><text class="form-label">生产批号</text><input class="form-input" v-model="form.batchNo" /></view>
          <view class="form-item"><text class="form-label">有效期至</text><picker mode="date" :value="form.expireDate" @change="e => form.expireDate = e.detail.value"><view class="form-picker">{{ form.expireDate || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">清理数量</text><input class="form-input" v-model="form.cleanQty" type="number" /></view>
          <view class="form-item"><text class="form-label">清理方式</text>
            <picker :range="cleanTypeRange" range-key="label" @change="e => form.cleanType = cleanTypeRange[e.detail.value].value">
              <view class="form-picker">{{ cleanTypeText(form.cleanType) }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">清理原因</text><input class="form-input" v-model="form.cleanReason" /></view>
          <view class="form-item"><text class="form-label">清理状态</text>
            <picker :range="cleanStatusRange" range-key="label" @change="e => form.cleanStatus = cleanStatusRange[e.detail.value].value">
              <view class="form-picker">{{ statusText(form.cleanStatus) }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">申请人</text><input class="form-input" v-model="form.cleanUser" /></view>
          <view class="form-item"><text class="form-label">申请时间</text><picker mode="date" :value="form.cleanTime" @change="e => form.cleanTime = e.detail.value"><view class="form-picker">{{ form.cleanTime || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">确认人</text><input class="form-input" v-model="form.auditBy" /></view>
          <view class="form-item"><text class="form-label">确认时间</text><picker mode="date" :value="form.auditTime" @change="e => form.auditTime = e.detail.value"><view class="form-picker">{{ form.auditTime || '请选择' }}</view></picker></view>
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
import { listClean, getClean, addClean, updateClean, delClean } from '@/api/system/clean'
import { listInfo } from '@/api/system/info'

export default {
  data() {
    return {
      queryParams: { batchNo: '', pageNum: 1, pageSize: 10 },
      list: [], total: 0, finished: false,
      medMap: {}, medRange: [],
      cleanTypeRange: [{ value: '0', label: '退货' }, { value: '1', label: '销毁' }, { value: '2', label: '报损' }],
      cleanStatusRange: [{ value: '0', label: '待审核' }, { value: '1', label: '已确认' }, { value: '2', label: '已驳回' }],
      form: this.emptyForm()
    }
  },
  onLoad() { this.loadMedMap().then(() => this.getList(true)) },
  onPullDownRefresh() { this.getList(true).finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return { cleanId: null, cleanNo: '', medId: null, batchId: '', batchNo: '', expireDate: null, cleanQty: 0, cleanType: '1', cleanReason: '', cleanStatus: '0', cleanUser: '', cleanTime: null, auditBy: '', auditTime: null, remark: '' }
    },
    async loadMedMap() {
      try {
        const res = await listInfo({ pageNum: 1, pageSize: 1000 })
        const rows = res.rows || []
        const map = {}
        rows.forEach(m => { map[m.medId] = m.medName })
        this.medMap = map
        this.medRange = rows.map(m => ({ value: m.medId, label: m.medName }))
      } catch (e) {}
    },
    getMedName(id) { return this.medMap[id] || (id ? '药品' + id : '') },
    statusText(s) { return ({ '0': '待审核', '1': '已确认', '2': '已驳回' })[s] || '待审核' },
    statusTag(s) { return ({ '0': 'orange', '1': 'green', '2': 'red' })[s] || 'gray' },
    cleanTypeText(t) { return ({ '0': '退货', '1': '销毁', '2': '报损' })[t] || '销毁' },
    formatDate(t) { if (!t) return '-'; const d = new Date(t); const p = n => n < 10 ? '0' + n : n; return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}` },
    async getList(reset = false) {
      if (reset) { this.queryParams.pageNum = 1; this.finished = false }
      try {
        const res = await listClean(this.queryParams)
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
      const res = await getClean(item.cleanId)
      this.form = { ...this.emptyForm(), ...res.data }
      this.$refs.formPopup.open()
    },
    closePopup() { this.$refs.formPopup.close() },
    submitForm() {
      if (!this.form.cleanNo || !this.form.medId) return uni.showToast({ title: '请填写必填项', icon: 'none' })
      const api = this.form.cleanId ? updateClean : addClean
      api(this.form).then(() => { uni.showToast({ title: '操作成功', icon: 'success' }); this.closePopup(); this.getList(true) })
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示', content: `确认删除清理单「${item.cleanNo}」？`,
        success: r => { if (r.confirm) delClean(item.cleanId).then(() => { uni.showToast({ title: '删除成功', icon: 'success' }); this.getList(true) }) }
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
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
.card-title { font-size: 30rpx; color: #303133; font-weight: 600; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 12rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx;
  &.orange { background: rgba(230,162,60,.12); color: #e6a23c; }
  &.green { background: rgba(103,194,58,.12); color: #67c23a; }
  &.red { background: rgba(245,108,108,.12); color: #f56c6c; }
  &.gray { background: #f2f3f5; color: #909399; }
}
.card-info { background: #f7f8fa; border-radius: 10rpx; padding: 12rpx 16rpx; }
.info-item { display: flex; align-items: center; font-size: 24rpx; padding: 6rpx 0; .label { color: #909399; width: 140rpx; flex-shrink: 0; } .value { color: #303133; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } .value.red { color: #f56c6c; font-weight: 600; } }
.card-reason { margin-top: 16rpx; padding: 12rpx 16rpx; background: rgba(230,162,60,.08); border-radius: 10rpx; .reason-text { font-size: 24rpx; color: #e6a23c; } }
.card-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f2f3f5; }
.time { font-size: 22rpx; color: #909399; }
.card-actions { display: flex; gap: 24rpx; }
.empty { padding: 200rpx 0; text-align: center; } .empty-text { font-size: 26rpx; color: #c0c4cc; }
.no-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #c0c4cc; }
.fab { position: fixed; right: 40rpx; bottom: 80rpx; width: 100rpx; height: 100rpx; background: linear-gradient(135deg, #409eff, #66b1ff); border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(64,158,255,.4); z-index: 99; }
</style>