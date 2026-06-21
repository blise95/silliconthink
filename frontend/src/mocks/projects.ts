import type { Project } from '@/types'

export const projectsMock: Project[] = [
  {
    id: '1',
    title: '个人网站（Vue + Mock）',
    slug: 'personal-website-vue-mock',
    summary: 'OpenSpec 驱动的个人站点，Repository 抽象 + Omni-Growth 风格 UI。',
    contentMd: `## 项目简介

本仓库前端阶段使用 Vue 3 + Vite，数据来自 Mock Repository。

## 亮点

- 规格驱动开发（OpenSpec）
- CSS 设计令牌 + BEM
- 预留 Spring Boot API 对接`,
    coverUrl: 'https://images.unsplash.com/photo-1461742680684-dccba630e2f6?w=800&auto=format&fit=crop',
    category: 'Web',
    techStack: ['Vue 3', 'TypeScript', 'Vite', 'Pinia'],
    demoUrl: 'https://example.com',
    repoUrl: 'https://github.com',
    featured: true,
    sortOrder: 100,
    status: 'published',
  },
  {
    id: '2',
    title: '任务看板 API',
    slug: 'task-board-api',
    summary: 'Spring Boot 单体 REST API，JWT 鉴权与 MyBatis Plus。',
    contentMd: '演示用后端练习项目，包含用户、任务、标签模块。',
    coverUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop',
    category: 'Backend',
    techStack: ['Spring Boot', 'MySQL', 'MyBatis Plus'],
    repoUrl: 'https://github.com',
    featured: true,
    sortOrder: 90,
    status: 'published',
  },
  {
    id: '3',
    title: '组件库 Playground',
    slug: 'component-playground',
    summary: '可复用 UI 组件与文档站原型。',
    contentMd: '组件库 playground 与文档示例。',
    coverUrl: 'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=800&auto=format&fit=crop',
    category: 'Web',
    techStack: ['Vue 3', 'Storybook'],
    demoUrl: 'https://example.com',
    featured: true,
    sortOrder: 80,
    status: 'published',
  },
  {
    id: '4',
    title: 'CLI 工具集',
    slug: 'cli-toolkit',
    summary: 'Node 脚本与 CI 辅助工具集合。',
    contentMd: '常用脚本与自动化任务集合。',
    category: 'Tools',
    techStack: ['Node.js', 'TypeScript'],
    repoUrl: 'https://github.com',
    featured: false,
    sortOrder: 70,
    status: 'published',
  },
  {
    id: '5',
    title: '数据可视化仪表盘',
    slug: 'data-viz-dashboard',
    summary: 'ECharts 业务指标看板（内部 demo）。',
    contentMd: '内部数据可视化 demo。',
    category: 'Web',
    techStack: ['Vue 3', 'ECharts'],
    demoUrl: 'https://example.com',
    featured: false,
    sortOrder: 60,
    status: 'published',
  },
  {
    id: '6',
    title: '草稿项目',
    slug: 'draft-project',
    summary: '未发布项目',
    contentMd: 'draft',
    category: 'Other',
    techStack: [],
    featured: false,
    sortOrder: 0,
    status: 'draft',
  },
]
