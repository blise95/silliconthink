/**
 * 生成与后端一致的 URL slug（仅 a-z / 0-9 / 连字符）。
 * 纯中文等标题无法产出合法片段时回退到时间戳，避免保存被 INVALID_SLUG 拒绝。
 */
export function slugify(title: string): string {
  const base = title
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 200)
  return base || `post-${Date.now().toString(36)}`
}
