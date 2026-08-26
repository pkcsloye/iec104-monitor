<template>
  <div class="app-container">
    <!-- 筛选区域 -->
    <el-card shadow="never" class="mb20">
      <el-form :inline="true" label-width="80px">
        <el-form-item label="日志文件">
          <el-select v-model="fileId" placeholder="请选择日志文件" style="width: 300px" @change="handleFileChange">
            <el-option
              v-for="item in logFileOptions"
              :key="item.fileId"
              :label="item.fileName"
              :value="item.fileId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IOA筛选">
          <el-select
            v-model="selectedIoas"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="全部IOA"
            style="width: 400px"
            @change="loadChartData"
          >
            <el-option
              v-for="item in ioaOptions"
              :key="item.ioa"
              :label="item.label"
              :value="item.ioa"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="loadChartData">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图表区域 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>测量值趋势图</span>
          <span style="color: #999; font-size: 12px; margin-left: 10px;">
            {{ chartInfo }}
          </span>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 500px;" v-loading="chartLoading"></div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="mt20" v-if="seriesData.length > 0">
      <template #header>
        <span>数据明细</span>
      </template>
      <el-table :data="tableData" max-height="400" border>
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="IOA" align="center" prop="ioa" width="80" />
        <el-table-column label="量名称" align="center" prop="name" width="150" />
        <el-table-column label="单位" align="center" prop="unit" width="80" />
        <el-table-column label="数据点数" align="center" prop="count" width="100" />
        <el-table-column label="最小值" align="center" width="120">
          <template #default="scope">
            {{ scope.row.min != null ? scope.row.min.toFixed(4) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="最大值" align="center" width="120">
          <template #default="scope">
            {{ scope.row.max != null ? scope.row.max.toFixed(4) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="平均值" align="center" width="120">
          <template #default="scope">
            {{ scope.row.avg != null ? scope.row.avg.toFixed(4) : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="Iec104Chart">
import { listLogFile, getIoaList, getChartData } from "@/api/iec104/log"
import { listAllMapping } from "@/api/iec104/mapping"
import * as echarts from 'echarts'
import { useRoute } from 'vue-router'

const route = useRoute()
const { proxy } = getCurrentInstance()

const chartRef = ref(null)
let chartInstance = null

const logFileOptions = ref([])
const fileId = ref(null)
const ioaOptions = ref([])
const selectedIoas = ref([])
const chartLoading = ref(false)
const chartInfo = ref('')
const seriesData = ref([])
const tableData = ref([])
const mappingMap = ref({}) // IOA -> 映射信息

/** 初始化 */
onMounted(async () => {
  // 初始化 echarts
  chartInstance = echarts.init(chartRef.value)
  window.addEventListener('resize', handleResize)

  // 加载日志文件列表
  await loadLogFileList()

  // 加载IOA映射配置
  await loadMappingConfig()

  // 如果从路由传入了fileId，则自动选中
  const queryFileId = route.query.fileId
  if (queryFileId) {
    fileId.value = Number(queryFileId)
    await handleFileChange()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
  }
})

function handleResize() {
  if (chartInstance) {
    chartInstance.resize()
  }
}

/** 加载日志文件列表 */
async function loadLogFileList() {
  try {
    const res = await listLogFile({ pageNum: 1, pageSize: 100, status: '0' })
    logFileOptions.value = res.rows || []
  } catch (e) {
    console.error('加载日志文件列表失败', e)
  }
}

/** 加载IOA映射配置 */
async function loadMappingConfig() {
  try {
    const res = await listAllMapping()
    const map = {}
    ;(res.data || []).forEach(item => {
      map[item.ioa] = { name: item.quantityName, unit: item.unit, type: item.quantityType }
    })
    mappingMap.value = map
  } catch (e) {
    console.error('加载映射配置失败', e)
  }
}

/** 文件切换 */
async function handleFileChange() {
  if (!fileId.value) return
  // 加载该文件的IOA列表
  try {
    const res = await getIoaList(fileId.value)
    const ioas = res.data || []
    ioaOptions.value = ioas.map(ioa => {
      const mapping = mappingMap.value[ioa]
      const label = mapping ? `${ioa} - ${mapping.name}` : `IOA ${ioa}`
      return { ioa, label }
    })
    selectedIoas.value = [] // 默认全选（空=全部）
    await loadChartData()
  } catch (e) {
    console.error('加载IOA列表失败', e)
  }
}

/** 加载图表数据 */
async function loadChartData() {
  if (!fileId.value) {
    chartInfo.value = '请先选择日志文件'
    return
  }
  chartLoading.value = true
  try {
    const ioas = selectedIoas.value.length > 0 ? selectedIoas.value : null
    const res = await getChartData(fileId.value, ioas)
    const data = res.data || {}
    const series = data.series || []
    const xAxis = data.xAxis || []

    seriesData.value = series
    renderChart(series, xAxis)
    buildTableData(series)
    chartInfo.value = `共 ${series.length} 个测点，${xAxis.length} 个采样点`
  } catch (e) {
    console.error('加载图表数据失败', e)
    chartInfo.value = '加载数据失败'
  } finally {
    chartLoading.value = false
  }
}

/** 渲染 ECharts 折线图 */
function renderChart(series, xAxis) {
  if (!chartInstance) return

  // 颜色方案
  const colors = [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b8d0',
    '#c4a76c', '#d978e8', '#6be6c1', '#626c91', '#a0a7e6'
  ]

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: function(params) {
        let html = `<div style="font-weight:bold;margin-bottom:5px;">${params[0].axisValue}</div>`
        params.forEach(p => {
          html += `<div>${p.marker} ${p.seriesName}: <b>${p.value != null ? p.value.toFixed(4) : '-'}</b></div>`
        })
        return html
      }
    },
    legend: {
      type: 'scroll',
      bottom: 0,
      data: series.map(s => s.name)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    toolbox: {
      feature: {
        dataZoom: { yAxisIndex: 'none' },
        restore: {},
        saveAsImage: {}
      }
    },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', start: 0, end: 100, bottom: 40 }
    ],
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxis,
      axisLabel: {
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      name: '测量值'
    },
    series: series.map((s, i) => ({
      name: s.name,
      type: 'line',
      data: s.values,
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 1.5 },
      itemStyle: { color: colors[i % colors.length] }
    }))
  }

  chartInstance.setOption(option, true)
}

/** 构建统计表格数据 */
function buildTableData(series) {
  tableData.value = series.map(s => {
    const values = (s.values || []).filter(v => v != null)
    const count = values.length
    const min = count > 0 ? Math.min(...values) : null
    const max = count > 0 ? Math.max(...values) : null
    const avg = count > 0 ? values.reduce((a, b) => a + b, 0) / count : null
    return {
      ioa: s.ioa,
      name: s.name,
      unit: s.unit || '',
      count,
      min,
      max,
      avg
    }
  })
}
</script>

<style scoped>
.mb20 { margin-bottom: 20px; }
.mt20 { margin-top: 20px; }
.card-header { display: flex; align-items: center; }
</style>
