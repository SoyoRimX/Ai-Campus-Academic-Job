/*
 * ============================================================
 *  ACAJ 微信小程序 — 全局入口
 *  认证管理 · 请求封装 · 共享导航 · 全局工具
 * ============================================================
 */

App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    wsUrl: 'ws://localhost:8080/ws',
    token: '',
    userInfo: null,
    role: ''
  },

  /* ============================================================
     Lifecycle
     ============================================================ */
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.fetchUserInfo()
    }
  },

  /* ============================================================
     Auth helpers
     ============================================================ */

  /** 拉取当前用户信息，存入 globalData */
  fetchUserInfo() {
    wx.request({
      url: this.globalData.baseUrl + '/auth/info',
      header: { Authorization: 'Bearer ' + this.globalData.token },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.globalData.userInfo = res.data.data
          this.globalData.role = res.data.data.role || this.getRoleFromType(res.data.data.userType)
        }
      }
    })
  },

  getRoleFromType(userType) {
    if (userType === 2) return 'ROLE_ADMIN'
    if (userType === 1) return 'ROLE_TEACHER'
    return 'ROLE_STUDENT'
  },

  /** 检查登录状态，未登录则跳转登录页 */
  requireLogin() {
    if (!this.globalData.token) {
      wx.redirectTo({ url: '/pages/login/index' })
      return false
    }
    return true
  },

  /** 退出登录 */
  logout() {
    wx.removeStorageSync('token')
    this.globalData.token = ''
    this.globalData.userInfo = null
    this.globalData.role = ''
  },

  /* ============================================================
     HTTP helpers
     ============================================================ */

  /**
   * 通用 request 封装
   * - 自动拼接 baseUrl
   * - 自动携带 Authorization header
   * - 401 自动跳转登录页
   * - 非 200 自动 toast
   */
  request(url, method, data) {
    method = (method || 'GET').toUpperCase()
    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.baseUrl + url,
        method: method,
        data: method === 'GET' ? data : (data || {}),
        header: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.globalData.token
        },
        success: (res) => {
          var body = res.data
          if (body.code === 200) {
            resolve(body)
          } else if (body.code === 401) {
            this.logout()
            wx.redirectTo({ url: '/pages/login/index' })
            reject(body)
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none', duration: 2000 })
            reject(body)
          }
        },
        fail: function () {
          wx.showToast({ title: '网络错误', icon: 'none', duration: 2000 })
          reject({ message: '网络错误' })
        }
      })
    })
  },

  /** 文件上传 */
  uploadFile(url, filePath, formData) {
    formData = formData || {}
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: this.globalData.baseUrl + url,
        filePath: filePath,
        name: 'file',
        formData: formData,
        header: { 'Authorization': 'Bearer ' + this.globalData.token },
        success: function (res) {
          try {
            var data = JSON.parse(res.data)
            if (data.code === 200) resolve(data)
            else reject(data)
          } catch (e) { reject({ message: '解析响应失败' }) }
        },
        fail: function () {
          wx.showToast({ title: '网络错误', icon: 'none', duration: 2000 })
          reject({ message: '网络错误' })
        }
      })
    })
  },

  /* ============================================================
     Navigation helper
     ============================================================ */

  /** 统一导航：自动判断 tabBar 页面 */
  navigateTo(url) {
    var tabPages = [
      '/pages/index/index',
      '/pages/jobs-tab/jobs-tab',
      '/pages/study/study',
      '/pages/resume/index',
      '/pages/profile/index'
    ]
    var isTab = tabPages.some(function (p) { return url.indexOf(p) === 0 })
    if (isTab) {
      wx.switchTab({ url: url })
    } else {
      wx.navigateTo({ url: url })
    }
  }
})
