<template>
  <div class="health-view">
    <header class="main-header">
      <nav class="navbar navbar-expand-lg">
        <div class="container">
          <router-link to="/" class="navbar-brand">
            <i class="bi bi-heart-pulse"></i> 智慧养老
          </router-link>
          <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
              <li class="nav-item">
                <router-link to="/" class="nav-link">
                  <i class="bi bi-house"></i> 首页
                </router-link>
              </li>
              <li class="nav-item">
                <router-link to="/health" class="nav-link active">
                  <i class="bi bi-heart-pulse"></i> 健康守护
                </router-link>
              </li>
              <li class="nav-item">
                <router-link to="/anti-fraud" class="nav-link">
                  <i class="bi bi-shield-plus"></i> 数字反诈
                </router-link>
              </li>
              <li class="nav-item">
                <router-link to="/classroom" class="nav-link">
                  <i class="bi bi-mortarboard"></i> 银发课堂
                </router-link>
              </li>
              <li class="nav-item">
                <router-link to="/profile" class="nav-link">
                  <i class="bi bi-person-circle"></i> 个人中心
                </router-link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </header>

    <div id="health" class="page-section active">
      <div class="container">
        <div class="page-hero">
          <div>
            <span class="page-eyebrow">DAILY WELLNESS</span>
            <h2 class="page-title">健康守护</h2>
            <p class="page-sub">记录每日血压、血糖与睡眠，形成长期趋势，让家人和医生都安心。</p>
          </div>
        </div>

        <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
          <el-tab-pane label="健康打卡" name="checkin">
            <el-card class="checkin-card">
              <template #header>
                <div class="card-header">
                  <span><i class="bi bi-clipboard2-plus"></i> 今日健康打卡</span>
                </div>
              </template>

              <el-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" label-width="120px" size="large">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="血压 (mmHg)" prop="bloodPressure">
                      <el-input v-model="checkinForm.bloodPressure" placeholder="如: 120/80">
                        <template #append>mmHg</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="血糖 (mmol/L)" prop="bloodSugar">
                      <el-input v-model="checkinForm.bloodSugar" placeholder="如: 5.6" type="number">
                        <template #append>mmol/L</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="睡眠时长" prop="sleepHours">
                      <el-input v-model="checkinForm.sleepHours" placeholder="如: 7.5" type="number">
                        <template #append>小时</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="心率" prop="heartRate">
                      <el-input v-model="checkinForm.heartRate" placeholder="如: 72" type="number">
                        <template #append>次/分</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="今日感受" prop="feeling">
                  <el-radio-group v-model="checkinForm.feeling">
                    <el-radio-button label="good">精神良好</el-radio-button>
                    <el-radio-button label="normal">一般</el-radio-button>
                    <el-radio-button label="tired">有些疲惫</el-radio-button>
                    <el-radio-button label="unwell">身体不适</el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="备注">
                  <el-input v-model="checkinForm.note" type="textarea" :rows="3" placeholder="其他需要记录的信息..." />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" size="large" @click="submitCheckin" :loading="submitting" style="width: 100%;">
                    <i class="bi bi-check-circle"></i> 提交打卡
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="历史记录" name="records">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span><i class="bi bi-clock-history"></i> 健康历史记录</span>
                  <el-button type="primary" @click="exportRecords" size="small">
                    <i class="bi bi-download"></i> 导出数据
                  </el-button>
                </div>
              </template>

              <el-table :data="healthRecords" stripe style="width: 100%" v-loading="loading">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="time" label="时间" width="100" />
                <el-table-column label="血压" width="120">
                  <template #default="scope">
                    {{ scope.row.bloodPressure }} mmHg
                  </template>
                </el-table-column>
                <el-table-column label="血糖" width="100">
                  <template #default="scope">
                    {{ scope.row.bloodSugar }} mmol/L
                  </template>
                </el-table-column>
                <el-table-column label="睡眠" width="100">
                  <template #default="scope">
                    {{ scope.row.sleepHours }} 小时
                  </template>
                </el-table-column>
                <el-table-column label="心率" width="100">
                  <template #default="scope">
                    {{ scope.row.heartRate }} 次/分
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getFeelingType(scope.row.feeling)">
                      {{ getFeelingText(scope.row.feeling) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="note" label="备注" show-overflow-tooltip />
              </el-table>

              <el-empty v-if="healthRecords.length === 0 && !loading" description="暂无打卡记录" />
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="健康资讯" name="tips">
            <div class="tips-hero">
              <div class="tips-hero-icon">
                <i class="bi bi-lightbulb"></i>
              </div>
              <div class="tips-hero-text">
                <h3>健康小知识</h3>
                <p>每天一点小改变，守护长辈好身体</p>
              </div>
            </div>

            <div class="tips-grid">
              <div v-for="(tip, index) in healthTips" :key="tip.title" class="tip-card" :class="`tip-card-${index}`">
                <div class="tip-card-head">
                  <span class="tip-icon"><i :class="tip.icon"></i></span>
                  <span class="tip-title">{{ tip.title }}</span>
                  <span class="tip-count">{{ tip.items.length }} 条</span>
                </div>
                <ul class="tip-list">
                  <li v-for="item in tip.items" :key="item">
                    <i class="bi bi-check-circle-fill"></i>
                    <span>{{ item }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHealthRecords, saveHealthRecords } from '../utils/healthStore'

const route = useRoute()
const activeTab = ref(route.query.tab || 'checkin')
const loading = ref(false)
const submitting = ref(false)
const checkinFormRef = ref(null)

// 健康小知识（按卡片展示）
const healthTips = [
  {
    title: '晨起养生三宜',
    icon: 'bi bi-sunrise',
    items: [
      '宜慢起：醒来先躺1分钟再坐起，坐起1分钟再站立',
      '宜喝温水：补充夜间流失的水分，稀释血液',
      '宜伸展：轻柔活动四肢，促进血液循环'
    ]
  },
  {
    title: '每日饮水指南',
    icon: 'bi bi-droplet-half',
    items: [
      '晨起一杯水：约200ml，帮助排便',
      '午前一杯水：补充工作消耗',
      '午后一杯水：提神醒脑',
      '睡前少量水：不宜过多，以免影响睡眠'
    ]
  },
  {
    title: '健康饮食建议',
    icon: 'bi bi-basket',
    items: [
      '少油少盐：每日盐摄入不超过6克',
      '粗细搭配：多吃全谷物和杂粮',
      '水果蔬菜：每天500克以上',
      '定时定量：规律用餐，避免暴饮暴食'
    ]
  },
  {
    title: '适度运动建议',
    icon: 'bi bi-bicycle',
    items: [
      '散步：每天30分钟，分2-3次完成',
      '太极拳：每周3-5次，每次30分钟',
      '广场舞：适量即可，注意休息',
      '量力而行：根据身体状况调整强度'
    ]
  }
]

const checkinForm = reactive({
  bloodPressure: '',
  bloodSugar: '',
  sleepHours: '',
  heartRate: '',
  feeling: 'good',
  note: ''
})

const checkinRules = {
  bloodPressure: [
    { required: true, message: '请输入血压值', trigger: 'blur' }
  ],
  bloodSugar: [
    { required: true, message: '请输入血糖值', trigger: 'blur' }
  ],
  sleepHours: [
    { required: true, message: '请输入睡眠时长', trigger: 'blur' }
  ],
  heartRate: [
    { required: true, message: '请输入心率', trigger: 'blur' }
  ],
  feeling: [
    { required: true, message: '请选择今日感受', trigger: 'change' }
  ]
}

const healthRecords = ref([])

const getFeelingType = (feeling) => {
  const types = { good: 'success', normal: 'info', tired: 'warning', unwell: 'danger' }
  return types[feeling] || 'info'
}

const getFeelingText = (feeling) => {
  const texts = { good: '精神良好', normal: '一般', tired: '有些疲惫', unwell: '身体不适' }
  return texts[feeling] || '未知'
}

const submitCheckin = async () => {
  if (!checkinFormRef.value) return

  await checkinFormRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true

      const data = {
        date: new Date().toLocaleDateString('zh-CN'),
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        bloodPressure: checkinForm.bloodPressure,
        bloodSugar: checkinForm.bloodSugar,
        sleepHours: checkinForm.sleepHours,
        heartRate: checkinForm.heartRate,
        feeling: checkinForm.feeling,
        note: checkinForm.note
      }

      let records = getHealthRecords()
      records.unshift(data)
      saveHealthRecords(records)

      setTimeout(() => {
        submitting.value = false
        ElMessage.success('健康打卡成功！')
        loadRecords()
        resetForm()
        activeTab.value = 'records'
      }, 1000)
    }
  })
}

const resetForm = () => {
  checkinForm.bloodPressure = ''
  checkinForm.bloodSugar = ''
  checkinForm.sleepHours = ''
  checkinForm.heartRate = ''
  checkinForm.feeling = 'good'
  checkinForm.note = ''
}

const loadRecords = () => {
  loading.value = true
  healthRecords.value = getHealthRecords()
  loading.value = false
}

const exportRecords = () => {
  const records = getHealthRecords()
  if (records.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const csvContent = 'data:text/csv;charset=utf-8,\uFEFF'
  const headers = ['日期', '时间', '血压', '血糖', '睡眠时长', '心率', '状态', '备注']
  const rows = records.map(r => [
    r.date,
    r.time,
    r.bloodPressure,
    r.bloodSugar,
    r.sleepHours,
    r.heartRate,
    getFeelingText(r.feeling),
    r.note || ''
  ])

  const csv = [headers, ...rows].map(e => e.join(',')).join('\n')
  const link = document.createElement('a')
  link.href = encodeURI(csvContent + csv)
  link.download = `健康记录_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.csv`
  link.click()

  ElMessage.success('数据导出成功')
}

const handleTabChange = (tabName) => {
  if (tabName === 'records') {
    loadRecords()
  }
}

onMounted(() => {
  loadRecords()
  if (route.query.tab) activeTab.value = route.query.tab
})

watch(() => route.query.tab, (val) => {
  if (val) activeTab.value = val
})
</script>

<style scoped>
.health-view {
  min-height: 100vh;
  background-color: var(--c-bg);
}

.checkin-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* ===== 健康资讯 ===== */
.tips-hero {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-5) var(--sp-5);
  border-radius: var(--r-md);
  background: var(--c-accent);
  color: #fbfaf6;
  box-shadow: var(--sh-1);
  margin-bottom: var(--sp-5);
}

.tips-hero-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: var(--r-sm);
  background: rgba(255, 255, 255, 0.14);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.tips-hero-text h3 {
  margin: 0 0 4px;
  font-size: 1.25rem;
  font-weight: 600;
}

.tips-hero-text p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .tips-grid {
    grid-template-columns: 1fr;
  }
}

.tip-card {
  background: var(--c-surface);
  border-radius: var(--r-md);
  padding: var(--sp-4) var(--sp-5) var(--sp-3);
  border-top: 3px solid var(--c-accent);
  box-shadow: var(--sh-1);
  transition: transform var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}

.tip-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 26px rgba(0, 0, 0, 0.12);
}

.tip-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.tip-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: var(--r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  color: #fbfaf6;
  background: var(--c-accent);
}

.tip-title {
  flex: 1;
  font-size: var(--fs-16);
  line-height: var(--lh-16);
  font-weight: 500;
  color: var(--c-text);
}

.tip-count {
  font-size: var(--fs-12);
  line-height: var(--lh-12);
  color: var(--c-text-2);
  background: var(--c-surface-2);
  padding: 2px var(--sp-3);
  border-radius: var(--r-pill);
}

.tip-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.tip-list li {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-2);
  padding: var(--sp-2) 0;
  color: var(--c-text-2);
  font-size: var(--fs-12);
  line-height: var(--lh-12);
  border-bottom: 1px dashed var(--c-border);
}

.tip-list li:last-child {
  border-bottom: none;
}

.tip-list li i {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 0.9rem;
  color: var(--c-accent);
}

/* 四张卡片各自的主题色（统一取自 livery 调色台） */
.tip-card-0 { border-top-color: var(--c-gold); }
.tip-card-0 .tip-icon { background: var(--c-gold); }
.tip-card-0 .tip-count {
  color: var(--c-gold);
  background: color-mix(in srgb, var(--c-gold) 12%, var(--c-surface));
}
.tip-card-0 .tip-list li i { color: var(--c-gold); }

.tip-card-2 { border-top-color: var(--c-success); }
.tip-card-2 .tip-icon { background: var(--c-success); }
.tip-card-2 .tip-count {
  color: var(--c-success);
  background: color-mix(in srgb, var(--c-success) 14%, var(--c-surface));
}
.tip-card-2 .tip-list li i { color: var(--c-success); }

.tip-card-3 { border-top-color: var(--c-text-3); }
.tip-card-3 .tip-icon { background: var(--c-text-3); }
.tip-card-3 .tip-count {
  color: var(--c-text-2);
  background: var(--c-surface-2);
}
.tip-card-3 .tip-list li i { color: var(--c-text-3); }

:deep(.el-radio-button__inner) {
  padding: var(--sp-3) var(--sp-5);
}

:deep(.el-tabs__content) {
  padding: var(--sp-5) 0;
}
</style>
