<template>
  <div class="app-container ai-workbench">
    <div class="workbench-shell">
      <aside class="task-rail">
        <div class="rail-brand">
          <div class="brand-mark"><i class="el-icon-first-aid-kit" /></div>
          <div>
            <p>PHARMACY COPILOT</p>
            <h2>药房智能工作台</h2>
          </div>
        </div>

        <div class="rail-section task-section">
          <div class="section-label">常用任务</div>
          <button
            v-for="task in quickTasks"
            :key="task.title"
            type="button"
            class="task-button"
            :disabled="sending"
            @click="handleQuickAsk(task.prompt)"
          >
            <i :class="task.icon" />
            <span>
              <strong>{{ task.title }}</strong>
              <small>{{ task.description }}</small>
            </span>
          </button>
        </div>

        <div class="rail-section session-section">
          <div class="section-heading">
            <span class="section-label">工作记录</span>
            <el-button type="text" icon="el-icon-plus" :disabled="sending" @click="handleNewSession">新建</el-button>
          </div>
          <div class="session-list">
            <button
              v-for="session in sessions"
              :key="session.id"
              type="button"
              class="session-item"
              :class="{ active: session.id === activeSessionId }"
              :disabled="sending"
              @click="handleSwitchSession(session.id)"
            >
              <span class="session-copy">
                <strong>{{ session.title }}</strong>
                <small>{{ formatTime(session.updateTime) }}</small>
              </span>
              <i
                class="el-icon-close session-delete"
                title="删除记录"
                aria-label="删除记录"
                @click.stop="handleDeleteSession(session.id)"
              />
            </button>
          </div>
        </div>

        <div class="source-note">
          <i class="el-icon-lock" />
          <div>
            <strong>回答边界</strong>
            <p>库存结论来自实时业务数据；药学内容仅依据内置权威资料并附来源。</p>
          </div>
        </div>
      </aside>

      <main class="conversation-panel">
        <header class="conversation-header">
          <div>
            <span class="eyebrow">当前任务</span>
            <h1>{{ currentSession ? currentSession.title : '药房智能工作台' }}</h1>
          </div>
          <div class="header-actions">
            <span class="status-pill data"><i class="el-icon-connection" />实时库存</span>
            <span class="status-pill source"><i class="el-icon-document-checked" />权威资料</span>
            <el-button
              size="mini"
              icon="el-icon-delete"
              :disabled="sending || currentMessages.length <= 1"
              @click="handleClearCurrent"
            >清空</el-button>
          </div>
        </header>

        <div ref="msgBody" class="message-ledger" aria-live="polite">
          <div
            v-for="(message, index) in currentMessages"
            :key="index"
            class="message-row"
            :class="message.role === 'user' ? 'from-user' : 'from-ai'"
          >
            <div class="message-avatar">
              <i :class="message.role === 'user' ? 'el-icon-user-solid' : 'el-icon-first-aid-kit'" />
            </div>
            <div class="message-column">
              <div class="message-meta">
                <strong>{{ message.role === 'user' ? '我' : '药房助手' }}</strong>
                <span>{{ formatTime(message.time) }}</span>
              </div>
              <div class="message-bubble">
                <div v-if="message.thinking" class="thinking-state">
                  <span /><span /><span />
                  <em>正在核对数据与资料</em>
                </div>
                <template v-else>{{ message.content }}</template>
                <span v-if="message.streaming && !message.thinking" class="stream-caret" aria-hidden="true" />
              </div>
            </div>
          </div>
        </div>

        <footer class="composer">
          <div class="composer-context">
            <span><i class="el-icon-data-analysis" /> 可查询库存、效期与出入库统计</span>
            <span><i class="el-icon-reading" /> 药学回答标注资料来源</span>
          </div>
          <div class="composer-box">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              resize="none"
              maxlength="2000"
              show-word-limit
              placeholder="例如：列出当前库存不足的药品，并给出补货优先级"
              @keydown.native.enter.exact.prevent="handleSend"
            />
            <div class="composer-actions">
              <span>Enter 发送 · Shift + Enter 换行</span>
              <el-button v-if="sending" type="danger" plain icon="el-icon-video-pause" @click="stopGeneration">
                停止生成
              </el-button>
              <el-button
                v-else
                type="primary"
                icon="el-icon-position"
                :disabled="!inputText.trim()"
                @click="handleSend"
              >发送</el-button>
            </div>
          </div>
          <p class="safety-line">AI 结果用于辅助药房运营，不替代药师审方、临床诊断或用药决定。</p>
        </footer>
      </main>
    </div>
  </div>
</template>

<script>
import { streamAiMessage } from '@/api/system/ai'

const STORAGE_KEY = 'ai_chat_sessions'
const WELCOME_MESSAGE = '你好，我是药房智能助手。我可以读取实时库存与出入库数据，也可以检索内置的权威药学资料。你可以直接描述需要核对的问题。'

export default {
  name: 'AiChat',
  data() {
    return {
      inputText: '',
      sending: false,
      streamController: null,
      sessions: [],
      activeSessionId: null,
      quickTasks: [
        {
          title: '补货优先级',
          description: '结合库存下限与近 30 天出库',
          icon: 'el-icon-shopping-cart-2',
          prompt: '请根据当前库存、库存上下限和近30天出库情况，列出需要补货的药品、建议补货数量及优先级。'
        },
        {
          title: '运营简报',
          description: '汇总近 30 天业务与风险',
          icon: 'el-icon-data-line',
          prompt: '请生成近30天药房库存运营简报，包含出入库、退货、盘点调整、效期处理和当前库存风险。'
        },
        {
          title: '效期排查',
          description: '定位近期到期批次',
          icon: 'el-icon-alarm-clock',
          prompt: '请查询未来90天内即将到期的药品批次，按紧急程度排序，并给出处理重点。'
        },
        {
          title: '权威药学资料',
          description: '检索法规、指南与用药资料',
          icon: 'el-icon-reading',
          prompt: '请检索知识库，说明药品储存与养护有哪些关键要求，并逐项标注权威资料来源。'
        }
      ]
    }
  },
  computed: {
    currentSession() {
      return this.sessions.find(session => session.id === this.activeSessionId) || null
    },
    currentMessages() {
      return this.currentSession ? this.currentSession.messages : []
    }
  },
  created() {
    this.loadSessions()
    if (this.sessions.length === 0) this.handleNewSession()
  },
  beforeDestroy() {
    if (this.streamController) this.streamController.abort()
  },
  methods: {
    loadSessions() {
      try {
        const stored = localStorage.getItem(STORAGE_KEY)
        const sessions = stored ? JSON.parse(stored) : []
        this.sessions = Array.isArray(sessions) ? sessions : []
        if (this.sessions.length > 0) this.activeSessionId = this.sessions[0].id
      } catch (error) {
        this.sessions = []
      }
    },
    saveSessions() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.sessions))
      } catch (error) {
        // 浏览器禁用本地存储时，会话仍可在当前页面继续使用。
      }
    },
    handleNewSession() {
      if (this.sending) return
      const now = Date.now()
      const session = {
        id: 'S' + now + Math.floor(Math.random() * 1000),
        title: '新的药房任务',
        createTime: now,
        updateTime: now,
        messages: [{ role: 'assistant', content: WELCOME_MESSAGE, time: now, localOnly: true }]
      }
      this.sessions.unshift(session)
      this.activeSessionId = session.id
      this.inputText = ''
      this.saveSessions()
      this.$nextTick(this.scrollToBottom)
    },
    handleSwitchSession(id) {
      if (this.sending || id === this.activeSessionId) return
      this.activeSessionId = id
      this.inputText = ''
      this.$nextTick(this.scrollToBottom)
    },
    handleDeleteSession(id) {
      if (this.sending) return
      this.$modal.confirm('是否确认删除这条工作记录？').then(() => {
        const index = this.sessions.findIndex(session => session.id === id)
        if (index === -1) return
        this.sessions.splice(index, 1)
        if (this.activeSessionId === id) {
          this.activeSessionId = this.sessions.length > 0 ? this.sessions[0].id : null
        }
        if (this.sessions.length === 0) this.handleNewSession()
        else this.saveSessions()
      }).catch(() => {})
    },
    handleClearCurrent() {
      const session = this.currentSession
      if (!session || this.sending) return
      this.$modal.confirm('是否确认清空当前对话？').then(() => {
        const now = Date.now()
        session.messages = [{ role: 'assistant', content: WELCOME_MESSAGE, time: now, localOnly: true }]
        session.updateTime = now
        session.title = '新的药房任务'
        this.saveSessions()
        this.$nextTick(this.scrollToBottom)
      }).catch(() => {})
    },
    handleQuickAsk(question) {
      if (this.sending) return
      this.inputText = question
      this.$nextTick(this.handleSend)
    },
    async handleSend() {
      const text = this.inputText.trim()
      const session = this.currentSession
      if (!text || !session || this.sending) return

      const now = Date.now()
      session.messages.push({ role: 'user', content: text, time: now })
      if (session.title === '新的药房任务' || session.title === '新会话') {
        session.title = text.length > 16 ? text.slice(0, 16) + '…' : text
      }
      session.updateTime = now
      this.inputText = ''
      this.sending = true

      const assistantMessage = {
        role: 'assistant',
        content: '',
        thinking: true,
        streaming: true,
        time: Date.now()
      }
      session.messages.push(assistantMessage)
      this.$nextTick(this.scrollToBottom)

      const history = session.messages
        .filter(message => !message.streaming && !message.localOnly && message.content)
        .slice(0, -1)
        .map(message => ({ role: message.role, content: message.content }))

      this.streamController = typeof AbortController === 'undefined' ? null : new AbortController()
      let serviceError = ''

      try {
        await streamAiMessage({
          message: text,
          history,
          sessionId: session.remoteSessionId || session.id
        }, event => {
          if (event.sessionId) this.$set(session, 'remoteSessionId', event.sessionId)
          if (event.type === 'delta' && event.content) {
            assistantMessage.thinking = false
            assistantMessage.content += event.content
            this.$nextTick(this.scrollToBottom)
          }
          if (event.type === 'error') {
            serviceError = event.content || 'AI 服务暂不可用，请稍后重试'
            throw new Error(serviceError)
          }
        }, this.streamController ? this.streamController.signal : undefined)

        if (!assistantMessage.content) {
          assistantMessage.localOnly = true
          assistantMessage.content = 'AI 服务未返回有效内容，请稍后重试。'
        }
      } catch (error) {
        assistantMessage.localOnly = true
        if (error && error.name === 'AbortError') {
          if (!assistantMessage.content) assistantMessage.content = '本次生成已停止。'
        } else {
          assistantMessage.content = serviceError || 'AI 服务暂不可用，请稍后重试。'
        }
      } finally {
        assistantMessage.thinking = false
        assistantMessage.streaming = false
        assistantMessage.time = Date.now()
        session.updateTime = Date.now()
        this.sending = false
        this.streamController = null
        this.saveSessions()
        this.$nextTick(this.scrollToBottom)
      }
    },
    stopGeneration() {
      if (this.streamController) this.streamController.abort()
    },
    scrollToBottom() {
      const messageBody = this.$refs.msgBody
      if (messageBody) messageBody.scrollTop = messageBody.scrollHeight
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const pad = number => (number < 10 ? '0' + number : number)
      const now = new Date()
      const today = date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() && date.getDate() === now.getDate()
      if (today) return `${pad(date.getHours())}:${pad(date.getMinutes())}`
      return `${date.getMonth() + 1}月${date.getDate()}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
$paper: #f7faf8;
$ink: #1e2b27;
$green: #173f35;
$green-soft: #e5efea;
$blue: #2c6e9b;
$amber: #c47a16;
$line: #dbe5df;

.ai-workbench {
  height: calc(100vh - 84px);
  min-height: 620px;
  padding: 18px;
  box-sizing: border-box;
  overflow: hidden;
  color: $ink;
  background: #edf2ef;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

.workbench-shell {
  display: grid;
  grid-template-columns: 292px minmax(0, 1fr);
  height: 100%;
  max-width: 1540px;
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid #cfdbd4;
  border-radius: 12px;
  background: $paper;
  box-shadow: 0 16px 42px rgba(29, 54, 46, 0.08);
}

.task-rail {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 22px 18px 18px;
  color: #eef6f2;
  background: $green;
}

.rail-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 6px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);

  .brand-mark {
    display: grid;
    width: 40px;
    height: 40px;
    place-items: center;
    border-radius: 8px;
    color: $green;
    background: #dcece4;
    font-size: 20px;
  }

  p {
    margin: 0 0 3px;
    color: #9fc5b7;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 1.4px;
  }

  h2 { margin: 0; font-size: 17px; font-weight: 600; }
}

.rail-section { padding-top: 20px; }
.section-label {
  color: #8fb5a8;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
}

.task-button {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  color: #eef6f2;
  text-align: left;
  background: transparent;
  cursor: pointer;

  > i { width: 22px; color: #a7d2c3; font-size: 18px; text-align: center; }
  span { min-width: 0; }
  strong, small { display: block; }
  strong { margin-bottom: 3px; font-size: 13px; font-weight: 600; }
  small { color: #9ab9ae; font-size: 11px; line-height: 1.4; }

  &:hover, &:focus {
    outline: none;
    border-color: rgba(255, 255, 255, 0.12);
    background: rgba(255, 255, 255, 0.07);
  }
  &:disabled { cursor: not-allowed; opacity: 0.55; }
}

.session-section {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;

  ::v-deep .el-button { color: #c7ddd5; font-size: 12px; }
}

.session-list {
  min-height: 0;
  margin-top: 5px;
  overflow-y: auto;
  scrollbar-width: thin;
}

.session-item {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
  padding: 9px 10px;
  border: 0;
  border-radius: 7px;
  color: #cadbd5;
  text-align: left;
  background: transparent;
  cursor: pointer;

  &.active { color: #fff; background: #235247; }
  &:hover { background: #204b41; }
  &:disabled { cursor: default; }
  &:hover .session-delete, &:focus .session-delete { opacity: 1; }
}

.session-copy {
  min-width: 0;
  strong, small { display: block; }
  strong { overflow: hidden; font-size: 12px; font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
  small { margin-top: 3px; color: #83a89c; font-size: 10px; }
}

.session-delete {
  padding: 5px;
  color: #d4a48b;
  opacity: 0;
}

.source-note {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.11);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.08);

  > i { padding-top: 2px; color: #a8c9bd; }
  strong { font-size: 11px; }
  p { margin: 4px 0 0; color: #9db9af; font-size: 10px; line-height: 1.55; }
}

.conversation-panel {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  background: $paper;
}

.conversation-header {
  display: flex;
  min-height: 76px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 0 24px;
  border-bottom: 1px solid $line;
  background: #fff;

  .eyebrow { color: #779086; font-size: 10px; font-weight: 700; letter-spacing: 1px; }
  h1 { max-width: 620px; margin: 4px 0 0; overflow: hidden; font-size: 18px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
}

.header-actions { display: flex; align-items: center; gap: 8px; }
.status-pill {
  padding: 5px 9px;
  border: 1px solid;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;

  i { margin-right: 4px; }
  &.data { color: $blue; border-color: #bdd4e4; background: #edf5fa; }
  &.source { color: #477364; border-color: #c6dbd2; background: #eef6f2; }
}

.message-ledger {
  flex: 1;
  min-height: 0;
  padding: 30px clamp(24px, 5vw, 76px);
  overflow-y: auto;
  background-color: $paper;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin: 0 auto 24px;
  max-width: 920px;

  &.from-user {
    flex-direction: row-reverse;
    .message-column { align-items: flex-end; }
    .message-meta { flex-direction: row-reverse; }
    .message-avatar { color: #fff; background: $blue; }
    .message-bubble { color: #fff; border-color: $blue; background: $blue; }
  }
}

.message-avatar {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  place-items: center;
  border: 1px solid #c9dcd3;
  border-radius: 8px;
  color: $green;
  background: $green-soft;
}

.message-column { display: flex; max-width: 78%; flex-direction: column; }
.message-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 0 3px 6px;
  strong { font-size: 12px; font-weight: 600; }
  span { color: #899a93; font-size: 10px; font-variant-numeric: tabular-nums; }
}

.message-bubble {
  position: relative;
  padding: 12px 15px;
  border: 1px solid #d5e1da;
  border-radius: 4px 12px 12px;
  color: #263a33;
  background: #fff;
  box-shadow: 0 5px 18px rgba(29, 54, 46, 0.05);
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.thinking-state {
  display: flex;
  min-height: 24px;
  align-items: center;

  span {
    width: 6px;
    height: 6px;
    margin-right: 4px;
    border-radius: 50%;
    background: #76968a;
    animation: thinking 1.1s infinite ease-in-out;
    &:nth-child(2) { animation-delay: 0.12s; }
    &:nth-child(3) { animation-delay: 0.24s; }
  }
  em { margin-left: 6px; color: #6e817a; font-size: 12px; font-style: normal; }
}

.stream-caret {
  display: inline-block;
  width: 2px;
  height: 15px;
  margin-left: 3px;
  vertical-align: -2px;
  background: $amber;
  animation: caret 0.9s step-end infinite;
}

.composer {
  flex-shrink: 0;
  padding: 12px clamp(24px, 5vw, 76px) 14px;
  border-top: 1px solid $line;
  background: #fff;
}

.composer-context {
  display: flex;
  gap: 18px;
  max-width: 920px;
  margin: 0 auto 8px;
  color: #647b72;
  font-size: 11px;
  i { margin-right: 4px; color: $green; }
}

.composer-box {
  max-width: 920px;
  margin: 0 auto;
  padding: 10px 11px 8px;
  border: 1px solid #bfcfc6;
  border-radius: 10px;
  background: $paper;
  box-shadow: 0 5px 16px rgba(28, 55, 46, 0.05);

  ::v-deep .el-textarea__inner {
    padding: 4px 6px;
    border: 0;
    color: $ink;
    background: transparent;
    box-shadow: none;
    font-family: inherit;
    line-height: 1.65;
  }
  ::v-deep .el-input__count { bottom: -29px; color: #90a098; background: transparent; }
}

.composer-actions {
  display: flex;
  min-height: 34px;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0 0 6px;
  border-top: 1px solid #e1e9e4;
  color: #82938c;
  font-size: 11px;

  ::v-deep .el-button--primary { border-color: $green; background: $green; }
}

.safety-line {
  max-width: 920px;
  margin: 8px auto 0;
  color: #87978f;
  font-size: 10px;
  text-align: center;
}

@keyframes thinking {
  0%, 70%, 100% { opacity: 0.35; transform: translateY(0); }
  35% { opacity: 1; transform: translateY(-3px); }
}
@keyframes caret { 50% { opacity: 0; } }

@media (prefers-reduced-motion: reduce) {
  .thinking-state span, .stream-caret { animation: none; }
}

@media (max-width: 980px) {
  .ai-workbench { height: auto; min-height: calc(100vh - 84px); overflow: visible; }
  .workbench-shell { grid-template-columns: 1fr; overflow: visible; }
  .task-rail { min-height: auto; }
  .task-section { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; }
  .task-section .section-label { grid-column: 1 / -1; }
  .session-section, .source-note { display: none; }
  .conversation-panel { min-height: 720px; }
}

@media (max-width: 640px) {
  .ai-workbench { padding: 8px; }
  .task-section { grid-template-columns: 1fr; }
  .conversation-header { align-items: flex-start; flex-direction: column; padding: 14px 16px; }
  .header-actions { flex-wrap: wrap; }
  .message-ledger { padding: 22px 14px; }
  .message-column { max-width: 86%; }
  .composer { padding: 10px 14px; }
  .composer-context { flex-direction: column; gap: 4px; }
  .composer-actions > span { display: none; }
  .composer-actions { justify-content: flex-end; }
}
</style>
