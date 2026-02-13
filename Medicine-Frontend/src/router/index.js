import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../components/Login.vue'
import Signup from '../components/Signup.vue'
import DoctorDashboard from '../components/DoctorDashboard.vue'
import PatientDashboard from '../components/PatientDashboard.vue'
import PharmacyDashboard from '../components/PharmacyDashboard.vue'
import WritePrescription from '../components/WritePrescription.vue'
import MedicalAnalysis from '../components/MedicalAnalysis.vue'
import MedicineSelection from '../components/MedicineSelection.vue'
import PrescriptionConfirmation from '../components/PrescriptionConfirmation.vue'
import ATMStockManagement from '../components/ATMStockManagement.vue'
import ATMLocations from '../components/ATMLocations.vue'
import ReportsAndAnalysis from '../components/ReportsAndAnalysis.vue'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { requiresAuth: false }
    },
    {
        path: '/signup',
        name: 'Signup',
        component: Signup,
        meta: { requiresAuth: false }
    },
    {
        path: '/doctor/dashboard',
        name: 'DoctorDashboard',
        component: DoctorDashboard,
        meta: { requiresAuth: true, role: 'doctor' }
    },
    {
        path: '/doctor/write-prescription',
        name: 'WritePrescription',
        component: WritePrescription,
        meta: { requiresAuth: true, role: 'doctor' }
    },
    {
        path: '/doctor/medical-analysis',
        name: 'MedicalAnalysis',
        component: MedicalAnalysis,
        meta: { requiresAuth: true, role: 'doctor' }
    },
    {
        path: '/doctor/medicine-selection',
        name: 'MedicineSelection',
        component: MedicineSelection,
        meta: { requiresAuth: true, role: 'doctor' }
    },
    {
        path: '/doctor/prescription-confirmation',
        name: 'PrescriptionConfirmation',
        component: PrescriptionConfirmation,
        meta: { requiresAuth: true, role: 'doctor' }
    },
    {
        path: '/patient/dashboard',
        name: 'PatientDashboard',
        component: PatientDashboard,
        meta: { requiresAuth: true, role: 'patient' }
    },
    {
        path: '/pharmacy/dashboard',
        name: 'PharmacyDashboard',
        component: PharmacyDashboard,
        meta: { requiresAuth: true, role: 'pharmacy' }
    },
    {
        path: '/pharmacy/stock-management',
        name: 'ATMStockManagement',
        component: ATMStockManagement,
        meta: { requiresAuth: true, role: 'pharmacy' }
    },
    {
        path: '/pharmacy/atm-locations',
        name: 'ATMLocations',
        component: ATMLocations,
        meta: { requiresAuth: true, role: 'pharmacy' }
    },
    {
        path: '/pharmacy/reports',
        name: 'ReportsAndAnalysis',
        component: ReportsAndAnalysis,
        meta: { requiresAuth: true, role: 'pharmacy' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// Navigation guard for authentication and role-based access
router.beforeEach((to, from, next) => {
    // Import authStore inside the guard to avoid circular dependency
    const authStore = useAuthStore()

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    const requiredRole = to.meta.role

    if (requiresAuth) {
        if (!authStore.isAuthenticated) {
            // Not logged in, redirect to login
            next('/login')
        } else if (requiredRole && authStore.currentUser.role !== requiredRole) {
            // Logged in but wrong role, redirect to their dashboard
            const userRole = authStore.currentUser.role
            if (userRole === 'doctor') {
                next('/doctor/dashboard')
            } else if (userRole === 'patient') {
                next('/patient/dashboard')
            } else if (userRole === 'pharmacy') {
                next('/pharmacy/dashboard')
            } else {
                next('/login')
            }
        } else {
            // All checks passed
            next()
        }
    } else {
        // Public route
        next()
    }
})

export default router
