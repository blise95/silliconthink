import type { SiteConfig } from '@/types'

export const siteMock: SiteConfig = {
  siteName: 'Silicon Think',
  tagline: '全栈开发者 · Vue & Spring · 技术博客与项目实践',
  seoDescription:
    'Silicon Think 个人网站，分享前端工程、后端架构与技术博客。',
  seoKeywords: 'Vue,Spring Boot,全栈,个人博客,技术分享',
  aboutMd: `## 关于我

我是一名全栈开发者，关注 **Vue 前端工程** 与 **Java 后端架构**，喜欢把复杂系统拆成可维护的模块。

### 我在做什么

- 构建清晰、可扩展的前后端分离应用
- 写技术博客，记录踩坑与最佳实践
- 探索 AI 辅助开发与工作流

### 工作方式

重视可读代码、规格驱动开发（OpenSpec）与渐进式交付。`,
  skills: [
    'Vue 3',
    'TypeScript',
    'Spring Boot',
    'MySQL',
    'MyBatis Plus',
    'Docker',
    '系统设计',
  ],
  contact: [
    { label: 'GitHub', url: 'https://github.com', icon: 'github' },
    { label: 'Email', url: 'mailto:hello@example.com', icon: 'mail' },
  ],
  ogImage: '/og-default.svg',
}
