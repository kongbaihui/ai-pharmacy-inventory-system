<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="盘点单号" prop="checkNo">
        <el-input
          v-model="queryParams.checkNo"
          placeholder="请输入盘点单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="盘点名称" prop="checkName">
        <el-input
          v-model="queryParams.checkName"
          placeholder="请输入盘点名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="盘点类型" prop="checkType">
        <el-select v-model="queryParams.checkType" placeholder="请选择盘点类型" clearable>
          <el-option label="全盘" value="0" />
          <el-option label="抽盘" value="1" />
          <el-option label="重点盘点" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点状态" prop="checkStatus">
        <el-select v-model="queryParams.checkStatus" placeholder="请选择盘点状态" clearable>
          <el-option label="待盘点" value="0" />
          <el-option label="盘点中" value="1" />
          <el-option label="已盘点" value="2" />
          <el-option label="已审核" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点日期">
        <el-date-picker
          v-model="dateRange"
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
          v-hasPermi="['system:check:add']"
        >新增盘点</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:check:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="checkList">
      <el-table-column label="盘点单号" align="center" prop="checkNo" width="150" />
      <el-table-column label="盘点名称" align="center" prop="checkName" :show-overflow-tooltip="true" />
      <el-table-column label="盘点类型" align="center" prop="checkType" width="100">
        <template slot-scope="scope">
          <span>{{ formatCheckType(scope.row.checkType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="盘点状态" align="center" prop="checkStatus" width="110">
        <template slot-scope="scope">
          <el-tag :type="checkStatusTag(scope.row.checkStatus)">{{ formatCheckStatus(scope.row.checkStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="盘点日期" align="center" prop="checkDate" width="110" />
      <el-table-column label="盘点人" align="center" prop="checkUser" width="100" />
      <el-table-column label="审核人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审核时间" align="center" prop="auditTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            :disabled="scope.row.checkStatus === '3'"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:check:edit']"
          >录入盘点</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-s-check"
            :disabled="scope.row.checkStatus === '3'"
            @click="handleAudit(scope.row)"
            v-hasPermi="['system:check:audit']"
          >审核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:check:remove']"
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

    <!-- 添加或修改库存盘点对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1080px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="盘点名称" prop="checkName">
              <el-input v-model="form.checkName" placeholder="请输入盘点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点单号" prop="checkNo">
              <el-input v-model="form.checkNo" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点类型" prop="checkType">
              <el-select v-model="form.checkType" placeholder="请选择盘点类型">
                <el-option label="全盘" value="0" />
                <el-option label="抽盘" value="1" />
                <el-option label="重点盘点" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点日期" prop="checkDate">
              <el-date-picker clearable
                v-model="form.checkDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择盘点日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点人" prop="checkUser">
              <el-input v-model="form.checkUser" placeholder="请输入盘点人" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点状态" prop="checkStatus">
              <el-select v-model="form.checkStatus" placeholder="请选择盘点状态">
                <el-option label="待盘点" value="0" />
                <el-option label="盘点中" value="1" />
                <el-option label="已盘点" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">盘点明细</el-divider>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5" v-if="form.checkId == null">
          <el-button type="primary" plain icon="el-icon-refresh" size="mini" @click="loadBookItems">带出库存明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-plus" size="mini" @click="addItemRow">添加明细行</el-button>
        </el-col>
      </el-row>
      <el-table :data="form.medStockCheckItemList" border size="mini">
        <el-table-column label="药品名称" align="center" prop="medName" min-width="160">
          <template slot-scope="scope">
            <el-select v-model="scope.row.medId" filterable placeholder="请选择药品" @change="onMedChange(scope.row)">
              <el-option
                v-for="item in medOptions"
                :key="item.medId"
                :label="item.medName"
                :value="item.medId"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="规格" align="center" prop="medSpec" width="120" />
        <el-table-column label="生产批号" align="center" prop="batchNo" width="130" />
        <el-table-column label="账面数量" align="center" prop="bookQty" width="110">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.bookQty" :min="0" size="mini" controls-position="right" @change="calcDiff(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" align="center" prop="realQty" width="110">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.realQty" :min="0" size="mini" controls-position="right" @change="calcDiff(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="盈亏数量" align="center" width="100">
          <template slot-scope="scope">
            <span :style="{ color: diffColor(scope.row) }">{{ scope.row.diffQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="盈亏原因" align="center" width="160">
          <template slot-scope="scope">
            <el-input v-model="scope.row.diffReason" size="mini" placeholder="请输入" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="80">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-delete" @click="removeItemRow(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCheck, getCheck, delCheck, addCheck, updateCheck, auditCheck, listBookItems } from "@/api/system/check"
import { optionselectInfo } from "@/api/system/info"

export default {
  name: "MedStockCheck",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存盘点表格数据
      checkList: [],
      // 药品下拉选项
      medOptions: [],
      // 日期范围
      dateRange: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        checkNo: null,
        checkName: null,
        checkType: null,
        checkStatus: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        checkName: [
          { required: true, message: "盘点名称不能为空", trigger: "blur" }
        ],
        checkType: [
          { required: true, message: "盘点类型不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getMedOptions()
  },
  methods: {
    /** 查询库存盘点列表 */
    getList() {
      this.loading = true
      listCheck(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.checkList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询药品下拉选项 */
    getMedOptions() {
      optionselectInfo().then(response => {
        this.medOptions = response.data
      })
    },
    /** 盘点类型格式化 */
    formatCheckType(value) {
      if (value === "1") return "抽盘"
      if (value === "2") return "重点盘点"
      return "全盘"
    },
    /** 盘点状态格式化 */
    formatCheckStatus(value) {
      if (value === "1") return "盘点中"
      if (value === "2") return "已盘点"
      if (value === "3") return "已审核"
      return "待盘点"
    },
    /** 盘点状态标签颜色 */
    checkStatusTag(value) {
      if (value === "3") return "success"
      if (value === "2") return "warning"
      if (value === "1") return "primary"
      return "info"
    },
    /** 盈亏数量颜色 */
    diffColor(row) {
      if (row.diffQty > 0) return '#67C23A'
      if (row.diffQty < 0) return '#F56C6C'
      return ''
    },
    /** 计算盈亏数量与盈亏类型 */
    calcDiff(row) {
      const bookQty = row.bookQty == null ? 0 : row.bookQty
      const realQty = row.realQty == null ? 0 : row.realQty
      row.diffQty = realQty - bookQty
      row.diffType = row.diffQty > 0 ? "1" : (row.diffQty < 0 ? "2" : "0")
    },
    /** 选择药品后带出规格与单位 */
    onMedChange(row) {
      const med = this.medOptions.find(item => item.medId === row.medId)
      if (med) {
        row.medName = med.medName
        row.medSpec = med.medSpec
        row.unit = med.unit
      }
    },
    /** 带出库存明细（按批次生成账面数量） */
    loadBookItems() {
      listBookItems({ categoryId: this.form.categoryId, medId: this.form.medId }).then(response => {
        const list = response.data || []
        if (!list.length) {
          this.$modal.msgWarning("没有查询到可盘点的批次库存数据")
          return
        }
        this.form.medStockCheckItemList = list.map(item => {
          item.realQty = item.bookQty
          item.diffQty = 0
          item.diffType = "0"
          return item
        })
      })
    },
    /** 添加明细行 */
    addItemRow() {
      if (!this.form.medStockCheckItemList) {
        this.$set(this.form, 'medStockCheckItemList', [])
      }
      this.form.medStockCheckItemList.push({
        medId: null,
        medName: null,
        batchNo: null,
        bookQty: 0,
        realQty: 0,
        diffQty: 0,
        diffType: "0",
        diffReason: null
      })
    },
    /** 删除明细行 */
    removeItemRow(index) {
      this.form.medStockCheckItemList.splice(index, 1)
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        checkId: null,
        checkNo: null,
        checkName: null,
        checkType: "0",
        checkStatus: "0",
        checkDate: null,
        checkUser: null,
        remark: null,
        medStockCheckItemList: []
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
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.form.checkDate = this.parseTime(new Date(), '{y}-{m}-{d}')
      this.open = true
      this.title = "新增库存盘点"
    },
    /** 修改按钮操作（录入实盘数量） */
    handleUpdate(row) {
      this.reset()
      getCheck(row.checkId).then(response => {
        this.form = response.data
        if (!this.form.medStockCheckItemList) {
          this.$set(this.form, 'medStockCheckItemList', [])
        }
        this.open = true
        this.title = "录入盘点数据"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.medStockCheckItemList) {
            this.form.medStockCheckItemList.forEach(item => this.calcDiff(item))
          }
          if (this.form.checkId != null) {
            updateCheck(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCheck(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 审核按钮操作（按盈亏数量调整库存） */
    handleAudit(row) {
      this.$modal.confirm('是否确认审核盘点单"' + row.checkNo + '"？审核后将按盈亏数量调整库存。').then(function() {
        return auditCheck(row.checkId)
      }).then(response => {
        this.getList()
        this.$modal.msgSuccess(response.msg || "审核成功")
      }).catch(() => {})
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除盘点单"' + row.checkNo + '"？').then(function() {
        return delCheck(row.checkId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/check/export', {
        ...this.queryParams
      }, `medcheck_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
