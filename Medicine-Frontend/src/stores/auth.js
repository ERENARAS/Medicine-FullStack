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

    // Getters
    const isAuthenticated = computed(() => {
        return currentUser.value.id !== null && currentUser.value.role !== null
    })

    const getUserRole = computed(() => {
        return currentUser.value.role
    })

    // Actions
    const login = (user) => {
        console.log('🔍 === AUTH STORE LOGIN DEBUG ===')
        console.log('📦 Gelen user objesi:', user)

        currentUser.value.id = user.id
        currentUser.value.name = user.name
        currentUser.value.email = user.email

        // Determine role based on email domain
        if (user.email.endsWith('@dr.medicine')) {
            currentUser.value.role = 'doctor'
            console.log('✅ Rol belirlendi: DOCTOR')
        } else if (user.email.endsWith('@pt.medicine')) {
            currentUser.value.role = 'patient'
            console.log('✅ Rol belirlendi: PATIENT')
        } else if (user.email.endsWith('@ph.medicine')) {
            currentUser.value.role = 'pharmacy'
            console.log('✅ Rol belirlendi: PHARMACY')
        } else {
            console.log('❌ Rol belirlenemedi! Email uzantısı tanınmıyor:', user.email)
            currentUser.value.role = null
        }

        console.log('🎭 Final currentUser:', currentUser.value)
        console.log('🔍 === AUTH STORE LOGIN DEBUG BİTTİ ===')
    }

    const logout = () => {
        console.log('🚪 Çıkış yapılıyor...')
        currentUser.value = {
            id: null,
            name: null,
            email: null,
            role: null
        }
        console.log('✅ Kullanıcı bilgileri temizlendi')
    }

    return {
        currentUser,
        isAuthenticated,
        getUserRole,
        login,
        logout
    }
})
