import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePrescriptionStore = defineStore('prescription', () => {
    // State
    const selectedPatient = ref(null)
    const selectedMedicines = ref([])
    const analysisData = ref(null)

    // Actions
    const setSelectedPatient = (patient) => {
        console.log('📋 Hasta bilgileri kaydediliyor:', patient)
        selectedPatient.value = patient
    }

    const setSelectedMedicines = (medicines) => {
        console.log('💊 İlaç listesi kaydediliyor:', medicines)
        selectedMedicines.value = medicines
    }

    const setAnalysisData = (data) => {
        console.log('🔬 Analiz verileri kaydediliyor:', data)
        analysisData.value = data
    }

    const clearPrescriptionData = () => {
        console.log('🧹 Reçete verileri temizleniyor...')
        selectedPatient.value = null
        selectedMedicines.value = []
        analysisData.value = null
        console.log('✅ Reçete verileri temizlendi')
    }

    return {
        selectedPatient,
        selectedMedicines,
        analysisData,
        setSelectedPatient,
        setSelectedMedicines,
        setAnalysisData,
        clearPrescriptionData
    }
})
