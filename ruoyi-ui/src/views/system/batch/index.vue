<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">近效期批次</div>
          <div class="stat-value color-orange">{{ summary.nearCount || 0 }} 批 / {{ summary.nearQty || 0 }} 件</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">已过期批次</div>
          <div class="stat-value color-red">{{ summary.expiredCount || 0 }} 批 / {{ summary.expiredQty || 0 }} 件</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">正常在库数量</div>
          <div class="stat-value">{{ summary.normalQty || 0 }} 件</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">效期状态说明</div>
          <div class="stat-tip">临期天数取自药品信息中的临期预警天数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="药品名称" prop="medName">
        <el-input
          v-model="queryParams.medName"
          placeholder="请输入药品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产批号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入生产批号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="批次状态" prop="batchStatus">
        <el-select v-model="queryParams.batchStatus" placeholder="请选择批次状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="临期" value="1" />
          <el-option label="过期" value="2" />
          <el-option label="已清理" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否过期" prop="expireFlag">
        <el-select v-model="queryParams.expireFlag" placeholder="请选择" clearable>
          <el-option label="已过期" value="1" />
          <el-option label="未过期" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效期至">
        <el-date-picker
          v-model="expireDateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
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
          v-hasPermi="['system:batch:add']"
        >新增批次</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="handleRefresh"
          v-hasPermi="['system:batch:edit']"
        >刷新效期状态</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:batch:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></el-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="batchList">
      <el-table-column label="药品编码" align="center" prop="medCode" width="110" />
      <el-table-column label="药品名称" align="center" prop="medName" :show-overflow-tooltip="true" />
      <el-table-column label="规格" align="center" prop="medSpec" width="130" />
      <el-table-column label="生产批号" align="center" prop="batchNo" width="130" />
      <el-table-column label="供应商" align="center" prop="supplierName" :show-overflow-tooltip="true" />
      <el-table-column label="生产日期" align="center" prop="produceDate" width="110" />
      <el-table-column label="有效期至" align="center" prop="expireDate" width="110" />
      <el-table-column label="剩余天数" align="center" prop="remainDays" width="100">
        <template slot-scope="scope">
          <span :style="remainDaysStyle(scope.row)">{{ scope.row.remainDays }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批次数量" align="center" prop="batchQty" width="90" />
      <el-table-column label="剩余数量" align="center" prop="remainQty" width="90" />
      <el-table-column label="批次状态" align="center" prop="batchStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="batchStatusTag(scope.row.batchStatus)">{{ formatBatchStatus(scope.row.batchStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:batch:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:batch:remove']"
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

    <!-- 添加或修改药品批次对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="药品" prop="medId">
              <el-select v-model="form.medId" filterable placeholder="请选择药品">
                <el-option
                  v-for="item in medOptions"
                  :key="item.medId"
                  :label="item.medName"
                  :value="item.medId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" filterable clearable placeholder="请选择供应商">
                <el-option
                  v-for="item in supplierOptions"
                  :key="item.supplierId"
                  :label="item.supplierName"
                  :value="item.supplierId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产批号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入生产批号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产日期" prop="produceDate">
              <el-date-picker clearable
                v-model="form.produceDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择生产日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期至" prop="expireDate">
              <el-date-picker clearable
                v-model="form.expireDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择有效期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次入库数量" prop="batchQty">
              <el-input-number v-model="form.batchQty" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次剩余数量" prop="remainQty">
              <el-input-number v-model="form.remainQty" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次进价" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBatch, getBatch, delBatch, addBatch, updateBatch, refreshBatch, getExpirySummary } from "@/api/system/batch"
import { optionselectInfo } from "@/api/system/info"
import { optionselectSupplier } from "@/api/system/supplier"

export default {
  name: "MedStockBatch",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 批次表格数据
      batchList: [],
      // 效期统计
      summary: {},
      // 药品下拉选项
      medOptions: [],
      // 供应商下拉选项
      supplierOptions: [],
      // 有效期范围
      expireDateRange: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medName: null,
        batchNo: null,
        batchStatus: null,
        expireFlag: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        medId: [
          { required: true, message: "药品不能为空", trigger: "change" }
        ],
        batchNo: [
          { required: true, message: "生产批号不能为空", trigger: "blur" }
        ],
        expireDate: [
          { required: true, message: "有效期不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getSummary()
    this.getMedOptions()
    this.getSupplierOptions()
  },
  methods: {
    /** 查询批次列表 */
    getList() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.expireDateRange && this.expireDateRange.length === 2) {
        params.beginExpireDate = this.expireDateRange[0]
        params.endExpireDate = this.expireDateRange[1]
      }
      listBatch(params).then(response => {
        this.batchList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询效期统计 */
    getSummary() {
      getExpirySummary().then(response => {
        this.summary = response.data || {}
      })
    },
    /** 查询药品下拉选项 */
    getMedOptions() {
      optionselectInfo().then(response => {
        this.medOptions = response.data
      })
    },
    /** 查询供应商下拉选项 */
    getSupplierOptions() {
      optionselectSupplier().then(response => {
        this.supplierOptions = response.data
      })
    },
    /** 批次状态格式化 */
    formatBatchStatus(value) {
      if (value === "1") return "临期"
      if (value === "2") return "过期"
      if (value === "3") return "已清理"
      return "正常"
    },
    /** 批次状态标签颜色 */
    batchStatusTag(value) {
      if (value === "1") return "warning"
      if (value === "2") return "danger"
      if (value === "3") return "info"
      return "success"
    },
    /** 剩余天数颜色提示 */
    remainDaysStyle(row) {
      if (row.remainDays == null) {
        return {}
      }
      if (row.remainDays < 0) {
        return { color: '#F56C6C', fontWeight: 'bold' }
      }
      if (row.warnDays != null && row.remainDays <= row.warnDays) {
        return { color: '#E6A23C', fontWeight: 'bold' }
      }
      return {}
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        batchId: null,
        medId: null,
        supplierId: null,
        batchNo: null,
        produceDate: null,
        expireDate: null,
        batchQty: 0,
        remainQty: 0,
        purchasePrice: 0,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.expireDateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 刷新效期状态 */
    handleRefresh() {
      refreshBatch().then(response => {
        this.$modal.msgSuccess(response.msg || "刷新成功")
        this.getList()
        this.getSummary()
      })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加药品批次"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      getBatch(row.batchId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改药品批次"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.batchId != null) {
            updateBatch(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
              this.getSummary()
            })
          } else {
            addBatch(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
              this.getSummary()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除批号为"' + row.batchNo + '"的批次数据？').then(function() {
        return delBatch(row.batchId)
      }).then(() => {
        this.getList()
        this.getSummary()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/batch/export', {
        ...this.queryParams
      }, `medbatch_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.stat-title {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
  margin-top: 6px;
}
.stat-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.color-red {
  color: #f56c6c;
}
.color-orange {
  color: #e6a23c;
}
</style>
