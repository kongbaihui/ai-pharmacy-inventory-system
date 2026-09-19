<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <!-- 药品：下拉选择，值仍为 medId -->
      <el-form-item label="药品" prop="medId">
        <el-select
          v-model="queryParams.medId"
          placeholder="请选择药品"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="item in medOptions"
            :key="item.medId"
            :label="item.medName"
            :value="item.medId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="库存总数量" prop="totalQty">
        <el-input
          v-model="queryParams.totalQty"
          placeholder="请输入库存总数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="锁定数量" prop="lockQty">
        <el-input
          v-model="queryParams.lockQty"
          placeholder="请输入锁定数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最近入库时间" prop="lastInTime">
        <el-date-picker clearable
          v-model="queryParams.lastInTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择最近入库时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="最近出库时间" prop="lastOutTime">
        <el-date-picker clearable
          v-model="queryParams.lastOutTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择最近出库时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:stock:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:stock:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:stock:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:stock:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="库存ID" align="center" prop="stockId" width="90" />
      <!-- 药品：显示药品名称 -->
      <el-table-column label="药品名称" align="center" prop="medId" min-width="150">
        <template slot-scope="scope">
          <span>{{ getMedName(scope.row.medId) }}</span>
        </template>
      </el-table-column>
      <!-- 新增：库存预警列 -->
      <el-table-column label="库存预警" align="center" width="110">
        <template slot-scope="scope">
          <el-tooltip
            :disabled="!getStockWarn(scope.row).tip"
            effect="dark"
            placement="top"
          >
            <div slot="content" v-html="getStockWarn(scope.row).tip"></div>
            <el-tag
              :type="getStockWarn(scope.row).type"
              size="mini"
              effect="dark"
            >
              {{ getStockWarn(scope.row).text }}
            </el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <!-- 库存总数量：按预警状态着色 -->
      <el-table-column label="库存总数量" align="center" prop="totalQty" width="120">
        <template slot-scope="scope">
          <span :style="{ color: getQtyColor(scope.row), fontWeight: 600 }">
            {{ scope.row.totalQty }}
          </span>
        </template>
      </el-table-column>
      <!-- 库存上下限参考 -->
      <el-table-column label="库存下限" align="center" width="90">
        <template slot-scope="scope">
          <span>{{ getMedLimit(scope.row.medId).stockMin }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存上限" align="center" width="90">
        <template slot-scope="scope">
          <span>{{ getMedLimit(scope.row.medId).stockMax }}</span>
        </template>
      </el-table-column>
      <el-table-column label="锁定数量" align="center" prop="lockQty" width="100" />
      <el-table-column label="最近入库时间" align="center" prop="lastInTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastInTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近出库时间" align="center" prop="lastOutTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastOutTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:stock:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:stock:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改药品库存对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <!-- 药品：下拉选择，值仍为 medId -->
            <el-form-item label="药品" prop="medId">
              <el-select
                v-model="form.medId"
                placeholder="请选择药品"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in medOptions"
                  :key="item.medId"
                  :label="item.medName"
                  :value="item.medId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="库存总数量" prop="totalQty">
              <el-input v-model="form.totalQty" placeholder="请输入库存总数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="锁定数量" prop="lockQty">
              <el-input v-model="form.lockQty" placeholder="请输入锁定数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最近入库时间" prop="lastInTime">
              <el-date-picker clearable
                v-model="form.lastInTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择最近入库时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最近出库时间" prop="lastOutTime">
              <el-date-picker clearable
                v-model="form.lastOutTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择最近出库时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">药品库存批次信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAddMedStockBatch">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleDeleteMedStockBatch">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="medStockBatchList" :row-class-name="rowMedStockBatchIndex" @selection-change="handleMedStockBatchSelectionChange" ref="medStockBatch">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="供应商ID" prop="supplierId" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.supplierId" placeholder="请输入供应商ID" />
            </template>
          </el-table-column>
          <el-table-column label="生产批号" prop="batchNo" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.batchNo" placeholder="请输入生产批号" />
            </template>
          </el-table-column>
          <el-table-column label="生产日期" prop="produceDate" width="240">
            <template slot-scope="scope">
              <el-date-picker clearable v-model="scope.row.produceDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择生产日期" />
            </template>
          </el-table-column>
          <el-table-column label="有效期至" prop="expireDate" width="240">
            <template slot-scope="scope">
              <el-date-picker clearable v-model="scope.row.expireDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择有效期至" />
            </template>
          </el-table-column>
          <el-table-column label="批次入库数量" prop="batchQty" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.batchQty" placeholder="请输入批次入库数量" />
            </template>
          </el-table-column>
          <el-table-column label="批次剩余数量" prop="remainQty" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.remainQty" placeholder="请输入批次剩余数量" />
            </template>
          </el-table-column>
          <el-table-column label="批次进价" prop="purchasePrice" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.purchasePrice" placeholder="请输入批次进价" />
            </template>
          </el-table-column>
          <el-table-column label="入库时间" prop="inTime" width="240">
            <template slot-scope="scope">
              <el-date-picker clearable v-model="scope.row.inTime" type="date" value-format="yyyy-MM-dd" placeholder="请选择入库时间" />
            </template>
          </el-table-column>
          <el-table-column label="批次状态" prop="batchStatus" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.batchStatus" placeholder="请选择批次状态">
                <el-option label="请选择字典生成" value="" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStock, getStock, delStock, addStock, updateStock } from "@/api/system/stock"
import { listInfo } from "@/api/system/info"

export default {
  name: "Stock",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedMedStockBatch: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 药品库存表格数据
      stockList: [],
      // 药品库存批次表格数据
      medStockBatchList: [],
      // 药品下拉数据
      medOptions: [],
      // 药品 id -> { medName, stockMin, stockMax } 映射
      medMap: {},
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medId: null,
        totalQty: null,
        lockQty: null,
        lastInTime: null,
        lastOutTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        medId: [
          { required: true, message: "药品不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getMedOptions()
    this.getList()
  },
  methods: {
    /** 查询药品下拉数据，同时构建 id -> {medName, stockMin, stockMax} 映射 */
    getMedOptions() {
      listInfo({ pageNum: 1, pageSize: 1000 }).then(response => {
        const rows = response.rows || []
        this.medOptions = rows
        const map = {}
        rows.forEach(item => {
          map[item.medId] = {
            medName: item.medName,
            stockMin: item.stockMin,
            stockMax: item.stockMax
          }
        })
        this.medMap = map
      })
    },

    /** 根据药品ID获取药品名称 */
    getMedName(medId) {
      if (medId == null || medId === '') return ''
      const info = this.medMap[medId]
      return info ? info.medName : medId
    },

    /** 根据药品ID获取库存上下限（用于表格展示） */
    getMedLimit(medId) {
      const info = this.medMap[medId]
      if (!info) return { stockMin: '-', stockMax: '-' }
      return {
        stockMin: info.stockMin != null ? info.stockMin : '-',
        stockMax: info.stockMax != null ? info.stockMax : '-'
      }
    },

    /**
     * 根据当前库存与药品上下限判断预警状态
     * 返回：{ type: el-tag 类型, text: 文案, tip: 悬浮提示 HTML }
     */
    getStockWarn(row) {
      const info = this.medMap[row.medId]
      // 找不到药品信息时不展示预警
      if (!info) {
        return { type: 'info', text: '未知', tip: '' }
      }

      const qty = Number(row.totalQty) || 0
      const min = Number(info.stockMin) || 0
      const max = Number(info.stockMax) || 0

      // 库存不足：库存下限 > 0 且当前数量 < 下限
      if (min > 0 && qty < min) {
        const diff = min - qty
        return {
          type: 'danger',
          text: '库存不足',
          tip: `当前库存 <b style="color:#F56C6C">${qty}</b>，低于库存下限 <b>${min}</b>，建议补货 <b>${diff}</b> 件`
        }
      }

      // 库存积压：库存上限 > 0 且当前数量 > 上限
      if (max > 0 && qty > max) {
        const diff = qty - max
        return {
          type: 'warning',
          text: '库存积压',
          tip: `当前库存 <b style="color:#E6A23C">${qty}</b>，高于库存上限 <b>${max}</b>，超出 <b>${diff}</b> 件，注意积压`
        }
      }

      // 正常
      const rangeText = (min > 0 || max > 0)
        ? `（参考区间：${min} ~ ${max}）`
        : ''
      return {
        type: 'success',
        text: '正常',
        tip: `当前库存 <b style="color:#67C23A">${qty}</b>，处于正常范围${rangeText}`
      }
    },

    /** 库存总数量单元格颜色：不足红、积压橙、正常默认色 */
    getQtyColor(row) {
      const warn = this.getStockWarn(row)
      if (warn.text === '库存不足') return '#F56C6C'
      if (warn.text === '库存积压') return '#E6A23C'
      return '#303133'
    },

    /** 查询药品库存列表 */
    getList() {
      this.loading = true
      listStock(this.queryParams).then(response => {
        this.stockList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        stockId: null,
        medId: null,
        totalQty: null,
        lockQty: null,
        lastInTime: null,
        lastOutTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      }
      this.medStockBatchList = []
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.stockId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加药品库存"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const stockId = row.stockId || this.ids
      getStock(stockId).then(response => {
        this.form = response.data
        this.medStockBatchList = response.data.medStockBatchList
        this.open = true
        this.title = "修改药品库存"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.medStockBatchList = this.medStockBatchList
          if (this.form.stockId != null) {
            updateStock(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStock(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const stockIds = row.stockId || this.ids
      this.$modal.confirm('是否确认删除药品库存编号为"' + stockIds + '"的数据项？').then(function() {
        return delStock(stockIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 药品库存批次序号 */
    rowMedStockBatchIndex({ row, rowIndex }) {
      row.index = rowIndex + 1
    },
    /** 药品库存批次添加按钮操作 */
    handleAddMedStockBatch() {
      let obj = {}
      obj.supplierId = ""
      obj.batchNo = ""
      obj.produceDate = ""
      obj.expireDate = ""
      obj.batchQty = ""
      obj.remainQty = ""
      obj.purchasePrice = ""
      obj.inTime = ""
      obj.batchStatus = ""
      obj.remark = ""
      this.medStockBatchList.push(obj)
    },
    /** 药品库存批次删除按钮操作 */
    handleDeleteMedStockBatch() {
      if (this.checkedMedStockBatch.length == 0) {
        this.$modal.msgError("请先选择要删除的药品库存批次数据")
      } else {
        const medStockBatchList = this.medStockBatchList
        const checkedMedStockBatch = this.checkedMedStockBatch
        this.medStockBatchList = medStockBatchList.filter(function(item) {
          return checkedMedStockBatch.indexOf(item.index) == -1
        })
      }
    },
    /** 复选框选中数据 */
    handleMedStockBatchSelectionChange(selection) {
      this.checkedMedStockBatch = selection.map(item => item.index)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/stock/export', {
        ...this.queryParams
      }, `stock_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>