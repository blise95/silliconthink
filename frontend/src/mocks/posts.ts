import type { Post } from '@/types'

export const postsMock: Post[] = [
  {
    id: '1',
    title: 'Vue 3 组合式 API 实践笔记',
    slug: 'vue3-composition-api-notes',
    summary: '从 Options API 迁移到 Composition API 的经验与模式总结。',
    contentMd: `## 为什么用 Composition API

组合式 API 让逻辑复用更自然，\`setup\` 中的代码按**功能**组织，而不是按选项分散。

### 典型模式

\`\`\`typescript
export function useCounter(initial = 0) {
  const count = ref(initial)
  const inc = () => count.value++
  return { count, inc }
}
\`\`\`

> 小函数 + 清晰命名，比 mixin 更易维护。

## 与 Pinia 配合

全局状态交给 Pinia，组件内局部状态用 \`ref\` / \`computed\` 即可。`,
    coverUrl: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800&auto=format&fit=crop',
    tags: ['vue', 'typescript'],
    publishedAt: '2026-06-01T10:00:00.000Z',
    status: 'published',
  },
  {
    id: '2',
    title: '个人站点 Mock 数据层设计',
    slug: 'mock-data-layer-design',
    summary: '用 Repository 抽象隔离 Mock 与真实 API，便于前后端并行开发。',
    contentMd: `## Repository 模式

页面只依赖接口，不直接读取 mock 文件：

\`\`\`typescript
export interface PostRepository {
  list(params: PostListParams): Promise<Paginated<Post>>
  getBySlug(slug: string): Promise<Post | null>
}
\`\`\`

## Mock 行为对齐 API

- 只返回 \`published\` 文章
- 支持分页、标签、关键词筛选
- 环境变量切换数据源`,
    tags: ['vue', 'engineering'],
    publishedAt: '2026-06-10T08:00:00.000Z',
    status: 'published',
  },
  {
    id: '3',
    title: 'Spring Boot 单体 vs 微服务：个人项目的取舍',
    slug: 'spring-monolith-vs-microservices',
    summary: '个人网站不必上微服务，单体 + 清晰模块边界往往更合适。',
    contentMd: `## 结论先行

对个人站点：**Spring Boot 单体 + MySQL** 足够；微服务适合团队与规模，不是默认答案。

## 何时考虑拆分

- 多团队独立发布
- 某模块需要独立扩缩容
- 明确的领域边界与 SLA 差异`,
    tags: ['spring-boot', 'architecture'],
    publishedAt: '2026-06-12T14:30:00.000Z',
    status: 'published',
  },
  {
    id: '4',
    title: 'CSS 设计令牌与 BEM 在 Vue 中的用法',
    slug: 'css-design-tokens-bem-vue',
    summary: '参考 Omni-Growth 官网，用 CSS Variables + BEM 构建一致视觉。',
    contentMd: `## 设计令牌

在 \`:root\` 定义颜色、间距、字体，组件只引用变量：

\`\`\`css
:root {
  --color-cinnabar: #b83b2d;
  --spacing-lg: 24px;
}
\`\`\`

## BEM 命名

\`block__element--modifier\` 与 Vue scoped CSS 配合良好。`,
    tags: ['vue', 'engineering'],
    publishedAt: '2026-06-15T09:00:00.000Z',
    status: 'published',
  },
  {
    id: '5',
    title: 'Markdown 博客渲染与安全',
    slug: 'markdown-rendering-security',
    summary: 'markdown-it + highlight.js 实现代码高亮，注意 XSS 边界。',
    contentMd: `## 渲染管线

1. \`markdown-it\` 解析 MD
2. \`highlight.js\` 处理 fenced code
3. \`v-html\` 输出（内容需可信或 sanitize）

\`\`\`javascript
const md = new MarkdownIt({
  highlight(str, lang) {
    return hljs.highlight(str, { language: lang }).value
  },
})
\`\`\``,
    tags: ['typescript', 'engineering'],
    publishedAt: '2026-06-17T11:00:00.000Z',
    status: 'published',
  },
  {
    id: '6',
    title: '草稿：尚未发布的架构草图',
    slug: 'draft-architecture-sketch',
    summary: '这是一篇草稿，不应出现在公开列表。',
    contentMd: '草稿内容',
    tags: ['architecture'],
    publishedAt: '2026-06-18T00:00:00.000Z',
    status: 'draft',
  },
]
