var app = getApp()

Page({
  data: {
    greeting: '早上好',
    entries: [
      { key: 'resume', icon: '📄', label: '我的简历', url: '/pages/resume/index', bg: '#ECF3F6' },
      { key: 'interview', icon: '🎙', label: 'AI 面试', url: '/pages/interview/index', bg: '#F5ECEC' },
      { key: 'study', icon: '📊', label: '学业数据', url: '/pages/study/study', bg: '#F7F1E8' },
      { key: 'jobs', icon: '💼', label: '岗位市场', url: '/pages/jobs-tab/jobs-tab', bg: '#ECF2EB' }
    ],
    jobs: [],
    warnings: []
  },

  onLoad() {
    this.setData({ greeting: this.getGreeting() })
  },

  onShow() {
    this.loadJobs()
    this.loadWarnings()
  },

  getGreeting() {
    var h = new Date().getHours()
    if (h < 9) return '早上好'
    if (h < 12) return '上午好'
    if (h < 14) return '中午好'
    if (h < 18) return '下午好'
    return '晚上好'
  },

  /* ---- 数据加载 ---- */
  loadJobs() {
    app.request('/employ/jobs', 'GET', { page: 1, size: 4 })
      .then(function (res) {
        if (res.data && res.data.records) this.setData({ jobs: res.data.records })
      }.bind(this))
      .catch(function () {
        // 降级到空状态
      })
  },

  loadWarnings() {
    app.request('/academic/warnings', 'GET', { page: 1, size: 5 })
      .then(function (res) {
        if (res.data && res.data.records) {
          this.setData({ warnings: res.data.records.slice(0, 5) })
        }
      }.bind(this))
      .catch(function () {
        // 降级到空状态
      })
  },

  /* ---- 导航 ---- */
  goPage(e) {
    app.navigateTo(e.currentTarget.dataset.url)
  }
})
