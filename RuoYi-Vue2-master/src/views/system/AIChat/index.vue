<template>
  <div class="app-container ai-chat-page">
    <el-row :gutter="16" class="chat-layout">
      <!-- ==================== 左侧：会话列表 ==================== -->
      <el-col :span="5">
        <el-card shadow="never" class="session-card">
          <div slot="header" class="session-header">
            <span>会话列表</span>
            <el-button
              type="primary"
              size="mini"
              icon="el-icon-plus"
              @click="handleNewSession"
            >新建</el-button>
          </div>

          <div class="session-list">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="session-item"
              :class="{ active: s.id === activeSessionId }"
              @click="handleSwitchSession(s.id)"
            >
              <i class="el-icon-chat-line-round session-icon"></i>
              <div class="session-info">
                <div class="session-title">{{ s.title }}</div>
                <div class="session-time">{{ formatTime(s.updateTime) }}</div>
              </div>
              <i
                class="el-icon-delete session-del"
                title="删除会话"
                @click.stop="handleDeleteSession(s.id)"
              ></i>
            </div>

            <div v-if="sessions.length === 0" class="session-empty">
              暂无会话，点击右上角「新建」
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- ==================== 右侧：聊天区 ==================== -->
      <el-col :span="19">
        <el-card shadow="never" class="chat-card">
          <!-- 聊天头部 -->
          <div slot="header" class="chat-header">
            <div class="chat-title">
              <i class="el-icon-cpu"></i>
              <span>AI 智能助手</span>
              <el-tag size="mini" type="success" effect="plain">在线</el-tag>
            </div>
            <div class="chat-tools">
              <el-button
                size="mini"
                icon="el-icon-delete"
                :disabled="currentMessages.length === 0"
                @click="handleClearCurrent"
              >清空当前对话</el-button>
            </div>
          </div>

          <!-- 消息区 -->
          <div ref="msgBody" class="chat-body">
            <div
              v-for="(msg, idx) in currentMessages"
              :key="idx"
              class="msg-row"
              :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
            >
              <div class="msg-avatar">
                <i :class="msg.role === 'user' ? 'el-icon-user-solid' : 'el-icon-cpu'"></i>
              </div>
              <div class="msg-content">
                <div class="msg-bubble">
                  <div v-if="msg.thinking" class="thinking">
                    <span></span><span></span><span></span>
                    <em>思考中…</em>
                  </div>
                  <template v-else>{{ msg.content }}</template>
                </div>
                <div class="msg-time">{{ formatTime(msg.time) }}</div>
              </div>
            </div>

            <div v-if="currentMessages.length === 0" class="chat-empty">
              <i class="el-icon-chat-dot-round"></i>
              <p>开始和 AI 助手对话吧</p>
              <div class="quick-questions">
                <el-tag
                  v-for="q in quickQuestions"
                  :key="q"
                  class="quick-tag"
                  @click="handleQuickAsk(q)"
                >{{ q }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="chat-footer">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="请输入您的问题，回车发送，Shift + 回车换行"
              @keydown.native.enter.exact.prevent="handleSend"
            />
            <div class="footer-actions">
              <span class="tips">
                <i class="el-icon-info"></i>
                按 Enter 发送，Shift + Enter 换行
              </span>
              <el-button
                type="primary"
                icon="el-icon-position"
                :loading="sending"
                :disabled="!inputText.trim()"
                @click="handleSend"
              >发送</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { sendAiMessage } from '@/api/system/ai'

const STORAGE_KEY = 'ai_chat_sessions'

export default {
  name: 'AiChat',
  data() {
    return {
      // 输入框内容
      inputText: '',
      // 是否正在等待回复
      sending: false,
      // 会话列表
      sessions: [],
      // 当前激活的会话 ID
      activeSessionId: null,
      // 快捷提问
      quickQuestions: [
        '如何查询库存不足的药品？',
        '本月有哪些药品即将过期？',
        '帮我统计各分类药品的数量'
      ]
    }
  },
  computed: {
    /** 当前会话对象 */
    currentSession() {
      return this.sessions.find(s => s.id === this.activeSessionId) || null
    },
    /** 当前会话的消息列表 */
    currentMessages() {
      return this.currentSession ? this.currentSession.messages : []
    }
  },
  created() {
    this.loadSessions()
    // 没有会话时自动新建一个
    if (this.sessions.length === 0) {
      this.handleNewSession()
    }
  },
  methods: {
    /* ---------------- 会话管理 ---------------- */

    /** 从本地存储加载会话 */
    loadSessions() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY)
        const list = raw ? JSON.parse(raw) : []
        this.sessions = Array.isArray(list) ? list : []
        if (this.sessions.length > 0) {
          this.activeSessionId = this.sessions[0].id
        }
      } catch (e) {
        this.sessions = []
      }
    },

    /** 持久化会话 */
    saveSessions() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.sessions))
      } catch (e) {
        // 忽略存储异常（如隐私模式）
      }
    },

    /** 新建会话 */
    handleNewSession() {
      const now = Date.now()
      const session = {
        id: 'S' + now + Math.floor(Math.random() * 1000),
        title: '新会话',
        createTime: now,
        updateTime: now,
        messages: [
          {
            role: 'assistant',
            content: '您好，我是 AI 智能助手，有什么可以帮您？',
            time: now
          }
        ]
      }
      this.sessions.unshift(session)
      this.activeSessionId = session.id
      this.inputText = ''
      this.saveSessions()
      this.$nextTick(this.scrollToBottom)
    },

    /** 切换会话 */
    handleSwitchSession(id) {
      if (id === this.activeSessionId) return
      this.activeSessionId = id
      this.inputText = ''
      this.$nextTick(this.scrollToBottom)
    },

    /** 删除会话 */
    handleDeleteSession(id) {
      this.$modal.confirm('是否确认删除该会话？').then(() => {
        const idx = this.sessions.findIndex(s => s.id === id)
        if (idx === -1) return
        this.sessions.splice(idx, 1)
        // 若删除的是当前会话，自动切到第一个
        if (this.activeSessionId === id) {
          this.activeSessionId = this.sessions.length > 0 ? this.sessions[0].id : null
        }
        // 全部删光后自动新建
        if (this.sessions.length === 0) {
          this.handleNewSession()
        } else {
          this.saveSessions()
        }
      }).catch(() => {})
    },

    /** 清空当前会话的消息（保留会话本身） */
    handleClearCurrent() {
      const session = this.currentSession
      if (!session) return
      this.$modal.confirm('是否确认清空当前对话？').then(() => {
        session.messages = [
          {
            role: 'assistant',
            content: '对话已清空，有什么可以帮您？',
            time: Date.now()
          }
        ]
        session.updateTime = Date.now()
        this.saveSessions()
        this.$nextTick(this.scrollToBottom)
      }).catch(() => {})
    },

    /* ---------------- 消息发送 ---------------- */

    /** 快捷提问 */
    handleQuickAsk(question) {
      this.inputText = question
      this.$nextTick(() => this.handleSend())
    },

    /** 发送消息 */
    handleSend() {
      const text = this.inputText.trim()
      if (!text || this.sending) return

      const session = this.currentSession
      if (!session) return

      const now = Date.now()

      // 1. 推入用户消息
      session.messages.push({ role: 'user', content: text, time: now })
      // 首次对话用问题前 12 个字作为标题
      if (session.title === '新会话') {
        session.title = text.length > 12 ? text.slice(0, 12) + '…' : text
      }
      session.updateTime = now
      this.inputText = ''
      this.sending = true
      this.$nextTick(this.scrollToBottom)

      // 2. 推入 AI 思考中占位
      const aiMsg = {
        role: 'assistant',
        content: '',
        thinking: true,
        time: Date.now()
      }
      session.messages.push(aiMsg)
      this.$nextTick(this.scrollToBottom)

      // 3. 组装历史（不含正在思考的这条）
      const history = session.messages
        .filter(m => !m.thinking)
        .map(m => ({ role: m.role, content: m.content }))

      // 4. 发送请求
      sendAiMessage({
        message: text,
        history,
        sessionId: session.id
      })
        .then(res => {
          const reply =
            (res && res.data && (res.data.reply || res.data.content || res.data.message)) ||
            (res && (res.reply || res.content)) ||
            '抱歉，我暂时没有理解您的问题。'
          aiMsg.thinking = false
          aiMsg.content = reply
          aiMsg.time = Date.now()
        })
        .catch(err => {
          aiMsg.thinking = false
          aiMsg.content = '请求失败：' + (err.message || '网络异常，请稍后重试')
          aiMsg.time = Date.now()
        })
        .finally(() => {
          session.updateTime = Date.now()
          this.sending = false
          this.saveSessions()
          this.$nextTick(this.scrollToBottom)
        })
    },

    /* ---------------- 工具方法 ---------------- */

    /** 滚动到底部 */
    scrollToBottom() {
      const el = this.$refs.msgBody
      if (el) el.scrollTop = el.scrollHeight
    },

    /** 时间格式化 */
    formatTime(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      const pad = n => (n < 10 ? '0' + n : n)
      const now = new Date()
      const isToday =
        d.getFullYear() === now.getFullYear() &&
        d.getMonth() === now.getMonth() &&
        d.getDate() === now.getDate()
      if (isToday) {
        return `${pad(d.getHours())}:${pad(d.getMinutes())}`
      }
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat-page {
  /* 减去若依顶部导航栏 + 标签栏的高度，让页面占满剩余视口 */
  height: calc(100vh - 84px);
  box-sizing: border-box;
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  /* el-row：接管剩余空间，min-height:0 保证内部可滚动 */
  .chat-layout {
    flex: 1;
    min-height: 0;
    height: auto !important;
  }

  /* el-col：撑满 el-row 高度 */
  ::v-deep .el-col {
    height: 100%;
  }

  /* el-card：纵向 flex，让 header/body 分别占位 */
  ::v-deep .el-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    margin-bottom: 0;
    overflow: hidden;
  }

  ::v-deep .el-card__header {
    padding: 12px 16px;
    flex-shrink: 0;
  }

  /* el-card__body：占满卡片剩余高度，内部再纵向分配 body/footer */
  ::v-deep .el-card__body {
    flex: 1;
    padding: 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  /* ============ 左侧会话列表 ============ */
  .session-card {
    .session-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: 600;
      color: #303133;
    }

    .session-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;

      &::-webkit-scrollbar { width: 6px; }
      &::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
    }

    .session-item {
      position: relative;
      display: flex;
      align-items: center;
      padding: 10px 12px;
      margin-bottom: 6px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
        .session-del { opacity: 1; }
      }

      &.active {
        background: #ecf5ff;
        .session-icon { color: #409eff; }
        .session-title { color: #409eff; font-weight: 600; }
      }
    }

    .session-icon {
      font-size: 18px;
      color: #909399;
      margin-right: 10px;
      flex-shrink: 0;
    }

    .session-info {
      flex: 1;
      min-width: 0;
    }

    .session-title {
      font-size: 13px;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .session-time {
      font-size: 11px;
      color: #909399;
      margin-top: 2px;
    }

    .session-del {
      font-size: 15px;
      color: #f56c6c;
      opacity: 0;
      transition: opacity 0.2s;
      flex-shrink: 0;
      &:hover { color: #f78989; }
    }

    .session-empty {
      text-align: center;
      color: #c0c4cc;
      font-size: 13px;
      padding: 40px 0;
    }
  }

  /* ============ 右侧聊天区 ============ */
  .chat-card {
    .chat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .chat-title {
      display: flex;
      align-items: center;
      font-weight: 600;
      color: #303133;
      gap: 8px;

      i { font-size: 20px; color: #409eff; }
    }

    /* 消息区：flex:1 撑满，滚动条只在自己身上 */
    .chat-body {
      flex: 1;
      padding: 20px 24px;
      overflow-y: auto;
      background: #f7f8fa;
      min-height: 0;

      &::-webkit-scrollbar { width: 6px; }
      &::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
    }

    .msg-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 20px;

      &.is-user {
        flex-direction: row-reverse;

        .msg-avatar {
          background: #c6e2ff;
          color: #409eff;
        }

        .msg-bubble {
          background: #409eff;
          color: #fff;
          border-radius: 12px 2px 12px 12px;
        }

        .msg-time { text-align: right; }
      }

      &.is-ai {
        .msg-avatar {
          background: #e1f0ff;
          color: #409eff;
        }

        .msg-bubble {
          background: #fff;
          color: #303133;
          border-radius: 2px 12px 12px 12px;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
        }
      }
    }

    .msg-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      flex-shrink: 0;
    }

    .msg-content {
      max-width: 70%;
      margin: 0 12px;
    }

    .msg-bubble {
      padding: 10px 14px;
      font-size: 14px;
      line-height: 1.65;
      word-break: break-word;
      white-space: pre-wrap;
    }

    .msg-time {
      font-size: 11px;
      color: #a8abb2;
      margin-top: 4px;
    }

    /* 思考中动画 */
    .thinking {
      display: flex;
      align-items: center;
      height: 22px;

      span {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: #b0b3b8;
        margin-right: 4px;
        animation: bounce 1.2s infinite ease-in-out;
        &:nth-child(2) { animation-delay: 0.15s; }
        &:nth-child(3) { animation-delay: 0.3s; }
      }
      em {
        font-style: normal;
        color: #909399;
        font-size: 13px;
        margin-left: 4px;
      }
    }

    .chat-empty {
      text-align: center;
      color: #909399;
      padding: 80px 0 0;

      i { font-size: 48px; color: #c0c4cc; }
      p { margin: 12px 0 20px; font-size: 14px; }

      .quick-questions {
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
        gap: 10px;

        .quick-tag {
          cursor: pointer;
          transition: all 0.2s;
          &:hover { color: #409eff; border-color: #409eff; }
        }
      }
    }

    /* 输入区：固定高度，不参与拉伸 */
    .chat-footer {
      padding: 12px 20px 16px;
      border-top: 1px solid #ebeef5;
      background: #fff;
      flex-shrink: 0;

      ::v-deep .el-textarea__inner {
        font-size: 14px;
        border-radius: 6px;
        line-height: 1.6;
        resize: none;
      }

      .footer-actions {
        margin-top: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .tips {
          font-size: 12px;
          color: #909399;
          i { margin-right: 4px; }
        }
      }
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
</style>