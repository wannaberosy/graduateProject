<template>
  <div class="analysis-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="6" v-for="(stat, index) in statisticsData" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ formatValue(stat.value) }}</div>
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-icon">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-trend" :class="{'up': stat.trend > 0, 'down': stat.trend < 0}">
              {{ formatTrend(stat.trend) }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 资产增长趋势图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header">
            <span>资产调用趋势</span>
            <el-radio-group v-model="trendTimeRange" size="small" @change="loadTrendData">
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 资产使用热度图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header">
            <span>资产使用频率分析</span>
            <el-select v-model="selectedType" size="small" @change="loadHeatmapData" style="margin-left: 10px;">
              <el-option label="全部类型" value=""></el-option>
              <el-option
                v-for="type in assetTypes"
                :key="type.type"
                :label="type.typeName"
                :value="type.type"
              ></el-option>
            </el-select>
          </div>
          <div ref="heatmapChart" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导出按钮 -->
    <el-row class="action-row">
      <el-col :span="24" style="text-align: right">
        <el-button type="primary" @click="exportReport">
          导出分析报告
          <i class="el-icon-download"></i>
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'DataAnalysis',
  data() {
    return {
      statisticsData: [
        { title: '总资产数', value: 0, icon: 'el-icon-document', trend: 0 },
        { title: '本月新增', value: 0, icon: 'el-icon-plus', trend: 0 },
        { title: '活跃资产', value: 0, icon: 'el-icon-star-on', trend: 0 },
        { title: '使用人数', value: 0, icon: 'el-icon-user', trend: 0 }
      ],
      trendTimeRange: 'month',
      trendChart: null,
      selectedType: '',
      assetTypes: [],
      heatmapChart: null,
      chartsInitialized: false
    }
  },
  mounted() {
    this.initCharts()
    this.loadData()
    this.loadAssetTypes()
  },
  methods: {
    initCharts() {
      this.$nextTick(() => {
        if (this.$refs.trendChart) {
          this.trendChart = echarts.init(this.$refs.trendChart)
        }
        if (this.$refs.heatmapChart) {
          this.heatmapChart = echarts.init(this.$refs.heatmapChart)
        }
        this.chartsInitialized = true
        window.addEventListener('resize', this.handleResize)
        
        this.loadHeatmapData()
      })
    },

    handleResize() {
      if (this.trendChart) {
        this.trendChart.resize()
      }
      if (this.heatmapChart) {
        this.heatmapChart.resize()
      }
    },

    async loadData() {
      await this.loadStatistics()
      await this.loadTrendData()
    },

    formatValue(value) {
      return value.toLocaleString()
    },

    formatTrend(trend) {
      if (trend === 0) return '持平'
      return `${Math.abs(trend)}% ${trend > 0 ? '增长' : '下降'}`
    },

    async loadStatistics() {
      try {
        const res = await this.$axios.get(this.$httpUrl+'/analysis/statistics')
        if (res.data.code === 200 && res.data.data) {
          const data = res.data.data
          this.statisticsData = [
            {
              title: '总资产数',
              value: data.totalAssets || 0,
              icon: 'el-icon-document',
              trend: data.assetGrowth || 0
            },
            {
              title: '本月新增',
              value: data.newAssets || 0,
              icon: 'el-icon-plus',
              trend: data.assetGrowth || 0
            },
            {
              title: '活跃资产',
              value: data.activeAssets || 0,
              icon: 'el-icon-star-on',
              trend: 0
            },
            {
              title: '使用人数',
              value: data.activeUsers || 0,
              icon: 'el-icon-user',
              trend: data.userGrowth || 0
            }
          ]
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.$message.error('加载统计数据失败')
      }
    },

    async loadTrendData() {
      try {
        const res = await this.$axios.get(this.$httpUrl+'/analysis/trend', {
          params: { timeRange: this.trendTimeRange }
        })
        if (res.data.code === 200 && res.data.data) {
          const { dates, values } = res.data.data
          const option = {
            tooltip: {
              trigger: 'axis',
              formatter: '{b}<br/>{a}: {c}'
            },
            xAxis: {
              type: 'category',
              data: dates,
              boundaryGap: false
            },
            yAxis: {
              type: 'value',
              name: '资产调用次数'
            },
            series: [{
              name: '资产调用次数',
              type: 'line',
              smooth: true,
              data: values,
              areaStyle: {
                opacity: 0.3
              }
            }]
          }
          this.trendChart.setOption(option)
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        this.$message.error('加载趋势数据失败')
      }
    },

    async loadAssetTypes() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/analysis/type-distribution')
        if (res.data.code === 200) {
          this.assetTypes = res.data.data
        }
      } catch (error) {
        console.error('加载资产类型失败:', error)
        this.$message.error('加载资产类型失败')
      }
    },

    async loadHeatmapData() {
      if (!this.chartsInitialized || !this.heatmapChart) {
        console.warn('热力图尚未初始化，等待初始化完成...')
        return
      }

      try {
        const res = await this.$axios.get(this.$httpUrl + '/analysis/heatmap', {
          params: { goodsType: this.selectedType }
        })
        
        if (res.data.code === 200) {
          const data = res.data.data
          
          if (!data || data.length === 0) {
            this.$message.warning('暂无使用频率数据')
            return
          }

          const dates = [...new Set(data.map(item => item.date))].sort()
          const goodsNames = [...new Set(data.map(item => item.goodsName))]
          const heatmapData = data.map(item => [
            item.date,
            item.goodsName,
            item.useCount
          ])
          
          const maxCount = Math.max(...data.map(item => item.useCount))
          
          const option = {
            tooltip: {
              position: 'top',
              formatter: function (params) {
                return `${params.value[0]}<br/>${params.value[1]}: ${params.value[2]}次`
              }
            },
            grid: {
              height: '60%',
              top: '10%',
              right: '10%'
            },
            xAxis: {
              type: 'category',
              data: dates,
              splitArea: {
                show: true
              },
              axisLabel: {
                rotate: 45
              }
            },
            yAxis: {
              type: 'category',
              data: goodsNames,
              splitArea: {
                show: true
              }
            },
            visualMap: {
              min: 0,
              max: maxCount,
              calculable: true,
              orient: 'horizontal',
              left: 'center',
              bottom: '5%',
              color: ['#d94e5d', '#eac736', '#50a3ba']
            },
            series: [{
              name: '使用频率',
              type: 'heatmap',
              data: heatmapData,
              label: {
                show: true,
                formatter: function(params) {
                  return params.value[2] > 0 ? params.value[2] : '';
                }
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }]
          }
          
          this.heatmapChart.setOption(option)
        }
      } catch (error) {
        console.error('加载热度图数据失败:', error)
        this.$message.error('加载热度图数据失败')
      }
    },

    async exportReport() {
      try {
        const response = await this.$axios.get(this.$httpUrl + '/analysis/export', {
          responseType: 'blob'
        })
        
        const blob = new Blob([response.data], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `资产分析报告_${new Date().toLocaleDateString()}.xlsx`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出报告失败:', error)
        this.$message.error('导出报告失败')
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) {
      this.trendChart.dispose()
    }
    if (this.heatmapChart) {
      this.heatmapChart.dispose()
    }
  }
}
</script>

<style scoped>
.analysis-container {
  padding: 20px;
}

.chart-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  text-align: center;
  position: relative;
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-title {
  color: #909399;
  font-size: 14px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 24px;
  color: #409EFF;
}

.stat-trend {
  margin-top: 10px;
  font-size: 12px;
}

.stat-trend.up {
  color: #67C23A;
}

.stat-trend.down {
  color: #F56C6C;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-card >>> .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.action-row {
  margin-top: 20px;
  padding: 0 20px;
}

/* 添加响应式样式 */
@media screen and (max-width: 768px) {
  .chart-card >>> .el-card__header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .chart-card >>> .el-select {
    margin-top: 10px;
  }
}
</style>





