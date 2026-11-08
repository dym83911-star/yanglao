<template>
  <div class="classroom-view">
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
                <router-link to="/" class="nav-link"><i class="bi bi-house"></i> 首页</router-link>
              </li>
              <li class="nav-item" v-if="!isManager">
                <router-link to="/health" class="nav-link"><i class="bi bi-heart-pulse"></i> 健康守护</router-link>
              </li>
              <li class="nav-item" v-if="!isManager">
                <router-link to="/anti-fraud" class="nav-link"><i class="bi bi-shield-plus"></i> 数字反诈</router-link>
              </li>
              <li class="nav-item">
                <router-link to="/classroom" class="nav-link active"><i class="bi bi-mortarboard"></i> 银发课堂</router-link>
              </li>
              <li class="nav-item">
                <router-link to="/profile" class="nav-link"><i class="bi bi-person-circle"></i> 个人中心</router-link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </header>

    <div id="classroom" class="page-section active">
      <div class="container">
        <div class="page-hero">
          <div>
            <span class="page-eyebrow">SILVER CLASSROOM</span>
            <h2 class="page-title">银发课堂</h2>
            <p class="page-sub">线上课程随时看，线下课程可预约，学习节奏由你掌握。</p>
          </div>
          <el-button round type="primary" @click="router.push('/my-courses')">
            <i class="bi bi-bookmark"></i> 我的课程
          </el-button>
        </div>

        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="课程列表" name="list">
            <template v-for="group in courseGroups" :key="group.key">
              <div class="course-group" v-if="group.list.length" v-loading="loading">
                <header class="course-group-head">
                  <h6>{{ group.label }}</h6>
                  <span class="course-group-note">{{ group.note }} · {{ group.list.length }} 门</span>
                </header>
                <div class="course-grid">
                  <el-card v-for="course in group.list" :key="course.id" :body-style="{ padding: '0px' }" shadow="hover">
                    <div class="course-cover" :class="{ 'has-cover': course.cover }" :style="{ background: course.cover ? '#ffffff' : course.bgColor }">
                      <img v-if="course.cover" :src="course.cover" :alt="course.name" class="course-cover-img" :style="{ objectFit: course.coverFit, objectPosition: course.coverPos }" />
                      <i v-else :class="course.icon"></i>
                      <span class="course-tag">{{ course.tag }}</span>
                    </div>
                    <div class="course-info">
                      <h5>{{ course.name }}</h5>
                      <p>{{ course.description }}</p>
                      <div class="course-meta">
                        <span><i class="bi bi-people"></i> {{ course.students }}人学习</span>
                      </div>
                      <el-button type="primary" @click="startLearning(course)" style="width: 100%;">
                        <i class="bi bi-play-circle"></i> 开始学习
                      </el-button>
                    </div>
                  </el-card>
                </div>
              </div>
            </template>
          </el-tab-pane>

          <el-tab-pane label="课程预约" name="book">
            <el-card>
              <template #header>
                <span><i class="bi bi-calendar-plus"></i> 线下课程预约</span>
              </template>

              <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="100px" size="large">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="姓名" prop="name">
                      <el-input v-model="bookForm.name" placeholder="请输入姓名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系电话" prop="phone">
                      <el-input v-model="bookForm.phone" placeholder="请输入11位手机号" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="选择课程" prop="course">
                  <el-select v-model="bookForm.course" placeholder="请选择要预约的线下课程" style="width: 100%;">
                    <el-option
                      v-for="c in offlineCourses"
                      :key="c.id"
                      :label="`${c.name}（${c.location || '地点待定'}）`"
                      :value="c.id" />
                  </el-select>
                  <div v-if="offlineCourses.length === 0" style="color: var(--c-text-3); font-size: 13px; margin-top: 6px;">
                    暂无可预约的线下课程
                  </div>
                </el-form-item>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="预约日期" prop="date">
                      <el-date-picker v-model="bookForm.date" type="date" value-format="YYYY-MM-DD"
                        :disabled-date="(d) => d && d.getTime() < Date.now() - 86400000"
                        placeholder="选择日期" style="width: 100%;" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="时间段" prop="time">
                      <el-select v-model="bookForm.time" placeholder="请选择时间段" style="width: 100%;">
                        <el-option label="上午（9:00-11:00）" value="上午（9:00-11:00）" />
                        <el-option label="下午（14:00-16:00）" value="下午（14:00-16:00）" />
                        <el-option label="晚上（19:00-21:00）" value="晚上（19:00-21:00）" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="备注">
                  <el-input v-model="bookForm.note" type="textarea" :rows="2" placeholder="如有特殊需求请在此说明" />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" size="large" @click="submitBooking" :loading="submitting" style="width: 100%;">
                    <i class="bi bi-calendar-check"></i> 确认预约
                  </el-button>
                </el-form-item>
              </el-form>

              <el-divider>我的预约</el-divider>

              <el-table :data="bookings" stripe style="width: 100%" v-loading="loading">
                <el-table-column prop="courseName" label="课程" min-width="140" />
                <el-table-column prop="bookDate" label="日期" width="110" />
                <el-table-column prop="timeSlot" label="时间段" width="140" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="statusTagType(scope.row.status)">
                      {{ statusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="90">
                  <template #default="scope">
                    <el-button v-if="scope.row.status === 'pending' || scope.row.status === 'confirmed'"
                      type="danger" size="small" link @click="cancelOneBooking(scope.row.id)">
                      取消
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="bookings.length === 0 && !loading" description="暂无预约记录" />
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="courseDialogVisible" :title="selectedCourse?.name" width="600px" center>
      <div class="course-detail">
        <p><strong>课程介绍：</strong></p>
        <p>{{ selectedCourse?.description }}</p>
        <p><strong>学习人数：</strong>{{ selectedCourse?.students }}人</p>
      </div>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStartCourse">开始学习</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { isManager as checkManager } from '../utils/roles'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourses as fetchCourses, joinCourse } from '../api/course'
import { createBooking, getBookings as fetchBookings, cancelBooking } from '../api/booking'
import { getToken, getUser } from '../utils/session'
import taiChiCover from '@/assets/course-taichi.png'
import autumnCareCover from '@/assets/course-autumn-care.jpg'
import safeTravelCover from '@/assets/course-safe-travel.jpg'
import hypertensionCover from '@/assets/course-hypertension.jpg'
import phoneBasicsCover from '@/assets/course-phone-basics.jpg'
import mobilePayCover from '@/assets/course-mobile-pay.jpg'

const route = useRoute()
const router = useRouter()
// 医生/管理员不展示健康守护、数字反诈入口
const isManager = checkManager()
const activeTab = ref(route.query.tab || 'list')
const loading = ref(false)
const submitting = ref(false)
const bookFormRef = ref(null)
const courseDialogVisible = ref(false)
const selectedCourse = ref(null)

// 课程卡片样式（按顺序循环套用，兼容后端返回数据没有 icon/bgColor 的情况）
const courseStyles = [
  { icon: 'bi bi-phone', bgColor: 'linear-gradient(135deg, #5c6b55 0%, #8a957e 100%)' },
  { icon: 'bi bi-credit-card-2-front', bgColor: 'linear-gradient(135deg, #8d7a5c 0%, #b7a482 100%)' },
  { icon: 'bi bi-person-arms-up', bgColor: 'linear-gradient(135deg, #4f6354 0%, #7d9180 100%)' },
  { icon: 'bi bi-heart-pulse', bgColor: 'linear-gradient(135deg, #6d7a63 0%, #9aa78d 100%)' },
  { icon: 'bi bi-people', bgColor: 'linear-gradient(135deg, #7a6a56 0%, #a89a82 100%)' }
]

// 兜底课程数据：后端未启动时页面也能正常展示
const fallbackCourses = [
  { id: 1, name: '智能手机基础操作', type: 'ONLINE', description: '从零学习智能手机基本功能，包括打电话、发短信、微信视频。', tag: '热门', students: 128 },
  { id: 2, name: '移动支付安全指南', type: 'ONLINE', description: '教您安全使用微信支付和支付宝，防止上当受骗。', tag: '推荐', students: 96 },
  { id: 3, name: '太极拳入门', type: 'OFFLINE', description: '线下太极拳培训，强身健体、修身养性。', tag: '新上', students: 45 },
  { id: 4, name: '高血压防治知识', type: 'ONLINE', description: '由健康医生讲解高血压的饮食、运动和药物管理。', tag: '推荐', students: 210 },
  { id: 5, name: '老年人安全出行指南', type: 'ONLINE', description: '教您安全乘坐公交地铁、用手机打车，以及出行防走失、防诈骗的实用技巧。', tag: '热门', students: 300 }
]

// 部分课程使用插画封面，其余沿用渐变色块
const courseCovers = [
  { match: '太极拳', cover: taiChiCover, fit: 'contain' },
  { match: '秋季养生', cover: autumnCareCover },
  { match: '安全出行', cover: safeTravelCover },
  { match: '高血压', cover: hypertensionCover },
  { match: '智能手机', cover: phoneBasicsCover, pos: 'center 32%' },
  { match: '移动支付', cover: mobilePayCover, pos: 'center 72%' }
]

// 横图（太极拳）完整不裁切，竖构图用 cover 铺满，可按图微调取景位置
const coverStyleOf = (name) => {
  const hit = courseCovers.find(item => String(name || '').includes(item.match))
  return {
    cover: hit ? hit.cover : null,
    fit: (hit && hit.fit) || 'cover',
    pos: (hit && hit.pos) || 'center 55%'
  }
}

// 补全卡片展示所需字段（icon/bgColor/cover/students），兼容后端返回结构
const decorateCourses = (list) => (list || []).map((c, i) => {
  const style = courseStyles[i % courseStyles.length]
  const cover = coverStyleOf(c.name)
  return {
    ...c,
    icon: c.icon || style.icon,
    bgColor: c.bgColor || style.bgColor,
    cover: c.coverUrl || c.cover || cover.cover,
    coverFit: c.coverFit || cover.fit,
    coverPos: c.coverPos || cover.pos,
    students: c.students ?? c.currentStudents ?? 0
  }
})

// 课程列表（从后端加载，后端不可用时回退到兜底数据）
const courses = ref([])

// 线下课程供预约下拉选择
const offlineCourses = computed(() => courses.value.filter(c => c.type === 'OFFLINE'))

// 课程列表按线上 / 线下分组展示
const courseGroups = computed(() => [
  {
    key: 'online',
    label: '线上课程',
    note: '随时可学，支持手机端观看',
    list: courses.value.filter(c => c.type !== 'OFFLINE')
  },
  {
    key: 'offline',
    label: '线下课程',
    note: '在社区活动中心开课，可预约',
    list: courses.value.filter(c => c.type === 'OFFLINE')
  }
])

const bookings = ref([])

const bookForm = reactive({
  name: '',
  phone: '',
  course: '',
  date: '',
  time: '',
  note: ''
})

const bookRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  course: [{ required: true, message: '请选择课程', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  time: [{ required: true, message: '请选择时间段', trigger: 'change' }]
}

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await fetchCourses()
    let list = []
    if (res.code === 200 && Array.isArray(res.data)) {
      list = res.data
    } else if (res.code === 200 && Array.isArray(res.data?.list)) {
      list = res.data.list
    }
    courses.value = list.length ? decorateCourses(list) : decorateCourses(fallbackCourses)
  } catch (e) {
    courses.value = decorateCourses(fallbackCourses)
  } finally {
    loading.value = false
  }
}

const startLearning = (course) => {
  selectedCourse.value = course
  courseDialogVisible.value = true
}

// 点击课程卡「开始学习」→ 加入课程后跳转到「我的课程」页面
const confirmStartCourse = async () => {
  const course = selectedCourse.value
  if (!course) return

  courseDialogVisible.value = false

  if (!getToken()) {
    ElMessage.warning('请先登录后再学习课程')
    router.push('/profile')
    return
  }

  try {
    const res = await joinCourse(course.id)
    // code 200 = 加入成功；code 400「已加入该课程」也直接进入我的课程
    if (res.code === 200 || (res.message || '').includes('已加入')) {
      ElMessage.success(`已加入课程：${course.name}`)
      router.push('/my-courses')
    } else {
      ElMessage.error(res.message || '加入课程失败')
    }
  } catch (e) {
    ElMessage.error('加入课程失败，请检查网络')
  }
}

const submitBooking = async () => {
  if (!bookFormRef.value) return

  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true

      try {
        // 后端字段：courseId 为数字课程ID，bookDate 为 YYYY-MM-DD，timeSlot 为中文时间段
        const dto = {
          courseId: bookForm.course,
          userName: bookForm.name,
          userPhone: bookForm.phone,
          bookDate: bookForm.date,
          timeSlot: bookForm.time,
          note: bookForm.note
        }

        const res = await createBooking(dto)
        if (res.code === 200) {
          ElMessage.success('预约成功，等待管理员确认！')
          bookFormRef.value.resetFields()
          loadBookings()
        } else {
          ElMessage.error(res.message || '预约失败')
        }
      } catch (e) {
        ElMessage.error('预约失败，请检查网络')
      } finally {
        submitting.value = false
      }
    }
  })
}

const statusText = (s) => ({
  pending: '待确认',
  confirmed: '已确认',
  rejected: '已拒绝',
  cancelled: '已取消'
}[s] || s)

const statusTagType = (s) => ({
  pending: 'warning',
  confirmed: 'success',
  rejected: 'danger',
  cancelled: 'info'
}[s] || 'info')

const cancelOneBooking = async (id) => {
  try {
    const res = await cancelBooking(id)
    if (res.code === 200) {
      ElMessage.success('预约已取消')
      loadBookings()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    ElMessage.error('取消失败，请检查网络')
  }
}

const loadBookings = async () => {
  loading.value = true
  try {
    const res = await fetchBookings({ page: 1, pageSize: 50 })
    if (res.code === 200 && Array.isArray(res.data)) {
      bookings.value = res.data
    } else if (res.code === 200 && Array.isArray(res.data?.list)) {
      bookings.value = res.data.list
    } else {
      bookings.value = []
    }
  } catch (e) {
    bookings.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadCourses()
  // 「课程预约」需要登录，未登录时不发请求，避免 403
  if (getToken()) {
    loadBookings()
    // 已登录时预填姓名和手机号，少输几个字
    try {
      const user = getUser()
      if (user) {
        bookForm.name = user.name || ''
        bookForm.phone = user.phone || ''
      }
    } catch (e) { /* 忽略解析失败 */ }
  }
  if (route.query.tab) activeTab.value = route.query.tab
})

watch(() => route.query.tab, (val) => {
  if (val) activeTab.value = val
})
</script>

<style scoped>
.classroom-view {
  min-height: 100vh;
  background-color: var(--c-bg);
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--sp-5);
}

/* 线上 / 线下分组 */
.course-group + .course-group {
  margin-top: var(--sp-6);
}

.course-group-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--sp-3);
  padding-bottom: var(--sp-2);
  margin-bottom: var(--sp-4);
  border-bottom: 1px solid var(--c-border);
}

.course-group-head h6 {
  margin: 0;
  font-size: var(--fs-16);
  line-height: var(--lh-16);
  font-weight: 500;
  letter-spacing: .06em;
  color: var(--c-text);
}

.course-group-note {
  font-size: var(--fs-12);
  line-height: var(--lh-12);
  letter-spacing: .06em;
  color: var(--c-text-3);
}

.course-cover {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem;
  color: white;
  position: relative;
}

.course-cover i {
  position: relative;
  z-index: 1;
}

.course-cover.has-cover {
  border-bottom: 1px solid var(--c-border);
}

.course-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 55%;
  display: block;
  position: relative;
  z-index: 1;
}

.course-tag {
  position: absolute;
  top: var(--sp-3);
  right: var(--sp-3);
  padding: 2px var(--sp-3);
  background: color-mix(in srgb, var(--c-surface) 88%, transparent);
  border-radius: var(--r-pill);
  font-size: var(--fs-12);
  line-height: var(--lh-12);
  color: var(--c-text);
  z-index: 2;
}

/* 插画封面是白底，给标签补一圈细边框，避免白底白标签看不见 */
.course-cover.has-cover .course-tag {
  border: 1px solid var(--c-border);
}

/* 卡片撑满整行高度，说明文字长短不同时按钮仍对齐在底部 */
.course-grid > .el-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.course-grid > .el-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-info {
  padding: var(--sp-4);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-info .el-button {
  margin-top: auto;
}

.course-info h5 {
  margin-bottom: var(--sp-3);
}

.course-info p {
  color: var(--c-text-2);
  font-size: var(--fs-14);
  margin-bottom: var(--sp-3);
}

.course-meta {
  margin-bottom: var(--sp-4);
  color: var(--c-text-3);
  font-size: var(--fs-12);
}

.course-detail {
  padding: var(--sp-5);
}

.course-detail p {
  margin-bottom: var(--sp-3);
  line-height: 1.8;
}
</style>
