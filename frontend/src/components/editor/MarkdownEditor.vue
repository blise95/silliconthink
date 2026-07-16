<script setup lang="ts">
import { nextTick, ref } from 'vue'
import {
  Bold,
  Code,
  Code2,
  Heading2,
  ImagePlus,
  Italic,
  Link as LinkIcon,
  List,
} from 'lucide-vue-next'
import MarkdownViewer from '@/components/blog/MarkdownViewer.vue'
import { postRepo } from '@/services'

const content = defineModel<string>({ required: true })

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const uploading = ref(false)
const uploadError = ref('')

function wrapSelection(before: string, after = before, placeholder = '') {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = content.value.slice(start, end) || placeholder
  const next = content.value.slice(0, start) + before + selected + after + content.value.slice(end)
  content.value = next
  void nextTick(() => {
    el.focus()
    const cursor = start + before.length + selected.length
    el.setSelectionRange(cursor, cursor)
  })
}

function insertBlock(block: string) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const prefix = start > 0 && content.value[start - 1] !== '\n' ? '\n\n' : ''
  const suffix = '\n\n'
  const insert = prefix + block + suffix
  content.value = content.value.slice(0, start) + insert + content.value.slice(start)
  void nextTick(() => {
    el.focus()
    const cursor = start + insert.length
    el.setSelectionRange(cursor, cursor)
  })
}

function onBold() {
  wrapSelection('**', '**', '加粗文本')
}
function onItalic() {
  wrapSelection('*', '*', '斜体')
}
function onInlineCode() {
  wrapSelection('`', '`', 'code')
}
function onCodeBlock() {
  insertBlock('```ts\n// code\n```')
}
function onHeading() {
  insertBlock('## 标题')
}
function onList() {
  insertBlock('- 列表项')
}
function onLink() {
  wrapSelection('[', '](https://)', '链接文字')
}

async function insertImageFile(file: File) {
  uploadError.value = ''
  uploading.value = true
  try {
    const url = await postRepo.uploadImage(file)
    insertBlock(`![${file.name || 'image'}](${url})`)
  } catch (e) {
    uploadError.value = e instanceof Error ? e.message : '上传失败'
  } finally {
    uploading.value = false
  }
}

function onPickImage(ev: Event) {
  const input = ev.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (file) void insertImageFile(file)
}

async function onPaste(ev: ClipboardEvent) {
  const items = ev.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      ev.preventDefault()
      const file = item.getAsFile()
      if (file) await insertImageFile(file)
      return
    }
  }
}
</script>

<template>
  <div class="md-editor">
    <div class="md-editor__toolbar">
      <button type="button" title="加粗" @click="onBold"><Bold :size="16" /></button>
      <button type="button" title="斜体" @click="onItalic"><Italic :size="16" /></button>
      <button type="button" title="标题" @click="onHeading"><Heading2 :size="16" /></button>
      <button type="button" title="列表" @click="onList"><List :size="16" /></button>
      <button type="button" title="链接" @click="onLink"><LinkIcon :size="16" /></button>
      <button type="button" title="行内代码" @click="onInlineCode"><Code :size="16" /></button>
      <button type="button" title="代码块" @click="onCodeBlock"><Code2 :size="16" /></button>
      <label class="md-editor__upload" title="插入图片">
        <ImagePlus :size="16" />
        <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden @change="onPickImage" />
      </label>
      <span v-if="uploading" class="md-editor__hint">上传中…</span>
      <span v-if="uploadError" class="md-editor__error">{{ uploadError }}</span>
    </div>
    <div class="md-editor__panes">
      <textarea
        ref="textareaRef"
        v-model="content"
        class="md-editor__source"
        placeholder="用 Markdown 书写…"
        spellcheck="false"
        @paste="onPaste"
      />
      <div class="md-editor__preview">
        <MarkdownViewer :content="content || '*预览将显示在这里*'" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.md-editor {
  border: 1px solid var(--color-line, var(--color-border));
  border-radius: var(--radius-md);
  background: var(--color-surface);
  overflow: hidden;
}

.md-editor__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--color-line, var(--color-border));
}

.md-editor__toolbar button,
.md-editor__upload {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-ink-soft, var(--color-text));
  cursor: pointer;
}

.md-editor__toolbar button:hover,
.md-editor__upload:hover {
  border-color: var(--color-line, var(--color-border));
  background: var(--color-paper, var(--color-surface-muted));
}

.md-editor__hint {
  margin-left: 0.5rem;
  font-size: 0.8125rem;
  color: var(--color-mist, var(--color-text-muted));
}

.md-editor__error {
  margin-left: 0.5rem;
  font-size: 0.8125rem;
  color: var(--color-cinnabar, #b83b2d);
}

.md-editor__panes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 420px;
}

.md-editor__source {
  width: 100%;
  min-height: 420px;
  padding: 1rem 1.25rem;
  border: none;
  border-right: 1px solid var(--color-line, var(--color-border));
  resize: vertical;
  outline: none;
  font-family: var(--font-mono);
  font-size: 0.9375rem;
  line-height: 1.7;
  background: var(--color-surface);
  color: var(--color-ink, var(--color-text));
}

.md-editor__preview {
  padding: 1rem 1.25rem;
  overflow: auto;
  background: var(--color-paper, var(--color-surface-muted));
}

@media (max-width: 900px) {
  .md-editor__panes {
    grid-template-columns: 1fr;
  }

  .md-editor__source {
    border-right: none;
    border-bottom: 1px solid var(--color-line, var(--color-border));
    min-height: 280px;
  }
}
</style>
