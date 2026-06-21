import { ref, watch, type Ref } from 'vue'
import { projectRepo } from '@/services'
import type { Paginated, Project, ProjectListParams } from '@/types'

export function useProjects(params: Ref<ProjectListParams>) {
  const data = ref<Paginated<Project> | null>(null)
  const loading = ref(true)
  const error = ref<string | null>(null)

  async function fetchProjects() {
    loading.value = true
    error.value = null
    try {
      data.value = await projectRepo.list(params.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  }

  watch(params, fetchProjects, { immediate: true, deep: true })

  return { data, loading, error }
}

export function useProject(slug: Ref<string>) {
  const project = ref<Project | null>(null)
  const loading = ref(true)
  const error = ref<string | null>(null)

  watch(
    slug,
    async (value) => {
      loading.value = true
      error.value = null
      try {
        project.value = await projectRepo.getBySlug(value)
      } catch (e) {
        error.value = e instanceof Error ? e.message : '加载失败'
      } finally {
        loading.value = false
      }
    },
    { immediate: true },
  )

  return { project, loading, error }
}

export function useFeaturedProjects(count = 3) {
  const projects = ref<Project[]>([])
  const loading = ref(true)

  watch(
    () => count,
    async () => {
      loading.value = true
      projects.value = await projectRepo.getFeatured(count)
      loading.value = false
    },
    { immediate: true },
  )

  return { projects, loading }
}
