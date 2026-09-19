<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="药品编码" prop="medCode">
        <el-input
          v-model="queryParams.medCode"
          placeholder="请输入药品编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="药品通用名" prop="medName">
        <el-input
          v-model="queryParams.medName"
          placeholder="请输入药品通用名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品名" prop="tradeName">
        <el-input
          v-model="queryParams.tradeName"
          placeholder="请输入商品名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <!-- 分类：改为级联选择，值仍为 categoryId -->
      <el-form-item label="药品分类" prop="categoryId">
        <el-cascader
          v-model="queryParams.categoryId"
          :options="categoryOptions"
          :props="cascaderProps"
          placeholder="请选择药品分类"
          clearable
          filterable
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="默认供应商ID" prop="supplierId">
        <el-input
          v-model="queryParams.supplierId"
          placeholder="请输入默认供应商ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="规格" prop="medSpec">
        <el-input
          v-model="queryParams.medSpec"
          placeholder="请输入规格"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="剂型" prop="dosageForm">
        <el-input
          v-model="queryParams.dosageForm"
          placeholder="请输入剂型"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="计量单位" prop="unit">
        <el-input
          v-model="queryParams.unit"
          placeholder="请输入计量单位"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
          v-model="queryParams.manufacturer"
          placeholder="请输入生产厂家"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="批准文号" prop="approvalNo">
        <el-input
          v-model="queryParams.approvalNo"
          placeholder="请输入批准文号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="存储条件" prop="storageCond">
        <el-input
          v-model="queryParams.storageCond"
          placeholder="请输入存储条件"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="参考进价" prop="purchasePrice">
        <el-input
          v-model="queryParams.purchasePrice"
          placeholder="请输入参考进价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="零售价" prop="salePrice">
        <el-input
          v-model="queryParams.salePrice"
          placeholder="请输入零售价"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="库存下限" prop="stockMin">
        <el-input
          v-model="queryParams.stockMin"
          placeholder="请输入库存下限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="库存上限" prop="stockMax">
        <el-input
          v-model="queryParams.stockMax"
          placeholder="请输入库存上限"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="临期预警天数" prop="warnDays">
        <el-input
          v-model="queryParams.warnDays"
          placeholder="请输入临期预警天数"
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
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:info:add']"
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
          v-hasPermi="['system:info:edit']"
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
          v-hasPermi="['system:info:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:info:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="infoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="药品ID" align="center" prop="medId" />
      <el-table-column label="药品编码" align="center" prop="medCode" />
      <el-table-column label="药品通用名" align="center" prop="medName" />
      <el-table-column label="商品名" align="center" prop="tradeName" />
      <!-- 分类：显示分类名称而不是ID -->
      <el-table-column label="药品分类" align="center" prop="categoryId">
        <template slot-scope="scope">
          <span>{{ getCategoryName(scope.row.categoryId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="默认供应商ID" align="center" prop="supplierId" />
      <el-table-column label="规格" align="center" prop="medSpec" />
      <el-table-column label="剂型" align="center" prop="dosageForm" />
      <el-table-column label="计量单位" align="center" prop="unit" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column label="批准文号" align="center" prop="approvalNo" />
      <el-table-column label="存储条件" align="center" prop="storageCond" />
      <el-table-column label="参考进价" align="center" prop="purchasePrice" />
      <el-table-column label="零售价" align="center" prop="salePrice" />
      <el-table-column label="库存下限" align="center" prop="stockMin" />
      <el-table-column label="库存上限" align="center" prop="stockMax" />
      <el-table-column label="临期预警天数" align="center" prop="warnDays" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:info:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:info:remove']"
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

    <!-- 添加或修改药品信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="药品编码" prop="medCode">
              <el-input v-model="form.medCode" placeholder="请输入药品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="药品通用名" prop="medName">
              <el-input v-model="form.medName" placeholder="请输入药品通用名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品名" prop="tradeName">
              <el-input v-model="form.tradeName" placeholder="请输入商品名" />
            </el-form-item>
          </el-col>
          <!-- 分类：改为级联选择，最终提交的 form.categoryId 仍是分类ID -->
          <el-col :span="24">
            <el-form-item label="药品分类" prop="categoryId">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryOptions"
                :props="cascaderProps"
                placeholder="请选择药品分类"
                clearable
                filterable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="默认供应商ID" prop="supplierId">
              <el-input v-model="form.supplierId" placeholder="请输入默认供应商ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规格" prop="medSpec">
              <el-input v-model="form.medSpec" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="剂型" prop="dosageForm">
              <el-input v-model="form.dosageForm" placeholder="请输入剂型" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="请输入计量单位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="批准文号" prop="approvalNo">
              <el-input v-model="form.approvalNo" placeholder="请输入批准文号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="存储条件" prop="storageCond">
              <el-input v-model="form.storageCond" placeholder="请输入存储条件" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="参考进价" prop="purchasePrice">
              <el-input v-model="form.purchasePrice" placeholder="请输入参考进价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="零售价" prop="salePrice">
              <el-input v-model="form.salePrice" placeholder="请输入零售价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="库存下限" prop="stockMin">
              <el-input v-model="form.stockMin" placeholder="请输入库存下限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="库存上限" prop="stockMax">
              <el-input v-model="form.stockMax" placeholder="请输入库存上限" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="临期预警天数" prop="warnDays">
              <el-input v-model="form.warnDays" placeholder="请输入临期预警天数" />
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
import { listInfo, getInfo, delInfo, addInfo, updateInfo } from "@/api/system/info"
import { listCategory } from "@/api/system/category"

export default {
  name: "Info",
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
      // 药品信息表格数据
      infoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 分类级联选择数据（树形结构）
      categoryOptions: [],
      // 分类 id -> name 映射，用于表格展示
      categoryMap: {},
      // 级联选择器配置：可选中任意层级节点，v-model 只绑定 categoryId
      cascaderProps: {
        value: 'categoryId',
        label: 'categoryName',
        children: 'children',
        checkStrictly: true,
        emitPath: false
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        medCode: null,
        medName: null,
        tradeName: null,
        categoryId: null,
        supplierId: null,
        medSpec: null,
        dosageForm: null,
        unit: null,
        manufacturer: null,
        approvalNo: null,
        storageCond: null,
        purchasePrice: null,
        salePrice: null,
        stockMin: null,
        stockMax: null,
        warnDays: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        medCode: [
          { required: true, message: "药品编码不能为空", trigger: "blur" }
        ],
        medName: [
          { required: true, message: "药品通用名不能为空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "药品分类不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getCategoryOptions()
    this.getList()
  },
  methods: {
    /** 查询药品分类数据（组装成树形 + 建立 id->name 映射） */
    getCategoryOptions() {
      // 一次拿全量，避免分页漏数据
      listCategory({ pageNum: 1, pageSize: 1000 }).then(response => {
        const rows = response.rows || []
        // 若后端已返回树结构则直接用，否则前端组装
        const alreadyTree = rows.some(r => Array.isArray(r.children) && r.children.length > 0)
        this.categoryOptions = alreadyTree ? rows : this.buildCategoryTree(rows)
        // 建立 id 到 name 的映射（用于表格列展示）
        const map = {}
        rows.forEach(item => {
          map[item.categoryId] = item.categoryName
        })
        this.categoryMap = map
      })
    },
    /**
     * 将平铺的分类列表组装为树
     */
    buildCategoryTree(list) {
      const map = {}
      const roots = []
      list.forEach(item => {
        map[item.categoryId] = { ...item, children: [] }
      })
      list.forEach(item => {
        const node = map[item.categoryId]
        const parent = map[item.parentId]
        if (parent && item.parentId !== item.categoryId) {
          parent.children.push(node)
        } else {
          roots.push(node)
        }
      })
      const clean = nodes => {
        nodes.forEach(n => {
          if (n.children && n.children.length > 0) {
            clean(n.children)
          } else {
            delete n.children
          }
        })
      }
      clean(roots)
      return roots
    },
    /** 根据分类ID获取分类名称 */
    getCategoryName(categoryId) {
      if (categoryId == null || categoryId === '') return ''
      return this.categoryMap[categoryId] || categoryId
    },
    /** 查询药品信息列表 */
    getList() {
      this.loading = true
      listInfo(this.queryParams).then(response => {
        this.infoList = response.rows
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
        medId: null,
        medCode: null,
        medName: null,
        tradeName: null,
        categoryId: null,
        supplierId: null,
        medSpec: null,
        dosageForm: null,
        unit: null,
        manufacturer: null,
        approvalNo: null,
        storageCond: null,
        purchasePrice: null,
        salePrice: null,
        stockMin: null,
        stockMax: null,
        warnDays: null,
        status: null,
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
      this.ids = selection.map(item => item.medId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加药品信息"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const medId = row.medId || this.ids
      getInfo(medId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改药品信息"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // form.categoryId 就是级联选择器返回的分类ID，无需额外转换
          if (this.form.medId != null) {
            updateInfo(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addInfo(this.form).then(response => {
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
      const medIds = row.medId || this.ids
      this.$modal.confirm('是否确认删除药品信息编号为"' + medIds + '"的数据项？').then(function() {
        return delInfo(medIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/info/export', {
        ...this.queryParams
      }, `info_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>