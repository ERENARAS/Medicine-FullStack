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
    <div v-else-if="currentView === 'dispense'" class="dispense-view">
      <div class="dispense-container-new">
        <div class="dispense-header">
          <h1 class="dispense-title-new">İlaç Alma Onayı</h1>
          <p class="dispense-subtitle">Lütfen reçete detaylarını, ilaç stok durumunu ve alerji bilgilerinizi kontrol ediniz.</p>
        </div>
        
        <div class="dispense-content">
          <!-- Prescription Details Card -->
          <div class="dispense-card-new">
            <h3 class="card-title-new">Reçete Detayları</h3>
            <div class="prescription-details-grid">
              <div class="detail-item">
                <span class="detail-label">Reçete ID:</span>
                <span class="detail-value">{{ selectedPrescription.id.substring(0, 12) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Doktor Adı:</span>
                <span class="detail-value">Dr. {{ selectedPrescription.doctor.name }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Tarih:</span>
                <span class="detail-value">{{ formatDate(selectedPrescription.date) }}</span>
              </div>
            </div>
          </div>

          <!-- Medicine and Stock Status Table -->
          <div class="dispense-card-new">
            <h3 class="card-title-new">İlaç ve Stok Durumu</h3>
            <div class="medicine-table-container">
              <table class="medicine-table-new">
                <thead>
                  <tr>
                    <th>İLAÇ ADI</th>
                    <th>DOZAJ BİLGİSİ</th>
                    <th>STOK DURUMU (ATM)</th>
                    <th>ALERJİ KONTROLÜ</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="medicine in selectedPrescription.medicines" :key="medicine.id" :class="{ 'allergy-row': checkAllergy(medicine.name) }">
                    <td :class="{ 'medicine-name-allergy': checkAllergy(medicine.name) }">{{ medicine.name }}</td>
                    <td :class="{ 'dosage-allergy': checkAllergy(medicine.name) }">{{ medicine.treatment || '500mg' }}</td>
                    <td>
                      <span v-if="stockStatus[medicine.name]" class="stock-badge stock-available-new">✓ Stokta Var</span>
                      <span v-else class="stock-badge stock-unavailable-new" style="background-color: #ffebee; color: #dc3545;">✖ Stokta Yok</span>
                    </td>
                    <td>
                      <span v-if="checkAllergy(medicine.name)" class="allergy-badge allergy-warning-new">▲ Alerjik</span>
                      <span v-else class="allergy-badge allergy-safe-new">● Güvenli</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Confirmation and Payment Card -->
          <div class="dispense-card-new">
            <h3 class="card-title-new">Onay ve Ödeme</h3>
            <p class="payment-info">Onaylamanız durumunda, stokta olan ve alerjik olmayan ilaçlarınız ATM haznesine düşecektir. Ödeme <strong>125.50 TL</strong> kartınızdan tahsil edilecektir.</p>
            
            <div v-if="hasAllergicMedicines" class="allergy-warning-box">
              <v-icon color="#dc3545" size="small">mdi-alert-circle</v-icon>
              <span>Reçetenizde alerjiniz olan bir ilaç bulunduğu için işleme devam edilememektedir. Lütfen doktorunuzla iletişime geçin.</span>
            </div>

            <p v-if="dispensing" class="loading-message">
              <v-progress-linear indeterminate color="#4ade80"></v-progress-linear>
              İşlem gerçekleştiriliyor...
            </p>
            <p v-if="dispenseError" class="error-message-new">{{ dispenseError }}</p>
            <p v-if="dispenseSuccess" class="success-message-new">{{ dispenseSuccess }}</p>

            <div class="confirmation-actions-new">
              <v-btn class="cancel-btn-new" @click="cancelDispense" :disabled="dispensing">İptal</v-btn>
              <v-btn class="confirm-btn-new" @click="confirmDispense" :disabled="dispensing || hasAllergicMedicines || hasStockIssues">
                <v-icon left size="small">mdi-check-circle</v-icon>
                Onayla ve İlaçları Ver
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- NEARBY ATMs VIEW (Placeholder) -->
    <div v-else-if="currentView === 'nearby'">
      <v-card class="nearby-card elevation-4">
        <h2>Yakınımdaki ATM'ler</h2>
        <div class="nearby-list">
          <p v-if="isLoadingATMs">ATM'ler yükleniyor...</p>
          <div v-else-if="atms.length > 0" v-for="atm in atms" :key="atm.id" class="atm-item">
            <v-icon color="#a2d6b8" size="large">mdi-map-marker</v-icon>
            <div class="atm-info">
              <h4>ATM #{{ atm.id }} - {{ atm.location }}</h4>
              <p>Adres: {{ atm.location }}</p>
              <p>ID: {{ atm.id }}</p>
            </div>
          </div>
          <p v-else>Kayıtlı ATM bulunamadı.</p>
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
const stockStatus = ref({});
const atms = ref([]);
const isLoadingATMs = ref(false);

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Computed: Pending prescriptions (not dispensed yet)
const pendingPrescriptions = computed(() => {
  return prescriptions.value.filter(p => !p.dispensed);
});

// Computed: Dispensed prescriptions (for recent dispenses)
const dispensedPrescriptions = computed(() => {
  return prescriptions.value.filter(p => p.dispensed).slice(0, 5);
});

// Computed: Check if selected prescription has allergic medicines
const hasAllergicMedicines = computed(() => {
  if (!selectedPrescription.value || !patientInfo.value) return false;
  return selectedPrescription.value.medicines.some(medicine => 
    checkAllergy(medicine.name)
  );
});

const hasStockIssues = computed(() => {
  if (!selectedPrescription.value) return false;
  return selectedPrescription.value.medicines.some(medicine => 
    stockStatus.value[medicine.name] === false
  );
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
const proceedToDispense = async () => {
  if (!manualAtmId.value) {
    alert('Lütfen ATM ID giriniz.');
    return;
  }
  
  // Check stock before proceeding
  await checkStockStatus();
  
  currentView.value = 'dispense';
};

// Check stock status
const checkStockStatus = async () => {
  stockStatus.value = {};
  if (!selectedPrescription.value) return;

  const medicineNames = selectedPrescription.value.medicines.map(m => m.name);

  try {
    const response = await axios.post(`${API_BASE_URL}/api/atm/check-stock`, {
      atmId: manualAtmId.value,
      medicineNames: medicineNames
    });
    stockStatus.value = response.data;
  } catch (error) {
    console.error("Stok kontrolü hatası:", error);
    // In case of error, you might want to show a message or assume false
  }
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

// Fetch ATMs
const fetchATMs = async () => {
  isLoadingATMs.value = true;
  try {
    const response = await axios.get(`${API_BASE_URL}/api/atm/all`);
    atms.value = response.data;
  } catch (error) {
    console.error("ATM listesi çekilemedi:", error);
  } finally {
    isLoadingATMs.value = false;
  }
};

// Watch for view change to fetch ATMs
import { watch } from 'vue';
watch(currentView, (newView) => {
  if (newView === 'nearby') {
    fetchATMs();
  }
});

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

/* NEW DISPENSE CONFIRMATION VIEW */
.dispense-view {
  background-color: #1a1d2e;
  min-height: 100vh;
  padding: 20px;
}

.dispense-container-new {
  max-width: 900px;
  margin: 0 auto;
}

.dispense-header {
  margin-bottom: 30px;
}

.dispense-title-new {
  font-size: 2.5em;
  color: #fff;
  margin: 0 0 10px 0;
  font-weight: 300;
}

.dispense-subtitle {
  color: #9ca3af;
  font-size: 1em;
  margin: 0;
}

.dispense-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dispense-card-new {
  background-color: #252b3d;
  border-radius: 12px;
  padding: 25px;
  border: 1px solid #374151;
}

.card-title-new {
  font-size: 1.3em;
  color: #fff;
  margin: 0 0 20px 0;
  font-weight: 500;
}

.prescription-details-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-label {
  color: #9ca3af;
  font-size: 0.9em;
}

.detail-value {
  color: #fff;
  font-size: 1em;
  font-weight: 500;
}

.medicine-table-container {
  overflow-x: auto;
}

.medicine-table-new {
  width: 100%;
  border-collapse: collapse;
}

.medicine-table-new thead th {
  text-align: left;
  padding: 12px;
  color: #9ca3af;
  font-size: 0.85em;
  font-weight: 600;
  border-bottom: 2px solid #374151;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.medicine-table-new tbody td {
  padding: 15px 12px;
  color: #fff;
  font-size: 0.95em;
  border-bottom: 1px solid #374151;
}

.medicine-table-new tbody tr.allergy-row {
  background-color: rgba(220, 53, 69, 0.05);
}

.medicine-name-allergy {
  color: #ef4444 !important;
  font-weight: 600;
}

.dosage-allergy {
  color: #ef4444 !important;
}

.stock-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 600;
}

.stock-available-new {
  background-color: rgba(74, 222, 128, 0.15);
  color: #4ade80;
}

.stock-unavailable-new {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.allergy-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 600;
}

.allergy-safe-new {
  background-color: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.allergy-warning-new {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.payment-info {
  color: #d1d5db;
  font-size: 0.95em;
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.payment-info strong {
  color: #fff;
  font-weight: 600;
}

.allergy-warning-box {
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid #ef4444;
  border-radius: 8px;
  padding: 12px 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 15px 0;
  color: #fca5a5;
  font-size: 0.9em;
}

.loading-message {
  color: #9ca3af;
  margin: 15px 0;
}

.error-message-new {
  color: #ef4444;
  background-color: rgba(239, 68, 68, 0.1);
  padding: 12px;
  border-radius: 8px;
  margin: 15px 0;
}

.success-message-new {
  color: #4ade80;
  background-color: rgba(74, 222, 128, 0.1);
  padding: 12px;
  border-radius: 8px;
  margin: 15px 0;
}

.confirmation-actions-new {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.cancel-btn-new {
  background-color: #6b7280 !important;
  color: #fff !important;
  padding: 12px 30px;
  border-radius: 8px;
  text-transform: none;
  font-weight: 500;
}

.cancel-btn-new:hover {
  background-color: #4b5563 !important;
}

.confirm-btn-new {
  background-color: #4ade80 !important;
  color: #1a1d2e !important;
  padding: 12px 30px;
  border-radius: 8px;
  text-transform: none;
  font-weight: 600;
  flex: 1;
}

.confirm-btn-new:hover {
  background-color: #22c55e !important;
}

.confirm-btn-new:disabled {
  background-color: #374151 !important;
  color: #6b7280 !important;
  opacity: 0.5;
}
</style>
