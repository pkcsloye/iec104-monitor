<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="IOA地址" prop="ioa">
        <el-input-number v-model="queryParams.ioa" :min="0" :max="16777215" placeholder="IOA地址" clearable style="width: 200px" controls-position="right" />
      </el-form-item>
      <el-form-item label="量名称" prop="quantityName">
        <el-input v-model="queryParams.quantityName" placeholder="请输入量名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="量类型" prop="quantityType">
        <el-select v-model="queryParams.quantityType" placeholder="量类型" clearable style="width: 200px">
          <el-option label="电压" value="voltage" />
          <el-option label="电流" value="current" />
          <el-option label="有功功率" value="power" />
          <el-option label="无功功率" value="reactive" />
          <el-option label="频率" value="frequency" />
          <el-option label="功率因数" value="pf" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['iec104:mapping:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['iec104:mapping:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="mappingList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="映射ID" align="center" prop="mappingId" width="80" />
      <el-table-column label="IOA地址" align="center" prop="ioa" width="100" />
      <el-table-column label="量名称" align="center" prop="quantityName" width="150" />
      <el-table-column label="量类型" align="center" prop="quantityType" width="100">
        <template #default="scope">
          <el-tag :type="getQuantityTypeTag(scope.row.quantityType)">{{ getQuantityTypeLabel(scope.row.quantityType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="单位" align="center" prop="unit" width="80" />
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iec104:mapping:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iec104:mapping:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="IOA地址" prop="ioa">
          <el-input-number v-model="form.ioa" :min="0" :max="16777215" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="量名称" prop="quantityName">
          <el-input v-model="form.quantityName" placeholder="请输入量名称（如：A相电压）" />
        </el-form-item>
        <el-form-item label="量类型" prop="quantityType">
          <el-select v-model="form.quantityType" placeholder="请选择量类型" style="width: 100%">
            <el-option label="电压" value="voltage" />
            <el-option label="电流" value="current" />
            <el-option label="有功功率" value="power" />
            <el-option label="无功功率" value="reactive" />
            <el-option label="频率" value="frequency" />
            <el-option label="功率因数" value="pf" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位（如：kV、A、kW）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Iec104Mapping">
import { listMapping, getMapping, delMapping, addMapping, updateMapping } from "@/api/iec104/mapping"

const { proxy } = getCurrentInstance()

const mappingList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ioa: undefined, quantityName: undefined, quantityType: undefined },
  rules: {
    ioa: [{ required: true, message: "IOA地址不能为空", trigger: "blur" }],
    quantityName: [{ required: true, message: "量名称不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const quantityTypeMap = {
  voltage: { label: '电压', tag: '' },
  current: { label: '电流', tag: 'success' },
  power: { label: '有功功率', tag: 'warning' },
  reactive: { label: '无功功率', tag: 'danger' },
  frequency: { label: '频率', tag: 'info' },
  pf: { label: '功率因数', tag: '' },
  other: { label: '其他', tag: 'info' }
}

function getQuantityTypeLabel(type) { return quantityTypeMap[type]?.label || type || '-' }
function getQuantityTypeTag(type) { return quantityTypeMap[type]?.tag || 'info' }

function getList() {
  loading.value = true
  listMapping(queryParams.value).then(response => {
    mappingList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }

function reset() {
  form.value = { mappingId: undefined, ioa: undefined, quantityName: undefined, quantityType: undefined, unit: undefined, description: undefined, status: "0" }
  proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.mappingId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() { reset(); open.value = true; title.value = "新增IOA映射" }

function handleUpdate(row) {
  reset()
  const mappingId = row.mappingId || ids.value[0]
  getMapping(mappingId).then(response => { form.value = response.data; open.value = true; title.value = "修改IOA映射" })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.mappingId != undefined) {
        updateMapping(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList() })
      } else {
        addMapping(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList() })
      }
    }
  })
}

function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要' + text + ' IOA "' + row.ioa + '" 的映射吗？').then(function () {
    return updateMapping(row)
  }).then(() => { proxy.$modal.msgSuccess(text + "成功") }).catch(function () { row.status = row.status === "0" ? "1" : "0" })
}

function handleDelete(row) {
  const mappingIds = row.mappingId || ids.value
  proxy.$modal.confirm('是否确认删除所选的映射配置？').then(function () {
    return delMapping(mappingIds)
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功") }).catch(() => {})
}

getList()
</script>
