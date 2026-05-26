/*
 * ============================================
 *  AI校园学业就业助手 — 岗位推荐页逻辑
 *  TabBar「岗位」入口
 * ============================================
 */

Page({
  data: {
    jobs: []
  },

  onLoad() {
    this.loadJobs()
  },

  loadJobs() {
    this.setData({
      jobs: [
        {
          id: 1, title: '前端开发实习生',
          company: '字节跳动', location: '北京',
          salary: '200-300/天', date: '05-22'
        },
        {
          id: 2, title: 'Java开发工程师',
          company: '阿里巴巴', location: '杭州',
          salary: '18-25K', date: '05-21'
        },
        {
          id: 3, title: '数据分析师',
          company: '腾讯', location: '深圳',
          salary: '15-22K', date: '05-20'
        },
        {
          id: 4, title: '产品经理实习生',
          company: '美团', location: '上海',
          salary: '180-280/天', date: '05-19'
        }
      ]
    })
  },

  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return

    /* tabBar 页面使用 switchTab，详情页使用 navigateTo */
    const tabPages = ['/pages/jobs-tab/', '/pages/study/', '/pages/index/', '/pages/resume/', '/pages/profile/']
    const isTab = tabPages.some(path => url.indexOf(path) === 0)
    if (isTab) {
      wx.switchTab({ url })
    } else {
      wx.navigateTo({ url })
    }
  }
})
