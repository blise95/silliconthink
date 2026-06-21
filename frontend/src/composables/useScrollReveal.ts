import { onMounted, onUnmounted, type Ref } from 'vue'

export function useScrollReveal(root: Ref<HTMLElement | null>) {
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!root.value) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer?.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' },
    )

    root.value.querySelectorAll('.animate-on-scroll').forEach((el) => {
      observer!.observe(el)
    })
  })

  onUnmounted(() => observer?.disconnect())
}
