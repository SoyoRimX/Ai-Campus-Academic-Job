/*
 * ============================================
 *  AI校园学业就业助手 — 学业页逻辑
 *  盐系极简设计风格
 * ============================================
 */

const app = getApp()

Page({
  data: {
    /* 学生信息 */
    studentInfo: {
      name: '张三',
      major: '计算机科学与技术',
      grade: '2024级',
      gpa: 3.6,
      totalCredits: 48.5,
      requiredCredits: 115,
      failCount: 0
    },
    /* 学分完成百分比 */
    creditPercent: 0,
    /* 剩余学分 */
    remainingCredits: 0,
    /* 不及格状态文字 */
    failStatusText: '暂无不通过',
    /* 预警列表 */
    warnings: []
  },

  /* ===== 页面加载 ===== */
  onLoad() {
    this.initPageData()
  },

  /* ===== 页面显示时触发 ===== */
  onShow() {
    /* tabBar 切换时可刷新数据 */
  },

  /* ===== 初始化页面数据 ===== */
  initPageData() {
    const apiUrl = '/academic/student/1'

    /* 尝试从后端获取学生信息 */
    app.request(apiUrl, 'GET')
      .then(res => {
        const data = res.data || res
        this.updateStudentInfo(data)
      })
      .catch(() => {
        /* 后端不可用时使用模拟数据 */
        this.updateStudentInfo({
          name: '张三',
          major: '计算机科学与技术',
          grade: '2024级',
          gpa: 3.6,
          totalCredits: 48.5,
          requiredCredits: 115,
          failCount: 0
        })
        /* 加载模拟预警数据 */
        this.loadMockWarnings()
      })
  },

  /* ===== 更新学生信息 ===== */
  updateStudentInfo(data) {
    const totalCredits = data.totalCredits || 0
    const requiredCredits = data.requiredCredits || 0
    const remainingCredits = Math.max(0, requiredCredits - totalCredits)

    /* 计算百分比，保留一位小数 */
    const percent = requiredCredits > 0
      ? Math.min(100, (totalCredits / requiredCredits * 100)).toFixed(1)
      : 0

    /* 根据不及格科目数生成状态文案 */
    const failCount = data.failCount || 0
    let failStatusText = '暂无不通过'
    if (failCount > 0) {
      failStatusText = failCount + '门待重修'
    }

    /* 先设置基础数据，再通过延迟设置百分比触发进度条动画 */
    this.setData({
      studentInfo: {
        name: data.name || '未设置',
        major: data.major || '未设置',
        grade: data.grade || '',
        gpa: data.gpa || 0,
        totalCredits: totalCredits,
        requiredCredits: requiredCredits,
        failCount: failCount
      },
      remainingCredits: remainingCredits,
      failStatusText: failStatusText,
      creditPercent: 0
    })

    /* 延迟设置百分比，触发 CSS transition 动画（500-800ms 平滑过渡） */
    setTimeout(() => {
      this.setData({
        creditPercent: Number(percent)
      })
    }, 100)
  },

  /* ===== 加载模拟预警数据 ===== */
  loadMockWarnings() {
    this.setData({
      warnings: []
    })
  },

  /* ===== 加载预警数据（后端） ===== */
  fetchWarnings(userId) {
    app.request('/academic/warnings/' + userId, 'GET')
      .then(res => {
        const data = res.data || res
        if (Array.isArray(data) && data.length > 0) {
          this.setData({
            warnings: data.map(item => ({
              id: item.id,
              title: item.courseName || item.title,
              description: item.description || item.reason,
              time: item.createTime || item.time,
              tagType: this.getTagType(item.level),
              tagText: this.getTagText(item.level)
            }))
          })
        }
      })
      .catch(() => {
        /* 静默失败，保持预警列表为空 */
      })
  },

  /* ===== 根据预警级别返回标签样式类型 ===== */
  getTagType(level) {
    if (level === 'danger' || level === 'high') return 'red'
    if (level === 'warning' || level === 'medium') return 'orange'
    return 'blue'
  },

  /* ===== 根据预警级别返回标签文字 ===== */
  getTagText(level) {
    if (level === 'danger' || level === 'high') return '严重'
    if (level === 'warning' || level === 'medium') return '警告'
    return '提醒'
  }
})
