import type { Paginated, Project, ProjectListParams } from '@/types'

export interface ProjectRepository {
  list(params?: ProjectListParams): Promise<Paginated<Project>>
  getBySlug(slug: string): Promise<Project | null>
  getFeatured(count: number): Promise<Project[]>
}
