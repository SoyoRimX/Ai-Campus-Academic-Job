const app = getApp()

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    interviewCount: 3,
    warningCount: 1
  },

  onShow() {
    this.checkLoginState()
    this.loadCounts()
  },

  // === 检查登录状态 ===
  checkLoginState() {
    const token = app.globalData.token
    const userInfo = app.globalData.userInfo

    if (token && userInfo) {
      this.setData({
        isLoggedIn: true,
        userInfo: userInfo
      })
    } else if (token) {
      // 有 token 但没有 userInfo，尝试获取
      app.fetchUserInfo()
      this.setData({ isLoggedIn: true })

      // 轮询等待 userInfo 返回
      let retries = 0
      const checkInterval = setInterval(() => {
        retries++
        if (app.globalData.userInfo) {
          clearInterval(checkInterval)
          this.setData({ userInfo: app.globalData.userInfo })
        } else if (retries > 10) {
          clearInterval(checkInterval)
        }
      }, 300)
    } else {
      this.setData({
        isLoggedIn: false,
        userInfo: {}
      })
    }
  },

  // === 加载 badge 数量 ===
  loadCounts() {
    // 硬编码：面试记录 3，学业预警 1
    this.setData({
      interviewCount: 3,
      warningCount: 1
    })
  },

  // === 页面导航 ===
  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (url) {
      wx.navigateTo({ url })
    }
  },

  // === 联系辅导员 ===
  contactCounselor() {
    wx.showModal({
      title: '联系辅导员',
      content: '辅导员热线: 010-12345678\n工作时间: 周一至周五 9:00-17:00',
      showCancel: true,
      cancelText: '关闭',
      confirmText: '拨打',
      success: (res) => {
        if (res.confirm) {
          wx.makePhoneCall({ phoneNumber: '01012345678' })
        }
      }
    })
  },

  // === 微信授权登录 ===
  handleLogin() {
    wx.showLoading({ title: '登录中…' })

    // 模拟登录流程
    setTimeout(() => {
      const mockToken = 'mock_token_' + Date.now()
      const mockUserInfo = {
        name: '张三',
        studentNo: '20210101001',
        major: '计算机科学与技术',
        avatar: ''
      }

      // 存储 token
      wx.setStorageSync('token', mockToken)
      app.globalData.token = mockToken
      app.globalData.userInfo = mockUserInfo

      wx.hideLoading()
      wx.showToast({ title: '登录成功', icon: 'success' })

      this.setData({
        isLoggedIn: true,
        userInfo: mockUserInfo
      })
    }, 800)
  },

  // === 退出登录 ===
  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '退出后需要重新登录，确定退出吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          app.globalData.token = ''
          app.globalData.userInfo = null

          this.setData({
            isLoggedIn: false,
            userInfo: {}
          })

          wx.showToast({ title: '已退出登录', icon: 'none' })

          // 切换到首页 tab
          wx.switchTab({ url: '/pages/index/index' })
        }
      }
    })
  }
})
