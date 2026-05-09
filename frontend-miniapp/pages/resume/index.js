const app = getApp()

Page({
  data: {
    resume: null,
    basicInfo: {},
    parsedContent: {},
    starRating: 0
  },

  onLoad() {
    this.loadResume()
  },

  loadResume() {
    const studentId = (app.globalData.userInfo && app.globalData.userInfo.studentId) || '1'

    const mockResume = {
      title: '前端开发工程师',
      targetJob: '前端开发',
      targetCity: '深圳',
      aiScore: 85,
      content: '{"education":[{"school":"XX大学","major":"计算机科学","period":"2020-2024"}],"internship":[{"company":"XX科技","role":"前端实习生","period":"2023.06-2023.09","desc":"参与React项目开发"}],"skills":["JavaScript","Vue.js","React","TypeScript"],"selfIntro":"热爱前端开发..."}'
    }

    app.request('/employ/resume/' + studentId, 'GET')
      .then((res) => {
        if (res.data) {
          this.setResumeData(res.data)
        } else {
          this.setResumeData(mockResume)
        }
      })
      .catch(() => {
        this.setResumeData(mockResume)
      })
  },

  setResumeData(data) {
    const basicInfo = {
      name: (app.globalData.userInfo && app.globalData.userInfo.name) || '',
      phone: (app.globalData.userInfo && app.globalData.userInfo.phone) || '',
      email: (app.globalData.userInfo && app.globalData.userInfo.email) || ''
    }

    let parsedContent = {}
    if (data.content) {
      try {
        parsedContent = typeof data.content === 'string' ? JSON.parse(data.content) : data.content
      } catch (e) {
        parsedContent = { raw: data.content }
      }
    }

    const starRating = Math.round((data.aiScore || 0) / 20)

    this.setData({
      resume: data,
      basicInfo,
      parsedContent,
      starRating
    })
  },

  onAIOptimize() {
    const studentId = (app.globalData.userInfo && app.globalData.userInfo.studentId) || '1'

    wx.showLoading({ title: 'AI 优化中...' })

    app.request('/employ/resume/generate', 'POST', { studentId })
      .then((res) => {
        wx.hideLoading()
        wx.showToast({ title: '优化成功', icon: 'success' })
        if (res.data) {
          this.setResumeData(res.data)
        }
      })
      .catch(() => {
        wx.hideLoading()
        wx.showToast({ title: '优化失败，请稍后重试', icon: 'none' })
      })
  }
})
