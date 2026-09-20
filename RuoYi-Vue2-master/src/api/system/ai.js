import request from '@/utils/request'
import { getToken } from '@/utils/auth'

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

/**
 * 以 SSE 流接收 AI 回复。POST 请求无法使用原生 EventSource，因而在这里解析响应流。
 * @param {Object} data 对话请求
 * @param {Function} onEvent 每收到一个事件即回调 ({ type, content, sessionId })
 * @param {AbortSignal} signal 用于停止生成
 */
export async function streamAiMessage(data, onEvent, signal) {
  const response = await fetch(process.env.VUE_APP_BASE_API + '/system/ai/chat/stream', {
    method: 'POST',
    headers: {
      Authorization: 'Bearer ' + getToken(),
      Accept: 'text/event-stream',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data),
    signal
  })

  if (!response.ok) {
    throw new Error('AI 服务暂不可用，请稍后重试')
  }
  if (!response.body || !response.body.getReader) {
    throw new Error('当前浏览器不支持流式回复，请升级浏览器后重试')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const result = await reader.read()
    buffer += decoder.decode(result.value || new Uint8Array(), { stream: !result.done })
    buffer = dispatchCompleteEvents(buffer, onEvent)
    if (result.done) break
  }

  if (buffer.trim()) {
    dispatchEventBlock(buffer, onEvent)
  }
}

function dispatchCompleteEvents(buffer, onEvent) {
  const normalized = buffer.replace(/\r\n/g, '\n')
  const blocks = normalized.split('\n\n')
  const remainder = blocks.pop()
  blocks.forEach(block => dispatchEventBlock(block, onEvent))
  return remainder
}

function dispatchEventBlock(block, onEvent) {
  let eventName = ''
  const dataLines = []
  block.split('\n').forEach(line => {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  })
  if (dataLines.length === 0) return

  const event = JSON.parse(dataLines.join('\n'))
  if (!event.type && eventName) event.type = eventName
  onEvent(event)
}
