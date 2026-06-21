import { projectsMock } from '@/mocks/projects'
import type { ProjectRepository } from '@/services/projectRepository'
import type { Paginated, Project, ProjectListParams } from '@/types'

function publishedProjects(): Project[] {
  return projectsMock
    .filter((p) => p.status === 'published')
    .sort((a, b) => b.sortOrder - a.sortOrder)
}

function filterProjects(
  projects: Project[],
  params: ProjectListParams = {},
): Project[] {
  if (!params.category) return projects
  return projects.filter((p) => p.category === params.category)
}

function paginate<T>(
  items: T[],
  page = 1,
  pageSize = 12,
): Paginated<T> {
  const start = (page - 1) * pageSize
  return {
    list: items.slice(start, start + pageSize),
    total: items.length,
    page,
    pageSize,
  }
}

export const mockProjectRepository: ProjectRepository = {
  async list(params = {}) {
    const filtered = filterProjects(publishedProjects(), params)
    return paginate(filtered, params.page ?? 1, params.pageSize ?? 12)
  },

  async getBySlug(slug) {
    return publishedProjects().find((p) => p.slug === slug) ?? null
  },

  async getFeatured(count) {
    return publishedProjects()
      .filter((p) => p.featured)
      .slice(0, count)
  },
}
