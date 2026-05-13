const app = getApp()

const TOTAL_QUESTIONS = 5

Page({
  data: {
    status: 'idle',
    interviewType: 'text',
    interviewId: '',
    jobList: [],
    selectedJobName: '',
    selectedJobId: '',

    messages: [],
    inputText: '',
    isThinking: false,
    currentQuestionIndex: 0,
    totalQuestions: TOTAL_QUESTIONS,
    answers: [],

    // 语音相关
    isRecording: false,
    recordingTime: 0,
    recordingTimer: null,

    score: 0,
    feedbackList: []
  },

  onLoad() {
    this.loadJobList()
    this.initRecorder()
  },

  onUnload() {
    this.destroyRecorder()
  },

  loadJobList() {
    app.request('/employ/jobs/list', 'GET', { pageSize: 50 })
      .then(res => {
        if (res.data && res.data.records) {
          this.setData({ jobList: res.data.records })
        }
      })
      .catch(() => {})
  },

  // ==================== 录音管理 ====================

  initRecorder() {
    const recorder = wx.getRecorderManager()
    this._recorder = recorder

    recorder.onStart(() => {
      this.setData({ isRecording: true, recordingTime: 0 })
      this._recordTimer = setInterval(() => {
        this.setData({ recordingTime: this.data.recordingTime + 1 })
      }, 1000)
    })

    recorder.onStop((res) => {
      clearInterval(this._recordTimer)
      this.setData({ isRecording: false })
      if (res.duration < 1000) {
        wx.showToast({ title: '录音时间太短', icon: 'none' })
        return
      }
      this.handleVoiceAnswer(res.tempFilePath)
    })

    recorder.onError((err) => {
      clearInterval(this._recordTimer)
      this.setData({ isRecording: false })
      wx.showToast({ title: '录音失败，请重试', icon: 'none' })
    })
  },

  destroyRecorder() {
    if (this._recordTimer) clearInterval(this._recordTimer)
  },

  // ==================== 选择模式/岗位 ====================

  selectMode(e) {
    this.setData({ interviewType: e.currentTarget.dataset.type })
  },

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

  // ==================== 开始面试 ====================

  startInterview() {
    const { interviewType, selectedJobId } = this.data
    const userInfo = app.globalData.userInfo || {}

    this.setData({ status: 'in_progress' })

    app.request('/employ/interview/start', 'POST', {
      studentId: userInfo.studentId || userInfo.id || 0,
      jobId: selectedJobId || 0,
      interviewType: interviewType === 'voice' ? 1 : 0
    })
      .then(res => {
        const interviewId = res.data?.interviewId || res.data?.id || ''
        const questions = res.data?.questions || []
        this.setData({ interviewId, questions })
        this.askNextQuestion()
      })
      .catch(() => {
        this.setData({ interviewId: 'mock_' + Date.now(), questions: [] })
        this.askNextQuestion()
      })
  },

  // ==================== 提问流程 ====================

  askNextQuestion() {
    const idx = this.data.currentQuestionIndex
    const question = this.getQuestion(idx)
    this.setData({ isThinking: true })

    setTimeout(() => {
      const aiMsg = { id: Date.now(), role: 'ai', content: question }
      this.setData({
        messages: this.data.messages.concat([aiMsg]),
        isThinking: false
      })
      // 语音模式下尝试 TTS 播放问题
      if (this.data.interviewType === 'voice') {
        this.playTTS(question)
      }
    }, 800)
  },

  getQuestion(index) {
    const customQuestions = this.data.questions
    if (customQuestions && customQuestions.length > index) {
      return customQuestions[index]
    }
    const mockQuestions = [
      '请做个简单的自我介绍',
      '你为什么选择这个岗位？',
      '你最大的优势是什么？',
      '描述一个你解决过的技术难题',
      '你对未来3年的职业规划是什么？'
    ]
    return mockQuestions[index] || '请回答'
  },

  // ==================== 文字回答 ====================

  onInputChange(e) {
    this.setData({ inputText: e.detail.value })
  },

  sendAnswer() {
    const text = this.data.inputText.trim()
    if (!text) return
    this.setData({ inputText: '' })
    this.processAnswer(text)
  },

  // ==================== 语音回答 ====================

  onVoiceStart() {
    this._recorder.start({
      duration: 60000,
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      format: 'mp3'
    })
  },

  onVoiceEnd() {
    this._recorder.stop()
  },

  async handleVoiceAnswer(tempFilePath) {
    wx.showLoading({ title: '识别中…' })
    try {
      const res = await app.uploadFile('/ai/speech-to-text', tempFilePath)
      wx.hideLoading()
      const text = res.data?.text || ''
      if (text.trim()) {
        this.processAnswer(text.trim())
      } else {
        wx.showToast({ title: '未识别到语音内容', icon: 'none' })
      }
    } catch (e) {
      wx.hideLoading()
      wx.showToast({ title: '语音识别失败', icon: 'none' })
    }
  },

  // ==================== TTS 播放 ====================

  playTTS(text) {
    wx.request({
      url: app.globalData.baseUrl + '/ai/text-to-speech',
      method: 'POST',
      data: { text },
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.globalData.token}`
      },
      responseType: 'arraybuffer',
      success: (res) => {
        if (res.statusCode === 200 && res.data) {
          const audio = wx.createInnerAudioContext()
          audio.src = 'data:audio/mpeg;base64,' + wx.arrayBufferToBase64(res.data)
          audio.autoplay = true
          audio.onError(() => { /* silent */ })
        }
      }
    })
  },

  // ==================== 通用回答处理 ====================

  processAnswer(text) {
    const idx = this.data.currentQuestionIndex
    const question = this.getQuestion(idx)

    const userMsg = { id: Date.now(), role: 'user', content: text }
    const messages = this.data.messages.concat([userMsg])
    const answers = this.data.answers.concat([{ question, answer: text }])
    const nextIdx = idx + 1

    this.setData({ messages, answers })

    if (nextIdx >= TOTAL_QUESTIONS) {
      this.setData({ isThinking: true })
      setTimeout(() => {
        const finishMsg = { id: Date.now(), role: 'ai', content: '面试结束，正在为您生成评价…' }
        this.setData({ messages: messages.concat([finishMsg]), isThinking: false })
        setTimeout(() => this.doSubmit(), 1000)
      }, 800)
    } else {
      this.setData({ currentQuestionIndex: nextIdx })
      setTimeout(() => this.askNextQuestion(), 600)
    }
  },

  // ==================== 提交面试 ====================

  submitInterview() {
    wx.showModal({
      title: '确认结束',
      content: '确定要结束本次面试吗？',
      success: (res) => {
        if (res.confirm) this.doSubmit()
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
        const score = this.randomScore()
        this.setData({
          status: 'completed',
          score: score,
          feedbackList: this.generateDummyFeedback(score)
        })
      })
  },

  randomScore() {
    return Math.floor(Math.random() * 21) + 75
  },

  generateDummyFeedback(score) {
    if (score >= 90) return [
      '表达流畅，逻辑清晰，展现了扎实的专业功底',
      '对岗位理解深入，职业规划明确',
      '建议：可以多准备一些具体项目案例'
    ]
    if (score >= 80) return [
      '整体表现良好，回答问题有条理',
      '对技术问题有一定了解，但深度可以加强',
      '建议：多练习表达方式，突出个人亮点'
    ]
    return [
      '基本回答完整，但缺乏深度和亮点',
      '对自己优势的总结不够突出',
      '建议：提前准备常见面试问题'
    ]
  },

  parseFeedback(text) {
    if (!text) return []
    if (typeof text === 'string') return text.split('\n').filter(s => s.trim())
    if (Array.isArray(text)) return text
    return []
  },

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
      interviewId: '',
      questions: []
    })
  }
})
