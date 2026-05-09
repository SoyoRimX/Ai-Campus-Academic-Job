<template>
  <div class="interview-simulator">
    <el-card v-if="state === 'idle'" class="start-card">
      <div class="welcome">
        <h2>AI 模拟面试</h2>
        <p>选择面试模式和目标岗位，开始你的 AI 模拟面试练习</p>
      </div>
      <el-form :model="config" label-width="100px" class="config-form" @submit.prevent="startInterview">
        <el-form-item label="面试模式">
          <el-radio-group v-model="config.interviewType">
            <el-radio-button :value="1">
              <el-icon><EditPen /></el-icon> 文字面试
            </el-radio-button>
            <el-radio-button :value="2">
              <el-icon><Microphone /></el-icon> 语音面试
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标岗位">
          <el-select v-model="config.jobId" placeholder="可选，选择目标岗位" clearable style="width:300px">
            <el-option v-for="job in jobs" :key="job.id" :label="job.jobTitle + ' - ' + job.company" :value="job.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="startInterview">
            <el-icon><VideoPlay /></el-icon> 开始面试
          </el-button>
        </el-form-item>
      </el-form>
      <el-divider />
      <div class="tips">
        <h4>面试说明</h4>
        <ul>
          <li><strong>文字模式</strong>：AI 面试官以文字提问，你以文字作答，最终获得评分和反馈</li>
          <li><strong>语音模式</strong>：通过浏览器麦克风实时对话（需授权麦克风权限）</li>
          <li>面试共 5 道题，涵盖自我介绍、技术能力、项目经验和综合素质</li>
        </ul>
      </div>
    </el-card>

    <!-- 文字面试进行中 -->
    <el-card v-if="state === 'text-progress'" class="chat-card">
      <template #header>
        <div class="chat-header">
          <span>AI 模拟面试 - 文字模式</span>
          <el-tag>第 {{ currentQuestion }} / {{ totalQuestions }} 题</el-tag>
          <el-button type="danger" size="small" @click="submitAndEnd">结束面试</el-button>
        </div>
      </template>
      <div class="chat-area" ref="chatRef">
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
          <div class="avatar">{{ msg.role === 'assistant' ? '🤖' : '👤' }}</div>
          <div class="bubble">{{ msg.content }}</div>
        </div>
      </div>
      <div class="input-area">
        <el-input v-model="currentAnswer" placeholder="输入你的回答..." @keyup.enter="sendAnswer" maxlength="500" show-word-limit>
          <template #append>
            <el-button @click="sendAnswer" :disabled="!currentAnswer.trim()">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <!-- 语音面试进行中 -->
    <el-card v-if="state === 'voice-progress'" class="voice-card">
      <template #header>
        <div class="chat-header">
          <span>AI 模拟面试 - 语音模式</span>
          <el-tag>第 {{ currentQuestion }} / {{ totalQuestions }} 题</el-tag>
          <el-button type="danger" size="small" @click="submitAndEnd">结束面试</el-button>
        </div>
      </template>
      <div class="voice-area">
        <div class="ai-speaking" v-if="aiSpeaking">
          <div class="pulse-ring"></div>
          <span>🤖 AI 正在提问...</span>
        </div>
        <div class="question-display" v-else>
          <div class="current-q">
            <span class="label">当前问题：</span>
            <span class="text">{{ messages[messages.length - 1]?.content || '' }}</span>
          </div>
          <div class="voice-controls">
            <el-button :type="recording ? 'danger' : 'primary'" size="large" :class="{ recording }" @mousedown="startRecord" @mouseup="stopRecord" @touchstart="startRecord" @touchend="stopRecord">
              <el-icon><Microphone /></el-icon>
              {{ recording ? '松开结束' : '按住说话' }}
            </el-button>
            <p class="hint">按住按钮开始录音，松开后自动识别并发送</p>
          </div>
        </div>
        <div class="transcript" v-if="currentAnswer">
          <el-tag>识别结果：{{ currentAnswer }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 面试结果 -->
    <el-card v-if="state === 'completed'" class="result-card">
      <div class="result-header">
        <div class="score-circle" :style="{ borderColor: scoreColor }">
          <span class="score-num">{{ score }}</span>
          <span class="score-unit">分</span>
        </div>
        <h2>面试评估报告</h2>
      </div>
      <el-divider />
      <div class="feedback-section">
        <h4>AI 综合评价</h4>
        <p>{{ feedback }}</p>
      </div>
      <div class="feedback-section" v-if="strengths.length">
        <h4>亮点</h4>
        <ul><li v-for="(s, i) in strengths" :key="i">{{ s }}</li></ul>
      </div>
      <div class="feedback-section" v-if="improvements.length">
        <h4>改进建议</h4>
        <ul><li v-for="(s, i) in improvements" :key="i">{{ s }}</li></ul>
      </div>
      <el-divider />
      <div class="qa-review">
        <h4>问答回顾</h4>
        <div v-for="(qa, i) in qaList" :key="i" class="qa-item">
          <p class="q"><strong>Q{{ i + 1 }}:</strong> {{ qa.question }}</p>
          <p class="a"><strong>A{{ i + 1 }}:</strong> {{ qa.answer }}</p>
        </div>
      </div>
      <el-button type="primary" size="large" @click="resetAll" style="margin-top:16px">再来一次</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { getJobs } from '@/api/employment'
import { ElMessage } from 'element-plus'

const state = ref<'idle' | 'text-progress' | 'voice-progress' | 'completed'>('idle')
const config = ref({ interviewType: 1, jobId: null as number | null })
const jobs = ref<any[]>([])
const messages = ref<{ role: string; content: string }[]>([])
const currentAnswer = ref('')
const currentQuestion = ref(1)
const totalQuestions = ref(5)
const score = ref(0)
const feedback = ref('')
const strengths = ref<string[]>([])
const improvements = ref<string[]>([])
const qaList = ref<{ question: string; answer: string }[]>([])
const recording = ref(false)
const aiSpeaking = ref(false)
const chatRef = ref<HTMLElement>()

const questions = [
  '请做一个简单的自我介绍，包括你的技术背景和项目经验。',
  '描述一个你参与过的最有挑战性的项目，以及你如何解决遇到的困难。',
  '你对这个岗位的技术栈了解多少？谈谈你的学习路径。',
  '在团队协作中，你如何处理与同事的意见分歧？请举例说明。',
  '你对未来3-5年的职业发展有什么规划？'
]

onMounted(async () => {
  try { const res = await getJobs({ page: 1, size: 100 }); jobs.value = res.data?.records || [] } catch { /* empty */ }
})

function startInterview() {
  messages.value = []
  currentQuestion.value = 1
  currentAnswer.value = ''
  qaList.value = []
  if (config.value.interviewType === 1) {
    state.value = 'text-progress'
    addQuestion()
  } else {
    state.value = 'voice-progress'
    addQuestion()
    simulateAiSpeaking()
  }
}

function addQuestion() {
  const idx = currentQuestion.value - 1
  const q = questions[idx] || `面试题 ${currentQuestion.value}`
  messages.value.push({ role: 'assistant', content: q })
  nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight })
}

function sendAnswer() {
  if (!currentAnswer.value.trim()) return
  const answer = currentAnswer.value.trim()
  messages.value.push({ role: 'user', content: answer })
  qaList.value.push({ question: messages.value.filter(m => m.role === 'assistant').pop()?.content || '', answer })
  currentAnswer.value = ''
  if (currentQuestion.value >= totalQuestions.value) {
    finishInterview()
  } else {
    currentQuestion.value++
    setTimeout(() => addQuestion(), 500)
  }
}

function simulateAiSpeaking() {
  aiSpeaking.value = true
  setTimeout(() => { aiSpeaking.value = false }, 3000)
}

let recognition: any = null

function initSpeechRecognition() {
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SpeechRecognition) {
    ElMessage.warning('浏览器不支持语音识别，请使用 Chrome 浏览器')
    return null
  }
  const rec = new SpeechRecognition()
  rec.lang = 'zh-CN'
  rec.interimResults = false
  rec.maxAlternatives = 1
  rec.onresult = (event: any) => {
    currentAnswer.value = event.results[0][0].transcript
    recording.value = false
    ElMessage.success('语音识别完成')
    // Auto-send in voice mode
    if (state.value === 'voice-progress') {
      setTimeout(() => sendAnswer(), 500)
    }
  }
  rec.onerror = () => {
    recording.value = false
    ElMessage.error('语音识别失败，请重试')
  }
  rec.onend = () => { recording.value = false }
  return rec
}

function startRecord() {
  if (!recognition) recognition = initSpeechRecognition()
  if (!recognition) return
  recording.value = true
  recognition.start()
}

function stopRecord() {
  if (recognition) {
    recognition.stop()
    recording.value = false
  }
}

function submitAndEnd() {
  finishInterview()
}

function finishInterview() {
  state.value = 'completed'
  const mockScores = [78, 82, 85, 88, 90]
  score.value = mockScores[Math.floor(Math.random() * mockScores.length)]
  feedback.value = '你在面试中展现了良好的沟通能力和技术素养。对项目经验的描述较为清晰，能够使用STAR法则进行阐述。建议在回答技术问题时增加更多具体的实现细节和量化成果。'
  strengths.value = ['表达清晰，逻辑性强', '项目经验丰富', '对技术栈有基本了解']
  improvements.value = ['技术细节可以更深入', '建议增加量化成果的描述', '职业规划可以更具体']
}

function resetAll() {
  state.value = 'idle'
  messages.value = []
  currentQuestion.value = 1
  currentAnswer.value = ''
  score.value = 0
  feedback.value = ''
  strengths.value = []
  improvements.value = []
  qaList.value = []
}

const scoreColor = ref('#67C23A')
</script>

<style scoped>
.interview-simulator { max-width: 800px; margin: 0 auto; }
.welcome { text-align: center; margin-bottom: 24px; }
.welcome h2 { font-size: 24px; margin-bottom: 8px; }
.config-form { max-width: 400px; margin: 0 auto; }
.tips { color: #606266; font-size: 14px; }
.tips ul { padding-left: 20px; }

.chat-header { display: flex; align-items: center; gap: 12px; }
.chat-header > :last-child { margin-left: auto; }
.chat-area { height: 400px; overflow-y: auto; padding: 16px; background: #f5f7fa; border-radius: 8px; margin-bottom: 12px; }
.message { display: flex; gap: 10px; margin-bottom: 16px; align-items: flex-start; }
.message.user { flex-direction: row-reverse; }
.message .avatar { width: 32px; height: 32px; border-radius: 50%; background: #e0e3e9; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
.message.user .avatar { background: #409EFF; }
.message .bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.message.assistant .bubble { background: #fff; border-bottom-left-radius: 4px; }
.message.user .bubble { background: #409EFF; color: #fff; border-bottom-right-radius: 4px; }
.input-area { margin-top: 8px; }

.voice-area { text-align: center; padding: 40px 0; }
.ai-speaking { padding: 20px; }
.pulse-ring { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px; background: #409EFF; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.8); opacity: 0.5; } 100% { transform: scale(1.2); opacity: 1; } }
.current-q { margin-bottom: 24px; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.voice-controls { margin: 20px 0; }
.voice-controls .el-button { width: 120px; height: 120px; border-radius: 50%; font-size: 16px; }
.voice-controls .recording { animation: pulse 1s infinite; }
.hint { color: #909399; font-size: 13px; margin-top: 12px; }
.transcript { margin-top: 16px; }

.result-card { text-align: center; }
.result-header { margin: 20px 0; }
.score-circle { width: 120px; height: 120px; border-radius: 50%; border: 6px solid #67C23A; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 0 auto; }
.score-num { font-size: 36px; font-weight: bold; line-height: 1; }
.score-unit { font-size: 14px; color: #909399; }
.feedback-section { text-align: left; margin: 16px 0; }
.feedback-section ul { padding-left: 20px; }
.qa-review { text-align: left; }
.qa-item { margin-bottom: 12px; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.qa-item .q { color: #409EFF; }
.qa-item .a { color: #606266; margin-top: 4px; }
</style>
