// Medicine-Frontend/src/App.vue

<template>
  <div id="app">
    <Signup v-if="currentMode === 'signup'" @switch-mode="switchMode" />

    <Login v-else-if="currentMode === 'login'" @switch-mode="switchMode" @login-success="handleLoginSuccess" />

    <DoctorDashboard v-else-if="currentMode === 'dashboard' && currentUser.role === 'doctor'"
                     :user="currentUser"
                     @logout="handleLogout"
                     @switch-mode="switchMode" />

    <WritePrescription v-else-if="currentMode === 'write-prescription' && currentUser.role === 'doctor'"
                       :user="currentUser"
                       @logout="handleLogout"
                       @switch-mode="switchMode"
                       @continue-prescription="handleContinuePrescription" />

    <MedicineSelection v-else-if="currentMode === 'medicine-selection' && currentUser.role === 'doctor'"
                       :user="currentUser"
                       :patient="selectedPatientForPrescription"
                       @logout="handleLogout"
                       @switch-mode="switchMode"
                       @finish-prescription="goToPrescriptionConfirmation" />

    <PrescriptionConfirmation v-else-if="currentMode === 'prescription-confirmation' && currentUser.role === 'doctor'"
                              :user="currentUser"
                              :patient="selectedPatientForPrescription"
                              :initialMedicines="selectedMedicinesForConfirmation"
                              @logout="handleLogout"
                              @switch-mode="switchMode"
                              @confirm-prescription="handleConfirmAndSubmitPrescription" />

    <div v-else>
      <h2>Hoş Geldiniz, {{ currentUser.name }}</h2>
      <p>Bu rol için henüz bir ana sayfa tasarlanmamıştır: {{ currentUser.role }}</p>
      <button @click="handleLogout">Çıkış Yap</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import Signup from './components/Signup.vue';
import Login from './components/Login.vue';
import DoctorDashboard from './components/DoctorDashboard.vue';
import WritePrescription from './components/WritePrescription.vue';
import MedicineSelection from './components/MedicineSelection.vue';
import PrescriptionConfirmation from './components/PrescriptionConfirmation.vue';

// currentMode: 'login', 'signup', 'dashboard', 'write-prescription', 'medicine-selection', 'prescription-confirmation'
const currentMode = ref('login');
const currentUser = ref({ role: null, name: null, email: null }); // Giriş yapan kullanıcının bilgisi
const selectedPatientForPrescription = ref(null); // Reçete yazılacak hasta bilgisi
const selectedMedicinesForConfirmation = ref([]); // Onay sayfasında gösterilecek ilaçlar
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const switchMode = (mode) => {
  currentMode.value = mode;
  console.log(`Mod değiştirildi: ${mode}`);
};

// Login.vue'dan gelen başarılı giriş bilgisini yakalar
const handleLoginSuccess = (user) => {
  currentUser.value.name = user.name;
  currentUser.value.email = user.email;

  // E-posta uzantısına göre rol belirleme (Domain'deki mantığı taklit ederiz)
  if (user.email.endsWith('@dr.medicine')) {
    currentUser.value.role = 'doctor';
  } else if (user.email.endsWith('@pt.medicine')) {
    currentUser.value.role = 'patient';
  } else if (user.email.endsWith('@ph.medicine')) {
    currentUser.value.role = 'pharmacy';
  }

  // Rol doktor ise dashboard'a yönlendir
  if (currentUser.value.role === 'doctor') {
    currentMode.value = 'dashboard';
    console.log(`Giriş Başarılı. Rol: ${currentUser.value.role}`);
  } else {
    // Diğer roller için şimdilik bir placeholder göster
    currentMode.value = 'other';
  }
};

// Çıkış yapma işlemi
const handleLogout = () => {
  currentUser.value = { role: null, name: null, email: null };
  currentMode.value = 'login';
  selectedPatientForPrescription.value = null;
  selectedMedicinesForConfirmation.value = [];
  console.log('Çıkış yapıldı.');
};

// Reçete Yaz -> İlaç Seçimi geçişi
const handleContinuePrescription = (patientData) => {
  selectedPatientForPrescription.value = patientData;
  switchMode('medicine-selection');
};

// İlaç Seçimi -> Onay Sayfası geçişi
const goToPrescriptionConfirmation = (medicines) => {
  selectedMedicinesForConfirmation.value = medicines;
  switchMode('prescription-confirmation');
};

// Reçeteyi Onayla ve Backend'e Gönder (Final Adım)
const handleConfirmAndSubmitPrescription = async (finalMedicines) => {
  if (!selectedPatientForPrescription.value || finalMedicines.length === 0) return;

  try {
    const payload = {
      doctorEmail: currentUser.value.email,
      patientEmail: selectedPatientForPrescription.value.email,
      medicineNames: finalMedicines.map(m => m.name)
    };

    console.log("Reçete Gönderiliyor:", payload);

    const response = await axios.post(`${API_BASE_URL}/api/doctor/write-prescription`, payload);

    alert('Reçete başarıyla oluşturuldu! ID: ' + (response.data.prescriptionId || ''));
    switchMode('dashboard');

  } catch (error) {
    console.error("Reçete gönderilemedi:", error);
    alert('Hata: Reçete oluşturulamadı. ' + (error.response?.data || error.message));
  }
};
</script>

<style>
/* ... (Mevcut stiller) */
body {
  background-color: #f0f0f0;
  font-family: Arial, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  margin: 0;
}
</style>