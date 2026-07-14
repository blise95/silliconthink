import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/pages/HomeView.vue'),
        },
        {
          path: 'about',
          name: 'about',
          component: () => import('@/pages/AboutView.vue'),
        },
        {
          path: 'blog',
          name: 'blog',
          component: () => import('@/pages/BlogListView.vue'),
        },
        {
          path: 'blog/:slug',
          name: 'blog-detail',
          component: () => import('@/pages/BlogDetailView.vue'),
        },
        {
          path: 'projects',
          name: 'projects',
          component: () => import('@/pages/ProjectListView.vue'),
        },
        {
          path: 'projects/:slug',
          name: 'project-detail',
          component: () => import('@/pages/ProjectDetailView.vue'),
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('@/pages/LoginView.vue'),
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/pages/RegisterView.vue'),
        },
        {
          path: 'oauth/callback',
          name: 'oauth-callback',
          component: () => import('@/pages/OAuthCallbackView.vue'),
        },
        {
          path: 'search',
          name: 'search',
          component: () => import('@/pages/SearchView.vue'),
        },
        {
          path: ':pathMatch(.*)*',
          name: 'not-found',
          component: () => import('@/pages/NotFoundView.vue'),
        },
      ],
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
