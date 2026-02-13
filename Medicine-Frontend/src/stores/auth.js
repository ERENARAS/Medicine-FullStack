import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    // State
    const currentUser = ref({
        id: null,
        name: null,
        email: null,
        role: null
    })
    const token = ref(localStorage.getItem('token') || null)

    // Getters
    const isAuthenticated = computed(() => !!token.value)

    const getUserRole = computed(() => currentUser.value.role)

    // Actions
    const login = (authData) => {
        // authData: { token: "...", user: { ... } }
        console.log('🔍 === AUTH STORE LOGIN ===')

        token.value = authData.token
        localStorage.setItem('token', authData.token)

        const user = authData.user
        currentUser.value = {
            id: user.id,
            name: user.name,
            email: user.email,
            role: null
        }

        // Determine role based on email domain (Server side also sends role but keeping this logic for consistency with frontend guards if needed)
        // Actually, we can decode JWT to get role, or trust the logic here. 
        // Let's stick to email domain logic for now as it maps 1:1 with backend entities
        if (user.email.endsWith('@dr.medicine')) {
            currentUser.value.role = 'doctor'
        } else if (user.email.endsWith('@pt.medicine')) {
            currentUser.value.role = 'patient'
        } else if (user.email.endsWith('@ph.medicine')) {
            currentUser.value.role = 'pharmacy'
        }

        localStorage.setItem('user', JSON.stringify(currentUser.value))
    }

    const logout = () => {
        token.value = null
        currentUser.value = {
            id: null,
            name: null,
            email: null,
            role: null
        }
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
    }

    const checkAuth = () => {
        const storedToken = localStorage.getItem('token')
        const storedUser = localStorage.getItem('user')

        if (storedToken && storedUser) {
            token.value = storedToken
            currentUser.value = JSON.parse(storedUser)
        }
    }

    return {
        currentUser,
        token,
        isAuthenticated,
        getUserRole,
        login,
        logout,
        checkAuth
    }
})
