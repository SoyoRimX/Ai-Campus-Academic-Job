// pages/academic/index.js
const app = getApp()

Page({
  data: {
    studentInfo: {
      studentNo: '',
      major: '',
      grade: '',
      gpa: 0,
      totalCredits: 0,
      requiredCredits: 0,
      failCount: 0
    },
    creditPercent: 0,
    warnings: []
  },

  onLoad() {
    const userId = (app.globalData.userInfo && app.globalData.userInfo.userId) || 1
    this.fetchStudentInfo(userId)
    this.fetchWarnings(userId)
  },

  fetchStudentInfo(userId) {
    app.request(`/academic/student/${userId}`, 'GET')
      .then(res => {
        const data = res.data || res
        this.updateStudentInfo(data)
      })
      .catch(() => {
        // Use mock data on failure
        this.updateStudentInfo({
          studentNo: '2024001',
          major: '计算机科学与技术',
          grade: '2024级',
          gpa: 3.6,
          totalCredits: 48.5,
          requiredCredits: 60,
          failCount: 0
        })

        // Mock warnings
        this.setData({
          warnings: []
        })
      })
  },

  fetchWarnings(userId) {
    app.request(`/academic/warnings/${userId}`, 'GET')
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
        // Silently fail, keep empty warnings
      })
  },

  updateStudentInfo(data) {
    const percent = data.requiredCredits > 0
      ? Math.min(100, (data.totalCredits / data.requiredCredits * 100)).toFixed(1)
      : 0

    this.setData({
      studentInfo: {
        studentNo: data.studentNo || '',
        major: data.major || '',
        grade: data.grade || '',
        gpa: data.gpa || 0,
        totalCredits: data.totalCredits || 0,
        requiredCredits: data.requiredCredits || 0,
        failCount: data.failCount || 0
      },
      creditPercent: percent
    })
  },

  getTagType(level) {
    if (level === 'danger' || level === 'high') return 'red'
    if (level === 'warning' || level === 'medium') return 'orange'
    return 'blue'
  },

  getTagText(level) {
    if (level === 'danger' || level === 'high') return '严重'
    if (level === 'warning' || level === 'medium') return '警告'
    return '提醒'
  }
})
