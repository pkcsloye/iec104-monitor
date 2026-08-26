<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="解析状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="解析状态" clearable style="width: 240px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-upload
          :show-file-list="false"
          :before-upload="handleUpload"
          accept=".txt,.log"
          style="display: inline-block"
        >
          <el-button type="primary" plain icon="Upload" v-hasPermi="['iec104:log:upload']" :loading="uploading">
            上传解析
          </el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-tooltip content="IOA基地址偏移，0=自动检测（推荐），手动设置如0x4000=16384" placement="top">
          <el-input-number v-model="ioaBaseOffset" :min="0" :max="16777215" placeholder="自动检测" controls-position="right" style="width: 180px" />
        </el-tooltip>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['iec104:log:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logFileList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文件ID" align="center" prop="fileId" width="80" />
      <el-table-column label="文件名称" align="center" prop="fileName" :show-overflow-tooltip="true" />
      <el-table-column label="文件大小" align="center" prop="fileSize" width="100">
        <template #default="scope">
          <span>{{ formatFileSize(scope.row.fileSize) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总帧数" align="center" prop="totalFrames" width="80" />
      <el-table-column label="数据点数" align="center" prop="totalPoints" width="100" />
      <el-table-column label="解析状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['iec104:log:detail']">详情</el-button>
          <el-button link type="primary" icon="DataLine" @click="handleChart(scope.row)" v-hasPermi="['iec104:log:detail']">图表</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iec104:log:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 数据点详情对话框 -->
    <el-dialog title="数据点详情" v-model="detailOpen" width="900px" append-to-body>
      <el-table v-loading="detailLoading" :data="dataPointList" max-height="500">
        <el-table-column label="帧序号" align="center" prop="frameSeq" width="80" />
        <el-table-column label="帧时间" align="center" prop="frameTime" width="100" />
        <el-table-column label="帧类型" align="center" prop="frameType" width="80" />
        <el-table-column label="IOA" align="center" prop="ioa" width="80" />
        <el-table-column label="量名称" align="center" prop="quantityName" width="120" />
        <el-table-column label="测量值" align="center" prop="rawValue" width="120">
          <template #default="scope">
            <span>{{ scope.row.rawValue != null ? scope.row.rawValue.toFixed(4) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" align="center" prop="quantityUnit" width="80" />
      </el-table>
      <pagination
        v-show="detailTotal > 0"
        :total="detailTotal"
        v-model:page="detailQuery.pageNum"
        v-model:limit="detailQuery.pageSize"
        @pagination="getDetailList"
      />
    </el-dialog>
  </div>
</template>

<script setup name="Iec104Log">
import { listLogFile, uploadLogFile, delLogFile, listDataPoint } from "@/api/iec104/log"
import { useRouter } from 'vue-router'

const router = useRouter()
const { proxy } = getCurrentInstance()

const logFileList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const uploading = ref(false)
const ioaBaseOffset = ref(0)

// 详情相关
const detailOpen = ref(false)
const detailLoading = ref(false)
const dataPointList = ref([])
const detailTotal = ref(0)
const detailQuery = reactive({
  pageNum: 1,
  pageSize: 20,
  fileId: null
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    fileName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

/** 查询日志文件列表 */
function getList() {
  loading.value = true
  listLogFile(queryParams.value).then(response => {
    logFileList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 格式化文件大小 */
function formatFileSize(size) {
  if (size == null || size === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let s = size
  while (s >= 1024 && i < units.length - 1) {
    s /= 1024
    i++
  }
  return s.toFixed(2) + ' ' + units[i]
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.fileId)
  multiple.value = !selection.length
}

/** 上传文件 */
function handleUpload(file) {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  formData.append('ioaBaseOffset', ioaBaseOffset.value || 0)
  uploadLogFile(formData).then(response => {
    const offset = response.data.ioaBaseOffset || response.data.ioa_base_offset || 0
    const offsetMsg = offset > 0 ? `，IOA偏移已自动扣除: 0x${offset.toString(16).toUpperCase()}` : ''
    proxy.$modal.msgSuccess("解析成功，共解析 " + response.data.totalPoints + " 个数据点" + offsetMsg)
    uploading.value = false
    getList()
  }).catch(() => {
    uploading.value = false
  })
  return false // 阻止默认上传行为
}

/** 查看详情 */
function handleDetail(row) {
  detailQuery.fileId = row.fileId
  detailQuery.pageNum = 1
  detailOpen.value = true
  getDetailList()
}

/** 获取数据点详情列表 */
function getDetailList() {
  detailLoading.value = true
  listDataPoint(detailQuery).then(response => {
    dataPointList.value = response.rows
    detailTotal.value = response.total
    detailLoading.value = false
  })
}

/** 查看图表 */
function handleChart(row) {
  router.push({ path: '/iec104/chart', query: { fileId: row.fileId, fileName: row.fileName } })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const fileIds = row.fileId || ids.value
  proxy.$modal.confirm('是否确认删除所选的日志文件？').then(function () {
    return delLogFile(fileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
