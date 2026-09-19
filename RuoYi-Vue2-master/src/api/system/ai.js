import request from '@/utils/request'

/**
 * 发送 AI 对话消息
 * @param {Object} data
 * @param {string} data.message  用户输入内容
 * @param {Array}  data.history  历史对话记录，格式 [{role:'user'|'assistant', content:'...'}]
 * @param {string} data.sessionId 会话ID（可选，便于后端做多轮上下文）
 * @returns Promise
 */
export function sendAiMessage(data) {
  return request({
    url: '/system/ai/chat',
    method: 'post',
    data: data,
    timeout: 60000
  })
}