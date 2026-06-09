<template>
  <div class="interview-simulator">
    <!-- ================================================================
         IDLE: Mode selection
         ================================================================ -->
    <div v-if="state === 'idle'" class="is-idle">
      <div class="is-welcome">
        <div class="is-welcome-icon">
          <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
            <rect width="56" height="56" rx="14" fill="var(--color-primary-50)"/>
            <path d="M22 28a6 6 0 1 1 12 0 6 6 0 0 1-12 0z" stroke="var(--color-primary-500)" stroke-width="2"/>
            <path d="M16 38c0-6.627 5.373-12 12-12s12 5.373 12 12" stroke="var(--color-primary-500)" stroke-width="2" stroke-linecap="round"/>
            <circle cx="28" cy="28" r="18" stroke="var(--color-primary-200)" stroke-width="1.5" stroke-dasharray="4 4"/>
          </svg>
        </div>
        <h2>AI 模拟面试</h2>
        <p>选择面试模式和目标岗位，AI 面试官将为您提供真实的面试体验和评估反馈</p>
      </div>

      <div class="is-config">
        <div class="mode-selector">
          <span class="config-label">面试模式</span>
          <div class="mode-cards">
            <div
              class="mode-card"
              :class="{ active: config.interviewType === 1 }"
              @click="config.interviewType = 1"
            >
              <div class="mode-icon">
                <el-icon :size="24"><EditPen /></el-icon>
              </div>
              <span class="mode-name">文字面试</span>
              <span class="mode-desc">AI 文字提问，键盘作答</span>
            </div>
            <div
              class="mode-card"
              :class="{ active: config.interviewType === 2 }"
              @click="config.interviewType = 2"
            >
              <div class="mode-icon">
                <el-icon :size="24"><Microphone /></el-icon>
              </div>
              <span class="mode-name">语音面试</span>
              <span class="mode-desc">语音实时对话交互</span>
            </div>
          </div>
        </div>

        <div class="job-selector">
          <span class="config-label">目标岗位（可选）</span>
          <el-select v-model="config.jobId" placeholder="选择目标岗位或留空" clearable style="width: 100%">
            <el-option v-for="job in jobs" :key="job.id" :label="job.jobTitle + ' — ' + job.company" :value="job.id" />
          </el-select>
        </div>

        <el-button type="primary" size="large" class="start-btn" @click="startInterview">
          <el-icon><VideoPlay /></el-icon>开始面试
        </el-button>
      </div>

      <div class="is-tips">
        <h4>面试说明</h4>
        <div class="tips-grid">
          <div class="tip-item">
            <span class="tip-marker">5</span>
            <span>共 5 道面试题，涵盖自我介绍、技术能力、项目经验和综合素质</span>
          </div>
          <div class="tip-item">
            <span class="tip-marker">AI</span>
            <span>AI 面试官实时提问，回答后获得评分与综合反馈</span>
          </div>
          <div class="tip-item">
            <span class="tip-marker">音</span>
            <span>语音模式需授权浏览器麦克风权限，支持 Chrome 浏览器</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ================================================================
         TEXT PROGRESS: Chat interface
         ================================================================ -->
    <div v-if="state === 'text-progress'" class="is-chat">
      <div class="chat-box">
        <div class="chat-topbar">
          <div class="chat-topbar-left">
            <span class="chat-mode-badge">文字模式</span>
            <span class="chat-progress">第 {{ currentQuestion }} / {{ totalQuestions }} 题</span>
          </div>
          <el-button size="small" text type="danger" @click="submitAndEnd">结束面试</el-button>
        </div>

        <div class="chat-messages" ref="chatRef">
          <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
            <div class="chat-avatar">{{ msg.role === 'assistant' ? 'AI' : '我' }}</div>
            <div class="chat-bubble">{{ msg.content }}</div>
          </div>
        </div>

        <div class="chat-input-row">
          <el-input
            v-model="currentAnswer"
            placeholder="输入你的回答..."
            maxlength="500"
            show-word-limit
            @keyup.enter="sendAnswer"
          >
            <template #append>
              <el-button :disabled="!currentAnswer.trim()" @click="sendAnswer" type="primary">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- ================================================================
         VOICE PROGRESS: Voice interface
         ================================================================ -->
    <div v-if="state === 'voice-progress'" class="is-voice">
      <div class="chat-box">
        <div class="chat-topbar">
          <div class="chat-topbar-left">
            <span class="chat-mode-badge voice">语音模式</span>
            <span class="chat-progress">第 {{ currentQuestion }} / {{ totalQuestions }} 题</span>
          </div>
          <el-button size="small" text type="danger" @click="submitAndEnd">结束面试</el-button>
        </div>

        <div class="voice-content">
        <!-- AI speaking indicator -->
        <div class="voice-ai-speaking" v-if="aiSpeaking">
          <div class="voice-pulse-ring"></div>
          <div class="voice-ai-avatar">AI</div>
          <p>AI 正在提问...</p>
        </div>

        <!-- Question + recording -->
        <div class="voice-interaction" v-else>
          <div class="voice-current-q">
            <span class="voice-q-label">当前问题</span>
            <p class="voice-q-text">{{ messages[messages.length - 1]?.content || '' }}</p>
          </div>

          <div class="voice-record-area">
            <button
              class="record-btn"
              :class="{ recording }"
              @mousedown.prevent="startRecord"
              @mouseup.prevent="stopRecord"
              @touchstart.prevent="startRecord"
              @touchend.prevent="stopRecord"
            >
              <el-icon :size="28"><Microphone /></el-icon>
            </button>
            <span class="record-hint">{{ recording ? '松开结束录音' : '按住开始说话' }}</span>
          </div>

          <div class="voice-result" v-if="voiceText">
            <el-input
              v-model="voiceText"
              type="textarea"
              :rows="3"
              placeholder="语音识别结果，可修改后发送"
              maxlength="500"
              show-word-limit
            />
            <div class="voice-result-actions">
              <el-button @click="voiceText = ''" :disabled="!voiceText">清空</el-button>
              <el-button type="primary" @click="sendVoiceAnswer" :disabled="!voiceText.trim()">
                确认发送 <el-icon><Promotion /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>

    <!-- ================================================================
         COMPLETED: Results
         ================================================================ -->
    <div v-if="state === 'completed'" class="is-result">
      <div class="result-hero">
        <div class="result-score-ring" :style="{ borderColor: scoreColor, background: scoreColor + '08' }">
          <span class="result-score-num">{{ score }}</span>
          <span class="result-score-unit">分</span>
        </div>
        <h2>面试评估报告</h2>
        <p class="result-subtitle">AI 基于你的回答给出的综合评价</p>
      </div>

      <div class="result-sections">
        <div class="result-section">
          <h4>AI 综合评价</h4>
          <p>{{ feedback }}</p>
        </div>

        <div class="result-columns">
          <div class="result-section" v-if="strengths.length">
            <h4>
              <el-icon color="var(--color-success-500)"><CircleCheck /></el-icon>
              亮点
            </h4>
            <ul>
              <li v-for="(s, i) in strengths" :key="i">{{ s }}</li>
            </ul>
          </div>

          <div class="result-section" v-if="improvements.length">
            <h4>
              <el-icon color="var(--color-warning-500)"><Warning /></el-icon>
              改进建议
            </h4>
            <ul>
              <li v-for="(s, i) in improvements" :key="i">{{ s }}</li>
            </ul>
          </div>
        </div>

        <div class="result-section">
          <h4>问答回顾</h4>
          <div class="qa-review-list">
            <div v-for="(qa, i) in qaList" :key="i" class="qa-review-item">
              <div class="qa-review-q">
                <span class="qa-review-num">Q{{ i + 1 }}</span>
                {{ qa.question }}
              </div>
              <div class="qa-review-a">{{ qa.answer }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="result-actions">
        <el-button size="large" @click="resetAll">再来一次</el-button>
        <el-button type="primary" size="large" @click="$router.push('/employment/interview')">查看面试记录</el-button>
      </div>
    </div>
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
const voiceText = ref('')
const chatRef = ref<HTMLElement>()

const questions = [
  '请做一个简单的自我介绍，包括你的技术背景和项目经验。',
  '描述一个你参与过的最有挑战性的项目，以及你如何解决遇到的困难。',
  '你对这个岗位的技术栈了解多少？谈谈你的学习路径。',
  '在团队协作中，你如何处理与同事的意见分歧？请举例说明。',
  '你对未来3-5年的职业发展有什么规划？'
]

onMounted(async () => {
  try { const res = await getJobs({ page: 1, size: 100 }); jobs.value = res.data?.records || [] } catch {}
})

function startInterview() {
  messages.value = []; currentQuestion.value = 1; currentAnswer.value = ''; voiceText.value = ''; qaList.value = []
  if (config.value.interviewType === 1) { state.value = 'text-progress'; addQuestion() }
  else { state.value = 'voice-progress'; addQuestion(); simulateAiSpeaking() }
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
  if (currentQuestion.value >= totalQuestions.value) { finishInterview() }
  else { currentQuestion.value++; setTimeout(() => addQuestion(), 500) }
}

function simulateAiSpeaking() { aiSpeaking.value = true; setTimeout(() => { aiSpeaking.value = false }, 3000) }

let recognition: any = null
function initSpeechRecognition() {
  const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SR) { ElMessage.warning('浏览器不支持语音识别，请使用 Chrome'); return null }
  const rec = new SR()
  rec.lang = 'zh-CN'; rec.interimResults = false; rec.maxAlternatives = 1
  rec.onresult = (e: any) => { voiceText.value = e.results[0][0].transcript; recording.value = false; ElMessage.success('识别完成，请确认后发送') }
  rec.onerror = () => { recording.value = false; ElMessage.error('语音识别失败，请重试') }
  rec.onend = () => { recording.value = false }
  return rec
}
function startRecord() { if (!recognition) recognition = initSpeechRecognition(); if (!recognition) return; recording.value = true; recognition.start() }
function stopRecord() { if (recognition) { recognition.stop(); recording.value = false } }
function sendVoiceAnswer() { if (!voiceText.value.trim()) return; currentAnswer.value = voiceText.value.trim(); voiceText.value = ''; sendAnswer() }

function submitAndEnd() { finishInterview() }

function finishInterview() {
  state.value = 'completed'
  const mockScores = [78, 82, 85, 88, 90]
  score.value = mockScores[Math.floor(Math.random() * mockScores.length)]
  feedback.value = '你在面试中展现了良好的沟通能力和技术素养。对项目经验的描述较为清晰，能够使用STAR法则进行阐述。建议在回答技术问题时增加更多具体的实现细节和量化成果。'
  strengths.value = ['表达清晰，逻辑性强', '项目经验丰富', '对技术栈有基本了解']
  improvements.value = ['技术细节可以更深入', '建议增加量化成果的描述', '职业规划可以更具体']
}

function resetAll() {
  state.value = 'idle'; messages.value = []; currentQuestion.value = 1; currentAnswer.value = ''
  score.value = 0; feedback.value = ''; strengths.value = []; improvements.value = []; qaList.value = []
}

const scoreColor = '#22c55e'
</script>

<style scoped>
.interview-simulator { max-width: 800px; margin: 0 auto; }

/* -------- IDLE -------- */
.is-idle { display: flex; flex-direction: column; gap: var(--space-6); }
.is-welcome { text-align: center; }
.is-welcome-icon { margin-bottom: var(--space-4); display: inline-flex; }
.is-welcome h2 { font-size: var(--font-size-2xl); margin-bottom: var(--space-2); }
.is-welcome p { color: var(--color-text-secondary); max-width: 440px; margin: 0 auto; line-height: var(--line-height-relaxed); }

.is-config { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-5); display: flex; flex-direction: column; gap: var(--space-5); }
.config-label { font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); display: block; margin-bottom: var(--space-3); }
.mode-cards { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-3); }
.mode-card { border: 2px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-4); text-align: center; cursor: pointer; transition: all var(--transition-fast); display: flex; flex-direction: column; align-items: center; gap: var(--space-1); }
.mode-card:hover { border-color: var(--color-primary-300); background: var(--color-primary-50); }
.mode-card.active { border-color: var(--color-primary-500); background: var(--color-primary-50); }
.mode-icon { color: var(--color-primary-500); margin-bottom: var(--space-1); }
.mode-name { font-weight: var(--font-weight-semibold); font-size: var(--font-size-base); }
.mode-desc { font-size: var(--font-size-xs); color: var(--color-text-secondary); }
.start-btn { height: 48px; font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); }

.is-tips { background: var(--color-body-bg); border-radius: var(--radius-lg); padding: var(--space-5); }
.is-tips h4 { font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); margin-bottom: var(--space-3); }
.tips-grid { display: flex; flex-direction: column; gap: var(--space-3); }
.tip-item { display: flex; align-items: flex-start; gap: var(--space-3); font-size: var(--font-size-sm); color: var(--color-text-secondary); line-height: 1.5; }
.tip-marker { width: 22px; height: 22px; border-radius: var(--radius-sm); background: var(--color-primary-100); color: var(--color-primary-600); font-size: var(--font-size-xs); font-weight: var(--font-weight-bold); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }

/* -------- CHAT (unified bordered container) -------- */
.chat-box {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-5);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-light);
}

.chat-topbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.chat-mode-badge {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: var(--color-primary-50);
  color: var(--color-primary-600);
}

.chat-mode-badge.voice {
  background: var(--color-warning-50);
  color: var(--color-warning-600);
}

.chat-progress {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* Chat messages */
.chat-messages {
  height: 420px;
  overflow-y: auto;
  padding: var(--space-4) var(--space-5);
  background: var(--color-body-bg);
}
.chat-msg { display: flex; gap: var(--space-3); margin-bottom: var(--space-4); align-items: flex-start; }
.chat-msg.user { flex-direction: row-reverse; }
.chat-avatar { width: 32px; height: 32px; border-radius: var(--radius-full); display: flex; align-items: center; justify-content: center; font-size: var(--font-size-xs); font-weight: var(--font-weight-bold); flex-shrink: 0; }
.chat-msg.assistant .chat-avatar { background: var(--color-primary-100); color: var(--color-primary-600); }
.chat-msg.user .chat-avatar { background: var(--color-primary-500); color: #fff; }
.chat-bubble { max-width: 72%; padding: var(--space-3) var(--space-4); border-radius: var(--radius-lg); font-size: var(--font-size-base); line-height: 1.6; }
.chat-msg.assistant .chat-bubble { background: var(--color-surface); border: 1px solid var(--color-border-light); border-bottom-left-radius: var(--radius-sm); }
.chat-msg.user .chat-bubble { background: var(--color-primary-500); color: #fff; border-bottom-right-radius: var(--radius-sm); }
.chat-input-row {
  padding: var(--space-3) var(--space-5);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
}

/* -------- VOICE -------- */
.is-voice { display: flex; flex-direction: column; }
.voice-content {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-body-bg);
  border-top: 1px solid var(--color-border-light);
  padding: var(--space-8);
}
.voice-ai-speaking { text-align: center; }
.voice-pulse-ring { width: 80px; height: 80px; border-radius: var(--radius-full); margin: 0 auto var(--space-4); background: var(--color-primary-500); animation: voice-pulse 1.5s ease infinite; }
@keyframes voice-pulse {
  0% { transform: scale(0.8); opacity: 0.4; }
  50% { opacity: 0.7; }
  100% { transform: scale(1.3); opacity: 0; }
}
.voice-ai-avatar { width: 48px; height: 48px; border-radius: var(--radius-full); background: var(--color-primary-100); color: var(--color-primary-600); display: flex; align-items: center; justify-content: center; font-weight: var(--font-weight-bold); margin: 0 auto var(--space-3); }
.voice-ai-speaking p { color: var(--color-text-secondary); }

.voice-interaction { width: 100%; max-width: 480px; }
.voice-current-q { margin-bottom: var(--space-6); padding: var(--space-4); background: var(--color-surface); border-radius: var(--radius-md); border: 1px solid var(--color-border-light); }
.voice-q-label { font-size: var(--font-size-xs); color: var(--color-text-placeholder); display: block; margin-bottom: var(--space-1); }
.voice-q-text { font-size: var(--font-size-md); line-height: 1.6; }
.voice-record-area { text-align: center; margin-bottom: var(--space-6); }
.record-btn { width: 80px; height: 80px; border-radius: var(--radius-full); border: 3px solid var(--color-border); background: var(--color-surface); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast); color: var(--color-primary-500); }
.record-btn:hover { border-color: var(--color-primary-400); }
.record-btn.recording { border-color: var(--color-danger-500); background: var(--color-danger-50); color: var(--color-danger-500); animation: voice-pulse 1s ease infinite; }
.record-hint { display: block; margin-top: var(--space-2); font-size: var(--font-size-xs); color: var(--color-text-placeholder); }
.voice-result { margin-top: var(--space-4); }
.voice-result-actions { display: flex; justify-content: flex-end; gap: var(--space-2); margin-top: var(--space-3); }

/* -------- RESULT -------- */
.is-result { text-align: center; }
.result-hero { padding: var(--space-8) 0; }
.result-score-ring { width: 120px; height: 120px; border-radius: var(--radius-full); border: 5px solid #22c55e; display: flex; flex-direction: column; align-items: center; justify-content: center; margin: 0 auto var(--space-4); }
.result-score-num { font-size: 40px; font-weight: var(--font-weight-bold); line-height: 1; color: var(--color-text-primary); }
.result-score-unit { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 2px; }
.result-hero h2 { font-size: var(--font-size-2xl); margin-bottom: var(--space-1); }
.result-subtitle { color: var(--color-text-secondary); }

.result-sections { text-align: left; max-width: 640px; margin: 0 auto; }
.result-section { margin-bottom: var(--space-5); }
.result-section h4 { font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); margin-bottom: var(--space-2); display: flex; align-items: center; gap: var(--space-2); }
.result-section p { color: var(--color-text-secondary); line-height: var(--line-height-relaxed); }
.result-section ul { padding-left: var(--space-5); color: var(--color-text-secondary); display: flex; flex-direction: column; gap: var(--space-1); }
.result-columns { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-5); }
.qa-review-list { display: flex; flex-direction: column; gap: var(--space-3); }
.qa-review-item { padding: var(--space-3) var(--space-4); background: var(--color-body-bg); border-radius: var(--radius-md); border: 1px solid var(--color-border-light); }
.qa-review-q { font-weight: var(--font-weight-medium); color: var(--color-primary-600); margin-bottom: var(--space-1); }
.qa-review-num { font-size: var(--font-size-xs); font-weight: var(--font-weight-bold); margin-right: var(--space-2); }
.qa-review-a { font-size: var(--font-size-sm); color: var(--color-text-secondary); line-height: 1.6; }
.result-actions { margin-top: var(--space-8); display: flex; justify-content: center; gap: var(--space-4); }

@media (max-width: 600px) {
  .mode-cards { grid-template-columns: 1fr; }
  .result-columns { grid-template-columns: 1fr; }
}
</style>
