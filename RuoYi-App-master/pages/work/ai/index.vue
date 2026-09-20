<template>
  <view class="ai-page" :style="{ height: windowHeight + 'px' }">
    <!-- 消息区 -->
    <scroll-view
      scroll-y
      class="msg-scroll"
      :scroll-into-view="scrollToId"
      scroll-with-animation
    >
      <view class="msg-list">
        <view
          v-for="(msg, idx) in messages"
          :key="idx"
          :id="'msg-' + idx"
          class="msg-row"
          :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
        >
          <view class="msg-avatar" :class="msg.role">
            <uni-icons
              :type="msg.role === 'user' ? 'person-filled' : 'chat-filled'"
              size="18"
              :color="msg.role === 'user' ? '#409eff' : '#67c23a'"
            ></uni-icons>
          </view>
          <view class="msg-bubble">
            <view v-if="msg.thinking" class="thinking">
              <text class="dot"></text>
              <text class="dot"></text>
              <text class="dot"></text>
              <text class="thinking-text">思考中…</text>
            </view>
            <text v-else class="msg-text">{{ msg.content }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="input-area">
      <input
        class="input-box"
        v-model="inputText"
        placeholder="请输入问题…"
        confirm-type="send"
        :adjust-position="false"
        @confirm="handleSend"
        :disabled="sending"
      />
      <view
        class="send-btn"
        :class="{ disabled: !inputText.trim() || sending }"
        @click="handleSend"
      >
        <uni-icons type="paperplane-filled" size="20" color="#fff"></uni-icons>
      </view>
    </view>
  </view>
</template>

<script>
import { sendAiMessage } from '@/api/system/ai'

export default {
  data() {
    return {
      inputText: '',
      sending: false,
      // 内容区高度（不含原生导航栏），onLoad 时从系统信息里取
      windowHeight: 500,
      messages: [
        {
          role: 'assistant',
          content: '您好，我是 AI 智能助手，有什么可以帮您？',
          time: Date.now()
        }
      ],
      scrollToId: ''
    }
  },
  onLoad() {
    const sys = uni.getSystemInfoSync()
    // windowHeight 在 uni-app 里就是「不含导航栏和 tabbar」的可用高度
    this.windowHeight = sys.windowHeight || 500
  },
  methods: {
    async handleSend() {
      const text = this.inputText.trim()
      if (!text || this.sending) return

      // 1. 推入用户消息
      this.messages.push({ role: 'user', content: text, time: Date.now() })
      this.inputText = ''
      this.sending = true
      this.$nextTick(() => this.scrollBottom())

      // 2. 推入 AI 思考中占位
      const aiMsg = { role: 'assistant', content: '', thinking: true }
      this.messages.push(aiMsg)
      this.$nextTick(() => this.scrollBottom())

      // 3. 组装历史（不含正在思考的这条）
      const history = this.messages
        .filter(m => !m.thinking)
        .slice(0, -1)
        .map(m => ({ role: m.role, content: m.content }))

      // 4. 发送请求
      try {
        const res = await sendAiMessage({ message: text, history })
        const reply =
          (res && res.data && (res.data.reply || res.data.content || res.data.message)) ||
          (res && (res.reply || res.content)) ||
          '抱歉，我暂时没有理解您的问题。'
        aiMsg.thinking = false
        aiMsg.content = reply
        aiMsg.time = Date.now()
      } catch (e) {
        aiMsg.thinking = false
        aiMsg.content = '请求失败：' + (e.message || '网络异常，请稍后重试')
        aiMsg.time = Date.now()
      } finally {
        this.sending = false
        this.$nextTick(() => this.scrollBottom())
      }
    },

    scrollBottom() {
      const last = this.messages.length - 1
      this.scrollToId = 'msg-' + last
    }
  }
}
</script>

<style lang="scss" scoped>
page {
  background: #f5f7fa;
  height: 100%;
  overflow: hidden;
}

/* 整页 flex 纵向布局：消息区占满剩余空间，输入区固定底部 */
.ai-page {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 消息区：flex: 1 吃掉除输入框外的所有空间，自己内部滚动 */
.msg-scroll {
  flex: 1;
  min-height: 0;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
  overflow-y: auto;
}

.msg-list {
  padding-bottom: 20rpx;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 28rpx;

  &.is-user {
    flex-direction: row-reverse;

    .msg-bubble {
      background: #409eff;
      color: #fff;
      border-radius: 20rpx 4rpx 20rpx 20rpx;
    }
  }

  &.is-ai {
    .msg-bubble {
      background: #fff;
      color: #303133;
      border-radius: 4rpx 20rpx 20rpx 20rpx;
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    }
  }
}

.msg-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.user {
    background: rgba(64, 158, 255, 0.12);
  }
  &.assistant {
    background: rgba(103, 194, 58, 0.12);
  }
}

.msg-bubble {
  max-width: 70%;
  padding: 20rpx 24rpx;
  margin: 0 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg-text {
  font-size: 28rpx;
  line-height: 1.6;
}

/* 思考中动画 */
.thinking {
  display: flex;
  align-items: center;
  height: 32rpx;
}

.thinking .dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #b0b3b8;
  margin-right: 6rpx;
  animation: bounce 1.2s infinite ease-in-out;
}

.thinking .dot:nth-child(2) {
  animation-delay: 0.15s;
}

.thinking .dot:nth-child(3) {
  animation-delay: 0.3s;
}

.thinking-text {
  font-size: 24rpx;
  color: #909399;
  margin-left: 10rpx;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.7);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区：普通流布局，固定在 flex 底部 */
.input-area {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #ebeef5;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.input-box {
  flex: 1;
  height: 76rpx;
  background: #f5f7fa;
  border-radius: 38rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 20rpx;
  flex-shrink: 0;

  &.disabled {
    background: #c0c4cc;
  }
}
</style>
