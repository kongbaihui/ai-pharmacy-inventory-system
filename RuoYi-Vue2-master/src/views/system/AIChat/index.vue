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
              v-hasPermi="['system:ai:report']"
              size="mini"
              icon="el-icon-data-analysis"
              @click="handleOpenReport"
            >库存月报</el-button>
            <el-button
              size="mini"
              icon="el-icon-delete"
              :disabled="sending || currentMessages.length <= 1"
              @click="handleClearCurrent"
            >清空</el-button>
          </div>
        </header>

        <section class="knowledge-strip" :class="'is-' + knowledgeState">
          <div class="knowledge-state">
            <span class="state-mark"><i :class="knowledgeStateIcon" /></span>
            <div>
              <strong>{{ knowledgeStateText }}</strong>
              <small>{{ knowledgeStateDescription }}</small>
            </div>
          </div>
          <div v-if="knowledgeStatus.buildId" class="knowledge-metrics">
            <span><b>{{ knowledgeStatus.successCount || 0 }}</b>/{{ knowledgeStatus.sourceCount || 0 }} 来源</span>
            <span><b>{{ knowledgeStatus.chunkCount || 0 }}</b> 片段</span>
            <span v-if="knowledgeStatus.finishedAt">{{ formatKnowledgeTime(knowledgeStatus.finishedAt) }}</span>
          </div>
          <div class="knowledge-actions">
            <el-button
              type="text"
              icon="el-icon-refresh"
              :loading="knowledgeStatusLoading"
              :disabled="knowledgeRebuilding"
              @click="loadKnowledgeStatus"
            >刷新</el-button>
            <el-button
              v-hasPermi="['system:ai:knowledge']"
              size="mini"
              icon="el-icon-upload2"
              :disabled="knowledgeRebuilding"
              @click="handleOpenKnowledgeImport"
            >导入资料</el-button>
            <el-button
              v-hasPermi="['system:ai:knowledge']"
              size="mini"
              type="primary"
              plain
              icon="el-icon-document-checked"
              :loading="knowledgeRebuilding"
              @click="handleKnowledgeRebuild"
            >{{ knowledgeRebuilding ? '正在构建' : '重新构建' }}</el-button>
          </div>
        </section>

        <div ref="msgBody" class="message-ledger" role="log" aria-label="对话记录" @scroll.passive="handleMessageScroll">
          <div
            v-for="(message, index) in currentMessages"
            :key="message.id || message.requestId || index"
            class="message-row"
            :class="[
              message.role === 'user' ? 'from-user' : 'from-ai',
              { 'is-streaming': message.streaming }
            ]"
          >
            <div class="message-avatar">
              <i :class="message.role === 'user' ? 'el-icon-user-solid' : 'el-icon-first-aid-kit'" />
            </div>
            <div class="message-column">
              <div class="message-meta">
                <strong>{{ message.role === 'user' ? '我' : '药房助手' }}</strong>
                <span>{{ formatTime(message.time) }}</span>
              </div>
              <div class="message-bubble" :class="{ 'is-local-message': message.localOnly }">
                <div v-if="message.thinking" class="thinking-state" role="status">
                  <span class="thinking-mark"><i class="el-icon-search" /></span>
                  <span class="thinking-copy">
                    <strong>正在整理回答</strong>
                    <small>核对实时库存与权威资料</small>
                  </span>
                  <span class="thinking-track" aria-hidden="true"><i /><i /><i /></span>
                </div>
                <div v-else-if="message.role === 'assistant'" class="ai-markdown" v-html="renderMarkdown(message.content)" />
                <div v-else class="message-plain">{{ message.content }}</div>
                <div v-if="message.streaming && !message.thinking" class="stream-status" role="status">
                  <span class="stream-status-dot" aria-hidden="true" />
                  <span>正在生成回答</span>
                </div>
                <div v-else-if="message.state === 'stopped'" class="message-state is-stopped" role="status">
                  <i class="el-icon-video-pause" />生成已停止，以上内容已保留
                </div>
                <div v-else-if="message.state === 'error'" class="message-state is-error" role="status">
                  <i class="el-icon-warning-outline" />{{ message.statusText || '生成中断，请稍后重试' }}
                </div>
              </div>
              <div v-if="message.sources && message.sources.length" class="source-list">
                <div class="source-list-heading">
                  <span><i class="el-icon-document-checked" />参考资料</span>
                  <small>{{ message.sources.length }} 项</small>
                </div>
                <template v-for="(source, sourceIndex) in message.sources">
                  <a
                    v-if="source.url"
                    :key="source.sourceId + '-link'"
                    :href="source.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="source-card"
                  >
                    <span class="source-index">{{ sourceIndex + 1 }}</span>
                    <span class="source-copy">
                      <strong>{{ source.title }}</strong><small>{{ source.authority }}</small>
                      <span v-if="source.citationIds && source.citationIds.length" class="source-citations">
                        <small v-for="citationId in source.citationIds" :key="citationId">{{ citationId }}</small>
                      </span>
                    </span>
                    <i class="el-icon-top-right source-link-mark" />
                  </a>
                  <div
                    v-else
                    :key="source.sourceId + '-local'"
                    class="source-card"
                  >
                    <span class="source-index">{{ sourceIndex + 1 }}</span>
                    <span class="source-copy">
                      <strong>{{ source.title }}</strong><small>{{ source.authority }}</small>
                      <span v-if="source.citationIds && source.citationIds.length" class="source-citations">
                        <small v-for="citationId in source.citationIds" :key="citationId">{{ citationId }}</small>
                      </span>
                    </span>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>

        <button v-if="sending && !autoFollowStream" type="button" class="jump-to-latest" @click="resumeAutoFollow">
          <i class="el-icon-bottom" />回到最新
        </button>

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

    <el-dialog
      title="月度库存分析"
      :visible.sync="reportVisible"
      width="880px"
      custom-class="monthly-report-dialog"
      :close-on-click-modal="!reportLoading"
    >
      <div class="report-toolbar">
        <el-date-picker
          v-model="reportMonth"
          type="month"
          value-format="yyyy-MM"
          placeholder="选择月份"
          :clearable="false"
          :disabled="reportLoading"
        />
        <el-button
          type="primary"
          icon="el-icon-data-analysis"
          :loading="reportLoading"
          @click="handleGenerateReport"
        >生成月报</el-button>
      </div>

      <div v-if="reportMetrics" class="report-metrics">
        <div><small>入库数量</small><strong>{{ reportMetrics.inboundQty || 0 }}</strong></div>
        <div><small>出库数量</small><strong>{{ reportMetrics.outboundQty || 0 }}</strong></div>
        <div><small>业务流水</small><strong>{{ reportMetrics.movementCount || 0 }}</strong></div>
        <div><small>当前过期批次</small><strong>{{ reportMetrics.expiredBatchCount || 0 }}</strong></div>
      </div>

      <div class="report-copy">
        <div v-if="reportLoading && !reportContent" class="thinking-state">
          <span class="thinking-mark"><i class="el-icon-data-analysis" /></span>
          <span class="thinking-copy">
            <strong>正在生成库存月报</strong>
            <small>核对月度指标、补货建议与效期风险</small>
          </span>
          <span class="thinking-track" aria-hidden="true"><i /><i /><i /></span>
        </div>
        <p v-else-if="!reportContent" class="report-empty">选择月份后生成报告，核心数字均来自库存流水。</p>
        <div v-else class="ai-markdown report-markdown" v-html="renderMarkdown(reportContent)" />
        <div v-if="reportLoading && reportContent" class="stream-status report-stream-status" role="status">
          <span class="stream-status-dot" aria-hidden="true" />
          <span>正在续写月报</span>
        </div>
      </div>
      <p v-if="reportMetrics" class="metric-basis">{{ reportMetrics.metricBasis }}</p>
    </el-dialog>

    <el-dialog
      title="导入药房知识资料"
      :visible.sync="knowledgeImportVisible"
      width="520px"
      custom-class="knowledge-import-dialog"
    >
      <el-form label-position="top">
        <el-form-item label="资料文件">
          <label class="knowledge-file-picker">
            <input type="file" accept=".pdf,.docx,.txt,.md" @change="handleKnowledgeFileChange">
            <i class="el-icon-upload2" />
            <span>{{ knowledgeImport.file ? knowledgeImport.file.name : '选择 PDF、DOCX、TXT 或 Markdown 文件' }}</span>
          </label>
        </el-form-item>
        <el-form-item label="资料标题">
          <el-input v-model.trim="knowledgeImport.title" maxlength="200" />
        </el-form-item>
        <el-form-item label="发布机构">
          <el-input v-model.trim="knowledgeImport.authority" maxlength="200" placeholder="例如：国家药品监督管理局" />
        </el-form-item>
        <el-form-item label="资料分类">
          <el-input v-model.trim="knowledgeImport.category" maxlength="100" placeholder="例如：药品说明书、药房制度" />
        </el-form-item>
        <el-form-item label="公开来源链接（可选）">
          <el-input v-model.trim="knowledgeImport.sourceUrl" maxlength="1000" placeholder="https://" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button :disabled="knowledgeImporting" @click="knowledgeImportVisible = false">取消</el-button>
        <el-button type="primary" :loading="knowledgeImporting" @click="handleKnowledgeImport">导入资料</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it/dist/browser/markdown-it.umd.min.js'
import {
  getKnowledgeStatus,
  getMonthlyInventoryMetrics,
  importKnowledgeDocument,
  rebuildKnowledge,
  streamAiMessage,
  streamMonthlyInventoryReport
} from '@/api/system/ai'

const STORAGE_KEY = 'ai_chat_sessions'
const WELCOME_MESSAGE = '你好，我是药房智能助手。我可以读取实时库存与出入库数据，也可以检索内置的权威药学资料。你可以直接描述需要核对的问题。'
const markdown = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: false
})
markdown.disable('image')
const markdownCache = new Map()
const MARKDOWN_CACHE_LIMIT = 80

const defaultLinkOpen = markdown.renderer.rules.link_open || ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))
markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  tokens[index].attrSet('target', '_blank')
  tokens[index].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, index, options, env, self)
}
markdown.renderer.rules.table_open = () => '<div class="markdown-table-wrap"><table>'
markdown.renderer.rules.table_close = () => '</table></div>'

function normalizeStreamingMarkdown(content) {
  const value = String(content || '')
    .replace(/\r\n?/g, '\n')
    .replace(/^(?:(?:i(?:'|’)?ll|i will|let me|i need to|first,\s*i(?:'|’)?ll)\b[^\n#。！？]*[.!]\s*)+/i, '')
  return value.split(/(```[\s\S]*?(?:```|$)|~~~[\s\S]*?(?:~~~|$))/g).map(segment => {
    if (segment.startsWith('```') || segment.startsWith('~~~')) return segment
    return segment
      .replace(/([^\n])(?=#{2,6}(?!#)[^\s#])/g, '$1\n\n')
      .replace(/^(#{1,6})(?=[^\s#])/gm, '$1 ')
      .replace(/^(#{1,6}\s+(?:[一二三四五六七八九十\d]+、)?(?:概览|进销情况|库存风险|重点药品|补货建议|过期清理建议|结论))(?=\S)/gm, '$1\n\n')
      .replace(/^(#{1,6}\s+[^|\n]+?)(\|[^\n]+\|)\s*$/gm, '$1\n\n$2')
      .replace(/([^\n])-(?=[A-Za-z\u4e00-\u9fff][^：:\n]{0,24}[：:])/g, '$1\n- ')
      .replace(/([^\n])(?=\*\*(?:\d+|[一二三四五六七八九十]+)[.、])/g, '$1\n\n')
      .replace(/^(\s*(?:-(?!-)|\+(?!\+)|\*(?!\*)))(?=\S)/gm, '$1 ')
  }).join('')
}

function renderMarkdownContent(content) {
  const source = String(content || '')
  if (markdownCache.has(source)) return markdownCache.get(source)
  const rendered = markdown.render(normalizeStreamingMarkdown(source))
  markdownCache.set(source, rendered)
  if (markdownCache.size > MARKDOWN_CACHE_LIMIT) {
    markdownCache.delete(markdownCache.keys().next().value)
  }
  return rendered
}

export default {
  name: 'AiChat',
  data() {
    return {
      inputText: '',
      sending: false,
      streamController: null,
      streamScrollFrame: null,
      autoFollowStream: true,
      knowledgeStatusLoading: false,
      knowledgeRebuilding: false,
      knowledgeStatus: { status: 'loading' },
      knowledgeImportVisible: false,
      knowledgeImporting: false,
      knowledgeImport: { file: null, title: '', authority: '', category: '', sourceUrl: '' },
      reportVisible: false,
      reportLoading: false,
      reportController: null,
      reportMonth: '',
      reportMetrics: null,
      reportContent: '',
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
    },
    knowledgeState() {
      if (this.knowledgeRebuilding) return 'building'
      return this.knowledgeStatus.status || 'unavailable'
    },
    knowledgeStateText() {
      const labels = {
        loading: '正在读取资料状态',
        ready: '权威资料已就绪',
        partial: '部分权威资料可用',
        not_built: '权威资料尚未构建',
        building: '正在构建权威资料',
        failed: '权威资料构建失败',
        unavailable: '暂时无法读取资料状态'
      }
      return labels[this.knowledgeState] || labels.unavailable
    },
    knowledgeStateDescription() {
      if (this.knowledgeState === 'not_built') return '管理员需要先执行一次构建，药学问答才能引用资料。'
      if (this.knowledgeState === 'building') return '正在下载、校验并切分官方资料，请保持页面开启。'
      if (this.knowledgeState === 'partial') return `有 ${this.knowledgeStatus.failedCount || 0} 个来源未成功，可继续使用已完成的资料。`
      if (this.knowledgeState === 'failed') return '有效来源数量不足，请检查服务器网络后重新构建。'
      if (this.knowledgeState === 'unavailable') return '确认后端已启动，并检查当前账号的 AI 对话权限。'
      if (this.knowledgeState === 'ready') return '药学回答将只依据已收录资料，并附标题、机构和原始链接。'
      return '正在确认当前可用的资料版本。'
    },
    knowledgeStateIcon() {
      if (this.knowledgeState === 'ready') return 'el-icon-check'
      if (this.knowledgeState === 'partial') return 'el-icon-warning-outline'
      if (this.knowledgeState === 'building' || this.knowledgeState === 'loading') return 'el-icon-loading'
      return 'el-icon-info'
    }
  },
  created() {
    this.loadSessions()
    if (this.sessions.length === 0) this.handleNewSession()
    this.loadKnowledgeStatus()
  },
  beforeDestroy() {
    if (this.streamController) this.streamController.abort()
    if (this.reportController) this.reportController.abort()
    if (this.streamScrollFrame) window.cancelAnimationFrame(this.streamScrollFrame)
  },
  methods: {
    handleOpenKnowledgeImport() {
      this.knowledgeImport = { file: null, title: '', authority: '', category: '', sourceUrl: '' }
      this.knowledgeImportVisible = true
    },
    handleKnowledgeFileChange(event) {
      const file = event.target.files && event.target.files[0]
      this.knowledgeImport.file = file || null
      if (file && !this.knowledgeImport.title) {
        this.knowledgeImport.title = file.name.replace(/\.[^.]+$/, '')
      }
    },
    async handleKnowledgeImport() {
      const item = this.knowledgeImport
      if (!item.file || !item.title || !item.authority || !item.category) {
        this.$modal.msgWarning('请选择文件并填写标题、发布机构和资料分类')
        return
      }
      if (item.file.size > 10 * 1024 * 1024) {
        this.$modal.msgWarning('单个知识文件不能超过10MB')
        return
      }
      const formData = new FormData()
      formData.append('file', item.file)
      formData.append('title', item.title)
      formData.append('authority', item.authority)
      formData.append('category', item.category)
      formData.append('sourceUrl', item.sourceUrl || '')
      this.knowledgeImporting = true
      try {
        const response = await importKnowledgeDocument(formData)
        this.knowledgeImportVisible = false
        if (response.data && response.data.duplicate) {
          this.$modal.msgWarning('该文件内容已经导入，无需重复上传')
        } else {
          this.$modal.msgSuccess('资料已导入，请重新构建知识库后使用')
        }
      } finally {
        this.knowledgeImporting = false
      }
    },
    handleOpenReport() {
      if (!this.reportMonth) {
        const now = new Date()
        this.reportMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      }
      this.reportVisible = true
      if (!this.reportMetrics) this.loadReportMetrics()
    },
    loadReportMetrics() {
      if (!this.reportMonth) return Promise.resolve()
      return getMonthlyInventoryMetrics(this.reportMonth).then(response => {
        this.reportMetrics = response.data || null
      })
    },
    async handleGenerateReport() {
      if (!this.reportMonth || this.reportLoading) return
      this.reportLoading = true
      this.reportContent = ''
      this.reportController = typeof AbortController === 'undefined' ? null : new AbortController()
      let serviceError = ''
      try {
        await this.loadReportMetrics()
        await streamMonthlyInventoryReport(this.reportMonth, event => {
          if (event.type === 'start' && event.metrics) this.reportMetrics = event.metrics
          if (event.type === 'delta' && event.content) this.reportContent += event.content
          if (event.type === 'error') {
            serviceError = event.content || '月报生成暂不可用，请稍后重试'
            throw new Error(serviceError)
          }
        }, this.reportController ? this.reportController.signal : undefined)
      } catch (error) {
        if (!error || error.name !== 'AbortError') {
          this.$modal.msgError(serviceError || '月报生成暂不可用，请稍后重试')
        }
      } finally {
        this.reportLoading = false
        this.reportController = null
      }
    },
    loadKnowledgeStatus() {
      if (this.knowledgeStatusLoading || this.knowledgeRebuilding) return
      this.knowledgeStatusLoading = true
      getKnowledgeStatus()
        .then(response => {
          this.knowledgeStatus = response && response.data
            ? response.data
            : { status: 'unavailable' }
        })
        .catch(() => {
          this.knowledgeStatus = { status: 'unavailable' }
        })
        .finally(() => {
          this.knowledgeStatusLoading = false
        })
    },
    handleKnowledgeRebuild() {
      if (this.knowledgeRebuilding) return
      this.$modal.confirm('将从已登记的官方来源和本地资料重新构建知识库，过程可能持续数分钟。是否继续？').then(() => {
        this.knowledgeRebuilding = true
        this.knowledgeStatus = Object.assign({}, this.knowledgeStatus, { status: 'building' })
        rebuildKnowledge()
          .then(response => {
            this.knowledgeStatus = response.data || { status: 'unavailable' }
            if (this.knowledgeStatus.status === 'ready') this.$modal.msgSuccess('知识库构建完成')
            else this.$modal.msgWarning('知识库已更新，但部分来源未能成功获取')
          })
          .catch(() => {
            this.$modal.msgError('知识库构建失败，请检查服务器网络后重试')
            this.knowledgeRebuilding = false
            this.loadKnowledgeStatus()
          })
          .finally(() => {
            this.knowledgeRebuilding = false
          })
      }).catch(() => {})
    },
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
      this.scheduleScrollToBottom(true)
    },
    handleSwitchSession(id) {
      if (this.sending || id === this.activeSessionId) return
      this.activeSessionId = id
      this.inputText = ''
      this.scheduleScrollToBottom(true)
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
        this.scheduleScrollToBottom(true)
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
      const userMessage = { id: 'U' + now, role: 'user', content: text, time: now }
      session.messages.push(userMessage)
      if (session.title === '新的药房任务' || session.title === '新会话') {
        session.title = text.length > 16 ? text.slice(0, 16) + '…' : text
      }
      session.updateTime = now
      this.inputText = ''
      this.sending = true
      this.autoFollowStream = true

      const assistantMessage = {
        id: 'A' + now,
        role: 'assistant',
        content: '',
        requestId: '',
        sources: [],
        thinking: true,
        streaming: true,
        state: 'streaming',
        statusText: '',
        time: Date.now()
      }
      session.messages.push(assistantMessage)
      this.scheduleScrollToBottom(true)

      const historyPairs = []
      for (let index = 0; index < session.messages.length - 1; index += 1) {
        const previousUser = session.messages[index]
        const previousAssistant = session.messages[index + 1]
        if (previousUser.role !== 'user' || previousUser.localOnly || !previousUser.content) continue
        if (previousAssistant.role !== 'assistant' || previousAssistant.streaming || previousAssistant.localOnly || !previousAssistant.content) continue
        historyPairs.push([
          { role: previousUser.role, content: previousUser.content.slice(0, 4000) },
          { role: previousAssistant.role, content: previousAssistant.content.slice(0, 4000) }
        ])
        index += 1
      }
      const selectedPairs = []
      let historyLength = 0
      for (let index = historyPairs.length - 1; index >= 0 && selectedPairs.length < 10; index -= 1) {
        const pair = historyPairs[index]
        const pairLength = pair[0].content.length + pair[1].content.length
        if (historyLength + pairLength > 16000) break
        selectedPairs.unshift(pair)
        historyLength += pairLength
      }
      const history = selectedPairs.reduce((messages, pair) => messages.concat(pair), [])

      this.streamController = typeof AbortController === 'undefined' ? null : new AbortController()
      let serviceError = ''

      try {
        await streamAiMessage({
          message: text,
          history,
          sessionId: session.remoteSessionId || session.id
        }, event => {
          if (event.sessionId) this.$set(session, 'remoteSessionId', event.sessionId)
          if (event.requestId) assistantMessage.requestId = event.requestId
          if (event.type === 'delta' && event.content) {
            assistantMessage.content += event.content
            if (assistantMessage.content.trim()) assistantMessage.thinking = false
            this.scheduleScrollToBottom()
          }
          if (event.type === 'error') {
            serviceError = event.content || 'AI 服务暂不可用，请稍后重试'
            throw new Error(serviceError)
          }
          if (event.type === 'sources' && Array.isArray(event.sources)) {
            assistantMessage.sources = event.sources
            this.scheduleScrollToBottom()
          }
          if (event.type === 'done') {
            assistantMessage.state = 'done'
            assistantMessage.streaming = false
          }
        }, this.streamController ? this.streamController.signal : undefined)

        if (!assistantMessage.content) {
          userMessage.localOnly = true
          assistantMessage.localOnly = true
          assistantMessage.content = 'AI 服务未返回有效内容，请稍后重试。'
          assistantMessage.state = 'error'
        }
      } catch (error) {
        userMessage.localOnly = true
        assistantMessage.localOnly = true
        if (error && error.name === 'AbortError') {
          assistantMessage.state = 'stopped'
        } else {
          assistantMessage.state = 'error'
          assistantMessage.statusText = serviceError || 'AI 服务暂不可用，请稍后重试。'
        }
      } finally {
        assistantMessage.thinking = false
        assistantMessage.streaming = false
        if (assistantMessage.state === 'streaming') assistantMessage.state = 'done'
        assistantMessage.time = Date.now()
        session.updateTime = Date.now()
        this.sending = false
        this.streamController = null
        this.saveSessions()
        this.scheduleScrollToBottom()
      }
    },
    stopGeneration() {
      if (this.streamController) this.streamController.abort()
    },
    handleMessageScroll() {
      const messageBody = this.$refs.msgBody
      if (!messageBody) return
      const distanceFromBottom = messageBody.scrollHeight - messageBody.scrollTop - messageBody.clientHeight
      this.autoFollowStream = distanceFromBottom < 96
    },
    scheduleScrollToBottom(force) {
      if (force) this.autoFollowStream = true
      if (!this.autoFollowStream || this.streamScrollFrame) return
      this.streamScrollFrame = window.requestAnimationFrame(() => {
        this.streamScrollFrame = null
        this.$nextTick(() => {
          if (this.autoFollowStream) this.scrollToBottom()
        })
      })
    },
    resumeAutoFollow() {
      this.scheduleScrollToBottom(true)
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
    },
    formatKnowledgeTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      if (Number.isNaN(date.getTime())) return ''
      const pad = number => (number < 10 ? '0' + number : number)
      return `${date.getMonth() + 1}月${date.getDate()}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    renderMarkdown(content) {
      return renderMarkdownContent(content)
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
  position: relative;
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
  white-space: nowrap;

  i { margin-right: 4px; }
  &.data { color: $blue; border-color: #bdd4e4; background: #edf5fa; }
  &.source { color: #477364; border-color: #c6dbd2; background: #eef6f2; }
}

.knowledge-strip {
  display: flex;
  min-height: 62px;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 9px 24px;
  border-bottom: 1px solid $line;
  background: #f3f7f5;

  &.is-partial, &.is-not_built, &.is-failed { background: #fbf7ee; }
  &.is-unavailable { background: #f7f7f6; }
}

.knowledge-state {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;

  strong, small { display: block; }
  strong { color: #28473d; font-size: 12px; font-weight: 600; }
  small { margin-top: 3px; color: #70827a; font-size: 10px; line-height: 1.4; }
}

.state-mark {
  display: grid;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  place-items: center;
  border: 1px solid #bdd3c9;
  border-radius: 7px;
  color: $green;
  background: #e4eee9;
}

.is-partial .state-mark, .is-not_built .state-mark, .is-failed .state-mark {
  border-color: #e1cda9;
  color: $amber;
  background: #f7ecd7;
}

.knowledge-metrics {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #6f8179;
  font-size: 10px;
  font-variant-numeric: tabular-nums;

  b { color: $ink; font-size: 13px; font-weight: 600; }
}

.knowledge-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;

  ::v-deep .el-button--primary.is-plain {
    border-color: #abc9bc;
    color: $green;
    background: #f8fbf9;
  }
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
  gap: 13px;
  margin: 0 auto 28px;
  max-width: 920px;

  &.from-user {
    flex-direction: row-reverse;
    .message-column { max-width: 70%; align-items: flex-end; }
    .message-meta { flex-direction: row-reverse; }
    .message-avatar { color: #fff; background: $blue; }
    .message-bubble {
      padding: 11px 15px;
      border-color: #2c6e9b;
      border-radius: 13px 4px 13px 13px;
      color: #fff;
      background: $blue;
      box-shadow: 0 6px 16px rgba(44, 110, 155, 0.14);
    }
  }

  &.from-ai {
    .message-column { width: calc(100% - 47px); max-width: 820px; }
    .message-bubble {
      width: 100%;
      padding: 18px 21px 19px;
      border-left: 3px solid #8eb5a4;
    }
    &.is-streaming .message-bubble {
      border-left-color: $amber;
      box-shadow: 0 8px 24px rgba(29, 54, 46, 0.08);
    }
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

.message-column { display: flex; min-width: 0; flex-direction: column; }
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
  border: 1px solid #d5e1da;
  border-radius: 4px 12px 12px;
  color: #263a33;
  background: #fff;
  box-shadow: 0 5px 18px rgba(29, 54, 46, 0.05);
  font-size: 14px;
  word-break: break-word;

  &.is-local-message { background: #fbfcfb; }
}

.message-plain { line-height: 1.75; white-space: pre-wrap; }

.thinking-state {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: 11px;

  .thinking-mark {
    display: grid;
    width: 34px;
    height: 34px;
    flex: 0 0 34px;
    place-items: center;
    border-radius: 50%;
    color: $green;
    background: $green-soft;
  }
  .thinking-copy { min-width: 0; flex: 1; }
  strong, small { display: block; }
  strong { color: #28473d; font-size: 13px; font-weight: 600; }
  small { margin-top: 2px; color: #7a8c84; font-size: 11px; }
}

.thinking-track {
  display: flex;
  align-items: center;
  gap: 3px;

  i {
    display: block;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: #78968a;
    animation: thinking 1.1s infinite ease-in-out;
    &:nth-child(2) { animation-delay: 0.12s; }
    &:nth-child(3) { animation-delay: 0.24s; }
  }
}

.stream-status {
  display: flex;
  width: fit-content;
  align-items: center;
  gap: 7px;
  margin-top: 14px;
  padding-top: 11px;
  border-top: 1px solid #e6ece8;
  color: #6e8179;
  font-size: 11px;

  .stream-status-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: $amber;
    box-shadow: 0 0 0 4px rgba(196, 122, 22, 0.12);
    animation: livePulse 1.4s ease-out infinite;
  }
}

.message-state {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 13px;
  padding-top: 10px;
  border-top: 1px solid #e6ece8;
  color: #71847c;
  font-size: 11px;

  &.is-stopped i { color: #8a765a; }
  &.is-error { color: #a04f43; }
}

.jump-to-latest {
  position: absolute;
  right: clamp(24px, 5vw, 76px);
  bottom: 147px;
  z-index: 3;
  padding: 7px 11px;
  border: 1px solid #bdcec5;
  border-radius: 999px;
  color: #315c4e;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 7px 20px rgba(29, 54, 46, 0.12);
  cursor: pointer;
  font-size: 11px;

  i { margin-right: 4px; }
  &:hover, &:focus { border-color: #789f8f; outline: none; background: #f5faf7; }
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
@keyframes livePulse {
  0% { box-shadow: 0 0 0 0 rgba(196, 122, 22, 0.28); }
  70%, 100% { box-shadow: 0 0 0 6px rgba(196, 122, 22, 0); }
}

@media (prefers-reduced-motion: reduce) {
  .thinking-track i, .stream-status-dot { animation: none; }
}

@media (max-width: 980px) {
  .ai-workbench { height: auto; min-height: calc(100vh - 84px); overflow: visible; }
  .workbench-shell { grid-template-columns: 1fr; overflow: visible; }
  .task-rail { min-height: auto; }
  .task-section { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; }
  .task-section .section-label { grid-column: 1 / -1; }
  .session-section, .source-note { display: none; }
  .conversation-panel { height: calc(100vh - 96px); min-height: 620px; max-height: 820px; overflow: hidden; }
  .conversation-header { align-items: flex-start; flex-direction: column; }
  .header-actions { flex-wrap: wrap; }
  .knowledge-strip { align-items: flex-start; flex-wrap: wrap; }
}

.source-list {
  display: grid;
  width: 100%;
  max-width: 720px;
  gap: 7px;
  margin-top: 10px;
}

.source-list-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2px 2px;
  color: #526d63;
  font-size: 11px;
  font-weight: 600;

  i { margin-right: 5px; color: $green; }
  small { color: #899a93; font-size: 10px; font-weight: 400; }
}

.source-card {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 10px;
  padding: 9px 11px;
  border: 1px solid #d4e1da;
  border-radius: 8px;
  color: #315c4e;
  background: #f6faf8;
  text-decoration: none;

  > .source-copy { min-width: 0; flex: 1; }
  strong, small { display: block; }
  strong { overflow: hidden; font-size: 11px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
  small { margin-top: 2px; color: #71857c; font-size: 10px; }
  &:hover, &:focus { border-color: #7eaa98; outline: none; background: #edf6f1; }
}

.source-index {
  display: grid;
  width: 24px;
  height: 24px;
  flex: 0 0 24px;
  place-items: center;
  border-radius: 50%;
  color: $green;
  background: #e3efe9;
  font-size: 10px;
  font-weight: 700;
}

.source-link-mark { color: #76978a; font-size: 12px; }

.source-citations {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 5px;

  small {
    margin: 0;
    padding: 2px 5px;
    border-radius: 4px;
    color: #55746a;
    background: #e7f0eb;
    font-size: 9px;
    line-height: 1.35;
  }
}

::v-deep .monthly-report-dialog {
  max-width: calc(100vw - 28px);
  border-radius: 12px;

  .el-dialog__header { border-bottom: 1px solid $line; }
  .el-dialog__body { padding: 20px 24px 24px; }
}

::v-deep .knowledge-import-dialog {
  max-width: calc(100vw - 28px);
  border-radius: 12px;
  .el-dialog__body { padding: 14px 24px 4px; }
  .el-form-item { margin-bottom: 14px; }
  .el-form-item__label { padding-bottom: 4px; color: #52685f; font-size: 12px; line-height: 1.4; }
}

.knowledge-file-picker {
  display: flex;
  min-height: 44px;
  align-items: center;
  gap: 9px;
  padding: 0 12px;
  border: 1px dashed #9eb9ad;
  border-radius: 8px;
  color: #3d6959;
  background: #f3f8f5;
  cursor: pointer;

  input { display: none; }
  span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  &:hover, &:focus-within { border-color: $green; background: #eaf3ee; }
}

.report-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  margin-top: 18px;
  overflow: hidden;
  border: 1px solid $line;
  border-radius: 8px;
  background: $line;

  div { padding: 12px 14px; background: #f8fbf9; }
  small, strong { display: block; }
  small { color: #72857c; font-size: 11px; }
  strong { margin-top: 4px; color: $green; font-size: 20px; font-weight: 600; }
}

.report-copy {
  min-height: 220px;
  max-height: min(62vh, 640px);
  margin-top: 16px;
  padding: 22px 24px;
  overflow: auto;
  border: 1px solid $line;
  border-radius: 8px;
  background: #fff;
}

.report-stream-status { margin-top: 18px; }

.report-empty { margin: 76px 0 0; color: #83938c; text-align: center; }
.metric-basis { margin: 10px 2px 0; color: #82938c; font-size: 10px; line-height: 1.5; }

@media (max-width: 640px) {
  .ai-workbench { padding: 8px; }
  .task-rail { padding: 16px 14px; }
  .rail-brand { padding-bottom: 14px; }
  .rail-section { padding-top: 14px; }
  .task-section { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
  .task-button { gap: 7px; margin-top: 0; padding: 9px 7px; }
  .task-button > i { width: 18px; font-size: 16px; }
  .task-button small { display: none; }
  .conversation-header { align-items: flex-start; flex-direction: column; padding: 14px 16px; }
  .header-actions { flex-wrap: wrap; }
  .knowledge-strip { padding: 10px 16px; }
  .knowledge-metrics { width: 100%; order: 3; }
  .message-ledger { padding: 22px 14px; }
  .message-row.from-ai .message-column { width: auto; max-width: none; flex: 1; }
  .message-row.from-user .message-column { max-width: 86%; }
  .message-avatar { width: 30px; height: 30px; flex-basis: 30px; }
  .message-row.from-ai .message-bubble { padding: 15px 16px 17px; }
  .composer { padding: 10px 14px; }
  .composer-context { flex-direction: column; gap: 4px; }
  .composer-actions > span { display: none; }
  .composer-actions { justify-content: flex-end; }
  .report-metrics { grid-template-columns: repeat(2, 1fr); }
  .jump-to-latest { right: 18px; bottom: 151px; }
}
</style>

<style lang="scss">
.ai-workbench .ai-markdown,
.monthly-report-dialog .report-markdown {
  color: #263a33;
  font-size: 14px;
  line-height: 1.78;
  overflow-wrap: anywhere;

  > :first-child { margin-top: 0 !important; }
  > :last-child { margin-bottom: 0 !important; }

  h1, h2, h3, h4 {
    color: #173f35;
    font-weight: 650;
    line-height: 1.4;
  }
  h1 { margin: 2px 0 17px; font-size: 20px; letter-spacing: -0.2px; }
  h2 {
    margin: 24px 0 11px;
    padding-left: 10px;
    border-left: 3px solid #8eb5a4;
    font-size: 16px;
  }
  h3 { margin: 19px 0 8px; font-size: 14px; }
  h4 { margin: 16px 0 7px; font-size: 13px; }
  p { margin: 0 0 12px; }
  strong { color: #193f35; font-weight: 650; }
  ul, ol { margin: 8px 0 14px; padding-left: 22px; }
  li { margin: 5px 0; padding-left: 2px; }
  li::marker { color: #4f806f; font-weight: 600; }
  a {
    color: #256889;
    text-decoration: underline;
    text-decoration-color: #a9c8d7;
    text-underline-offset: 3px;
  }
  a:hover, a:focus { color: #164d69; text-decoration-color: currentColor; }
  blockquote {
    margin: 14px 0;
    padding: 9px 13px;
    border-left: 3px solid #c9954d;
    color: #5f6f68;
    background: #faf7f0;
  }
  blockquote p { margin: 0; }
  hr { height: 1px; margin: 22px 0; border: 0; background: #dce6e0; }
  code {
    padding: 2px 5px;
    border-radius: 4px;
    color: #285547;
    background: #edf4f0;
    font-family: Consolas, "SFMono-Regular", monospace;
    font-size: 0.9em;
  }
  pre {
    margin: 13px 0 16px;
    padding: 13px 15px;
    overflow-x: auto;
    border-radius: 8px;
    color: #eaf3ef;
    background: #1f342d;
    line-height: 1.65;
  }
  pre code { padding: 0; color: inherit; background: transparent; }
  .markdown-table-wrap {
    max-width: 100%;
    margin: 13px 0 18px;
    overflow-x: auto;
    border: 1px solid #d5e1da;
    border-radius: 8px;
  }
  table { width: 100%; min-width: 520px; border-collapse: collapse; font-size: 12px; line-height: 1.55; }
  th, td { padding: 9px 11px; border-right: 1px solid #e0e8e3; border-bottom: 1px solid #e0e8e3; text-align: left; vertical-align: top; }
  th:last-child, td:last-child { border-right: 0; }
  tr:last-child td { border-bottom: 0; }
  th { color: #31584b; background: #edf5f1; font-weight: 650; white-space: nowrap; }
  tbody tr:nth-child(even) { background: #fafcfb; }
}

.monthly-report-dialog .report-markdown { font-size: 13px; line-height: 1.72; }

@media (max-width: 640px) {
  .ai-workbench .ai-markdown,
  .monthly-report-dialog .report-markdown { font-size: 13px; }
  .ai-workbench .ai-markdown h1,
  .monthly-report-dialog .report-markdown h1 { font-size: 18px; }
  .ai-workbench .ai-markdown h2,
  .monthly-report-dialog .report-markdown h2 { font-size: 15px; }
  .ai-workbench .ai-markdown table,
  .monthly-report-dialog .report-markdown table { min-width: 460px; }
}

</style>
