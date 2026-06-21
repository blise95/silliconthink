import { ref, watch, type Ref } from 'vue'
import { postRepo } from '@/services'
import type { Paginated, Post, PostListParams } from '@/types'

export function usePosts(params: Ref<PostListParams>) {
  const data = ref<Paginated<Post> | null>(null)
  const loading = ref(true)
  const error = ref<string | null>(null)

  async function fetchPosts() {
    loading.value = true
    error.value = null
    try {
      data.value = await postRepo.list(params.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  }

  watch(params, fetchPosts, { immediate: true, deep: true })

  return { data, loading, error, refresh: fetchPosts }
}

export function usePost(slug: Ref<string>) {
  const post = ref<Post | null>(null)
  const loading = ref(true)
  const error = ref<string | null>(null)

  watch(
    slug,
    async (value) => {
      loading.value = true
      error.value = null
      try {
        post.value = await postRepo.getBySlug(value)
      } catch (e) {
        error.value = e instanceof Error ? e.message : '加载失败'
      } finally {
        loading.value = false
      }
    },
    { immediate: true },
  )

  return { post, loading, error }
}
