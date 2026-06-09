const app = getApp()

Page({
  data: {
    isLoading: false
  },

  /**
   * 微信快捷登录
   * 1. 调用 wx.login 获取 code
   * 2. 将 code 发给后端
   * 3. 已绑定 → 拿到正式 token，进入首页
   * 4. 未绑定 → 拿到临时 token，跳转绑定页
   */
  handleWxLogin() {
    if (this.data.isLoading) return
    this.setData({ isLoading: true })

    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          this.setData({ isLoading: false })
          return
        }

        // 将 code 发给后端
        wx.request({
          url: app.globalData.baseUrl + '/auth/wx-login',
          method: 'POST',
          data: { code: res.code },
          header: { 'Content-Type': 'application/json' },
          success: (apiRes) => {
            this.setData({ isLoading: false })
            const body = apiRes.data

            if (body.code !== 200) {
              wx.showToast({ title: body.message || '登录失败', icon: 'none' })
              return
            }

            const data = body.data
            if (data.bound) {
              // 已绑定学号 → 保存正式 token，跳转首页
              app.globalData.token = data.token
              wx.setStorageSync('token', data.token)
              app.fetchUserInfo()
              wx.switchTab({ url: '/pages/index/index' })
            } else {
              // 未绑定学号 → 携带临时 token 跳转绑定页
              wx.redirectTo({
                url: '/pages/bind/index?tempToken=' + data.tempToken
              })
            }
          },
          fail: () => {
            this.setData({ isLoading: false })
            wx.showToast({ title: '网络错误', icon: 'none' })
          }
        })
      },
      fail: () => {
        this.setData({ isLoading: false })
        wx.showToast({ title: '微信授权失败', icon: 'none' })
      }
    })
  },

  /** 跳过登录，进入首页（游客模式） */
  handleSkip() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
