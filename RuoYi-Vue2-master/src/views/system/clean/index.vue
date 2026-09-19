<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="清理单号" prop="cleanNo">
        <el-input
          v-model="queryParams.cleanNo"
          placeholder="请输入清理单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
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
      <el-form-item label="生产批号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入生产批号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="有效期至" prop="expireDate">
        <el-date-picker clearable
          v-model="queryParams.expireDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择有效期至">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="清理数量" prop="cleanQty">
        <el-input
          v-model="queryParams.cleanQty"
          placeholder="请输入清理数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="清理原因" prop="cleanReason">
        <el-input
          v-model="queryParams.cleanReason"
          placeholder="请输入清理原因"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人" prop="cleanUser">
        <el-input
          v-model="queryParams.cleanUser"
          placeholder="请输入申请人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请时间" prop="cleanTime">
        <el-date-picker clearable
          v-model="queryParams.cleanTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择申请时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="确认人" prop="auditBy">
        <el-input
          v-model="queryParams.auditBy"
          placeholder="请输入确认人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="确认时间" prop="auditTime">
        <el-date-picker clearable
          v-model="queryParams.auditTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择确认时间">
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
          v-hasPermi="['system:clean:add']"
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
          v-hasPermi="['system:clean:edit']"
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
          v-hasPermi="['system:clean:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:clean:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cleanList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="清理ID" align="center" prop="cleanId" />
      <el-table-column label="清理单号" align="center" prop="cleanNo" />
      <el-table-column label="药品ID" align="center" prop="medId" />
      <el-table-column label="批次ID" align="center" prop="batchId" />
      <el-table-column label="生产批号" align="center" prop="batchNo" />
      <el-table-column label="有效期至" align="center" prop="expireDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expireDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="清理数量" align="center" prop="cleanQty" />
      <el-table-column label="清理方式" align="center" prop="cleanType" />
      <el-table-column label="清理原因" align="center" prop="cleanReason" />
      <el-table-column label="清理状态" align="center" prop="cleanStatus" />
      <el-table-column label="申请人" align="center" prop="cleanUser" />
      <el-table-column label="申请时间" align="center" prop="cleanTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.cleanTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="确认人" align="center" prop="auditBy" />
      <el-table-column label="确认时间" align="center" prop="auditTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.auditTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:clean:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:clean:remove']"
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
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="清理单号" prop="cleanNo">
              <el-input v-model="form.cleanNo" placeholder="请输入清理单号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品ID" prop="medId">
              <el-input v-model="form.medId" placeholder="请输入药品ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="批次ID" prop="batchId">
              <el-input v-model="form.batchId" placeholder="请输入批次ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生产批号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入生产批号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="有效期至" prop="expireDate">
              <el-date-picker clearable
                v-model="form.expireDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择有效期至">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="清理数量" prop="cleanQty">
              <el-input v-model="form.cleanQty" placeholder="请输入清理数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="清理原因" prop="cleanReason">
              <el-input v-model="form.cleanReason" placeholder="请输入清理原因" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="申请人" prop="cleanUser">
              <el-input v-model="form.cleanUser" placeholder="请输入申请人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="申请时间" prop="cleanTime">
              <el-date-picker clearable
                v-model="form.cleanTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择申请时间">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="确认人" prop="auditBy">
              <el-input v-model="form.auditBy" placeholder="请输入确认人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="确认时间" prop="auditTime">
              <el-date-picker clearable
                v-model="form.auditTime"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择确认时间">
              </el-date-picker>
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
import { listClean, getClean, delClean, addClean, updateClean } from "@/api/system/clean"

export default {
  name: "Clean",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 过期药品清理表格数据
      cleanList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        cleanNo: null,
        medId: null,
        batchId: null,
        batchNo: null,
        expireDate: null,
        cleanQty: null,
        cleanType: null,
        cleanReason: null,
        cleanStatus: null,
        cleanUser: null,
        cleanTime: null,
        auditBy: null,
        auditTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        cleanNo: [
          { required: true, message: "清理单号不能为空", trigger: "blur" }
        ],
        medId: [
          { required: true, message: "药品ID不能为空", trigger: "blur" }
        ],
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
      listClean(this.queryParams).then(response => {
        this.cleanList = response.rows
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
        cleanId: null,
        cleanNo: null,
        medId: null,
        batchId: null,
        batchNo: null,
        expireDate: null,
        cleanQty: null,
        cleanType: null,
        cleanReason: null,
        cleanStatus: null,
        cleanUser: null,
        cleanTime: null,
        auditBy: null,
        auditTime: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.cleanId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加过期药品清理"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const cleanId = row.cleanId || this.ids
      getClean(cleanId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改过期药品清理"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.cleanId != null) {
            updateClean(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addClean(this.form).then(response => {
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
      const cleanIds = row.cleanId || this.ids
      this.$modal.confirm('是否确认删除过期药品清理编号为"' + cleanIds + '"的数据项？').then(function() {
        return delClean(cleanIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/clean/export', {
        ...this.queryParams
      }, `clean_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
