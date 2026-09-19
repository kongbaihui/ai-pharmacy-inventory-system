<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="清理单号" prop="cleanNo">
        <el-input
          v-model="queryParams.cleanNo"
          placeholder="请输入清理单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
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
      <el-form-item label="清理方式" prop="cleanType">
        <el-select v-model="queryParams.cleanType" placeholder="请选择清理方式" clearable>
          <el-option label="退货" value="0" />
          <el-option label="销毁" value="1" />
          <el-option label="报损" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="清理状态" prop="cleanStatus">
        <el-select v-model="queryParams.cleanStatus" placeholder="请选择清理状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已确认" value="1" />
          <el-option label="已驳回" value="2" />
        </el-select>
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
          v-hasPermi="['system:expiredclean:add']"
        >新增清理</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:expiredclean:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cleanList">
      <el-table-column label="清理单号" align="center" prop="cleanNo" width="150" />
      <el-table-column label="药品名称" align="center" prop="medName" :show-overflow-tooltip="true" />
      <el-table-column label="规格" align="center" prop="medSpec" width="130" />
      <el-table-column label="生产批号" align="center" prop="batchNo" width="130" />
      <el-table-column label="有效期至" align="center" prop="expireDate" width="110" />
      <el-table-column label="清理数量" align="center" prop="cleanQty" width="100" />
      <el-table-column label="清理方式" align="center" prop="cleanType" width="100">
        <template slot-scope="scope">
          <span>{{ formatCleanType(scope.row.cleanType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="清理原因" align="center" prop="cleanReason" :show-overflow-tooltip="true" />
      <el-table-column label="清理状态" align="center" prop="cleanStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="cleanStatusTag(scope.row.cleanStatus)">{{ formatCleanStatus(scope.row.cleanStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请人" align="center" prop="cleanUser" width="100" />
      <el-table-column label="确认人" align="center" prop="auditBy" width="100" />
      <el-table-column label="确认时间" align="center" prop="auditTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            :disabled="scope.row.cleanStatus !== '0'"
            @click="handleConfirm(scope.row)"
            v-hasPermi="['system:expiredclean:confirm']"
          >确认清理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-close"
            :disabled="scope.row.cleanStatus !== '0'"
            @click="handleReject(scope.row)"
            v-hasPermi="['system:expiredclean:confirm']"
          >驳回</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            :disabled="scope.row.cleanStatus !== '0'"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:expiredclean:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            :disabled="scope.row.cleanStatus === '1'"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:expiredclean:remove']"
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

    <!-- 添加或修改过期药品清理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="药品批次" prop="batchId">
              <el-select v-model="form.batchId" filterable placeholder="请选择需要清理的批次" @change="onBatchChange">
                <el-option
                  v-for="item in batchOptions"
                  :key="item.batchId"
                  :label="item.medName + '（批号 ' + item.batchNo + '，剩余 ' + item.remainQty + '，有效期至 ' + item.expireDate + '）'"
                  :value="item.batchId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产批号" prop="batchNo">
              <el-input v-model="form.batchNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期至" prop="expireDate">
              <el-input v-model="form.expireDate" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次剩余数量">
              <el-input v-model="remainQty" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="清理数量" prop="cleanQty">
              <el-input-number v-model="form.cleanQty" :min="1" :max="remainQty" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="清理方式" prop="cleanType">
              <el-select v-model="form.cleanType" placeholder="请选择清理方式">
                <el-option label="退货" value="0" />
                <el-option label="销毁" value="1" />
                <el-option label="报损" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="清理原因" prop="cleanReason">
              <el-input v-model="form.cleanReason" type="textarea" placeholder="请输入清理原因" />
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
import { listExpiredclean, getExpiredclean, delExpiredclean, addExpiredclean, updateExpiredclean, confirmExpiredclean, rejectExpiredclean } from "@/api/system/expiredclean"
import { listBatch } from "@/api/system/batch"

export default {
  name: "MedExpiredClean",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 过期药品清理表格数据
      cleanList: [],
      // 可清理批次选项（临期与过期批次）
      batchOptions: [],
      // 当前批次剩余数量
      remainQty: 0,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        cleanNo: null,
        medName: null,
        batchNo: null,
        cleanType: null,
        cleanStatus: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        batchId: [
          { required: true, message: "请选择需要清理的药品批次", trigger: "change" }
        ],
        cleanQty: [
          { required: true, message: "清理数量不能为空", trigger: "blur" }
        ],
        cleanType: [
          { required: true, message: "清理方式不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询过期药品清理列表 */
    getList() {
      this.loading = true
      listExpiredclean(this.queryParams).then(response => {
        this.cleanList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询可清理的批次（临期与过期批次） */
    getBatchOptions() {
      listBatch({ pageNum: 1, pageSize: 200, expireFlag: "1" }).then(response => {
        this.batchOptions = (response.rows || []).filter(item => item.batchStatus !== '3')
      })
    },
    /** 选择批次后带出批号与有效期 */
    onBatchChange(batchId) {
      const batch = this.batchOptions.find(item => item.batchId === batchId)
      if (batch) {
        this.form.batchNo = batch.batchNo
        this.form.expireDate = batch.expireDate
        this.form.medId = batch.medId
        this.remainQty = batch.remainQty
        this.form.cleanQty = batch.remainQty
      }
    },
    /** 清理方式格式化 */
    formatCleanType(value) {
      if (value === "0") return "退货"
      if (value === "2") return "报损"
      return "销毁"
    },
    /** 清理状态格式化 */
    formatCleanStatus(value) {
      if (value === "1") return "已确认"
      if (value === "2") return "已驳回"
      return "待审核"
    },
    /** 清理状态标签颜色 */
    cleanStatusTag(value) {
      if (value === "1") return "success"
      if (value === "2") return "info"
      return "warning"
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.remainQty = 0
      this.form = {
        cleanId: null,
        cleanNo: null,
        medId: null,
        batchId: null,
        batchNo: null,
        expireDate: null,
        cleanQty: 1,
        cleanType: "1",
        cleanReason: null,
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
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getBatchOptions()
      this.open = true
      this.title = "新增过期药品清理"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      this.getBatchOptions()
      getExpiredclean(row.cleanId).then(response => {
        this.form = response.data
        this.remainQty = response.data.remainQty || response.data.cleanQty
        this.open = true
        this.title = "修改过期药品清理"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.cleanId != null) {
            updateExpiredclean(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addExpiredclean(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 确认清理（扣减库存并登记库存流水） */
    handleConfirm(row) {
      this.$prompt('请输入确认说明', '确认清理', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        return confirmExpiredclean(row.cleanId, value)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("清理已确认，库存已扣减")
      }).catch(() => {})
    },
    /** 驳回清理申请 */
    handleReject(row) {
      this.$prompt('请输入驳回说明', '驳回清理申请', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        return rejectExpiredclean(row.cleanId, value)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已驳回该清理申请")
      }).catch(() => {})
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除清理单"' + row.cleanNo + '"？').then(function() {
        return delExpiredclean(row.cleanId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/expiredclean/export', {
        ...this.queryParams
      }, `expiredclean_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
