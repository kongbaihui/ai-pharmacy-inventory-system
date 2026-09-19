<template>
  <view class="page">
    <view class="search-bar">
      <uni-search-bar v-model="queryParams.medId" placeholder="药品ID筛选" radius="100" cancelButton="none" @confirm="handleQuery" @clear="handleQuery" />
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="list.length === 0" class="empty"><text class="empty-text">暂无库存</text></view>
      <view v-for="item in list" :key="item.stockId" class="card">
        <view class="card-top">
          <text class="card-title">{{ getMedName(item.medId) }}</text>
          <view class="tag" :class="warnClass(item)">{{ warnText(item) }}</view>
        </view>
        <view class="qty-row">
          <view class="qty-item"><text class="qty-value" :class="warnClass(item)">{{ item.totalQty }}</text><text class="qty-label">库存总量</text></view>
          <view class="qty-item"><text class="qty-value">{{ item.lockQty || 0 }}</text><text class="qty-label">锁定</text></view>
          <view class="qty-item"><text class="qty-value">{{ getRange(item.medId) }}</text><text class="qty-label">区间</text></view>
        </view>
        <view class="card-bottom">
          <text class="time">最近入库 {{ formatDate(item.lastInTime) }}</text>
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
          <text class="form-title">{{ form.stockId ? '修改库存' : '新增库存' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item"><text class="form-label">药品 <text class="required">*</text></text>
            <picker :range="medRange" range-key="label" @change="e => form.medId = medRange[e.detail.value].value">
              <view class="form-picker">{{ getMedName(form.medId) || '请选择药品' }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">库存总数量</text><input class="form-input" v-model="form.totalQty" type="number" /></view>
          <view class="form-item"><text class="form-label">锁定数量</text><input class="form-input" v-model="form.lockQty" type="number" /></view>
          <view class="form-item"><text class="form-label">最近入库时间</text><picker mode="date" :value="form.lastInTime" @change="e => form.lastInTime = e.detail.value"><view class="form-picker">{{ form.lastInTime || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">最近出库时间</text><picker mode="date" :value="form.lastOutTime" @change="e => form.lastOutTime = e.detail.value"><view class="form-picker">{{ form.lastOutTime || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">备注</text><textarea class="form-textarea" v-model="form.remark" /></view>

          <view class="sub-header">
            <text class="sub-title">库存批次</text>
            <view class="sub-add" @click="addBatch"><uni-icons type="plusempty" size="14" color="#fff"></uni-icons><text>添加</text></view>
          </view>
          <view v-for="(b, idx) in batchList" :key="idx" class="batch-card">
            <view class="batch-head"><text class="batch-title">批次 {{ idx + 1 }}</text><uni-icons type="trash" size="18" color="#f56c6c" @click="removeBatch(idx)"></uni-icons></view>
            <view class="form-item"><text class="form-label">供应商ID</text><input class="form-input" v-model="b.supplierId" type="number" /></view>
            <view class="form-item"><text class="form-label">生产批号</text><input class="form-input" v-model="b.batchNo" /></view>
            <view class="form-item"><text class="form-label">生产日期</text><picker mode="date" :value="b.produceDate" @change="e => b.produceDate = e.detail.value"><view class="form-picker">{{ b.produceDate || '请选择' }}</view></picker></view>
            <view class="form-item"><text class="form-label">有效期至</text><picker mode="date" :value="b.expireDate" @change="e => b.expireDate = e.detail.value"><view class="form-picker">{{ b.expireDate || '请选择' }}</view></picker></view>
            <view class="form-item"><text class="form-label">入库数量</text><input class="form-input" v-model="b.batchQty" type="number" /></view>
            <view class="form-item"><text class="form-label">剩余数量</text><input class="form-input" v-model="b.remainQty" type="number" /></view>
            <view class="form-item"><text class="form-label">批次进价</text><input class="form-input" v-model="b.purchasePrice" type="digit" /></view>
            <view class="form-item"><text class="form-label">入库时间</text><picker mode="date" :value="b.inTime" @change="e => b.inTime = e.detail.value"><view class="form-picker">{{ b.inTime || '请选择' }}</view></picker></view>
            <view class="form-item"><text class="form-label">批次状态</text>
              <picker :range="batchStatusRange" range-key="label" @change="e => b.batchStatus = batchStatusRange[e.detail.value].value">
                <view class="form-picker">{{ batchStatusText(b.batchStatus) }}</view>
              </picker>
            </view>
          </view>
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
import { listStock, getStock, addStock, updateStock, delStock } from '@/api/system/stock'
import { listInfo } from '@/api/system/info'

export default {
  data() {
    return {
      queryParams: { medId: '', pageNum: 1, pageSize: 10 },
      list: [], total: 0, finished: false,
      medMap: {}, medRange: [],
      batchList: [],
      batchStatusRange: [
        { value: '0', label: '正常' }, { value: '1', label: '临期' },
        { value: '2', label: '过期' }, { value: '3', label: '已清理' }
      ],
      form: this.emptyForm()
    }
  },
  onLoad() { this.loadMedMap().then(() => this.getList(true)) },
  onPullDownRefresh() { this.getList(true).finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return { stockId: null, medId: null, totalQty: 0, lockQty: 0, lastInTime: null, lastOutTime: null, remark: '' }
    },
    emptyBatch() {
      return { supplierId: '', batchNo: '', produceDate: '', expireDate: '', batchQty: '', remainQty: '', purchasePrice: '', inTime: '', batchStatus: '0', remark: '' }
    },
    async loadMedMap() {
      try {
        const res = await listInfo({ pageNum: 1, pageSize: 1000 })
        const rows = res.rows || []
        const map = {}
        rows.forEach(m => { map[m.medId] = { medName: m.medName, stockMin: m.stockMin, stockMax: m.stockMax } })
        this.medMap = map
        this.medRange = rows.map(m => ({ value: m.medId, label: m.medName }))
      } catch (e) {}
    },
    getMedName(id) { const i = this.medMap[id]; return i ? i.medName : (id ? '药品' + id : '') },
    getRange(id) { const i = this.medMap[id]; return i ? `${i.stockMin || 0}~${i.stockMax || 0}` : '-' },
    warnClass(item) {
      const info = this.medMap[item.medId]
      if (!info) return 'normal'
      const qty = Number(item.totalQty) || 0
      if (info.stockMin > 0 && qty < info.stockMin) return 'low'
      if (info.stockMax > 0 && qty > info.stockMax) return 'high'
      return 'normal'
    },
    warnText(item) { return { low: '库存不足', high: '库存积压', normal: '正常' }[this.warnClass(item)] },
    batchStatusText(s) { return ({ '0': '正常', '1': '临期', '2': '过期', '3': '已清理' })[s] || '正常' },
    formatDate(t) { if (!t) return '-'; const d = new Date(t); const p = n => n < 10 ? '0' + n : n; return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}` },
    async getList(reset = false) {
      if (reset) { this.queryParams.pageNum = 1; this.finished = false }
      try {
        const res = await listStock(this.queryParams)
        const rows = res.rows || []
        this.list = reset ? rows : [...this.list, ...rows]
        this.total = res.total
        if (this.list.length >= res.total) this.finished = true
      } catch (e) {}
    },
    loadMore() { if (!this.finished) { this.queryParams.pageNum++; this.getList() } },
    handleQuery() { this.getList(true) },
    handleAdd() { this.form = this.emptyForm(); this.batchList = []; this.$refs.formPopup.open() },
    async handleEdit(item) {
      const res = await getStock(item.stockId)
      this.form = { ...this.emptyForm(), ...res.data }
      this.batchList = res.data.medStockBatchList || []
      this.$refs.formPopup.open()
    },
    closePopup() { this.$refs.formPopup.close() },
    addBatch() { this.batchList.push(this.emptyBatch()) },
    removeBatch(i) { this.batchList.splice(i, 1) },
    submitForm() {
      if (!this.form.medId) return uni.showToast({ title: '请选择药品', icon: 'none' })
      this.form.medStockBatchList = this.batchList
      const api = this.form.stockId ? updateStock : addStock
      api(this.form).then(() => { uni.showToast({ title: '操作成功', icon: 'success' }); this.closePopup(); this.getList(true) })
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示', content: `确认删除「${this.getMedName(item.medId)}」的库存记录？`,
        success: r => { if (r.confirm) delStock(item.stockId).then(() => { uni.showToast({ title: '删除成功', icon: 'success' }); this.getList(true) }) }
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
  &.low { background: rgba(245,108,108,.12); color: #f56c6c; }
  &.high { background: rgba(230,162,60,.12); color: #e6a23c; }
  &.normal { background: rgba(103,194,58,.12); color: #67c23a; }
}
.qty-row { display: flex; background: #f7f8fa; border-radius: 12rpx; padding: 16rpx 0; margin-bottom: 16rpx; }
.qty-item { flex: 1; display: flex; flex-direction: column; align-items: center; border-right: 1rpx solid #ebeef5; &:last-child { border-right: none; } }
.qty-value { font-size: 32rpx; font-weight: 600; color: #303133; &.low { color: #f56c6c; } &.high { color: #e6a23c; } }
.qty-label { font-size: 22rpx; color: #909399; margin-top: 6rpx; }
.card-bottom { display: flex; align-items: center; justify-content: space-between; padding-top: 12rpx; border-top: 1rpx solid #f2f3f5; }
.time { font-size: 22rpx; color: #909399; }
.card-actions { display: flex; gap: 24rpx; }
.empty { padding: 200rpx 0; text-align: center; } .empty-text { font-size: 26rpx; color: #c0c4cc; }
.no-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #c0c4cc; }
.fab { position: fixed; right: 40rpx; bottom: 80rpx; width: 100rpx; height: 100rpx; background: linear-gradient(135deg, #409eff, #66b1ff); border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(64,158,255,.4); z-index: 99; }

.sub-header { display: flex; align-items: center; justify-content: space-between; margin: 32rpx 0 16rpx; padding-top: 20rpx; border-top: 1rpx solid #ebeef5; }
.sub-title { font-size: 28rpx; font-weight: 600; color: #303133; }
.sub-add { display: flex; align-items: center; background: #409eff; color: #fff; font-size: 24rpx; padding: 8rpx 20rpx; border-radius: 24rpx; uni-icons { margin-right: 6rpx; } }
.batch-card { background: #f7f8fa; border-radius: 12rpx; padding: 20rpx; margin-bottom: 16rpx; }
.batch-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16rpx; }
.batch-title { font-size: 26rpx; font-weight: 600; color: #606266; }
</style>