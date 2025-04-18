<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-number">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <div slot="header">
            <span>出入库趋势</span>
          </div>
          <div class="chart-container">
            <div ref="trendChart" style="height: 300px"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作记录 -->
    <el-row>
      <el-col :span="24">
        <el-card class="activity-card">
          <div slot="header">
            <span>最近操作记录</span>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :type="activity.action === '1' ? 'success' : 'danger'"
              :timestamp="activity.createtime"
            >
              <span class="activity-content">
                {{ activity.username }}
                <span :class="{'in-action': activity.action === '1', 'out-action': activity.action === '2'}">
                  {{ activity.action === '1' ? '入库' : '出库' }}
                </span>
                {{ activity.goodsname }}
                <span class="count-text">
                  {{ Math.abs(activity.count) }}个
                </span>
              </span>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加资产分类饼图 -->
    <el-card class="chart-card">
      <div slot="header">
        <span>数据资产类型分布</span>
      </div>
      <div class="chart-container">
        <div ref="typeChart" style="height: 300px; width: 100%"></div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: [
        { title: '仓库总数', value: 0, icon: 'el-icon-house' },
        { title: '数据资产总数', value: 0, icon: 'el-icon-data-analysis' },
        { title: '今日入库', value: 0, icon: 'el-icon-top' },
        { title: '今日出库', value: 0, icon: 'el-icon-bottom' }
      ],
      trendChart: null,
      recentActivities: [],
      typeChart: null,
      typeDistribution: []
    }
  },
  methods: {
    async loadData() {
      await this.loadOverview()
      await this.loadRecentActivities()
      await this.loadTrend()
    },
    
    async loadOverview() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/dashboard/overview')
        if (res.data.code === 200) {
          const data = res.data.data
          this.stats[0].value = data.storageCount
          this.stats[1].value = data.goodsCount
          this.stats[2].value = data.todayIn
          this.stats[3].value = data.todayOut
        }
      } catch (error) {
        console.error('加载总览数据失败:', error)
      }
    },

    async loadRecentActivities() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/dashboard/recent-activities')
        if (res.data.code === 200) {
          // 处理每条记录
          this.recentActivities = res.data.data.map(item => ({
            ...item,
            // 确保 action 是字符串类型
            action: String(item.action),
            // 确保数量为正数显示
            count: Math.abs(Number(item.count))
          }));
          console.log('处理后的活动记录:', this.recentActivities); // 调试日志
        }
      } catch (error) {
        console.error('加载最近活动失败:', error)
        this.$message.error('加载最近活动失败: ' + error.message)
      }
    },

    async loadTrend() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/dashboard/inout-trend')
        if (res.data.code === 200) {
          const data = res.data.data
          this.initTrendChart(data)
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
      }
    },

    initTrendChart(data) {
      if (!this.trendChart) {
        this.trendChart = echarts.init(this.$refs.trendChart)
      }
      
      this.trendChart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['入库', '出库']
        },
        xAxis: {
          type: 'category',
          data: data.dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '入库',
            type: 'line',
            data: data.in
          },
          {
            name: '出库',
            type: 'line',
            data: data.out
          }
        ]
      })
    },

    handleResize() {
      if (this.trendChart) {
        this.trendChart.resize()
      }
    },

    initTypeChart() {
      this.typeChart = echarts.init(this.$refs.typeChart)
    },

    async loadTypeDistribution() {
      try {
        const res = await this.$axios.get(this.$httpUrl + '/dashboard/goods-type-distribution')
        console.log('资产分类数据:', res.data) // 添加调试日志
        
        if (res.data.code === 200) {
          this.typeDistribution = res.data.data
          this.updateTypeChart()
        } else {
          throw new Error(res.data.msg || '获取资产分类统计失败')
        }
      } catch (error) {
        console.error('加载资产分类统计失败:', error)
        this.$message.error('加载资产分类统计失败: ' + error.message)
      }
    },

    updateTypeChart() {
      if (!this.typeDistribution || this.typeDistribution.length === 0) {
        console.warn('没有资产分类数据')
        return
      }

      const option = {
        title: {
          text: '数据资产类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}个 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: '5%',
          top: 'middle',
          formatter: (name) => name
        },
        series: [
          {
            name: '资产类型',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['40%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}个\n{d}%',
              position: 'outside'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '16',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10
            },
            data: this.typeDistribution.map(item => ({
              name: item.category,
              value: item.count
            }))
          }
        ],
        color: [
          '#5470c6',  // Word类
          '#91cc75',  // PDF类
          '#fac858',  // Excel类
          '#ee6666',  // 图片类
          '#73c0de'   // 其他类型
        ]
      }

      this.typeChart && this.typeChart.setOption(option)
    },

    handleResize1() {
      this.typeChart && this.typeChart.resize()
    }
  },
  
  mounted() {
    this.loadData()
    this.initTypeChart()
    this.loadTypeDistribution()
    // 添加窗口大小变化监听
    window.addEventListener('resize', this.handleResize)
  },
  
  beforeDestroy() {
    if (this.trendChart) {
      this.trendChart.dispose()
    }
    if (this.typeChart) {
      this.typeChart.dispose()
      this.typeChart = null
    }
    // 移除窗口大小变化监听
    window.removeEventListener('resize', this.handleResize)
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
  color: #409EFF;
}

.stat-info {
  flex-grow: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin: 20px;
  height: 450px;  /* 增加高度以适应标题和图例 */
}

.chart-container {
  height: 350px;
  width: 100%;
}

.activity-card {
  height: 400px;
  overflow-y: auto;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.in-action {
  color: #67C23A;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #f0f9eb;
}

.out-action {
  color: #F56C6C;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #fef0f0;
}

.count-text {
  font-weight: bold;
  color: #606266;
}

.el-timeline-item {
  padding-bottom: 20px;
}

.el-timeline-item:last-child {
  padding-bottom: 0;
}

.el-timeline-item__timestamp {
  font-size: 13px;
  color: #909399;
}

/* 添加响应式样式 */
@media screen and (max-width: 768px) {
  .chart-card {
    margin: 10px;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style> 