const app = getApp()

Page({
  data: {
    tempToken: '',    // 从登录页传入的临时 token
    studentId: '',    // 学号
    password: '',     // 密码
    isLoading: false
  },

  onLoad(options) {
    // 从 URL 参数获取临时 token
    if (options.tempToken) {
      this.setData({ tempToken: options.tempToken })
    }
  },

  onStudentIdInput(e) {
    this.setData({ studentId: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  /**
   * 提交绑定
   * 将学号 + 密码 + 临时 token 发给后端
   */
  handleBind() {
    const { tempToken, studentId, password } = this.data

    if (!studentId.trim()) {
      wx.showToast({ title: '请输入学号', icon: 'none' })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
    if (!tempToken) {
      wx.showToast({ title: '登录状态已过期，请返回重新登录', icon: 'none' })
      return
    }

    this.setData({ isLoading: true })

    wx.request({
      url: app.globalData.baseUrl + '/auth/bind',
      method: 'POST',
      data: {
        studentId: studentId.trim(),
        password: password
      },
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + tempToken
      },
      success: (res) => {
        this.setData({ isLoading: false })
        const body = res.data

        if (body.code !== 200) {
          wx.showToast({ title: body.message || '绑定失败', icon: 'none' })
          return
        }

        // 绑定成功 → 保存正式 token
        const data = body.data
        app.globalData.token = data.token
        wx.setStorageSync('token', data.token)

        wx.showToast({
          title: '绑定成功',
          icon: 'success',
          duration: 1500,
          success: () => {
            // 跳转首页
            setTimeout(() => {
              wx.switchTab({ url: '/pages/index/index' })
            }, 1500)
          }
        })
      },
      fail: () => {
        this.setData({ isLoading: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  /** 返回登录页 */
  handleBack() {
    wx.redirectTo({ url: '/pages/login/index' })
  }
})
