<template>
  <view class="page">
    <view class="search-bar">
      <uni-search-bar v-model="queryParams.categoryName" placeholder="分类名称" radius="100" cancelButton="none" @confirm="handleQuery" @clear="handleQuery" />
    </view>

    <scroll-view scroll-y class="list-scroll">
      <view v-if="flatList.length === 0" class="empty"><text class="empty-text">暂无分类</text></view>
      <view v-for="item in flatList" :key="item.categoryId" class="card" :class="'lv' + item._level">
        <view class="card-main" @click="handleEdit(item)">
          <view class="card-head">
            <text class="card-title">{{ item.categoryName }}</text>
            <text v-if="item.categoryCode" class="card-code">{{ item.categoryCode }}</text>
          </view>
          <view class="card-sub">
            <text>{{ medTypeText(item.medType) }}</text>
            <text class="dot">·</text>
            <text>排序 {{ item.orderNum || 0 }}</text>
          </view>
        </view>
        <view class="card-actions">
          <uni-icons type="compose" size="20" color="#409eff" @click="handleEdit(item)"></uni-icons>
          <uni-icons type="trash" size="20" color="#f56c6c" @click="handleDelete(item)"></uni-icons>
        </view>
      </view>
    </scroll-view>

    <view class="fab" @click="handleAdd"><uni-icons type="plusempty" size="26" color="#fff"></uni-icons></view>

    <!-- 表单弹窗 -->
    <uni-popup ref="formPopup" type="bottom" background-color="#fff">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">{{ form.categoryId ? '修改分类' : '新增分类' }}</text>
          <uni-icons type="closeempty" size="22" color="#909399" @click="closePopup"></uni-icons>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-item">
            <text class="form-label">父分类ID <text class="required">*</text></text>
            <input class="form-input" v-model="form.parentId" type="number" placeholder="顶级分类填 0" />
          </view>
          <view class="form-item">
            <text class="form-label">分类名称 <text class="required">*</text></text>
            <input class="form-input" v-model="form.categoryName" placeholder="请输入分类名称" />
          </view>
          <view class="form-item">
            <text class="form-label">分类编码</text>
            <input class="form-input" v-model="form.categoryCode" placeholder="请输入分类编码" />
          </view>
          <view class="form-item">
            <text class="form-label">药品类型</text>
            <picker :range="medTypeRange" range-key="label" @change="onMedTypeChange">
              <view class="form-picker">{{ medTypeText(form.medType) }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">显示顺序</text>
            <input class="form-input" v-model="form.orderNum" type="number" placeholder="请输入显示顺序" />
          </view>
          <view class="form-item">
            <text class="form-label">状态</text>
            <picker :range="statusRange" range-key="label" @change="onStatusChange">
              <view class="form-picker">{{ statusText(form.status) }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">备注</text>
            <textarea class="form-textarea" v-model="form.remark" placeholder="请输入备注" />
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
import { listCategory, getCategory, addCategory, updateCategory, delCategory } from '@/api/system/category'

export default {
  data() {
    return {
      queryParams: { categoryName: '' },
      rawList: [],
      flatList: [],
      medTypeRange: [
        { value: '0', label: '处方药' },
        { value: '1', label: '非处方药' },
        { value: '2', label: '中成药' },
        { value: '3', label: '其他' }
      ],
      statusRange: [
        { value: '0', label: '正常' },
        { value: '1', label: '停用' }
      ],
      form: this.emptyForm()
    }
  },
  onLoad() { this.getList() },
  onPullDownRefresh() { this.getList().finally(() => uni.stopPullDownRefresh()) },
  methods: {
    emptyForm() {
      return {
        categoryId: null, parentId: '0', ancestors: null,
        categoryName: null, categoryCode: null, medType: '0',
        orderNum: 0, status: '0', remark: null
      }
    },
    async getList() {
      try {
        const res = await listCategory({ pageNum: 1, pageSize: 1000, ...this.queryParams })
        this.rawList = res.rows || []
        this.flatList = this.toFlatTree(this.rawList)
      } catch (e) { this.rawList = []; this.flatList = [] }
    },
    toFlatTree(list) {
      const map = {}, roots = []
      list.forEach(i => { map[i.categoryId] = { ...i, children: [] } })
      list.forEach(i => {
        const node = map[i.categoryId], parent = map[i.parentId]
        if (parent && i.parentId !== i.categoryId) parent.children.push(node)
        else roots.push(node)
      })
      const out = []
      const walk = (arr, lv) => arr.forEach(n => { out.push({ ...n, _level: lv }); if (n.children.length) walk(n.children, lv + 1) })
      walk(roots, 0)
      return out
    },
    medTypeText(t) { return ({ '0': '处方药', '1': '非处方药', '2': '中成药', '3': '其他' })[t] || '其他' },
    statusText(s) { return ({ '0': '正常', '1': '停用' })[s] || '正常' },
    onMedTypeChange(e) { this.form.medType = this.medTypeRange[e.detail.value].value },
    onStatusChange(e) { this.form.status = this.statusRange[e.detail.value].value },
    handleQuery() { this.getList() },
    handleAdd() {
      this.form = this.emptyForm()
      this.$refs.formPopup.open()
    },
    async handleEdit(item) {
      try {
        const res = await getCategory(item.categoryId)
        this.form = { ...this.emptyForm(), ...res.data }
        this.$refs.formPopup.open()
      } catch (e) { uni.showToast({ title: '加载失败', icon: 'none' }) }
    },
    closePopup() { this.$refs.formPopup.close() },
    submitForm() {
      if (!this.form.categoryName) return uni.showToast({ title: '请填写分类名称', icon: 'none' })
      const api = this.form.categoryId ? updateCategory : addCategory
      api(this.form).then(() => {
        uni.showToast({ title: '操作成功', icon: 'success' })
        this.closePopup()
        this.getList()
      }).catch(() => uni.showToast({ title: '操作失败', icon: 'none' }))
    },
    handleDelete(item) {
      uni.showModal({
        title: '提示',
        content: `确认删除分类「${item.categoryName}」？`,
        success: res => {
          if (!res.confirm) return
          delCategory(item.categoryId).then(() => {
            uni.showToast({ title: '删除成功', icon: 'success' })
            this.getList()
          })
        }
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

.card {
  display: flex;
  align-items: center;
  background: #fff;
  margin: 0 20rpx 16rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  &.lv0 { margin-left: 20rpx; }
  &.lv1 { margin-left: 60rpx; }
  &.lv2 { margin-left: 100rpx; }
  &.lv3 { margin-left: 140rpx; }
}
.card-main { flex: 1; min-width: 0; }
.card-head { display: flex; align-items: center; }
.card-title { font-size: 30rpx; color: #303133; font-weight: 500; }
.card-code { font-size: 22rpx; color: #909399; background: #f2f3f5; padding: 2rpx 10rpx; border-radius: 6rpx; margin-left: 12rpx; }
.card-sub { font-size: 24rpx; color: #909399; margin-top: 10rpx; display: flex; align-items: center; .dot { margin: 0 10rpx; } }
.card-actions { display: flex; gap: 24rpx; margin-left: 16rpx; }

.empty { padding: 200rpx 0; text-align: center; }
.empty-text { font-size: 26rpx; color: #c0c4cc; }

.fab {
  position: fixed; right: 40rpx; bottom: 80rpx;
  width: 100rpx; height: 100rpx;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(64, 158, 255, .4);
  z-index: 99;
}
</style>