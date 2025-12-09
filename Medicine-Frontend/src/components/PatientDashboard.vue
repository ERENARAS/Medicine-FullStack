<template>
  <v-container fluid class="patient-dashboard-container">

    <!-- Navbar -->
    <v-sheet class="navbar" color="transparent">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click="currentView = 'home'">Ana Sayfa</a>
        <a href="#" @click="currentView = 'nearby'">ATM Konumları</a>
        <v-btn @click="$emit('logout')" class="logout-btn" variant="outlined" size="small">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <!-- HOME VIEW -->
    <div v-if="currentView === 'home'">
      <!-- Welcome Card -->
      <v-card class="welcome-card elevation-4">
        <div class="card-content">
          <div class="info-section">
            <p class="tagline">
              <v-icon color="#a2d6b8" size="small">mdi-pill</v-icon> Kolay Ve Güvenli
            </p>
            <p class="tagline ml-4">
              <v-icon color="#a2d6b8" size="small">mdi-hospital-box</v-icon> Hızlı İlaç Alma Sistemi
            </p>
            <h2 class="welcome-message">Merhaba, {{ patientName }}</h2>
          </div>
          <div class="action-section">
            <v-btn class="action-btn elevation-2" @click="currentView = 'nearby'">
              Yakınımdaki ATM'ler
            </v-btn>
          </div>
        </div>
      </v-card>

      <!-- Content Grid -->
      <div class="content-grid">
        <!-- Left Column: Allergies and Recent Dispenses -->
        <div class="left-column">
          <!-- Allergies Card -->
          <v-card class="info-card elevation-2">
            <h3>Kayıtlı Alerjiler</h3>
            <p v-if="isLoadingPatient">
              <v-progress-linear indeterminate color="primary"></v-progress-linear>
              Veriler yükleniyor...
            </p>
            <div v-else>
              <!-- Allergy input and add button -->
              <div class="allergy-input-container">
                <input 
                  v-model="newAllergy" 
                  type="text" 
                  class="allergy-input" 
                  placeholder="Yeni alerji ekle (örn: Penisilin)"
                  @keyup.enter="addAllergy"
                />
                <button class="add-allergy-btn" @click="addAllergy">Ekle</button>
              </div>
              
              <!-- Allergies list -->
              <div v-if="patientInfo && patientInfo.allergicMedicines && patientInfo.allergicMedicines.length > 0" class="allergies-list">
                <span v-for="(allergy, index) in patientInfo.allergicMedicines" :key="index" class="allergy-tag">
                  {{ allergy }}
                  <button class="remove-allergy-btn" @click="removeAllergy(allergy)" title="Sil">×</button>
                </span>
              </div>
              <p v-else class="no-data">Kayıtlı alerji bulunmuyor.</p>
            </div>
          </v-card>

          <!-- Recent Dispenses Card -->
          <v-card class="info-card elevation-2">
            <h3>Son İlaç Alımları</h3>
            <p v-if="isLoadingPrescriptions">
              <v-progress-linear indeterminate color="primary"></v-progress-linear>
              Veriler yükleniyor...
            </p>
            <div v-else-if="dispensedPrescriptions.length > 0" class="recent-dispenses">
              <div v-for="prescription in dispensedPrescriptions" :key="prescription.id" class="dispense-item">
                <p class="dispense-date">{{ formatDate(prescription.date) }}</p>
                <p class="dispense-id">Reçete ID: {{ prescription.id.substring(0, 8) }}...</p>
              </div>
            </div>
            <p v-else class="no-data">Henüz ilaç alımı bulunmuyor.</p>
          </v-card>
        </div>

        <!-- Right Column: Pending Prescriptions -->
        <v-card class="pending-prescriptions elevation-2">
          <h3>Bekleyen Reçeteleriniz</h3>
          <p v-if="isLoadingPrescriptions">
            <v-progress-linear indeterminate color="primary"></v-progress-linear>
            Veriler yükleniyor...
          </p>
          <p v-else-if="errorMessage" class="error-message">Hata: {{ errorMessage }}</p>
          <div v-else-if="pendingPrescriptions.length > 0" class="prescriptions-list">
            <div v-for="prescription in pendingPrescriptions" :key="prescription.id" class="prescription-card">
              <div class="prescription-info">
                <p class="prescription-id"><strong>Reçete ID:</strong> {{ prescription.id.substring(0, 12) }}...</p>
                <p class="prescription-doctor"><strong>Doktor:</strong> Dr. {{ prescription.doctor.name }}</p>
                <p class="prescription-medicines">
                  <strong>İlaçlar:</strong> 
                  {{ prescription.medicines.map(m => m.name).join(', ').substring(0, 50) }}{{ prescription.medicines.map(m => m.name).join(', ').length > 50 ? '...' : '' }}
                </p>
                <p class="prescription-date"><strong>Tarih:</strong> {{ formatDate(prescription.date) }}</p>
              </div>
              <div class="prescription-action">
                <v-btn class="dispense-btn" @click="selectPrescriptionForDispense(prescription)">
                  İlaçları Al
                </v-btn>
              </div>
            </div>
          </div>
          <p v-else class="no-data">Bekleyen reçete bulunmuyor.</p>
        </v-card>
      </div>
    </div>

    <!-- ATM IDENTIFICATION VIEW -->
    <div v-else-if="currentView === 'atm-id'">
      <v-card class="atm-id-card elevation-4">
        <h2 class="atm-title">ATM Tanımlama</h2>
        <div class="atm-content">
          <div class="qr-placeholder">
            <v-icon size="80" color="#a2d6b8">mdi-qrcode-scan</v-icon>
            <p>QR Code ile ATM Tarama</p>
          </div>
          <p class="or-divider">VEYA</p>
          <div class="manual-input">
            <label>ATM ID Manuel Giriş</label>
            <input 
              v-model="manualAtmId" 
              type="number" 
              class="atm-input" 
              placeholder="ATM ID Giriniz (örn: 1)" 
            />
          </div>
          <div class="atm-actions">
            <v-btn class="continue-btn" @click="proceedToDispense">Devam Et</v-btn>
            <v-btn class="cancel-btn" @click="currentView = 'home'">İptal</v-btn>
          </div>
        </div>
      </v-card>
    </div>

    <!-- DISPENSE CONFIRMATION VIEW -->
    <div v-else-if="currentView === 'dispense'">
      <div class="dispense-container">
        <h1 class="dispense-title">İlaç Alma Onayı</h1>
        
        <div class="dispense-sections">
          <!-- Prescription Details -->
          <v-card class="dispense-card elevation-2">
            <h3>Reçete Detayları</h3>
            <p><strong>Reçete ID:</strong> {{ selectedPrescription.id.substring(0, 12) }}...</p>
            <p><strong>Doktor:</strong> Dr. {{ selectedPrescription.doctor.name }}</p>
            <p><strong>Tarih:</strong> {{ formatDate(selectedPrescription.date) }}</p>
          </v-card>

          <!-- Medicine and Stock Status -->
          <v-card class="dispense-card elevation-2">
            <h3>İlaç ve Stok Durumu</h3>
            <table class="medicine-table">
              <thead>
                <tr>
                  <th>İlaç Adı</th>
                  <th>Stok Durumu</th>
                  <th>Alerji Kontrolü</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="medicine in selectedPrescription.medicines" :key="medicine.id">
                  <td>{{ medicine.name }}</td>
                  <td>
                    <span class="stock-available">Stokta Var</span>
                  </td>
                  <td>
                    <span v-if="checkAllergy(medicine.name)" class="allergy-warning">⚠️ Alerjik</span>
                    <span v-else class="allergy-safe">✓ Güvenli</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </v-card>

          <!-- Confirmation and Payment -->
          <v-card class="dispense-card elevation-2">
            <h3>Onay ve Ödeme</h3>
            <p v-if="dispensing">
              <v-progress-linear indeterminate color="primary"></v-progress-linear>
              İşlem gerçekleştiriliyor...
            </p>
            <p v-if="dispenseError" class="error-message">{{ dispenseError }}</p>
            <p v-if="dispenseSuccess" class="success-message">{{ dispenseSuccess }}</p>
            <div class="confirmation-actions">
              <v-btn class="confirm-btn" @click="confirmDispense" :disabled="dispensing">
                Onayla ve İlaçları Ver
              </v-btn>
              <v-btn class="cancel-btn" @click="cancelDispense" :disabled="dispensing">
                İptal
              </v-btn>
            </div>
          </v-card>
        </div>
      </div>
    </div>

    <!-- NEARBY ATMs VIEW (Placeholder) -->
    <div v-else-if="currentView === 'nearby'">
      <v-card class="nearby-card elevation-4">
        <h2>Yakınımdaki ATM'ler</h2>
        <div class="nearby-list">
          <div class="atm-item">
            <v-icon color="#a2d6b8" size="large">mdi-map-marker</v-icon>
            <div class="atm-info">
              <h4>ATM #1 - Merkez Eczanesi</h4>
              <p>Adres: Atatürk Cad. No:15, Merkez</p>
              <p>Mesafe: 0.5 km</p>
            </div>
          </div>
          <div class="atm-item">
            <v-icon color="#a2d6b8" size="large">mdi-map-marker</v-icon>
            <div class="atm-info">
              <h4>ATM #2 - Sağlık Eczanesi</h4>
              <p>Adres: İstiklal Cad. No:42, Merkez</p>
              <p>Mesafe: 1.2 km</p>
            </div>
          </div>
        </div>
        <v-btn class="back-btn" @click="currentView = 'home'">Ana Sayfaya Dön</v-btn>
      </v-card>
    </div>

  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const props = defineProps({
  user: Object,
});

const emit = defineEmits(['logout']);

const patientName = computed(() => props.user?.name || 'Kullanıcı');
const patientEmail = computed(() => props.user?.email || '');

const currentView = ref('home'); // 'home', 'atm-id', 'dispense', 'nearby'
const patientInfo = ref(null);
const prescriptions = ref([]);
const isLoadingPatient = ref(true);
const isLoadingPrescriptions = ref(true);
const errorMessage = ref('');

const manualAtmId = ref(1);
const selectedPrescription = ref(null);
const dispensing = ref(false);
const dispenseError = ref('');
const dispenseSuccess = ref('');
const newAllergy = ref('');

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Computed: Pending prescriptions (not dispensed yet)
const pendingPrescriptions = computed(() => {
  return prescriptions.value.filter(p => !p.dispensed);
});

// Computed: Dispensed prescriptions (for recent dispenses)
const dispensedPrescriptions = computed(() => {
  return prescriptions.value.filter(p => p.dispensed).slice(0, 5);
});

// Fetch patient info (including allergies)
const fetchPatientInfo = async () => {
  isLoadingPatient.value = true;
  try {
    const response = await axios.get(`${API_BASE_URL}/api/patient/info/${patientEmail.value}`);
    patientInfo.value = response.data;
  } catch (error) {
    console.error("Hasta bilgileri çekilemedi:", error);
  } finally {
    isLoadingPatient.value = false;
  }
};

// Fetch prescriptions
const fetchPrescriptions = async () => {
  isLoadingPrescriptions.value = true;
  errorMessage.value = '';
  try {
    const response = await axios.get(`${API_BASE_URL}/api/patient/my-prescriptions/${patientEmail.value}`);
    prescriptions.value = response.data;
  } catch (error) {
    console.error("Reçeteler çekilemedi:", error);
    errorMessage.value = `Reçeteler yüklenirken bir hata oluştu: ${error.message}`;
  } finally {
    isLoadingPrescriptions.value = false;
  }
};

// Format date
const formatDate = (dateString) => {
  if (!dateString) return 'Tarih yok';
  const date = new Date(dateString);
  return date.toLocaleDateString('tr-TR');
};

// Check if patient is allergic to a medicine
const checkAllergy = (medicineName) => {
  if (!patientInfo.value || !patientInfo.value.allergicMedicines) return false;
  return patientInfo.value.allergicMedicines.includes(medicineName);
};

// Select prescription for dispense
const selectPrescriptionForDispense = (prescription) => {
  selectedPrescription.value = prescription;
  currentView.value = 'atm-id';
};

// Proceed to dispense view
const proceedToDispense = () => {
  if (!manualAtmId.value) {
    alert('Lütfen ATM ID giriniz.');
    return;
  }
  currentView.value = 'dispense';
};

// Confirm dispense
const confirmDispense = async () => {
  dispensing.value = true;
  dispenseError.value = '';
  dispenseSuccess.value = '';

  try {
    const response = await axios.post(`${API_BASE_URL}/api/patient/dispense`, {
      prescriptionId: selectedPrescription.value.id,
      patientEmail: patientEmail.value,
      atmId: manualAtmId.value
    });

    dispenseSuccess.value = response.data.message || 'İlaçlar başarıyla teslim edildi!';
    
    // Wait 2 seconds then refresh and go back to home
    setTimeout(() => {
      fetchPrescriptions();
      currentView.value = 'home';
      selectedPrescription.value = null;
      manualAtmId.value = 1;
      dispenseSuccess.value = '';
    }, 2000);

  } catch (error) {
    console.error("İlaç teslim hatası:", error);
    dispenseError.value = error.response?.data || 'İlaç teslim edilemedi. Lütfen tekrar deneyin.';
  } finally {
    dispensing.value = false;
  }
};

// Cancel dispense
const cancelDispense = () => {
  currentView.value = 'home';
  selectedPrescription.value = null;
  manualAtmId.value = 1;
  dispenseError.value = '';
  dispenseSuccess.value = '';
};

// Add allergy
const addAllergy = async () => {
  if (!newAllergy.value || newAllergy.value.trim() === '') {
    alert('Lütfen alerji adı giriniz.');
    return;
  }

  try {
    const response = await axios.post(`${API_BASE_URL}/api/patient/allergies/add`, {
      patientEmail: patientEmail.value,
      allergyName: newAllergy.value.trim()
    });

    // Refresh patient info to show new allergy
    await fetchPatientInfo();
    newAllergy.value = ''; // Clear input
    
  } catch (error) {
    console.error("Alerji ekleme hatası:", error);
    alert(error.response?.data || 'Alerji eklenemedi. Lütfen tekrar deneyin.');
  }
};

// Remove allergy
const removeAllergy = async (allergyName) => {
  if (!confirm(`"${allergyName}" alerjisini silmek istediğinizden emin misiniz?`)) {
    return;
  }

  try {
    await axios.delete(`${API_BASE_URL}/api/patient/allergies/remove`, {
      data: {
        patientEmail: patientEmail.value,
        allergyName: allergyName
      }
    });

    // Refresh patient info to remove allergy from list
    await fetchPatientInfo();
    
  } catch (error) {
    console.error("Alerji silme hatası:", error);
    alert(error.response?.data || 'Alerji silinemedi. Lütfen tekrar deneyin.');
  }
};

onMounted(() => {
  if (patientEmail.value) {
    fetchPatientInfo();
    fetchPrescriptions();
  }
});
</script>

<style scoped>
/* Main Container */
.patient-dashboard-container {
  height: 100vh;
  width: 100vw;
  background-color: #ECEEF9;
  padding: 10px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: sans-serif;
  overflow: hidden;
}

.patient-dashboard-container > * {
  width: 100%;
  max-width: 1200px;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.logo {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
}

.nav-links a {
  margin-left: 20px;
  text-decoration: none;
  color: #555;
  font-weight: 500;
}

.nav-links a:hover {
  color: #a2d6b8;
}

.logout-btn {
  margin-left: 30px;
  background: none;
  border: 1px solid #d6a2b8;
  color: #d6a2b8 !important;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
  text-transform: none;
}

.logout-btn:hover {
  background-color: #d6a2b8;
  color: white !important;
}

/* Welcome Card */
.welcome-card {
  background-color: #252B61 !important;
  border-radius: 12px;
  padding: 20px 30px;
  color: #fff;
  margin-bottom: 15px;
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tagline {
  font-size: 0.9em;
  color: #a2d6b8;
  margin: 5px 0;
  display: inline-block;
  margin-right: 20px;
}

.welcome-message {
  font-size: 2em;
  font-weight: 300;
  margin-top: 10px;
  margin-bottom: 0;
}

.action-section {
  display: flex;
  align-items: center;
}

.action-btn {
  background-color: #d6a2b8 !important;
  color: #fff !important;
  border: none;
  padding: 16px 24px;
  border-radius: 12px;
  font-size: 1em;
  cursor: pointer;
  transition: background-color 0.3s;
  text-transform: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background-color: #b8869c !important;
}

/* Content Grid (Home View) */
.content-grid {
  display: flex;
  gap: 20px;
  flex: 1;
  overflow: hidden;
}

.left-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  overflow: hidden;
}

.info-card {
  background-color: #252B61 !important;
  color: #fff;
  border-radius: 12px;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.info-card h3 {
  font-size: 1.2em;
  margin-top: 0;
  margin-bottom: 15px;
  color: #fff;
}

.allergies-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.allergy-tag {
  background-color: #a2d6b8;
  color: #252B61;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9em;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.allergy-input-container {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.allergy-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 0.9em;
}

.allergy-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.add-allergy-btn {
  background-color: #a2d6b8;
  color: #252B61;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 0.9em;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-allergy-btn:hover {
  background-color: #8bc4a3;
}

.remove-allergy-btn {
  background: none;
  border: none;
  color: #252B61;
  font-size: 1.2em;
  font-weight: bold;
  cursor: pointer;
  padding: 0;
  margin-left: 5px;
  line-height: 1;
  transition: color 0.3s;
}

.remove-allergy-btn:hover {
  color: #dc3545;
}

.recent-dispenses {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.dispense-item {
  border-left: 3px solid #a2d6b8;
  padding-left: 15px;
}

.dispense-date {
  font-size: 0.9em;
  color: #a2d6b8;
  margin: 0;
}

.dispense-id {
  font-size: 0.85em;
  color: #ccc;
  margin: 5px 0 0 0;
}

.no-data {
  color: #999;
  font-style: italic;
}

/* Pending Prescriptions */
.pending-prescriptions {
  flex: 2;
  background-color: #fff !important;
  border-radius: 12px;
  padding: 20px;
  overflow-y: auto;
}

.pending-prescriptions h3 {
  font-size: 1.2em;
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.prescriptions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.prescription-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.prescription-info {
  flex: 1;
}

.prescription-info p {
  margin: 8px 0;
  font-size: 0.95em;
  color: #333;
}

.prescription-id {
  color: #252B61;
  font-weight: 600;
}

.prescription-action {
  display: flex;
  align-items: center;
}

.dispense-btn {
  background-color: #a2d6b8 !important;
  color: #fff !important;
  padding: 12px 24px;
  border-radius: 8px;
  text-transform: none;
  font-weight: 600;
}

.dispense-btn:hover {
  background-color: #8bc4a3 !important;
}

/* ATM Identification View */
.atm-id-card {
  background-color: #252B61 !important;
  color: #fff;
  border-radius: 16px;
  padding: 50px;
  margin-top: 20px;
}

.atm-title {
  font-size: 2.5em;
  font-weight: 300;
  margin-bottom: 40px;
  text-align: center;
}

.atm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.qr-placeholder {
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 60px;
  text-align: center;
}

.qr-placeholder p {
  margin-top: 15px;
  font-size: 1.1em;
  color: #a2d6b8;
}

.or-divider {
  font-size: 1.2em;
  color: #999;
  font-weight: 600;
}

.manual-input {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.manual-input label {
  font-size: 1em;
  color: #a2d6b8;
}

.atm-input {
  width: 100%;
  padding: 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 1em;
}

.atm-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.atm-actions {
  display: flex;
  gap: 20px;
}

.continue-btn {
  background-color: #a2d6b8 !important;
  color: #fff !important;
  padding: 15px 40px;
  border-radius: 8px;
  text-transform: none;
  font-size: 1.1em;
}

.continue-btn:hover {
  background-color: #8bc4a3 !important;
}

.cancel-btn {
  background-color: transparent !important;
  color: #d6a2b8 !important;
  border: 1px solid #d6a2b8 !important;
  padding: 15px 40px;
  border-radius: 8px;
  text-transform: none;
  font-size: 1.1em;
}

.cancel-btn:hover {
  background-color: rgba(214, 162, 184, 0.1) !important;
}

/* Dispense Confirmation View */
.dispense-container {
  max-width: 900px;
  margin: 0 auto;
}

.dispense-title {
  font-size: 2em;
  color: #333;
  margin-bottom: 30px;
}

.dispense-sections {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.dispense-card {
  background-color: #fff !important;
  border-radius: 12px;
  padding: 30px;
}

.dispense-card h3 {
  font-size: 1.3em;
  margin-top: 0;
  margin-bottom: 20px;
  color: #252B61;
}

.dispense-card p {
  margin: 10px 0;
  font-size: 1em;
  color: #333;
}

.medicine-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.medicine-table th,
.medicine-table td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.medicine-table th {
  font-weight: 600;
  color: #666;
  background-color: #f8f9fa;
}

.stock-available {
  color: #28a745;
  font-weight: 600;
}

.stock-unavailable {
  color: #dc3545;
  font-weight: 600;
}

.allergy-safe {
  color: #28a745;
}

.allergy-warning {
  color: #dc3545;
  font-weight: 600;
}

.confirmation-actions {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.confirm-btn {
  background-color: #a2d6b8 !important;
  color: #fff !important;
  padding: 15px 30px;
  border-radius: 8px;
  text-transform: none;
  font-size: 1.1em;
  font-weight: 600;
}

.confirm-btn:hover {
  background-color: #8bc4a3 !important;
}

.error-message {
  color: #dc3545;
  font-weight: 600;
  margin: 15px 0;
}

.success-message {
  color: #28a745;
  font-weight: 600;
  margin: 15px 0;
}

/* Nearby ATMs View */
.nearby-card {
  background-color: #fff !important;
  border-radius: 16px;
  padding: 40px;
  margin-top: 20px;
}

.nearby-card h2 {
  font-size: 2em;
  color: #252B61;
  margin-bottom: 30px;
}

.nearby-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.atm-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.atm-info h4 {
  margin: 0 0 10px 0;
  color: #252B61;
  font-size: 1.2em;
}

.atm-info p {
  margin: 5px 0;
  color: #666;
}

.back-btn {
  background-color: #d6a2b8 !important;
  color: #fff !important;
  padding: 12px 30px;
  border-radius: 8px;
  text-transform: none;
}

.back-btn:hover {
  background-color: #b8869c !important;
}
</style>
