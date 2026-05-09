const app = getApp()

Page({
  data: {
    jobs: [],
    keyword: '',
    page: 1,
    size: 10,
    total: 0,
    loading: false,
    hasMore: true
  },

  onLoad() {
    this.loadJobs()
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadJobs(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadJobs()
    }
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onClear() {
    this.setData({ keyword: '' })
  },

  onSearch() {
    this.setData({ page: 1, hasMore: true, jobs: [] })
    this.loadJobs()
  },

  loadJobs(isRefresh) {
    if (this.data.loading) return

    this.setData({ loading: true })

    const { page, size, keyword } = this.data
    const params = `page=${page}&size=${size}&keyword=${encodeURIComponent(keyword)}`

    app.request('/employ/jobs?' + params, 'GET')
      .then((res) => {
        wx.stopPullDownRefresh && wx.stopPullDownRefresh()
        const list = res.data || []
        const newJobs = this.data.page === 1 ? list : this.data.jobs.concat(list)
        const total = res.total || list.length
        this.setData({
          jobs: newJobs,
          total,
          page: this.data.page + 1,
          loading: false,
          hasMore: newJobs.length < total
        })
      })
      .catch(() => {
        wx.stopPullDownRefresh && wx.stopPullDownRefresh()
        // Fallback mock data
        const mockJobs = [
          {
            id: 1,
            jobTitle: '前端开发工程师',
            company: '腾讯科技',
            salaryRange: '15K-25K',
            city: '深圳',
            education: '本科',
            experience: '1-3年',
            description: '负责Web前端开发，参与产品需求评审，与后端协作完成功能开发。'
          },
          {
            id: 2,
            jobTitle: 'Java后端开发',
            company: '阿里巴巴',
            salaryRange: '20K-35K',
            city: '杭州',
            education: '本科',
            experience: '3-5年',
            description: '负责后端服务设计与开发，参与系统架构优化，保障系统高可用。'
          },
          {
            id: 3,
            jobTitle: '数据分析师',
            company: '字节跳动',
            salaryRange: '18K-30K',
            city: '北京',
            education: '硕士',
            experience: '应届生',
            description: '负责业务数据分析与报表搭建，为产品决策提供数据支持。'
          }
        ]
        const newJobs = this.data.page === 1 ? mockJobs : this.data.jobs.concat(mockJobs)
        this.setData({
          jobs: newJobs,
          total: mockJobs.length,
          page: this.data.page + 1,
          loading: false,
          hasMore: false
        })
      })
  },

  onTapJob(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: '/pages/jobs/detail?id=' + id
    })
  }
})
