<template>
  <view class="ai-page" :style="{ height: windowHeight + 'px' }">
    <view class="workbench-header">
      <view class="header-copy">
        <text class="header-kicker">PHARMACY COPILOT</text>
        <text class="header-title">药房智能工作台</text>
        <text class="header-subtitle">连接实时库存与权威药学资料</text>
      </view>
      <view class="header-badges">
        <text class="badge inventory">实时库存</text>
        <text class="badge knowledge">权威资料</text>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="message-scroll"
      :scroll-into-view="scrollToId"
      scroll-with-animation
    >
      <view class="quick-section">
        <view class="section-heading">
          <text>常用任务</text>
          <text class="clear-action" :class="{ disabled: sending }" @click="handleClear">清空对话</text>
        </view>
        <scroll-view scroll-x class="quick-scroll" show-scrollbar="false">
          <view class="quick-list">
            <view
              v-for="task in quickTasks"
              :key="task.title"
              class="quick-card"
              :class="{ disabled: sending }"
              @click="handleQuickAsk(task.prompt)"
            >
              <view class="quick-icon"><uni-icons :type="task.icon" size="19" color="#173f35" /></view>
              <text class="quick-title">{{ task.title }}</text>
              <text class="quick-description">{{ task.description }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="message-list">
        <view
          v-for="(message, index) in messages"
          :key="index"
          :id="'message-' + index"
          class="message-row"
          :class="message.role === 'user' ? 'from-user' : 'from-ai'"
        >
          <view class="message-avatar" :class="message.role">
            <uni-icons
              :type="message.role === 'user' ? 'person-filled' : 'medal-filled'"
              size="17"
              :color="message.role === 'user' ? '#ffffff' : '#173f35'"
            />
          </view>
          <view class="message-column">
            <view class="message-meta">
              <text>{{ message.role === 'user' ? '我' : '药房助手' }}</text>
              <text>{{ formatTime(message.time) }}</text>
            </view>
            <view class="message-bubble">
              <view v-if="message.thinking" class="thinking-state">
                <text class="dot" /><text class="dot" /><text class="dot" />
                <text class="thinking-label">正在核对数据与资料</text>
              </view>
              <text v-else class="message-text" selectable>{{ message.content }}</text>
            </view>
          </view>
        </view>
        <view class="boundary-note">
          <uni-icons type="locked-filled" size="14" color="#667c73" />
          <text>库存结论来自业务数据；药学内容应附资料来源。AI 不替代药师审方或临床用药决定。</text>
        </view>
      </view>
    </scroll-view>

    <view class="composer">
      <view class="composer-box">
        <textarea
          v-model="inputText"
          class="composer-input"
          auto-height
          maxlength="2000"
          confirm-type="send"
          :confirm-hold="true"
          :disabled="sending"
          placeholder="描述要核对的库存或药学问题…"
          @confirm="handleSend"
        />
        <view class="composer-toolbar">
          <text>{{ inputText.length }}/2000</text>
          <view
            class="send-button"
            :class="{ disabled: !inputText.trim() || sending }"
            @click="handleSend"
          >
            <uni-icons v-if="sending" type="spinner-cycle" size="18" color="#ffffff" />
            <uni-icons v-else type="paperplane-filled" size="18" color="#ffffff" />
            <text>{{ sending ? '生成中' : '发送' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { sendAiMessage } from '@/api/system/ai'

const STORAGE_KEY = 'pharmacy_ai_mobile_conversation'
const WELCOME_MESSAGE = '你好，我是药房智能助手。我可以核对实时库存、效期和出入库数据，也可以从内置权威资料中检索药学信息。'

export default {
  data() {
    return {
      inputText: '',
      sending: false,
      windowHeight: 500,
      sessionId: '',
      messages: [],
      scrollToId: '',
      quickTasks: [
        {
          title: '补货优先级',
          description: '库存下限与近30天出库',
          icon: 'cart-filled',
          prompt: '请根据当前库存、库存上下限和近30天出库情况，列出需要补货的药品、建议补货数量及优先级。'
        },
        {
          title: '效期排查',
          description: '未来90天到期批次',
          icon: 'calendar-filled',
          prompt: '请查询未来90天内即将到期的药品批次，按紧急程度排序，并给出处理重点。'
        },
        {
          title: '运营简报',
          description: '汇总近30天业务风险',
          icon: 'bars',
          prompt: '请生成近30天药房库存运营简报，包含出入库、退货、盘点调整、效期处理和当前库存风险。'
        },
        {
          title: '药学资料',
          description: '检索权威法规与指南',
          icon: 'help-filled',
          prompt: '请检索知识库，说明药品储存与养护有哪些关键要求，并逐项标注权威资料来源。'
        }
      ]
    }
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync()
    this.windowHeight = systemInfo.windowHeight || 500
    this.loadConversation()
  },
  onUnload() {
    this.saveConversation()
  },
  methods: {
    createSessionId() {
      return 'APP' + Date.now() + Math.floor(Math.random() * 1000)
    },
    resetConversation() {
      const now = Date.now()
      this.sessionId = this.createSessionId()
      this.messages = [{
        role: 'assistant',
        content: WELCOME_MESSAGE,
        time: now,
        localOnly: true
      }]
    },
    loadConversation() {
      try {
        const saved = uni.getStorageSync(STORAGE_KEY)
        if (saved && saved.sessionId && Array.isArray(saved.messages) && saved.messages.length) {
          this.sessionId = saved.sessionId
          this.messages = saved.messages.filter(message => !message.thinking)
        } else {
          this.resetConversation()
        }
      } catch (error) {
        this.resetConversation()
      }
      this.$nextTick(this.scrollBottom)
    },
    saveConversation() {
      try {
        const messages = this.messages
          .filter(message => !message.thinking)
          .slice(-40)
        uni.setStorageSync(STORAGE_KEY, { sessionId: this.sessionId, messages })
      } catch (error) {
        // 存储空间不足时不影响当前对话。
      }
    },
    handleClear() {
      if (this.sending) return
      uni.showModal({
        title: '清空对话',
        content: '确认清空当前药房任务记录？',
        success: result => {
          if (!result.confirm) return
          this.resetConversation()
          this.inputText = ''
          this.saveConversation()
          this.$nextTick(this.scrollBottom)
        }
      })
    },
    handleQuickAsk(question) {
      if (this.sending) return
      this.inputText = question
      this.$nextTick(this.handleSend)
    },
    async handleSend() {
      const text = this.inputText.trim()
      if (!text || this.sending) return

      this.messages.push({ role: 'user', content: text, time: Date.now() })
      this.inputText = ''
      this.sending = true

      const assistantMessage = {
        role: 'assistant',
        content: '',
        thinking: true,
        time: Date.now()
      }
      this.messages.push(assistantMessage)
      this.$nextTick(this.scrollBottom)

      const history = this.messages
        .filter(message => !message.thinking && !message.localOnly && message.content)
        .slice(0, -1)
        .map(message => ({ role: message.role, content: message.content }))

      try {
        const response = await sendAiMessage({
          message: text,
          history,
          sessionId: this.sessionId
        })
        const data = response && response.data ? response.data : response
        const reply = data && (data.reply || data.content || data.message)
        assistantMessage.content = reply || 'AI 服务未返回有效内容，请稍后重试。'
        if (data && data.sessionId) this.sessionId = data.sessionId
      } catch (error) {
        assistantMessage.localOnly = true
        assistantMessage.content = 'AI 服务暂不可用，请稍后重试。'
      } finally {
        assistantMessage.thinking = false
        assistantMessage.time = Date.now()
        this.sending = false
        this.saveConversation()
        this.$nextTick(this.scrollBottom)
      }
    },
    scrollBottom() {
      const lastIndex = this.messages.length - 1
      this.scrollToId = ''
      this.$nextTick(() => {
        this.scrollToId = 'message-' + lastIndex
      })
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const pad = number => (number < 10 ? '0' + number : number)
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
$paper: #f4f7f5;
$green: #173f35;
$green-soft: #e3eee8;
$blue: #2c6e9b;
$ink: #1e2b27;
$line: #d6e0da;

page { height: 100%; overflow: hidden; background: $paper; }

.ai-page {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: $ink;
  background: $paper;
}

.workbench-header {
  display: flex;
  flex-shrink: 0;
  align-items: flex-end;
  justify-content: space-between;
  padding: 26rpx 30rpx 22rpx;
  color: #fff;
  background: $green;
}

.header-copy text { display: block; }
.header-kicker { color: #98beb0; font-size: 18rpx; font-weight: 700; letter-spacing: 2rpx; }
.header-title { margin-top: 7rpx; font-size: 34rpx; font-weight: 600; }
.header-subtitle { margin-top: 6rpx; color: #b7d0c7; font-size: 21rpx; }
.header-badges { display: flex; gap: 8rpx; padding-bottom: 2rpx; }
.badge {
  padding: 7rpx 12rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.22);
  border-radius: 999rpx;
  font-size: 18rpx;
  &.inventory { color: #c6e5f5; }
  &.knowledge { color: #cae2d9; }
}

.message-scroll { flex: 1; min-height: 0; box-sizing: border-box; }
.quick-section { padding: 24rpx 0 18rpx; border-bottom: 1rpx solid $line; background: #fff; }
.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx 14rpx;
  color: #6c7d76;
  font-size: 21rpx;
  font-weight: 600;
}
.clear-action { color: $blue; font-weight: 400; &.disabled { color: #aab4b0; } }
.quick-scroll { width: 100%; white-space: nowrap; }
.quick-list { display: inline-flex; gap: 14rpx; padding: 0 28rpx; }
.quick-card {
  display: inline-flex;
  width: 218rpx;
  min-height: 138rpx;
  box-sizing: border-box;
  flex-direction: column;
  padding: 16rpx;
  border: 1rpx solid #cfddd5;
  border-radius: 14rpx;
  vertical-align: top;
  background: #f9fbfa;
  white-space: normal;
  &.disabled { opacity: 0.55; }
}
.quick-icon {
  display: flex;
  width: 42rpx;
  height: 42rpx;
  align-items: center;
  justify-content: center;
  border-radius: 9rpx;
  background: $green-soft;
}
.quick-title { margin-top: 9rpx; font-size: 23rpx; font-weight: 600; }
.quick-description { margin-top: 4rpx; color: #788a83; font-size: 18rpx; line-height: 1.4; }

.message-list { padding: 30rpx 24rpx 20rpx; }
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  margin-bottom: 30rpx;
  &.from-user {
    flex-direction: row-reverse;
    .message-column { align-items: flex-end; }
    .message-meta { flex-direction: row-reverse; }
    .message-avatar { border-color: $blue; background: $blue; }
    .message-bubble { color: #fff; border-color: $blue; border-radius: 18rpx 5rpx 18rpx 18rpx; background: $blue; }
  }
}
.message-avatar {
  display: flex;
  width: 58rpx;
  height: 58rpx;
  flex: 0 0 58rpx;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #c7d8d0;
  border-radius: 12rpx;
  background: $green-soft;
}
.message-column { display: flex; max-width: 76%; flex-direction: column; }
.message-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin: 0 4rpx 7rpx;
  color: #819189;
  font-size: 18rpx;
  text:first-child { color: #40534c; font-weight: 600; }
}
.message-bubble {
  padding: 18rpx 22rpx;
  border: 1rpx solid #d3dfd8;
  border-radius: 5rpx 18rpx 18rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(30, 61, 51, 0.05);
}
.message-text { font-size: 26rpx; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
.thinking-state { display: flex; min-height: 34rpx; align-items: center; }
.dot {
  width: 9rpx;
  height: 9rpx;
  margin-right: 6rpx;
  border-radius: 50%;
  background: #76968a;
  animation: thinking 1.1s infinite ease-in-out;
  &:nth-child(2) { animation-delay: 0.12s; }
  &:nth-child(3) { animation-delay: 0.24s; }
}
.thinking-label { margin-left: 7rpx; color: #6f817a; font-size: 21rpx; }
.boundary-note {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  margin: 10rpx 12rpx 4rpx;
  padding: 18rpx;
  border: 1rpx solid $line;
  border-radius: 12rpx;
  color: #667c73;
  background: #edf3f0;
  font-size: 19rpx;
  line-height: 1.55;
}

.composer {
  flex-shrink: 0;
  padding: 16rpx 22rpx calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $line;
  background: #fff;
}
.composer-box { padding: 14rpx 14rpx 10rpx; border: 1rpx solid #b9cbc1; border-radius: 16rpx; background: #f8faf9; }
.composer-input {
  width: 100%;
  min-height: 68rpx;
  max-height: 180rpx;
  box-sizing: border-box;
  padding: 3rpx 5rpx;
  color: $ink;
  font-size: 26rpx;
  line-height: 1.55;
}
.composer-toolbar {
  display: flex;
  min-height: 54rpx;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
  padding: 8rpx 0 0 6rpx;
  border-top: 1rpx solid #e0e8e3;
  color: #8a9a93;
  font-size: 18rpx;
}
.send-button {
  display: flex;
  min-width: 120rpx;
  height: 54rpx;
  align-items: center;
  justify-content: center;
  gap: 7rpx;
  border-radius: 9rpx;
  color: #fff;
  background: $green;
  font-size: 22rpx;
  &.disabled { background: #aebbb5; }
}

@keyframes thinking {
  0%, 70%, 100% { opacity: 0.35; transform: translateY(0); }
  35% { opacity: 1; transform: translateY(-3rpx); }
}
</style>
