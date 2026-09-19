<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb8">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">未处理预警</div>
          <div class="stat-value">{{ summary.unhandleCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">库存不足</div>
          <div class="stat-value color-red">{{ summary.shortageCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">库存积压</div>
          <div class="stat-value color-orange">{{ summary.overstockCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">近效期药品</div>
          <div class="stat-value color-orange">{{ summary.nearExpiryCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">已过期药品</div>
          <div class="stat-value color-red">{{ summary.expiredCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">严重级别</div>
          <div class="stat-value color-red">{{ summary.seriousCount || 0 }}</div>
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
      <el-form-item label="预警类型" prop="warnType">
        <el-select v-model="queryParams.warnType" placeholder="请选择预警类型" clearable>
          <el-option label="库存不足" value="0" />
          <el-option label="库存积压" value="1" />
          <el-option label="近效期" value="2" />
          <el-option label="已过期" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警级别" prop="warnLevel">
        <el-select v-model="queryParams.warnLevel" placeholder="请选择预警级别" clearable>
          <el-option label="提示" value="0" />
          <el-option label="警告" value="1" />
          <el-option label="严重" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="handleStatus">
        <el-select v-model="queryParams.handleStatus" placeholder="请选择处理状态" clearable>
          <el-option label="未处理" value="0" />
          <el-option label="已处理" value="1" />
          <el-option label="已忽略" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="生产批号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入生产批号"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          icon="el-icon-search"
          size="mini"
          @click="handleScan"
          v-hasPermi="['system:warn:scan']"
        >扫描生成预警</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchHandle('1')"
          v-hasPermi="['system:warn:handle']"
        >批量处理</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple"
          @click="handleBatchHandle('2')"
          v-hasPermi="['system:warn:handle']"
        >批量忽略</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:warn:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:warn:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="warnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="药品名称" align="center" prop="medName" :show-overflow-tooltip="true" />
      <el-table-column label="规格" align="center" prop="medSpec" width="130" />
      <el-table-column label="生产批号" align="center" prop="batchNo" width="130" />
      <el-table-column label="预警类型" align="center" prop="warnType" width="110">
        <template slot-scope="scope">
          <el-tag :type="warnTypeTag(scope.row.warnType)">{{ formatWarnType(scope.row.warnType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警级别" align="center" prop="warnLevel" width="100">
        <template slot-scope="scope">
          <el-tag :type="warnLevelTag(scope.row.warnLevel)" effect="plain">{{ formatWarnLevel(scope.row.warnLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前数量" align="center" prop="currentQty" width="100" />
      <el-table-column label="预警阈值" align="center" prop="warnQty" width="100" />
      <el-table-column label="预警内容" align="center" prop="warnContent" :show-overflow-tooltip="true" />
      <el-table-column label="预警时间" align="center" prop="warnTime" width="160" />
      <el-table-column label="处理状态" align="center" prop="handleStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.handleStatus === '0' ? 'danger' : 'success'">
            {{ formatHandleStatus(scope.row.handleStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="handleUser" width="100" />
      <el-table-column label="处理说明" align="center" prop="handleRemark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            :disabled="scope.row.handleStatus !== '0'"
            @click="handleSingle(scope.row)"
            v-hasPermi="['system:warn:handle']"
          >处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:warn:remove']"
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

    <!-- 处理预警对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-form-item label="预警内容" prop="warnContent">
          <el-input v-model="form.warnContent" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="处理结果" prop="handleStatus">
          <el-radio-group v-model="form.handleStatus">
            <el-radio label="1">已处理</el-radio>
            <el-radio label="2">已忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="handleRemark">
          <el-input v-model="form.handleRemark" type="textarea" placeholder="请填写处理说明" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitHandle">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listWarn, delWarn, scanWarn, handleWarn, getWarnSummary } from "@/api/system/warn"

export default {
  name: "MedStockWarn",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存预警表格数据
      warnList: [],
      // 预警统计
      summary: {},
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medName: null,
        warnType: null,
        warnLevel: null,
        handleStatus: null,
        batchNo: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {}
    }
  },
  created() {
    this.getList()
    this.getSummary()
  },
  methods: {
    /** 查询库存预警列表 */
    getList() {
      this.loading = true
      listWarn(this.queryParams).then(response => {
        this.warnList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询预警统计 */
    getSummary() {
      getWarnSummary().then(response => {
        this.summary = response.data || {}
      })
    },
    /** 预警类型格式化 */
    formatWarnType(value) {
      if (value === "0") return "库存不足"
      if (value === "1") return "库存积压"
      if (value === "2") return "近效期"
      return "已过期"
    },
    /** 预警类型标签颜色 */
    warnTypeTag(value) {
      if (value === "0" || value === "3") return "danger"
      if (value === "1" || value === "2") return "warning"
      return "info"
    },
    /** 预警级别格式化 */
    formatWarnLevel(value) {
      if (value === "2") return "严重"
      if (value === "1") return "警告"
      return "提示"
    },
    /** 预警级别标签颜色 */
    warnLevelTag(value) {
      if (value === "2") return "danger"
      if (value === "1") return "warning"
      return "info"
    },
    /** 处理状态格式化 */
    formatHandleStatus(value) {
      if (value === "1") return "已处理"
      if (value === "2") return "已忽略"
      return "未处理"
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
      this.ids = selection.map(item => item.warnId)
      this.multiple = !selection.length
    },
    /** 扫描生成预警 */
    handleScan() {
      scanWarn().then(response => {
        this.$modal.msgSuccess(response.msg || "扫描完成")
        this.getList()
        this.getSummary()
      })
    },
    /** 单条处理 */
    handleSingle(row) {
      this.form = {
        warnId: row.warnId,
        warnContent: row.warnContent,
        handleStatus: "1",
        handleRemark: null
      }
      this.open = true
      this.title = "处理库存预警"
    },
    /** 提交处理 */
    submitHandle() {
      handleWarn(this.form).then(response => {
        this.$modal.msgSuccess("处理成功")
        this.open = false
        this.getList()
        this.getSummary()
      })
    },
    /** 批量处理与忽略 */
    handleBatchHandle(handleStatus) {
      const action = handleStatus === "1" ? "处理" : "忽略"
      this.$modal.confirm('是否确认' + action + '选中的 ' + this.ids.length + ' 条预警记录？').then(() => {
        return handleWarn({ warnIds: this.ids, handleStatus: handleStatus })
      }).then(() => {
        this.getList()
        this.getSummary()
        this.$modal.msgSuccess(action + "成功")
      }).catch(() => {})
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const warnIds = row.warnId || this.ids
      this.$modal.confirm('是否确认删除预警记录编号为"' + warnIds + '"的数据项？').then(function() {
        return delWarn(warnIds)
      }).then(() => {
        this.getList()
        this.getSummary()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/warn/export', {
        ...this.queryParams
      }, `medwarn_${new Date().getTime()}.xlsx`)
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
  font-size: 24px;
  font-weight: bold;
  margin-top: 6px;
}
.color-red {
  color: #f56c6c;
}
.color-orange {
  color: #e6a23c;
}
</style>
