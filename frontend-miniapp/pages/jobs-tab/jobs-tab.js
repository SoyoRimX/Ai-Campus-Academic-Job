var app = getApp()

Page({
  data: {
    keyword: '',
    jobs: [],
    total: 0,
    page: 1,
    hasMore: false
  },

  onLoad() { this.fetchJobs() },

  onShow() {
    if (this.data.jobs.length === 0) this.fetchJobs()
  },

  fetchJobs() {
    var that = this
    app.request('/employ/jobs', 'GET', { page: 1, size: 15, keyword: this.data.keyword || undefined })
      .then(function (res) {
        var records = (res.data && res.data.records) || []
        that.setData({
          jobs: records,
          total: res.data ? res.data.total || records.length : records.length,
          page: 1,
          hasMore: records.length >= 15
        })
      })
  },

  loadMore() {
    var that = this
    var next = this.data.page + 1
    app.request('/employ/jobs', 'GET', { page: next, size: 15, keyword: this.data.keyword || undefined })
      .then(function (res) {
        var records = (res.data && res.data.records) || []
        that.setData({
          jobs: that.data.jobs.concat(records),
          page: next,
          hasMore: records.length >= 15
        })
      })
  },

  onSearchInput(e) { this.setData({ keyword: e.detail.value }) },

  onSearch() { this.fetchJobs() },

  clearSearch() {
    this.setData({ keyword: '' })
    this.fetchJobs()
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/jobs/detail?id=' + e.currentTarget.dataset.id })
  }
})
