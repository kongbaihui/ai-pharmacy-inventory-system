<template>
  <view class="page">
    <!-- 概览 -->
    <view class="stat-row">
      <view class="stat-card"><text class="stat-value blue">{{ checkList.length }}</text><text class="stat-label">盘点单</text></view>
      <view class="stat-card"><text class="stat-value">{{ itemList.length }}</text><text class="stat-label">明细</text></view>
      <view class="stat-card"><text class="stat-value green">+{{ gainQty }}</text><text class="stat-label">盘盈</text></view>
      <view class="stat-card"><text class="stat-value red">-{{ lossQty }}</text><text class="stat-label">盘亏</text></view>
    </view>

    <!-- 盘点单列表 -->
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view v-if="checkList.length === 0" class="empty"><text class="empty-text">暂无盘点单</text></view>
      <view v-for="item in checkList" :key="item.checkId" class="card">
        <view class="card-top">
          <text class="card-title">{{ item.checkName }}</text>
          <view class="tag" :class="statusTag(item.checkStatus)">{{ statusText(item.checkStatus) }}</view>
        </view>
        <view class="card-sub">{{ item.checkNo }} · {{ checkTypeText(item.checkType) }}</view>
        <view class="card-info">
          <text>盘点人 {{ item.checkUser || '-' }}</text>
          <text>日期 {{ formatDate(item.checkDate) }}</text>
        </view>
        <view class="card-bottom">
          <text class="time">{{ formatDate(item.checkTime) }}</text>
          <view class="card-actions">
            <uni-icons type="compose" size="20" color="#409eff" @click="handleEdit(item)"></uni-icons>
            <uni-icons type="trash" size="20" color="#f56c6c" @click="handleDelete(item)"></uni-icons>
          </view>
        </view>
      </view>
      <view v-if="checkList.length && finished" class="no-more">没有更多了</view>
    </scroll-view>

    <view class="fab" @click="handleAdd"><uni-icons type="plusempty" size="26" color="#fff"></uni-icons></view>

    <uni-popup ref="formPopup" type="bottom">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">{{ form.checkId ? '修改盘点' : '新增盘点' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item"><text class="form-label">盘点单号 <text class="required">*</text></text><input class="form-input" v-model="form.checkNo" /></view>
          <view class="form-item"><text class="form-label">盘点名称 <text class="required">*</text></text><input class="form-input" v-model="form.checkName" /></view>
          <view class="form-item"><text class="form-label">盘点类型</text>
            <picker :range="checkTypeRange" range-key="label" @change="e => form.checkType = checkTypeRange[e.detail.value].value">
              <view class="form-picker">{{ checkTypeText(form.checkType) }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">盘点状态</text>
            <picker :range="checkStatusRange" range-key="label" @change="e => form.checkStatus = checkStatusRange[e.detail.value].value">
              <view class="form-picker">{{ statusText(form.checkStatus) }}</view>
            </picker>
          </view>
          <view class="form-item"><text class="form-label">盘点日期</text><picker mode="date" :value="form.checkDate" @change="e => form.checkDate = e.detail.value"><view class="form-picker">{{ form.checkDate || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">盘点人</text><input class="form-input" v-model="form.checkUser" /></view>
          <view class="form-item"><text class="form-label">盘点时间</text><picker mode="date" :value="form.checkTime" @change="e => form.checkTime = e.detail.value"><view class="form-picker">{{ form.checkTime || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">审核人</text><input class="form-input" v-model="form.auditBy" /></view>
          <view class="form-item"><text class="form-label">审核时间</text><picker mode="date" :value="form.auditTime" @change="e => form.auditTime = e.detail.value"><view class="form-picker">{{ form.auditTime || '请选择' }}</view></picker></view>
          <view class="form-item"><text class="form-label">备注</text><textarea class="form-textarea" v-model="form.remark" /></view>

          <view class="sub-header">
            <text class="sub-title">盘点明细</text>
            <view class="sub-add" @click="addItem"><uni-icons type="plusempty" size="14" color="#fff"></uni-icons><text>添加</text></view>
          </view>
          <view v-for="(it, idx) in itemFormList" :key="idx" class="batch-card">
            <view class="batch-head"><text class="batch-title">明细 {{ idx + 1 }}</text><uni-icons type="trash" size="18" color="#f56c6c" @click="removeItem(idx)"></uni-icons></view>
            <view class="form-item"><text class="form-label">药品 <text class="required">*</text></text>
              <picker :range="medRange" range-key="label" @change="e => it.medId = medRange[e.detail.value].value">
                <view class="form-picker">{{ getMedName(it.medId) || '请选择药品' }}</view>
              </picker>
            </view>
            <view class="form-item"><text class="form-label">批次ID</text><input class="form-input" v-model="it.batchId" type="number" /></view>
            <view class="form-item"><text class="form-label">生产批号</text><input class="form-input" v-model="it.batchNo" /></view>
            <view class="form-item"><text class="form-label">账面数量</text><input class="form-input" v-model="it.bookQty" type="number" /></view>
            <view class="form-item"><text class="form-label">实盘数量</text><input class="form-input" v-model="it.realQty" type="number" /></view>
            <view class="form-item"><text class="form-label">盈亏数量</text><input class="form-input" v-model="it.diffQty" type="number" /></view>
            <view class="form-item"><text class="form-label">盈亏类型</text>
              <picker :range="diffTypeRange" range-key="label" @change="e => it.diffType = diffTypeRange[e.detail.value].value">
                <view class="form-picker">{{ diffTypeText(it.diffType) }}</view>
              </picker>
            </view>
            <view class="form-item"><text class="form-label">盈亏原因</text><input class="form-input" v-model="it.diffReason" /></view>
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
import { listCheck, getCheck, addCheck, updateCheck, delCheck } from '@/api/system/check'
import { listInfo } from '@/api/system/info'

export default {
  data() {
    return {
      queryParams: { pageNum: 1, pageSize: 10 },
      checkList: [], itemList: [], total: 0, finished: false,
      medMap: {}, medRange: [],
      itemFormList: [],
      checkTypeRange: [{ value: '0', label: '全盘' }, { value: '1', label: '抽盘' }, { value: '2', label: '重点盘点' }],
      checkStatusRange: [{ value: '0', label: '待盘点' }, { value: '1', label: '盘点中' }, { value: '2', label: '已盘点' }, { value: '3', label: '已审核' }],
      diffTypeRange: [{ value: '0', label: '正常' }, { value: '1', label: '盘盈' }, { value: '2', label: '盘亏' }],
      form: this.emptyForm()
    }
  },
  computed: {
    gainQty() { return this.itemList.reduce((s, i) => s + (Number(i.diffQty) > 0 ? Number(i.diffQty) : 0), 0) },
    lossQty() { return this.itemList.reduce((s, i) => s + (Number(i.diffQty) < 0 ? Math.abs(Number(i.diffQty)) : 0), 0) }
  },
  onLoad() { this.loadMedMap().then(() => this.getList(true)) },
  onPullDownRefresh() { this.getList(true).finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return { checkId: null, checkNo: '', checkName: '', checkType: '0', checkStatus: '0', checkDate: null, checkUser: '', checkTime: null, auditBy: '', auditTime: null, remark: '' }
    },
    emptyItem() { return { medId: null, batchId: '', batchNo: '', bookQty: 0, realQty: 0, diffQty: 0, diffType: '0', diffReason: '', remark: '' } },
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
    checkTypeText(t) { return ({ '0': '全盘', '1': '抽盘', '2': '重点盘点' })[t] || '全盘' },
    statusText(s) { return ({ '0': '待盘点', '1': '盘点中', '2': '已盘点', '3': '已审核' })[s] || '待盘点' },
    statusTag(s) { return ({ '0': 'gray', '1': 'blue', '2': 'orange', '3': 'green' })[s] || 'gray' },
    diffTypeText(t) { return ({ '0': '正常', '1': '盘盈', '2': '盘亏' })[String(t)] || '正常' },
    formatDate(t) { if (!t) return '-'; const d = new Date(t); const p = n => n < 10 ? '0' + n : n; return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}` },
    async getList(reset = false) {
      if (reset) { this.queryParams.pageNum = 1; this.finished = false }
      try {
        const res = await listCheck(this.queryParams)
        const rows = res.rows || []
        this.checkList = reset ? rows : [...this.checkList, ...rows]
        this.total = res.total
        if (this.checkList.length >= res.total) this.finished = true
        // 拉取所有明细
        const detailRes = await Promise.all(this.checkList.map(c => getCheck(c.checkId).catch(() => ({ data: {} }))))
        const items = []
        detailRes.forEach((r, i) => {
          const c = this.checkList[i]
          ;((r.data && r.data.medStockCheckItemList) || []).forEach(it => {
            items.push({ ...it, checkId: c.checkId, checkName: c.checkName })
          })
        })
        this.itemList = items
      } catch (e) {}
    },
    loadMore() { if (!this.finished) { this.queryParams.pageNum++; this.getList() } },
    handleAdd() { this.form = this.emptyForm(); this.itemFormList = []; this.$refs.formPopup.open() },
    async handleEdit(item) {
      const res = await getCheck(item.checkId)
      this.form = { ...this.emptyForm(), ...res.data }
      this.itemFormList = res.data.medStockCheckItemList || []
      this.$refs.formPopup.open()
    },
    closePopup() { this.$refs.formPopup.close() },
    addItem() { this.itemFormList.push(this.emptyItem()) },
    removeItem(i) { this.itemFormList.splice(i, 1) },
    submitForm() {
      if (!this.form.checkNo || !this.form.checkName) return uni.showToast({ title: '请填写必填项', icon: 'none' })
      this.form.medStockCheckItemList = this.itemFormList
      const api = this.form.checkId ? updateCheck : addCheck
      api(this.form).then(() => { uni.showToast({ title: '操作成功', icon: 'success' }); this.closePopup(); this.getList(true) })
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示', content: `确认删除盘点单「${item.checkName}」？`,
        success: r => { if (r.confirm) delCheck(item.checkId).then(() => { uni.showToast({ title: '删除成功', icon: 'success' }); this.getList(true) }) }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/mobile-form.scss';
page { background: #f5f7fa; }
.page { min-height: 100vh; padding-bottom: 140rpx; }

.stat-row { display: flex; padding: 20rpx; gap: 12rpx; }
.stat-card { flex: 1; background: #fff; border-radius: 16rpx; padding: 24rpx 12rpx; display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: 36rpx; font-weight: 600; color: #303133; }
.stat-value.blue { color: #409eff; } .stat-value.green { color: #67c23a; } .stat-value.red { color: #f56c6c; }
.stat-label { font-size: 22rpx; color: #909399; margin-top: 8rpx; }

.list-scroll { padding: 0 0 20rpx; }
.card { background: #fff; margin: 0 20rpx 16rpx; padding: 24rpx; border-radius: 16rpx; }
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.card-title { font-size: 30rpx; color: #303133; font-weight: 600; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 12rpx; }
.tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx;
  &.gray { background: #f2f3f5; color: #909399; }
  &.blue { background: rgba(64,158,255,.12); color: #409eff; }
  &.orange { background: rgba(230,162,60,.12); color: #e6a23c; }
  &.green { background: rgba(103,194,58,.12); color: #67c23a; }
}
.card-sub { font-size: 24rpx; color: #909399; }
.card-info { display: flex; justify-content: space-between; font-size: 24rpx; color: #606266; margin-top: 12rpx; }
.card-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx solid #f2f3f5; }
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