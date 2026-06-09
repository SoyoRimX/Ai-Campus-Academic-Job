const app = getApp()

Page({
  data: {
    resume: null,
    basicInfo: {},
    parsedContent: {},
    starRating: 0,

    // 编辑模式
    isEditing: false,
    saving: false,
    form: {
      title: '',
      targetJob: '',
      targetCity: '',
      education: [],
      internship: [],
      skills: [],
      selfIntro: ''
    },
    skillInput: '' // 技能输入框的临时值
  },

  onLoad() {
    this.loadResume()
  },

  /* ================================================================
     数据加载
     ================================================================ */
  loadResume() {
    const userInfo = app.globalData.userInfo || {}
    const studentId = userInfo.studentId || '1'

    app.request('/employ/resume/' + studentId, 'GET')
      .then((res) => {
        if (res.data) {
          this.setResumeData(res.data)
        }
      })
      .catch(() => {
        // 网络错误或暂无简历，保持空状态
      })
  },

  setResumeData(data) {
    const userInfo = app.globalData.userInfo || {}
    const basicInfo = {
      name: userInfo.realName || userInfo.name || '',
      phone: userInfo.phone || '',
      email: userInfo.email || ''
    }

    let parsedContent = {}
    if (data.content) {
      try {
        parsedContent = typeof data.content === 'string' ? JSON.parse(data.content) : data.content
      } catch (e) {
        parsedContent = { raw: data.content }
      }
    }

    const starRating = Math.round((data.aiScore || 0) / 20)

    this.setData({
      resume: data,
      basicInfo,
      parsedContent,
      starRating
    })
  },

  /* ================================================================
     编辑模式切换
     ================================================================ */
  startCreate() {
    // 空状态点击"创建简历" → 进入空白编辑模式
    this.setData({
      isEditing: true,
      form: {
        title: '',
        targetJob: '',
        targetCity: '',
        education: [],
        internship: [],
        skills: [],
        selfIntro: ''
      }
    })
  },

  startEdit() {
    // 查看模式点击"编辑" → 用已有数据填充表单
    const { resume, parsedContent } = this.data
    this.setData({
      isEditing: true,
      form: {
        title: resume.title || '',
        targetJob: resume.targetJob || '',
        targetCity: resume.targetCity || '',
        education: (parsedContent.education || []).map(e => ({ ...e })),
        internship: (parsedContent.internship || []).map(e => ({ ...e })),
        skills: [...(parsedContent.skills || [])],
        selfIntro: parsedContent.selfIntro || ''
      }
    })
  },

  cancelEdit() {
    const { resume } = this.data
    if (resume) {
      // 有简历 → 退回查看模式
      this.setData({ isEditing: false })
    } else {
      // 无简历 → 退回空状态
      this.setData({ isEditing: false, resume: null })
    }
  },

  /* ================================================================
     表单字段更新
     ================================================================ */
  onFieldInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  /* 教育背景 */
  addEducation() {
    const edu = [...this.data.form.education, { school: '', major: '', period: '' }]
    this.setData({ 'form.education': edu })
  },
  delEducation(e) {
    const idx = e.currentTarget.dataset.idx
    const edu = this.data.form.education.filter((_, i) => i !== idx)
    this.setData({ 'form.education': edu })
  },
  onEduInput(e) {
    const { idx, field } = e.currentTarget.dataset
    this.setData({ ['form.education[' + idx + '].' + field]: e.detail.value })
  },

  /* 实习经历 */
  addInternship() {
    const intern = [...this.data.form.internship, { company: '', role: '', period: '', desc: '' }]
    this.setData({ 'form.internship': intern })
  },
  delInternship(e) {
    const idx = e.currentTarget.dataset.idx
    const intern = this.data.form.internship.filter((_, i) => i !== idx)
    this.setData({ 'form.internship': intern })
  },
  onInternInput(e) {
    const { idx, field } = e.currentTarget.dataset
    this.setData({ ['form.internship[' + idx + '].' + field]: e.detail.value })
  },

  /* 技能 */
  onSkillInput(e) {
    this.setData({ skillInput: e.detail.value })
  },
  addSkill() {
    const val = this.data.skillInput.trim()
    if (!val) return
    if (this.data.form.skills.includes(val)) {
      wx.showToast({ title: '技能已存在', icon: 'none' })
      return
    }
    const skills = [...this.data.form.skills, val]
    this.setData({ 'form.skills': skills, skillInput: '' })
  },
  delSkill(e) {
    const idx = e.currentTarget.dataset.idx
    const skills = this.data.form.skills.filter((_, i) => i !== idx)
    this.setData({ 'form.skills': skills })
  },

  /* ================================================================
     保存
     ================================================================ */
  saveResume() {
    const { form } = this.data
    if (!form.title.trim()) {
      wx.showToast({ title: '请输入简历标题', icon: 'none' })
      return
    }

    this.setData({ saving: true })

    // 组装 content JSON
    const content = JSON.stringify({
      education: form.education,
      internship: form.internship,
      skills: form.skills,
      selfIntro: form.selfIntro
    })

    const userInfo = app.globalData.userInfo || {}
    const studentId = userInfo.studentId || 1

    app.request('/employ/resume', 'POST', {
      studentId: studentId,
      title: form.title.trim(),
      targetJob: form.targetJob.trim(),
      targetCity: form.targetCity.trim(),
      content: content
    })
      .then((res) => {
        this.setData({ saving: false, isEditing: false })
        wx.showToast({ title: '保存成功', icon: 'success' })
        // 重新加载以展示最新数据
        if (res.data) {
          this.setResumeData(res.data)
        } else {
          this.loadResume()
        }
      })
      .catch(() => {
        this.setData({ saving: false })
        wx.showToast({ title: '保存失败，请重试', icon: 'none' })
      })
  },

  /* ================================================================
     AI 优化
     ================================================================ */
  onAIOptimize() {
    const studentNo = (app.globalData.userInfo && app.globalData.userInfo.studentNo) || ''
    if (!studentNo) {
      wx.showToast({ title: '未找到学号信息', icon: 'none' })
      return
    }

    wx.showLoading({ title: 'AI 优化中…' })
    app.request('/employ/resume/generate', 'POST', { studentNo })
      .then((res) => {
        wx.hideLoading()
        if (res.data) {
          // generate 返回的是 JSON 字符串，解析后 set
          try {
            const generated = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
            this.setResumeData(generated)
          } catch (e) {
            this.setResumeData(res.data)
          }
          wx.showToast({ title: '优化成功', icon: 'success' })
        }
      })
      .catch(() => {
        wx.hideLoading()
        wx.showToast({ title: '优化失败，请稍后重试', icon: 'none' })
      })
  }
})
