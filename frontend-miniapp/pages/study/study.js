var app = getApp()

var LV_TABLE = [
  { level: 0,  min: 0,       max: 0.0625,  title: '初学入门' },
  { level: 1,  min: 0.0625,  max: 0.125,   title: '渐入佳境' },
  { level: 2,  min: 0.125,   max: 0.1875,  title: '稳步进阶' },
  { level: 3,  min: 0.1875,  max: 0.25,    title: '小有所成' },
  { level: 4,  min: 0.25,    max: 0.3125,  title: '驾轻就熟' },
  { level: 5,  min: 0.3125,  max: 0.375,   title: '渐窥门径' },
  { level: 6,  min: 0.375,   max: 0.4375,  title: '融会贯通' },
  { level: 7,  min: 0.4375,  max: 0.5,     title: '得心应手' },
  { level: 8,  min: 0.5,     max: 0.5625,  title: '游刃有余' },
  { level: 9,  min: 0.5625,  max: 0.625,   title: '炉火纯青' },
  { level: 10, min: 0.625,   max: 0.6875,  title: '登堂入室' },
  { level: 11, min: 0.6875,  max: 0.75,    title: '学有所成' },
  { level: 12, min: 0.75,    max: 0.8125,  title: '精进不休' },
  { level: 13, min: 0.8125,  max: 0.875,   title: '学富五车' },
  { level: 14, min: 0.875,   max: 0.9375,  title: '博闻强识' },
  { level: 15, min: 0.9375,  max: 1.0,     title: '学业大成' }
]

Page({
  data: {
    studentInfo: { name: '', major: '', grade: '', gpa: 0, totalCredits: 0, requiredCredits: 115, failCount: 0 },
    creditPercent: 0,
    remainingCredits: 0,
    failStatusText: '暂无不通过',
    lvLevel: 0,
    lvTitle: '初学入门',
    lvScore: '0.000',
    creditRaw: '0.00',
    gpaRaw: '0.00',
    warnings: []
  },

  onLoad() { this.init() },

  init() {
    var studentId = (app.globalData.userInfo && app.globalData.userInfo.studentId) || 1
    var that = this

    // 获取学生数据
    app.request('/academic/student/' + studentId, 'GET')
      .then(function (res) { that.updateStudent(res.data || res) })
      .catch(function () { that.updateStudent({}) })

    // 获取预警
    app.request('/academic/warnings', 'GET', { page: 1, size: 10 })
      .then(function (res) {
        if (res.data && res.data.records) that.setData({ warnings: res.data.records })
      })
      .catch(function () {})
  },

  updateStudent(data) {
    data = data || {}
    var totalCredits = data.totalCredits || 0
    var requiredCredits = data.requiredCredits || 115
    var gpa = data.gpa || 0
    var failCount = data.failCount || 0
    var remainingCredits = Math.max(0, requiredCredits - totalCredits)

    var creditPercent = requiredCredits > 0 ? Math.min(100, (totalCredits / requiredCredits * 100)).toFixed(1) : '0.0'

    var failStatusText = failCount > 0 ? failCount + '门待重修' : '暂无不通过'

    // LV 计算
    var creditProgress = requiredCredits > 0 ? totalCredits / requiredCredits : 0
    var gpaProgress = gpa / 4.0
    var score = Math.max(0, Math.min(1, creditProgress * 0.6 + gpaProgress * 0.4))

    var lvLevel = 0, lvTitle = '初学入门'
    for (var i = 0; i < LV_TABLE.length; i++) {
      if (score >= LV_TABLE[i].min && score < LV_TABLE[i].max) { lvLevel = LV_TABLE[i].level; lvTitle = LV_TABLE[i].title; break }
    }
    if (score >= 1.0) { lvLevel = 15; lvTitle = '学业大成' }

    this.setData({
      studentInfo: {
        name: data.name || (app.globalData.userInfo && app.globalData.userInfo.realName) || '学生',
        major: data.major || '',
        grade: data.grade || '',
        gpa: gpa,
        totalCredits: totalCredits,
        requiredCredits: requiredCredits,
        failCount: failCount
      },
      creditPercent: 0,
      remainingCredits: remainingCredits,
      failStatusText: failStatusText,
      lvLevel: lvLevel,
      lvTitle: lvTitle,
      lvScore: score.toFixed(3),
      creditRaw: creditProgress.toFixed(2),
      gpaRaw: gpaProgress.toFixed(2)
    })

    var that = this
    setTimeout(function () {
      that.setData({ creditPercent: Number(creditPercent) })
      that.drawGpaRing(gpa)
    }, 300)
  },

  drawGpaRing(gpa) {
    var ctx = wx.createCanvasContext('gpaCanvas', this)
    var size = 68, lw = 3, r = (size - lw) / 2, cx = size / 2, cy = size / 2
    var ratio = Math.max(0, Math.min(1, (gpa || 0) / 4.0))

    ctx.beginPath(); ctx.arc(cx, cy, r, 0, 2 * Math.PI)
    ctx.setStrokeStyle('#E8ECF1'); ctx.setLineWidth(lw); ctx.setLineCap('round'); ctx.stroke()

    if (ratio > 0) {
      ctx.beginPath()
      ctx.arc(cx, cy, r, -Math.PI / 2, -Math.PI / 2 + 2 * Math.PI * ratio)
      ctx.setStrokeStyle('#9DB4C0'); ctx.setLineWidth(lw); ctx.setLineCap('round'); ctx.stroke()
    }
    ctx.draw()
  },

  onLvTap() {
    wx.navigateTo({
      url: '/pages/level-detail/level-detail?level=' + this.data.lvLevel +
           '&title=' + this.data.lvTitle + '&score=' + this.data.lvScore +
           '&creditRaw=' + this.data.creditRaw + '&gpaRaw=' + this.data.gpaRaw
    })
  }
})
