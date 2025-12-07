<template>
  <v-container fluid class="medicine-selection-container fill-height pa-0">
    <!-- Navbar -->
    <v-sheet class="navbar-area d-flex justify-space-between align-center px-6">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click.prevent="$emit('switch-mode', 'dashboard')">Ana Sayfa</a>
        <a href="#" class="ml-4">ATM Konumları</a>
        <a href="#" class="ml-4">Hakkında</a>
        <v-btn variant="outlined" size="small" class="logout-btn ml-6" @click="$emit('logout')">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <!-- Main Content -->
    <v-row no-gutters class="main-content-row justify-center">
      <v-col cols="12" class="d-flex flex-column align-center" style="max-width: 1200px; width: 100%;">
        
        <!-- Blue Header Area -->
        <div class="blue-header-area rounded-xl pa-8">
          <div class="header-content d-flex justify-space-between align-start">
            
            <!-- Search Bar -->
            <div class="search-section" style="width: 100%; max-width: 500px;">
              <v-text-field
                v-model="searchQuery"
                label="İlaç Adı / Hastalık"
                prepend-inner-icon="mdi-magnify"
                variant="solo"
                bg-color="white"
                hide-details
                rounded="lg"
                class="search-input"
              ></v-text-field>
            </div>

            <!-- Recipe Count / Basket -->
            <v-card class="recipe-count-card px-6 py-2 d-flex align-center justify-space-between" rounded="lg" width="200" height="60" elevation="0">
              <span class="text-h5 font-weight-medium">Reçete</span>
              <span class="text-h4 font-weight-bold ml-4">{{ selectedMedicines.length }}</span>
            </v-card>
          </div>

          <!-- Cards Grid -->
          <v-row class="mt-8">
            <v-col 
              v-for="medicine in filteredMedicines" 
              :key="medicine.id" 
              cols="12" sm="6" md="4" lg="3"
            >
              <v-card class="medicine-card pa-4" rounded="lg" elevation="0">
                <div class="d-flex justify-space-between align-start">
                  <div>
                    <div class="text-h6 font-weight-bold text-truncate">{{ medicine.name }}</div>
                    <div class="text-caption text-grey-darken-1">{{ medicine.treatment || 'Tedavi bilgisi yok' }}</div>
                  </div>
                  <!-- No 'i' icon as requested -->
                </div>

                <div class="medicine-image-area my-4 d-flex justify-center align-center bg-grey-lighten-4 rounded-lg" style="height: 120px;">
                  <!-- Placeholder visualization similar to screenshot using icons -->
                   <div style="opacity: 0.3;">
                      <v-icon size="64" color="grey">mdi-pill</v-icon>
                   </div>
                </div>

                <div class="d-flex justify-end align-center mt-2">
                   <v-alert
                    v-if="isAllergic(medicine)"
                    density="compact"
                    type="error"
                    variant="tonal"
                    icon="mdi-alert-circle"
                    class="pa-0 mb-0 flex-grow-1 mr-2 text-caption text-red"
                  >
                    Alerji Riski!
                  </v-alert>

                  <v-btn
                    :color="isSelected(medicine) ? 'red-darken-1' : '#252B61'"
                    class="text-white px-6"
                    rounded="pill"
                    size="small"
                    @click="toggleMedicine(medicine)"
                  >
                    {{ isSelected(medicine) ? 'Çıkar' : 'Ekle' }}
                  </v-btn>
                </div>
              </v-card>
            </v-col>
          </v-row>

          <!-- Complete Button (Optional functionality to finish) -->
           <div class="d-flex justify-end mt-6">
             <v-btn
                v-if="selectedMedicines.length > 0"
                size="large"
                color="#A2D6B8"
                class="text-white font-weight-bold"
                @click="finishSelection"
             >
               Reçeteyi Tamamla
             </v-btn>
           </div>
        </div>

      </v-col>
    </v-row>

  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  user: Object,
  patient: Object, // passed from App.vue
});

const emit = defineEmits(['switch-mode', 'logout', 'finish-prescription']);

const searchQuery = ref('');
const selectedMedicines = ref([]);
const allMedicines = ref([]);
const patientAllergies = ref([]);
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Fetch medicines from backend
const fetchMedicines = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/medicines`);
    allMedicines.value = response.data;
  } catch (error) {
    console.error("İlaçlar yüklenemedi:", error);
  }
};

// Fetch full patient details to get allergies
const fetchPatientDetails = async () => {
  if (!props.patient || !props.patient.email) return;
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/doctor/patient-record/${props.patient.email}`);
    // response.data contains { patient: {...}, prescriptionHistory: [...] }
    if (response.data.patient && response.data.patient.allergicMedicines) {
      patientAllergies.value = response.data.patient.allergicMedicines;
    }
  } catch (error) {
    console.error("Hasta detayları yüklenemedi:", error);
  }
};

onMounted(() => {
  fetchMedicines();
  fetchPatientDetails();
});

const filteredMedicines = computed(() => {
  if (!searchQuery.value) return allMedicines.value;
  const query = searchQuery.value.toLowerCase();
  return allMedicines.value.filter(m => 
    (m.name && m.name.toLowerCase().includes(query)) || 
    (m.treatment && m.treatment.toLowerCase().includes(query))
  );
});

const isSelected = (medicine) => {
  return selectedMedicines.value.some(m => m.id === medicine.id);
};

// Check if patient is allergic to this medicine
const isAllergic = (medicine) => {
  // medicine.name matches any string in patientAllergies array
  return patientAllergies.value.some(allergy => 
    allergy.toLowerCase() === medicine.name.toLowerCase()
  );
};

const toggleMedicine = (medicine) => {
  if (isSelected(medicine)) {
    selectedMedicines.value = selectedMedicines.value.filter(m => m.id !== medicine.id);
  } else {
    selectedMedicines.value.push(medicine);
  }
};

const finishSelection = () => {
  // Check for allergies in selected medicines before proceeding?
  // User asked to just warn, but backend will block it.
  const allergicSelections = selectedMedicines.value.filter(m => isAllergic(m));
  
  if (allergicSelections.length > 0) {
    const confirm = window.confirm(`DİKKAT: Hasta şu ilaçlara alerjik: ${allergicSelections.map(m => m.name).join(', ')}. Yine de devam etmek istiyor musunuz? (Backend reddedebilir)`);
    if (!confirm) return;
  }
  
  emit('finish-prescription', selectedMedicines.value);
};

</script>

<style scoped>
.medicine-selection-container {
  background-color: #ECEEF9;
  min-height: 100vh;
  padding-top: 20px;
}

.navbar-area {
  background-color: transparent !important;
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem 0;
  z-index: 10;
}

.logo {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.nav-links a {
  text-decoration: none;
  color: #555;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-links a:hover {
  color: #252B61;
}

.logout-btn {
  border: 1px solid #d6a2b8 !important;
  color: #d6a2b8 !important;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #d6a2b8 !important;
  color: white !important;
}

.main-content-row {
 width: 100%;
}

.blue-header-area {
  background-color: #202449; /* Deep blue from screenshot */
  width: 90%;
  min-height: 600px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.search-input :deep(.v-field__input) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.recipe-count-card {
  color: #000;
  background-color: white !important;
}

.medicine-card {
  background-color: #F8F9FD !important; /* Slight off-white/purple tint */
  transition: transform 0.2s;
  min-height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.medicine-card:hover {
  transform: translateY(-4px);
}
</style>
