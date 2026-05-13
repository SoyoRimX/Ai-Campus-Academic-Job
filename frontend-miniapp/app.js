App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    wsUrl: 'ws://localhost:8080/ws',
    token: '',
    userInfo: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.fetchUserInfo()
    }
  },

  fetchUserInfo() {
    wx.request({
      url: this.globalData.baseUrl + '/auth/info',
      header: { Authorization: `Bearer ${this.globalData.token}` },
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.userInfo = res.data.data
        }
      }
    })
  },

  request(url, method = 'GET', data = {}) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.baseUrl + url,
        method,
        data,
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.globalData.token}`
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            wx.removeStorageSync('token')
            wx.redirectTo({ url: '/pages/index/index' })
            reject(res.data)
          } else {
            wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
            reject(res.data)
          }
        },
        fail: (err) => {
          wx.showToast({ title: '网络错误', icon: 'none' })
          reject(err)
        }
      })
    })
  },

  uploadFile(url, filePath, formData = {}) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: this.globalData.baseUrl + url,
        filePath,
        name: 'file',
        formData,
        header: {
          'Authorization': `Bearer ${this.globalData.token}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data)
            } else if (data.code === 401) {
              wx.removeStorageSync('token')
              wx.redirectTo({ url: '/pages/index/index' })
              reject(data)
            } else {
              wx.showToast({ title: data.message || '上传失败', icon: 'none' })
              reject(data)
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        },
        fail: (err) => {
          wx.showToast({ title: '网络错误', icon: 'none' })
          reject(err)
        }
      })
    })
  }
})
