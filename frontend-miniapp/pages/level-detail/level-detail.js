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
    loading: true,
    currentLevel: 0,
    score: '0.000',
    creditRaw: '0.00',
    gpaRaw: '0.00',
    levelList: []
  },

  onLoad: function (options) {
    var currentLevel = Number(options.level) || 0
    this.setData({
      currentLevel: currentLevel,
      score: options.score || '0.000',
      creditRaw: options.creditRaw || '0.00',
      gpaRaw: options.gpaRaw || '0.00'
    })

    var that = this
    setTimeout(function () {
      var list = LV_TABLE.map(function (item) {
        return {
          level: item.level,
          title: item.title,
          minText: item.min.toFixed(4),
          maxText: item.max.toFixed(4)
        }
      })
      that.setData({ loading: false, levelList: list })
    }, 200)
  }
})
