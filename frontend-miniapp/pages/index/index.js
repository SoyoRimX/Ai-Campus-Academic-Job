/*
 * ============================================
 *  AI校园学业就业助手 — 首页逻辑
 *  索引页 | 盐系 · 毛玻璃 · 横滑公告
 * ============================================
 */

Page({
  data: {
    /* ---- 顶部公告轮播 ---- */
    noticeList: [
      { id: 1, text: '加油，少年' },
      { id: 2, text: '努力吧' },
      { id: 3, text: '春季招聘已启动' },
      { id: 4, text: '面试技巧讲座报名中' }
    ],

    /* 当前公告索引（与指示点联动） */
    noticeCurrent: 0,

    /* 控制 swiper 自动轮播开关 */
    noticeAutoPlay: true,

    /* ---- 4个静态功能入口 ---- */
    entryList: [
      { id: 1, icon: '📚', title: '专业情况', url: '/pages/study/study' },
      { id: 2, icon: '📄', title: '我的简历', url: '/pages/resume/index' },
      { id: 3, icon: '💼', title: '就业经验', url: '/pages/jobs/index' },
      { id: 4, icon: '🤖', title: '面试经验', url: '/pages/interview/index' }
    ],

    /* ---- 推荐岗位列表 ---- */
    jobs: [],

    /* ---- 最新预警列表 ---- */
    announcements: []
  },

  /* 恢复自动轮播的定时器引用 */
  _noticeResumeTimer: null,

  /* ===========================================
     生命周期
     =========================================== */

  onLoad() {
    this.loadJobs()
    this.loadAnnouncements()
  },

  onShow() {
    /* 每次回到首页恢复自动轮播 */
    if (this._noticeResumeTimer) {
      clearTimeout(this._noticeResumeTimer)
      this._noticeResumeTimer = null
    }
    this.setData({ noticeAutoPlay: true })
  },

  onHide() {
    /* 离开页面彻底停止轮播，避免后台消耗 */
    this.setData({ noticeAutoPlay: false })
    if (this._noticeResumeTimer) {
      clearTimeout(this._noticeResumeTimer)
      this._noticeResumeTimer = null
    }
  },

  onUnload() {
    if (this._noticeResumeTimer) {
      clearTimeout(this._noticeResumeTimer)
      this._noticeResumeTimer = null
    }
  },

  /* ===========================================
     公告轮播交互
     =========================================== */

  /*
   * swiper 滑动完成回调
   * e.detail.current — 当前 slide 索引
   */
  onNoticeChange(e) {
    this.setData({ noticeCurrent: e.detail.current })
  },

  /*
   * 用户手指触摸公告区域 → 暂停自动轮播
   */
  onNoticeTouchStart() {
    /* 清除尚未触发的恢复定时器 */
    if (this._noticeResumeTimer) {
      clearTimeout(this._noticeResumeTimer)
      this._noticeResumeTimer = null
    }
    this.setData({ noticeAutoPlay: false })
  },

  /*
   * 用户手指离开公告区域 → 3秒后恢复自动轮播
   */
  onNoticeTouchEnd() {
    if (this._noticeResumeTimer) {
      clearTimeout(this._noticeResumeTimer)
    }
    this._noticeResumeTimer = setTimeout(() => {
      this.setData({ noticeAutoPlay: true })
      this._noticeResumeTimer = null
    }, 3000)
  },

  /* ===========================================
     数据加载
     =========================================== */

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

  loadAnnouncements() {
    this.setData({
      announcements: [
        { id: 1, title: '2025届春季校园招聘活动正式开始', date: '05-20' },
        { id: 2, title: '关于举办AI面试技巧培训讲座的通知', date: '05-18' },
        { id: 3, title: '毕业生就业协议网签系统操作指南', date: '05-15' },
        { id: 4, title: '2025届毕业生就业质量报告发布', date: '05-10' }
      ]
    })
  },

  /* ===========================================
     页面导航
     =========================================== */

  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return

    const tabPages = ['/pages/jobs-tab/', '/pages/study/', '/pages/index/', '/pages/resume/', '/pages/profile/']
    const isTab = tabPages.some(function (path) { return url.indexOf(path) === 0 })
    if (isTab) {
      wx.switchTab({ url: url })
    } else {
      wx.navigateTo({ url: url })
    }
  }
})
