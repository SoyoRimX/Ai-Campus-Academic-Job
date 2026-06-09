var app = getApp()

Page({
  data: { job: {} },

  onLoad(options) {
    var id = options.id
    if (!id) return
    wx.showLoading({ title: '加载中' })
    app.request('/employ/job/' + id, 'GET')
      .then(function (res) {
        wx.hideLoading()
        if (res.data) this.setData({ job: res.data })
        else this.showFallback(id)
      }.bind(this))
      .catch(function () {
        wx.hideLoading()
        this.showFallback(id)
      }.bind(this))
  },

  showFallback(id) {
    this.setData({
      job: {
        id: id,
        jobTitle: '岗位 #' + id,
        company: '加载中',
        salaryRange: '面议',
        city: '—',
        education: '—',
        experience: '—',
        skills: [],
        description: '详情加载失败，请返回重试'
      }
    })
  },

  onApply() {
    wx.showToast({ title: '投递成功', icon: 'success' })
  }
})
