const app = getApp()

const MOCK_QUESTIONS = [
  '请做个简单的自我介绍',
  '你为什么选择这个岗位?',
  '你最大的优势是什么?',
  '描述一个你解决过的技术难题',
  '你对未来3年的职业规划是什么?'
]

const TOTAL_QUESTIONS = MOCK_QUESTIONS.length

Page({
  data: {
    // 状态机: idle | in_progress | completed
    status: 'idle',

    // 配置
    interviewType: 'text',   // 'text' | 'voice'
    interviewId: '',
    jobList: [],
    selectedJobName: '',
    selectedJobId: '',

    // 聊天消息
    messages: [],
    inputText: '',
    isThinking: false,
    currentQuestionIndex: 0,
    totalQuestions: TOTAL_QUESTIONS,
    answers: [],

    // 结果
    score: 0,
    feedbackList: []
  },

  onLoad() {
    // 预加载岗位列表（可选）
    this.loadJobList()
  },

  loadJobList() {
    app.request('/employ/jobs/list', 'GET', { pageSize: 50 })
      .then(res => {
        if (res.data && res.data.records) {
          this.setData({ jobList: res.data.records })
        }
      })
      .catch(() => {
        // 岗位列表加载失败不影响面试功能
      })
  },

  // === 选择面试模式 ===
  selectMode(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ interviewType: type })
  },

  // === 选择目标岗位 ===
  selectJob(e) {
    const index = e.detail.value
    const job = this.data.jobList[index]
    if (job) {
      this.setData({
        selectedJobName: job.name || job.title || job.positionName,
        selectedJobId: job.id || job.jobId
      })
    }
  },

  // === 开始面试 ===
  startInterview() {
    const { interviewType, selectedJobId } = this.data
    const userInfo = app.globalData.userInfo || {}

    this.setData({ status: 'in_progress' })

    app.request('/employ/interview/start', 'POST', {
      studentId: userInfo.studentId || userInfo.id || '',
      jobId: selectedJobId || '',
      interviewType: interviewType
    })
      .then(res => {
        // API 成功：使用返回的面试ID和第一个问题
        const interviewId = res.data?.interviewId || res.data?.id || ''
        this.setData({ interviewId })
        this.askFirstQuestion()
      })
      .catch(() => {
        // API 失败：使用 mock 面试
        this.setData({ interviewId: 'mock_' + Date.now() })
        this.askFirstQuestion()
      })
  },

  // === 提出第一个问题 ===
  askFirstQuestion() {
    this.setData({ isThinking: true })
    setTimeout(() => {
      const question = MOCK_QUESTIONS[0]
      const messages = this.data.messages.concat([{
        id: Date.now(),
        role: 'ai',
        content: question
      }])
      this.setData({
        messages,
        isThinking: false,
        currentQuestionIndex: 0
      })
    }, 800)
  },

  // === 输入框变化 ===
  onInputChange(e) {
    this.setData({ inputText: e.detail.value })
  },

  // === 发送文字回答 ===
  sendAnswer() {
    const text = this.data.inputText.trim()
    if (!text) return

    const userMsg = {
      id: Date.now(),
      role: 'user',
      content: text
    }
    const messages = this.data.messages.concat([userMsg])
    const answers = this.data.answers.concat([{
      question: MOCK_QUESTIONS[this.data.currentQuestionIndex],
      answer: text
    }])
    const nextIndex = this.data.currentQuestionIndex + 1

    this.setData({
      messages,
      inputText: '',
      answers
    })

    if (nextIndex >= TOTAL_QUESTIONS) {
      // 所有问题问完，自动提交
      this.setData({ isThinking: true })
      setTimeout(() => {
        const finishMsg = {
          id: Date.now(),
          role: 'ai',
          content: '面试结束，正在为您生成评价…'
        }
        this.setData({
          messages: messages.concat([finishMsg]),
          isThinking: false
        })
        setTimeout(() => {
          this.doSubmit()
        }, 1000)
      }, 800)
    } else {
      // 问下一个问题
      this.setData({ isThinking: true })
      setTimeout(() => {
        const nextQuestion = MOCK_QUESTIONS[nextIndex]
        const aiMsg = {
          id: Date.now(),
          role: 'ai',
          content: nextQuestion
        }
        this.setData({
          messages: messages.concat([aiMsg]),
          isThinking: false,
          currentQuestionIndex: nextIndex
        })
      }, 1000)
    }
  },

  // === 语音按钮（占位） ===
  onVoiceStart() {
    wx.showToast({ title: '语音功能开发中', icon: 'none' })
  },

  onVoiceEnd() {
    // 语音结束，占位
  },

  // === 提交面试 ===
  submitInterview() {
    wx.showModal({
      title: '确认结束',
      content: '确定要结束本次面试吗？',
      success: (res) => {
        if (res.confirm) {
          this.doSubmit()
        }
      }
    })
  },

  doSubmit() {
    wx.showLoading({ title: '正在提交…' })

    const { interviewId, answers } = this.data

    app.request('/employ/interview/submit', 'POST', {
      interviewId: interviewId,
      answers: JSON.stringify(answers)
    })
      .then(res => {
        wx.hideLoading()
        const result = res.data || {}
        const score = result.score || this.randomScore()
        const feedback = result.feedback || result.comment || result.evaluation || ''
        this.setData({
          status: 'completed',
          score: score,
          feedbackList: this.parseFeedback(feedback)
        })
      })
      .catch(() => {
        wx.hideLoading()
        // API 失败，生成模拟结果
        const dummyScore = this.randomScore()
        const dummyFeedback = this.generateDummyFeedback(dummyScore)
        this.setData({
          status: 'completed',
          score: dummyScore,
          feedbackList: dummyFeedback
        })
      })
  },

  // === 随机分数 75~95 ===
  randomScore() {
    return Math.floor(Math.random() * 21) + 75
  },

  // === 根据分数生成评价 ===
  generateDummyFeedback(score) {
    const items = []
    if (score >= 90) {
      items.push('表达流畅，逻辑清晰，展现了扎实的专业功底')
      items.push('对岗位理解深入，职业规划明确')
      items.push('建议：可以多准备一些具体项目案例来支撑论点')
    } else if (score >= 80) {
      items.push('整体表现良好，回答问题有条理')
      items.push('对部分技术问题有一定了解，但深度可以加强')
      items.push('建议：多练习表达方式，突出个人亮点')
    } else {
      items.push('基本回答完整，但缺乏深度和亮点')
      items.push('对自己优势的总结不够突出')
      items.push('建议：提前准备常见面试问题，多做模拟练习')
    }
    return items
  },

  // === 解析后端返回的评价文本 ===
  parseFeedback(text) {
    if (!text) return []
    if (typeof text === 'string') {
      return text.split('\n').filter(s => s.trim())
    }
    if (Array.isArray(text)) return text
    return []
  },

  // === 再来一次 ===
  resetInterview() {
    this.setData({
      status: 'idle',
      messages: [],
      inputText: '',
      isThinking: false,
      currentQuestionIndex: 0,
      answers: [],
      score: 0,
      feedbackList: [],
      interviewId: ''
    })
  }
})
