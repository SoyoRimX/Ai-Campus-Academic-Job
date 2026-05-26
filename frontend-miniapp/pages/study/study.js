/*
 * ============================================
 *  AI校园学业就业助手 — 学业页逻辑
 *  study.js | 纯前端计算 | GPA评级/LV保留不变
 * ============================================
 */

var app = getApp()

/* LV 等级配置表（保留不变） */
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
    /* 原始学生信息 */
    studentInfo: {
      name: '张三',
      major: '计算机科学与技术',
      grade: '2024级',
      gpa: 3.6,
      totalCredits: 48.5,
      requiredCredits: 115,
      failCount: 0
    },

    /* GPA 评级 */
    gpaRating: '良好',
    gpaRatingClass: 'good',

    /* 学分进度 */
    creditPercent: 0,
    remainingCredits: 0,
    failStatusText: '暂无不通过',

    /* LV 等级（显示在姓名旁 + 点击跳转） */
    lvLevel: 0,
    lvTitle: '初学入门',
    lvScore: '0.000',
    creditRaw: '0.00',
    gpaRaw: '0.00',

    /* 安全区域 */
    safeAreaBottom: 0
  },

  /* ===== 生命周期 ===== */
  onLoad: function () {
    this.getSafeArea()
    this.initStudentData()
  },

  onShow: function () {},

  getSafeArea: function () {
    try {
      var sysInfo = wx.getSystemInfoSync()
      var safeArea = sysInfo.safeArea || {}
      var safeBottom = sysInfo.screenHeight - (safeArea.bottom || sysInfo.screenHeight)
      this.setData({ safeAreaBottom: Math.max(safeBottom, 0) })
    } catch (e) {
      this.setData({ safeAreaBottom: 0 })
    }
  },

  /* ===== 初始化数据 ===== */
  initStudentData: function () {
    var that = this
    app.request('/academic/student/1', 'GET')
      .then(function (res) {
        var data = res.data || res
        that.updateAll(data)
      })
      .catch(function () {
        that.updateAll({
          name: '张三',
          major: '计算机科学与技术',
          grade: '2024级',
          gpa: 3.6,
          totalCredits: 48.5,
          requiredCredits: 115,
          failCount: 0
        })
      })
  },

  /* ===== 一站式更新 ===== */
  updateAll: function (data) {
    var totalCredits = data.totalCredits || 0
    var requiredCredits = data.requiredCredits || 0
    var gpa = data.gpa || 0
    var failCount = data.failCount || 0
    var remainingCredits = Math.max(0, requiredCredits - totalCredits)

    /* 学分百分比 */
    var creditPercent = requiredCredits > 0
      ? Math.min(100, (totalCredits / requiredCredits * 100)).toFixed(1)
      : '0.0'
    var creditPercentNum = Number(creditPercent)

    /* 不及格状态 */
    var failStatusText = '暂无不通过'
    if (failCount > 0) { failStatusText = failCount + '门待重修' }

    /* GPA 评级（保留原有计算逻辑） */
    var gpaRating = '良好'
    var gpaRatingClass = 'good'
    if (gpa >= 3.5) {
      gpaRating = '优秀'
      gpaRatingClass = 'excellent'
    } else if (gpa < 2.5) {
      gpaRating = '差'
      gpaRatingClass = 'poor'
    }

    /* LV 综合等级（保留原有计算逻辑，仅在姓名旁展示） */
    var creditProgress = requiredCredits > 0 ? totalCredits / requiredCredits : 0
    var gpaProgress = gpa / 4.0
    var score = creditProgress * 0.6 + gpaProgress * 0.4
    if (score < 0) score = 0
    if (score > 1) score = 1

    var lvLevel = 0
    var lvTitle = '初学入门'
    for (var i = 0; i < LV_TABLE.length; i++) {
      if (score >= LV_TABLE[i].min && score < LV_TABLE[i].max) {
        lvLevel = LV_TABLE[i].level
        lvTitle = LV_TABLE[i].title
        break
      }
    }
    if (score >= 1.0) { lvLevel = 15; lvTitle = '学业大成' }

    this.setData({
      studentInfo: {
        name: data.name || '未设置',
        major: data.major || '未设置',
        grade: data.grade || '',
        gpa: gpa,
        totalCredits: totalCredits,
        requiredCredits: requiredCredits,
        failCount: failCount
      },
      creditPercent: 0,
      remainingCredits: remainingCredits,
      failStatusText: failStatusText,

      gpaRating: gpaRating,
      gpaRatingClass: gpaRatingClass,

      lvLevel: lvLevel,
      lvTitle: lvTitle,
      lvScore: score.toFixed(3),
      creditRaw: creditProgress.toFixed(2),
      gpaRaw: gpaProgress.toFixed(2),
    })

    /* 延迟触发进度条动画 */
    var that = this
    setTimeout(function () {
      that.setData({ creditPercent: creditPercentNum })
      that.drawGpaSmallRing(gpa)
    }, 300)
  },

  /* ===== 小号 GPA 环形进度（68px） ===== */
  drawGpaSmallRing: function (gpa) {
    var ctx = wx.createCanvasContext('gpaSmallCanvas', this)
    var size = 68
    var lineWidth = 3
    var radius = (size - lineWidth) / 2
    var cx = size / 2
    var cy = size / 2
    var ratio = gpa / 4.0
    if (ratio < 0) ratio = 0
    if (ratio > 1) ratio = 1

    /* 背景圆环 */
    ctx.beginPath()
    ctx.arc(cx, cy, radius, 0, 2 * Math.PI)
    ctx.setStrokeStyle('#E0E5E9')
    ctx.setLineWidth(lineWidth)
    ctx.setLineCap('round')
    ctx.stroke()

    /* 前景弧 */
    if (ratio > 0) {
      var startAngle = -Math.PI / 2
      var endAngle = startAngle + 2 * Math.PI * ratio
      ctx.beginPath()
      ctx.arc(cx, cy, radius, startAngle, endAngle)
      ctx.setStrokeStyle('#9DB4C0')
      ctx.setLineWidth(lineWidth)
      ctx.setLineCap('round')
      ctx.stroke()
    }

    ctx.draw()
  },

  /* ===== LV 等级点击 → 等级详情页 ===== */
  onLvTap: function () {
    wx.navigateTo({
      url: '/pages/level-detail/level-detail?level=' + this.data.lvLevel +
           '&title=' + this.data.lvTitle +
           '&score=' + this.data.lvScore +
           '&creditRaw=' + this.data.creditRaw +
           '&gpaRaw=' + this.data.gpaRaw
    })
  }
})
