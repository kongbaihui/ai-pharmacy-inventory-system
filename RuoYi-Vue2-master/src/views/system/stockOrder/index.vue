<template>
  <div class="app-container stock-business-page">
    <div class="page-heading">
      <div>
        <h2>{{ pageTitle }}</h2>
        <p>{{ pageDescription }}</p>
      </div>
      <el-button icon="el-icon-refresh" plain @click="getList">刷新</el-button>
    </div>

    <template v-if="isFlowMode">
      <el-form ref="flowQueryForm" :model="flowQuery" :inline="true" size="small" class="filter-bar">
        <el-form-item label="业务单号">
          <el-input v-model="flowQuery.bizNo" clearable placeholder="请输入业务单号" @keyup.enter.native="handleFlowQuery" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="flowQuery.flowType" clearable placeholder="全部类型">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="药品">
          <el-select v-model="flowQuery.medId" filterable clearable placeholder="全部药品">
            <el-option v-for="item in medOptions" :key="item.medId" :label="item.medName" :value="item.medId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFlowQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetFlowQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="flowList" stripe>
        <el-table-column label="流水时间" prop="flowTime" width="170" />
        <el-table-column label="业务单号" prop="bizNo" min-width="175" show-overflow-tooltip />
        <el-table-column label="业务类型" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="typeTag(scope.row.flowType)" size="small">{{ typeName(scope.row.flowType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="药品" prop="medName" min-width="160" show-overflow-tooltip />
        <el-table-column label="变动数量" prop="changeQty" width="110" align="right">
          <template slot-scope="scope">
            <span :class="scope.row.changeQty >= 0 ? 'qty-in' : 'qty-out'">{{ scope.row.changeQty > 0 ? '+' : '' }}{{ scope.row.changeQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="变动前" prop="beforeQty" width="100" align="right" />
        <el-table-column label="变动后" prop="afterQty" width="100" align="right" />
        <el-table-column label="操作人" prop="operator" width="120" />
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
      </el-table>
      <pagination v-show="flowTotal > 0" :total="flowTotal" :page.sync="flowQuery.pageNum" :limit.sync="flowQuery.pageSize" @pagination="getFlowList" />
    </template>

    <template v-else>
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="filter-bar">
        <el-form-item label="单据编号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" clearable placeholder="请输入单据编号" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="单据状态" prop="orderStatus">
          <el-select v-model="queryParams.orderStatus" clearable placeholder="全部状态">
            <el-option label="草稿" value="0" />
            <el-option label="已确认" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-actions">
        <el-button v-hasPermi="['system:stockOrder:add']" type="primary" icon="el-icon-plus" size="small" @click="handleAdd">新建{{ typeName(currentType) }}单</el-button>
        <el-button v-hasPermi="['system:stockOrder:edit']" icon="el-icon-edit" size="small" :disabled="single" @click="handleUpdate">修改草稿</el-button>
        <el-button v-hasPermi="['system:stockOrder:confirm']" type="success" plain icon="el-icon-check" size="small" :disabled="single || selectedConfirmed" @click="handleConfirm">确认单据</el-button>
        <el-button v-hasPermi="['system:stockOrder:remove']" type="danger" plain icon="el-icon-delete" size="small" :disabled="multiple" @click="handleDelete">删除草稿</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="单据编号" prop="orderNo" min-width="190" show-overflow-tooltip />
        <el-table-column label="业务日期" prop="orderDate" width="120" />
        <el-table-column v-if="currentType === '1'" label="供应商" prop="supplierName" min-width="150" show-overflow-tooltip />
        <el-table-column v-else label="科室" prop="department" min-width="140" show-overflow-tooltip />
        <el-table-column label="总数量" prop="totalQty" width="110" align="right" />
        <el-table-column label="总金额（元）" prop="totalAmount" width="130" align="right">
          <template slot-scope="scope">{{ money(scope.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="经办人" prop="operator" width="110" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.orderStatus === '1' ? 'success' : 'info'" size="small">{{ scope.row.orderStatus === '1' ? '已确认' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.orderStatus === '0'" v-hasPermi="['system:stockOrder:edit']" type="text" size="small" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button v-if="scope.row.orderStatus === '0'" v-hasPermi="['system:stockOrder:confirm']" type="text" size="small" class="confirm-link" @click="handleConfirm(scope.row)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getOrderList" />
    </template>

    <el-dialog :title="dialogTitle" :visible.sync="open" width="1080px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="105px" :disabled="viewOnly">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="业务类型">
              <el-input :value="typeName(form.orderType)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务日期" prop="orderDate">
              <el-date-picker v-model="form.orderDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经办人" prop="operator">
              <el-input v-model="form.operator" placeholder="默认使用当前账号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col v-if="form.orderType === '1'" :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width:100%">
                <el-option v-for="item in supplierOptions" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-else :span="8">
            <el-form-item :label="form.orderType === '2' ? '领用科室' : '退回科室'" prop="department">
              <el-input v-model="form.department" placeholder="请输入科室名称" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="detail-title">
          <span>药品明细</span>
          <el-button v-if="!viewOnly" type="text" icon="el-icon-plus" @click="addItem">添加明细</el-button>
        </div>
        <el-table :data="form.itemList" border size="small">
          <el-table-column label="药品" min-width="180">
            <template slot-scope="scope">
              <el-select v-model="scope.row.medId" filterable placeholder="选择药品" style="width:100%" @change="onMedChange(scope.row)">
                <el-option v-for="item in medOptions" :key="item.medId" :label="item.medName + (item.medSpec ? ' / ' + item.medSpec : '')" :value="item.medId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column v-if="form.orderType === '1'" label="生产批号" min-width="130">
            <template slot-scope="scope"><el-input v-model="scope.row.batchNo" placeholder="输入批号" /></template>
          </el-table-column>
          <el-table-column v-else label="库存批次" min-width="190">
            <template slot-scope="scope">
              <el-select v-model="scope.row.batchId" filterable placeholder="选择批次" style="width:100%" @change="onBatchChange(scope.row)">
                <el-option v-for="item in batchOptionsFor(scope.row.medId)" :key="item.batchId" :label="item.batchNo + '（可用 ' + item.remainQty + '）'" :value="item.batchId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column v-if="form.orderType === '1'" label="生产日期" width="145">
            <template slot-scope="scope"><el-date-picker v-model="scope.row.produceDate" type="date" value-format="yyyy-MM-dd" placeholder="选填" style="width:130px" /></template>
          </el-table-column>
          <el-table-column v-if="form.orderType === '1'" label="有效期" width="145">
            <template slot-scope="scope"><el-date-picker v-model="scope.row.expireDate" type="date" value-format="yyyy-MM-dd" placeholder="必填" style="width:130px" /></template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template slot-scope="scope"><el-input-number v-model="scope.row.quantity" :min="1" :max="99999999" controls-position="right" style="width:100%" /></template>
          </el-table-column>
          <el-table-column label="单价（元）" width="130">
            <template slot-scope="scope"><el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" :step="0.1" controls-position="right" style="width:100%" /></template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template slot-scope="scope">{{ money((scope.row.quantity || 0) * (scope.row.unitPrice || 0)) }}</template>
          </el-table-column>
          <el-table-column v-if="!viewOnly" label="操作" width="70" align="center">
            <template slot-scope="scope"><el-button type="text" class="danger-link" @click="removeItem(scope.$index)">删除</el-button></template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <span class="total-line">合计：{{ totalQuantity }} 件 / ￥{{ totalAmount }}</span>
        <el-button @click="open = false">{{ viewOnly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!viewOnly" type="primary" :loading="submitLoading" @click="submitForm">保存草稿</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStockOrder, getStockOrder, addStockOrder, updateStockOrder, confirmStockOrder, delStockOrder, listStockFlow } from '@/api/system/stockOrder'
import { optionselectInfo } from '@/api/system/info'
import { optionselectSupplier } from '@/api/system/supplier'
import { listBatch } from '@/api/system/batch'

export default {
  name: 'StockOrder',
  data() {
    return {
      loading: false,
      submitLoading: false,
      open: false,
      viewOnly: false,
      orderList: [],
      flowList: [],
      total: 0,
      flowTotal: 0,
      ids: [],
      selectedRows: [],
      single: true,
      multiple: true,
      medOptions: [],
      supplierOptions: [],
      batchOptions: [],
      queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderStatus: undefined, orderType: '1' },
      flowQuery: { pageNum: 1, pageSize: 10, bizNo: undefined, flowType: undefined, medId: undefined },
      form: {},
      rules: {
        orderDate: [{ required: true, message: '请选择业务日期', trigger: 'change' }],
        supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        department: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
      },
      typeOptions: [
        { value: '1', label: '入库' }, { value: '2', label: '出库' }, { value: '3', label: '退库' },
        { value: '4', label: '盘点调整' }, { value: '5', label: '过期清理' }
      ]
    }
  },
  computed: {
    isFlowMode() { return this.$route.query.mode === 'flow' },
    currentType() { return this.$route.query.orderType || '1' },
    pageTitle() { return this.isFlowMode ? '库存流水' : this.typeName(this.currentType) + '管理' },
    pageDescription() {
      if (this.isFlowMode) return '统一记录入库、出库、退库、盘点及过期清理产生的库存变化'
      return this.currentType === '1' ? '登记采购到货，确认后自动增加批次及总库存' : (this.currentType === '2' ? '登记科室领用，确认后按批次扣减可用库存' : '登记科室退回，确认后恢复原批次库存')
    },
    selectedConfirmed() { return this.selectedRows.length !== 1 || this.selectedRows[0].orderStatus === '1' },
    dialogTitle() { return (this.viewOnly ? '查看' : (this.form.orderId ? '修改' : '新建')) + this.typeName(this.form.orderType) + '单' },
    totalQuantity() { return (this.form.itemList || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0) },
    totalAmount() { return this.money((this.form.itemList || []).reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0)) }
  },
  watch: {
    '$route.fullPath'() { this.syncRoute() }
  },
  created() {
    this.loadOptions()
    this.syncRoute()
  },
  methods: {
    syncRoute() {
      this.queryParams.orderType = this.currentType
      this.queryParams.pageNum = 1
      this.isFlowMode ? this.getFlowList() : this.getOrderList()
    },
    loadOptions() {
      optionselectInfo().then(res => { this.medOptions = res.data || [] })
      optionselectSupplier().then(res => { this.supplierOptions = res.data || [] })
      listBatch({ pageNum: 1, pageSize: 1000 }).then(res => { this.batchOptions = res.rows || [] })
    },
    getList() { this.isFlowMode ? this.getFlowList() : this.getOrderList() },
    getOrderList() {
      this.loading = true
      listStockOrder(this.queryParams).then(res => { this.orderList = res.rows || []; this.total = res.total || 0 }).finally(() => { this.loading = false })
    },
    getFlowList() {
      this.loading = true
      listStockFlow(this.flowQuery).then(res => { this.flowList = res.rows || []; this.flowTotal = res.total || 0 }).finally(() => { this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getOrderList() },
    resetQuery() { this.resetForm('queryForm'); this.queryParams.orderType = this.currentType; this.handleQuery() },
    handleFlowQuery() { this.flowQuery.pageNum = 1; this.getFlowList() },
    resetFlowQuery() { this.flowQuery = { pageNum: 1, pageSize: 10, bizNo: undefined, flowType: undefined, medId: undefined }; this.getFlowList() },
    handleSelectionChange(rows) {
      this.selectedRows = rows
      this.ids = rows.map(item => item.orderId)
      this.single = rows.length !== 1
      this.multiple = !rows.length
    },
    reset() {
      this.form = { orderId: undefined, orderType: this.currentType, orderDate: this.parseTime(new Date(), '{y}-{m}-{d}'), supplierId: undefined, department: undefined, operator: undefined, remark: undefined, itemList: [this.emptyItem()] }
      this.resetForm('form')
    },
    emptyItem() { return { medId: undefined, batchId: undefined, batchNo: undefined, produceDate: undefined, expireDate: undefined, quantity: 1, unitPrice: 0 } },
    handleAdd() { this.reset(); this.viewOnly = false; this.open = true },
    handleView(row) { this.openOrder(row.orderId, true) },
    handleUpdate(row) { this.openOrder(row.orderId || this.ids[0], false) },
    openOrder(orderId, viewOnly) {
      this.reset()
      getStockOrder(orderId).then(res => {
        this.form = res.data
        this.form.itemList = this.form.itemList || []
        this.viewOnly = viewOnly
        this.open = true
      })
    },
    addItem() { this.form.itemList.push(this.emptyItem()) },
    removeItem(index) { if (this.form.itemList.length > 1) this.form.itemList.splice(index, 1); else this.$modal.msgWarning('至少保留一条药品明细') },
    onMedChange(row) { row.batchId = undefined; row.batchNo = undefined; row.unitPrice = 0 },
    onBatchChange(row) {
      const batch = this.batchOptions.find(item => item.batchId === row.batchId)
      if (batch) { row.medId = batch.medId; row.batchNo = batch.batchNo; row.unitPrice = batch.purchasePrice || 0 }
    },
    batchOptionsFor(medId) {
      return this.batchOptions.filter(item => {
        if (item.medId !== medId) return false
        if (this.form.orderType === '2') return Number(item.remainQty || 0) > 0 && item.batchStatus !== '2'
        return Number(item.batchQty || 0) > Number(item.remainQty || 0)
      })
    },
    validateItems() {
      if (!this.form.itemList || !this.form.itemList.length) return '至少添加一条药品明细'
      for (let i = 0; i < this.form.itemList.length; i++) {
        const item = this.form.itemList[i]
        if (!item.medId || !item.quantity || item.quantity <= 0) return `第 ${i + 1} 行请填写药品和正数数量`
        if (this.form.orderType === '1' && (!item.batchNo || !item.expireDate)) return `第 ${i + 1} 行请填写生产批号和有效期`
        if (this.form.orderType !== '1' && !item.batchId) return `第 ${i + 1} 行请选择库存批次`
      }
      return ''
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const message = this.validateItems()
        if (message) return this.$modal.msgWarning(message)
        this.submitLoading = true
        const action = this.form.orderId ? updateStockOrder : addStockOrder
        action(this.form).then(() => {
          this.$modal.msgSuccess(this.form.orderId ? '修改成功' : '新增成功')
          this.open = false
          this.getOrderList()
        }).finally(() => { this.submitLoading = false })
      })
    },
    handleConfirm(row) {
      const target = row.orderId ? row : this.selectedRows[0]
      this.$modal.confirm(`确认单据“${target.orderNo}”吗？确认后将立即更新库存且不可撤销。`).then(() => confirmStockOrder(target.orderId)).then(() => {
        this.$modal.msgSuccess('单据已确认，库存已同步更新')
        this.getOrderList()
        this.loadOptions()
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.orderId || this.ids
      this.$modal.confirm('确认删除选中的草稿单据吗？').then(() => delStockOrder(ids)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getOrderList()
      }).catch(() => {})
    },
    typeName(type) { return ({ '1': '入库', '2': '出库', '3': '退库', '4': '盘点调整', '5': '过期清理' })[type] || '库存业务' },
    typeTag(type) { return ({ '1': 'success', '2': 'primary', '3': 'warning', '4': 'info', '5': 'danger' })[type] || 'info' },
    money(value) { return Number(value || 0).toFixed(2) }
  }
}
</script>

<style lang="scss" scoped>
.stock-business-page { background: #f6f8fb; min-height: calc(100vh - 84px); }
.page-heading { display:flex; align-items:center; justify-content:space-between; margin-bottom:18px; }
.page-heading h2 { margin:0; color:#17324d; font-size:24px; line-height:34px; }
.page-heading p { margin:4px 0 0; color:#7a8b9a; font-size:13px; }
.filter-bar { padding:16px 18px 0; margin-bottom:14px; background:#fff; border:1px solid #e4eaf0; border-radius:6px; }
.table-actions { margin-bottom:12px; }
.detail-title { display:flex; align-items:center; justify-content:space-between; margin:4px 0 12px; color:#17324d; font-weight:600; }
.total-line { float:left; color:#516579; line-height:36px; }
.qty-in { color:#079b83; font-weight:600; }
.qty-out { color:#e04f5f; font-weight:600; }
.confirm-link { color:#079b83; }
.danger-link { color:#e04f5f; }
::v-deep .el-table { border:1px solid #e4eaf0; border-radius:6px; }
::v-deep .el-table th { background:#f4f7fa; color:#465b70; }
@media (max-width: 900px) { .page-heading { align-items:flex-start; } ::v-deep .el-dialog { width:94% !important; } }
</style>
