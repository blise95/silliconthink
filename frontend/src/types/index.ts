export type PublishStatus = 'published' | 'draft'

export interface Paginated<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface ContactLink {
  label: string
  url: string
  icon?: string
}

export interface SiteConfig {
  siteName: string
  tagline: string
  seoDescription: string
  seoKeywords: string
  aboutMd: string
  skills: string[]
  contact: ContactLink[]
  ogImage?: string
}

export interface Tag {
  id: string
  name: string
  slug: string
}

export interface Post {
  id: string
  title: string
  slug: string
  summary: string
  contentMd: string
  coverUrl?: string
  tags: string[]
  publishedAt: string
  status: PublishStatus
}

export interface Project {
  id: string
  title: string
  slug: string
  summary: string
  contentMd: string
  coverUrl?: string
  category: string
  techStack: string[]
  demoUrl?: string
  repoUrl?: string
  featured: boolean
  sortOrder: number
  status: PublishStatus
}

export interface PostListParams {
  page?: number
  pageSize?: number
  tag?: string
  keyword?: string
}

export interface ProjectListParams {
  page?: number
  pageSize?: number
  category?: string
}

export interface HomeData {
  latestPosts: Post[]
  featuredProjects: Project[]
}
