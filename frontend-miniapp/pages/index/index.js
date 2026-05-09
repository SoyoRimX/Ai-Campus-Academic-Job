// pages/index/index.js
const app = getApp()

Page({
  data: {
    greeting: '',
    warnings: [],
    jobs: []
  },

  onLoad() {
    // Greeting based on time of day
    const hour = new Date().getHours()
    let greeting = '早上好'
    if (hour >= 12 && hour < 18) {
      greeting = '下午好'
    } else if (hour >= 18 || hour < 6) {
      greeting = '晚上好'
    }
    this.setData({ greeting })

    // Token check — no strict redirect for now
    if (!app.globalData.token) {
      const stored = wx.getStorageSync('token')
      if (stored) {
        app.globalData.token = stored
      }
    }

    // Load mock warnings
    this.setData({
      warnings: [
        { id: 1, title: '高等数学', desc: '当前成绩低于预警线', tag: '危险', tagType: 'red' },
        { id: 2, title: '英语四级', desc: '模拟测试未达标', tag: '警告', tagType: 'orange' },
        { id: 3, title: '学分进度', desc: '本学期学分不足预期', tag: '提醒', tagType: 'blue' }
      ],
      jobs: [
        { id: 1, title: '前端开发实习生', company: '字节跳动', location: '北京', salary: '20-30K' },
        { id: 2, title: 'Java开发工程师', company: '阿里巴巴', location: '杭州', salary: '18-25K' },
        { id: 3, title: '数据分析师', company: '腾讯', location: '深圳', salary: '15-22K' }
      ]
    })
  },

  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (url) {
      wx.navigateTo({ url })
    }
  }
})
