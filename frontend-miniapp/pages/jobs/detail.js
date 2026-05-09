const app = getApp()

Page({
  data: {
    job: {},
    jobTypeMap: {
      'FULL_TIME': '全职',
      'PART_TIME': '兼职',
      'INTERN': '实习',
      'CONTRACT': '合同制'
    }
  },

  onLoad(options) {
    const id = options.id
    if (id) {
      this.loadDetail(id)
    }
  },

  loadDetail(id) {
    wx.showLoading({ title: '加载中...' })

    app.request('/employ/job/' + id, 'GET')
      .then((res) => {
        wx.hideLoading()
        if (res.data) {
          this.setData({ job: res.data })
        } else {
          this.showMockDetail(id)
        }
      })
      .catch(() => {
        wx.hideLoading()
        this.showMockDetail(id)
      })
  },

  showMockDetail(id) {
    const mockMap = {
      '1': {
        id: 1,
        jobTitle: '前端开发工程师',
        company: '腾讯科技',
        salaryRange: '15K-25K',
        city: '深圳',
        education: '本科',
        experience: '1-3年',
        jobType: 'FULL_TIME',
        skills: ['JavaScript', 'Vue.js', 'React', 'TypeScript', 'CSS3', 'HTML5'],
        description: '岗位职责：\n1. 负责公司核心产品的前端开发与维护\n2. 参与产品需求评审，输出技术方案\n3. 与后端、设计团队紧密协作，推动项目落地\n4. 持续优化前端性能与用户体验\n\n任职要求：\n1. 计算机相关专业本科及以上学历\n2. 1-3年前端开发经验\n3. 精通JavaScript，熟悉Vue.js或React框架\n4. 具备良好的沟通能力和团队协作精神'
      },
      '2': {
        id: 2,
        jobTitle: 'Java后端开发',
        company: '阿里巴巴',
        salaryRange: '20K-35K',
        city: '杭州',
        education: '本科',
        experience: '3-5年',
        jobType: 'FULL_TIME',
        skills: ['Java', 'Spring Boot', 'MySQL', 'Redis', 'Kafka', 'Docker'],
        description: '岗位职责：\n1. 负责核心业务系统的后端架构设计与开发\n2. 参与系统性能优化与稳定性保障\n3. 编写技术文档，参与代码评审\n\n任职要求：\n1. 本科及以上学历，3-5年Java开发经验\n2. 精通Spring Boot、MyBatis等主流框架\n3. 熟悉分布式系统设计与开发\n4. 有大型互联网项目经验者优先'
      },
      '3': {
        id: 3,
        jobTitle: '数据分析师',
        company: '字节跳动',
        salaryRange: '18K-30K',
        city: '北京',
        education: '硕士',
        experience: '应届生',
        jobType: 'FULL_TIME',
        skills: ['Python', 'SQL', 'Excel', 'Tableau', '数据分析', '统计学'],
        description: '岗位职责：\n1. 负责业务数据采集、清洗与分析\n2. 搭建数据报表与可视化看板\n3. 为产品决策与运营策略提供数据支持\n\n任职要求：\n1. 统计学、数学或计算机相关专业硕士\n2. 熟练使用SQL和Python\n3. 具备良好的逻辑思维和数据敏感度'
      }
    }

    const job = mockMap[id] || {
      id: Number(id),
      jobTitle: '未知岗位',
      company: '未知公司',
      salaryRange: '面议',
      city: '不限',
      education: '本科',
      experience: '应届生',
      jobType: 'FULL_TIME',
      skills: [],
      description: '暂无详细描述'
    }

    this.setData({ job })
  },

  onApply() {
    wx.showToast({ title: '投递成功', icon: 'success' })
  }
})
