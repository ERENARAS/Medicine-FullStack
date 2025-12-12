<template>
  <div class="atm-locations-container">
    <!-- Header with Back Button -->
    <div class="header-section">
      <div class="header-left">
        <v-btn class="back-btn" @click="$emit('back')" variant="text" size="small">
          <v-icon left>mdi-arrow-left</v-icon>
          Geri Dön
        </v-btn>
        <h1 class="page-title">ATM Konumları Yönetimi</h1>
      </div>
      
      <!-- Search and Add Actions -->
      <div class="header-actions">
        <div class="search-box">
          <v-icon color="#555">mdi-magnify</v-icon>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Adrese göre ara..." 
            class="search-input"
          />
        </div>
        
        <v-btn 
          class="add-btn" 
          @click="showAddDialog = true"
          prepend-icon="mdi-plus"
        >
          Add New ATM
        </v-btn>
      </div>
    </div>

    <!-- Content Area -->
    <div class="content-area">
      
      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <v-progress-circular indeterminate color="#a2d6b8" size="64"></v-progress-circular>
        <p>ATM verileri yükleniyor...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="error-state">
        <v-alert type="error" variant="tonal" border="start">
          {{ errorMessage }}
          <template v-slot:append>
             <v-btn variant="text" size="small" @click="fetchATMs">Tekrar Dene</v-btn>
          </template>
        </v-alert>
      </div>

      <!-- ATM Grid -->
      <div v-else class="atm-grid">
        <v-card 
          v-for="atm in filteredATMs" 
          :key="atm.id" 
          class="atm-card elevation-2"
        >
          <div class="atm-icon-wrapper">
            <!-- Alternating icons for visual variety or based on status if backend supported it better -->
            <v-icon color="#252B61" size="32">mdi-hospital-box</v-icon>
          </div>
          
          <div class="atm-info">
            <h3 class="atm-title">ATM-{{ String(atm.id).padStart(3, '0') }} - {{ extractDistrict(atm.location) }}</h3>
            <p class="atm-address">{{ atm.location }}</p>
          </div>
          
          <div class="atm-status">
            <!-- Simulated status indicator -->
            <div class="status-indicator online"></div>
          </div>
        </v-card>

        <!-- No Results -->
        <div v-if="filteredATMs.length === 0" class="no-results">
          <v-icon size="64" color="#ccc">mdi-map-marker-off</v-icon>
          <p>Aradığınız kriterlere uygun ATM bulunamadı.</p>
        </div>
      </div>

    </div>

    <!-- Add ATM Dialog -->
    <v-dialog v-model="showAddDialog" max-width="500px">
      <v-card class="add-dialog-card">
        <v-card-title class="dialog-title">Yeni ATM Ekle</v-card-title>
        <v-card-text>
          <div class="dialog-form">
            <p class="dialog-subtitle">Lütfen yeni ATM'nin tam konum bilgisini giriniz.</p>
            
            <div class="form-group">
              <label>Konum / Adres</label>
              <input 
                v-model="newAtmLocation" 
                type="text" 
                class="form-input" 
                placeholder="Örn: Barbaros Bulvarı, No:12, Beşiktaş, İstanbul"
                @keyup.enter="addATM"
              />
            </div>
            
            <p v-if="addError" class="error-text">{{ addError }}</p>
          </div>
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-spacer></v-spacer>
          <v-btn color="#666" variant="text" @click="closeDialog">İptal</v-btn>
          <v-btn 
            color="#252B61" 
            class="save-btn" 
            :loading="isAdding" 
            @click="addATM"
          >
            Kaydet
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success Snackbar -->
    <v-snackbar v-model="showSuccessType" color="success" timeout="3000">
      {{ successMessage }}
      <template v-slot:actions>
        <v-btn variant="text" @click="showSuccessType = false">Kapat</v-btn>
      </template>
    </v-snackbar>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  userId: [Number, String]
});

const emit = defineEmits(['back']);
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// State
const atms = ref([]);
const isLoading = ref(true);
const errorMessage = ref('');
const searchQuery = ref('');

// Add Dialog State
const showAddDialog = ref(false);
const newAtmLocation = ref('');
const isAdding = ref(false);
const addError = ref('');
const showSuccessType = ref(false);
const successMessage = ref('');

// Computed
const filteredATMs = computed(() => {
  if (!searchQuery.value) return atms.value;
  const query = searchQuery.value.toLowerCase();
  return atms.value.filter(atm => 
    atm.location.toLowerCase().includes(query) || 
    `atm-${atm.id}`.includes(query)
  );
});

// Methods
const fetchATMs = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const params = props.userId ? { staffId: props.userId } : {};
    const response = await axios.get(`${API_BASE_URL}/api/atm/all`, { params });
    atms.value = response.data;
  } catch (error) {
    console.error('ATM listesi alınamadı:', error);
    errorMessage.value = 'ATM listesi yüklenirken bir hata oluştu.';
  } finally {
    isLoading.value = false;
  }
};

const extractDistrict = (location) => {
  if (!location) return '';
  // Try to find a distict-like part from a comma separated string, simplistic approach
  const parts = location.split(',');
  if (parts.length > 1) {
    // Return the second to last part usually (District, City) or just the first part if short
    return parts[0].trim();
  }
  return location.length > 20 ? location.substring(0, 20) + '...' : location;
};

const closeDialog = () => {
  showAddDialog.value = false;
  newAtmLocation.value = '';
  addError.value = '';
};

const addATM = async () => {
  if (!newAtmLocation.value.trim()) {
    addError.value = 'Lütfen bir konum giriniz.';
    return;
  }
  
  if (!props.userId) {
     addError.value = 'Kullanıcı bilgisi eksik, lütfen tekrar giriş yapın.';
     return;
  }

  isAdding.value = true;
  addError.value = '';

  try {
    await axios.post(`${API_BASE_URL}/api/atm/create`, { 
      location: newAtmLocation.value,
      staffId: props.userId
    });
    
    successMessage.value = 'Yeni ATM başarıyla eklendi!';
    showSuccessType.value = true;
    closeDialog();
    await fetchATMs(); // Reload list
    
  } catch (error) {
    console.error('ATM ekleme hatası:', error);
    addError.value = 'ATM eklenirken bir hata oluştu: ' + (error.response?.data?.message || error.message);
  } finally {
    isAdding.value = false;
  }
};

onMounted(() => {
  fetchATMs();
});
</script>

<style scoped>
.atm-locations-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  color: #555 !important;
  text-transform: none;
}

.page-title {
  font-size: 2em;
  color: #fff; /* Dark mode background assumed from image context, but parent is light/medicinal? 
                  The user image shows dark background. But PharmacyDashboard.vue has #ECEEF9 bg.
                  I will use dark text for light bg or adjust. User asked for colors from project.
                  PharmacyDashboard uses #252B61 text for titles mostly.
               */
  color: #252B61;               
  margin: 0;
  font-weight: 600;
}

/* Header Actions */
.header-actions {
  display: flex;
  gap: 15px;
  flex: 1;
  justify-content: flex-end;
  min-width: 300px;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(37, 43, 97, 0.05); /* Slight tint */
  border: 1px solid rgba(37, 43, 97, 0.1);
  border-radius: 8px;
  padding: 0 12px;
  flex: 1;
  max-width: 300px;
}

.search-input {
  border: none;
  background: transparent;
  padding: 10px;
  width: 100%;
  color: #252B61;
  outline: none;
}

.add-btn {
  background-color: #4CC9F0 !important; /* Cyan-ish button from image, or use project green/blue */
  color: #fff !important;
  text-transform: none;
  font-weight: 600;
}

/* Content */
.content-area {
  min-height: 400px;
}

.loading-state, .error-state, .no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px;
  text-align: center;
  color: #666;
  gap: 15px;
}

/* ATM Grid */
.atm-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.atm-card {
  background-color: #2a2f45 !important; /* Dark card color from image? Or light? 
     The image shows dark mode. But the rest of this project (PharmacyDashboard) seems to use light mode cards on #ECEEF9.
     "resimdeki renkler farkli olsa da digerlerinde ne kullandiysan onlar olsun" -> Use project colors.
     Project uses white cards on light bg usually.
     Let's use white cards for consistency with PharmacyDashboard.
  */
  background-color: #fff !important;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.2s;
}

.atm-card:hover {
  transform: translateY(-2px);
}

.atm-icon-wrapper {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: rgba(37, 43, 97, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.atm-info {
  flex: 1;
}

.atm-title {
  font-size: 1.1em;
  font-weight: 600;
  color: #252B61;
  margin-bottom: 4px;
}

.atm-address {
  font-size: 0.9em;
  color: #666;
  margin: 0;
  line-height: 1.4;
  /* limit lines */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.atm-status {
  padding-left: 10px;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-indicator.online {
  background-color: #4CAF50; /* Green */
  box-shadow: 0 0 8px rgba(76, 175, 80, 0.5);
}

/* Dialog */
.add-dialog-card {
  padding: 20px;
  border-radius: 16px !important;
}

.dialog-title {
  color: #252B61;
  font-weight: 600;
  padding-left: 0;
}

.dialog-subtitle {
  color: #666;
  margin-bottom: 20px;
  font-size: 0.95em;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #252B61;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1em;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #252B61;
}

.error-text {
  color: #ff5252;
  font-size: 0.9em;
  margin-top: 5px;
}

.save-btn {
  color: white !important;
  text-transform: none;
  padding: 0 25px;
}

/* Responsive */
@media (max-width: 600px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    max-width: none;
  }
}
</style>
