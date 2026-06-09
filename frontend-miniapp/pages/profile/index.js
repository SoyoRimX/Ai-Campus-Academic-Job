var app = getApp()

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    userInitial: 'U',
    roleLabel: '学生',
    resumeStatus: '—',
    interviewCount: 0,
    warningCount: 0,
    jobCount: 0
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    var token = app.globalData.token
    var info = app.globalData.userInfo

    if (token && info) {
      this.setData({
        isLoggedIn: true,
        userInfo: info,
        userInitial: (info.realName || info.username || 'U').charAt(0).toUpperCase(),
        roleLabel: this.getRoleLabel(info.userType)
      })
      this.loadCounts()
    } else if (token) {
      app.fetchUserInfo()
      // 等待 userInfo 返回
      this.waitForUserInfo()
    } else {
      this.setData({ isLoggedIn: false, userInfo: {} })
    }
  },

  waitForUserInfo() {
    var that = this
    var tries = 0
    var timer = setInterval(function () {
      tries++
      if (app.globalData.userInfo) {
        clearInterval(timer)
        that.refresh()
      } else if (tries > 15) {
        clearInterval(timer)
        that.setData({ isLoggedIn: false })
      }
    }, 300)
  },

  getRoleLabel(userType) {
    if (userType === 2) return '管理员'
    if (userType === 1) return '教师'
    return '学生'
  },

  /* ---- 数据统计 ---- */
  loadCounts() {
    // 简历状态
    app.request('/employ/resume/' + (app.globalData.userInfo.studentId || 1), 'GET')
      .then(function (res) { if (res.data) this.setData({ resumeStatus: '已创建' }) }.bind(this))
      .catch(function () { this.setData({ resumeStatus: '未创建' }) }.bind(this))

    // 面试记录数
    app.request('/employ/interviews', 'GET', { page: 1, size: 1 })
      .then(function (res) { this.setData({ interviewCount: res.data && res.data.total || 0 }) }.bind(this))
      .catch(function () {})

    // 学业预警数
    app.request('/academic/warnings', 'GET', { page: 1, size: 1 })
      .then(function (res) { this.setData({ warningCount: res.data && res.data.total || 0 }) }.bind(this))
      .catch(function () {})

    // 岗位数
    app.request('/employ/jobs', 'GET', { page: 1, size: 1 })
      .then(function (res) { this.setData({ jobCount: res.data && res.data.total || 0 }) }.bind(this))
      .catch(function () {})
  },

  /* ---- 导航 ---- */
  goLogin() {
    wx.redirectTo({ url: '/pages/login/index' })
  },

  goPage(e) {
    app.navigateTo(e.currentTarget.dataset.url)
  },

  /* ---- 联系辅导员 ---- */
  contactCounselor() {
    wx.showModal({
      title: '联系辅导员',
      content: '辅导员热线: 010-12345678\n工作时间: 周一至周五 9:00-17:00',
      confirmText: '拨打',
      cancelText: '关闭',
      success: function (res) {
        if (res.confirm) wx.makePhoneCall({ phoneNumber: '01012345678' })
      }
    })
  },

  /* ---- 退出 ---- */
  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '退出后需要重新登录，确定退出吗？',
      success: function (res) {
        if (!res.confirm) return
        app.logout()
        wx.showToast({ title: '已退出', icon: 'none' })
        wx.switchTab({ url: '/pages/index/index' })
      }.bind(this)
    })
  }
})
