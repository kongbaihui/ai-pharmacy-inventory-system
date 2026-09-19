<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="药品ID" prop="medId">
        <el-input
          v-model="queryParams.medId"
          placeholder="请输入药品ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="批次ID" prop="batchId">
        <el-input
          v-model="queryParams.batchId"
          placeholder="请输入批次ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="变动数量" prop="changeQty">
        <el-input
          v-model="queryParams.changeQty"
          placeholder="请输入变动数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="变动前库存" prop="beforeQty">
        <el-input
          v-model="queryParams.beforeQty"
          placeholder="请输入变动前库存"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="变动后库存" prop="afterQty">
        <el-input
          v-model="queryParams.afterQty"
          placeholder="请输入变动后库存"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务单号" prop="bizNo">
        <el-input
          v-model="queryParams.bizNo"
          placeholder="请输入业务单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务主键" prop="bizId">
        <el-input
          v-model="queryParams.bizId"
          placeholder="请输入业务主键"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人" prop="operator">
        <el-input
          v-model="queryParams.operator"
          placeholder="请输入操作人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="流水时间" prop="flowTime">
        <el-date-picker clearable
          v-model="queryParams.flowTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择流水时间">
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:flow:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="flowList">
      <el-table-column label="流水ID" align="center" prop="flowId" />
      <el-table-column label="药品ID" align="center" prop="medId" />
      <el-table-column label="批次ID" align="center" prop="batchId" />
      <el-table-column label="业务类型" align="center" prop="flowType" />
      <el-table-column label="变动数量" align="center" prop="changeQty" />
      <el-table-column label="变动前库存" align="center" prop="beforeQty" />
      <el-table-column label="变动后库存" align="center" prop="afterQty" />
      <el-table-column label="业务单号" align="center" prop="bizNo" />
      <el-table-column label="业务主键" align="center" prop="bizId" />
      <el-table-column label="操作人" align="center" prop="operator" />
      <el-table-column label="流水时间" align="center" prop="flowTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.flowTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listFlow } from "@/api/system/flow"

export default {
  name: "Flow",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存流水表格数据
      flowList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medId: null,
        batchId: null,
        flowType: null,
        changeQty: null,
        beforeQty: null,
        afterQty: null,
        bizNo: null,
        bizId: null,
        operator: null,
        flowTime: null,
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询库存流水列表 */
    getList() {
      this.loading = true
      listFlow(this.queryParams).then(response => {
        this.flowList = response.rows
        this.total = response.total
        this.loading = false
      })
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
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/flow/export', {
        ...this.queryParams
      }, `flow_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>