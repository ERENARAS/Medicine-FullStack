<template>
  <div class="stock-management-container">
    <!-- Header with Back Button -->
    <div class="header-section">
      <div class="header-left">
        <v-btn class="back-btn" @click="$emit('back')" variant="text" size="small">
          <v-icon left>mdi-arrow-left</v-icon>
          Geri Dön
        </v-btn>
        <h1 class="page-title">ATM Stok Yönetimi</h1>
      </div>
      <div class="atm-selector">
        <label>ATM Seçimi:</label>
        <select v-model="selectedAtmId" @change="fetchStock" class="atm-select">
          <option value="1">ATM #1</option>
          <option value="2">ATM #2</option>
        </select>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="content-grid">
      
      <!-- Stock Table Section -->
      <v-card class="stock-table-card elevation-2">
        <h2 class="section-title">İlaç Stokları</h2>
        
        <div class="search-container">
          <input 
            v-model="searchQuery" 
            type="text" 
            class="search-input" 
            placeholder="İlaç veya kod ara..."
          />
        </div>

        <p v-if="isLoading" class="loading-text">
          <v-progress-linear indeterminate color="#a2d6b8"></v-progress-linear>
          Stok bilgileri yükleniyor...
        </p>
        
        <p v-else-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div v-else class="table-container">
          <table class="stock-table">
            <thead>
              <tr>
                <th>İlaç Adı</th>
                <th>Mevcut Miktar</th>
                <th>Durum</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(quantity, medicineName) in filteredStock" :key="medicineName">
                <td>{{ medicineName }}</td>
                <td class="quantity-cell">{{ quantity }}</td>
                <td>
                  <span v-if="quantity > 20" class="status-badge status-good">Yeterli</span>
                  <span v-else-if="quantity > 5" class="status-badge status-warning">Düşük</span>
                  <span v-else class="status-badge status-critical">Kritik</span>
                </td>
              </tr>
              <tr v-if="Object.keys(filteredStock).length === 0">
                <td colspan="3" class="no-data">Stok bulunamadı.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </v-card>

      <!-- Add Stock Form Section -->
      <v-card class="add-stock-card elevation-2">
        <h2 class="section-title">Stok Ekle/Güncelle</h2>
        
        <form @submit.prevent="addStock" class="stock-form">
          <div class="form-group">
            <label for="medicine-name">İlaç Adı</label>
            <input 
              id="medicine-name"
              v-model="formData.medicineName"
              type="text"
              class="form-input"
              placeholder="İlaç adını yazın..."
              required
            />
          </div>

          <div class="form-group">
            <label for="quantity">Eklenecek Miktar</label>
            <input 
              id="quantity"
              v-model.number="formData.quantity"
              type="number"
              class="form-input"
              placeholder="0"
              min="1"
              required
            />
          </div>

          <div class="form-group">
            <label for="shelf-code">Raf/Bölme Kodu (Opsiyonel)</label>
            <input 
              id="shelf-code"
              v-model="formData.shelfCode"
              type="text"
              class="form-input"
              placeholder="Örn: A1-03"
            />
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>
          <p v-if="formSuccess" class="form-success">{{ formSuccess }}</p>

          <v-btn 
            type="submit" 
            class="submit-btn" 
            :disabled="isSubmitting"
            block
          >
            <v-icon left size="small">mdi-plus-circle</v-icon>
            {{ isSubmitting ? 'Ekleniyor...' : 'Stok Güncelle' }}
          </v-btn>
        </form>

        <!-- Quick Stats -->
        <div class="quick-stats">
          <div class="stat-item">
            <span class="stat-label">Toplam İlaç Çeşidi:</span>
            <span class="stat-value">{{ Object.keys(stockData).length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">ATM Konumu:</span>
            <span class="stat-value">{{ atmLocation }}</span>
          </div>
        </div>
      </v-card>

      <!-- Recent Activity Section -->
      <v-card class="activity-card elevation-2">
        <h2 class="section-title">Son 10 Hareket</h2>
        
        <div class="table-container">
          <table class="activity-table">
            <thead>
              <tr>
                <th>Tarih/Saat</th>
                <th>İlaç Adı</th>
                <th>Hareket Tipi</th>
                <th>Miktar</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="activity in recentActivities" :key="activity.id">
                <td>{{ activity.date }}</td>
                <td>{{ activity.medicineName }}</td>
                <td>
                  <span :class="['activity-type', activity.type === 'in' ? 'type-in' : 'type-out']">
                    {{ activity.type === 'in' ? 'Stok Girişi' : 'Stok Çıkışı' }}
                  </span>
                </td>
                <td :class="['quantity-change', activity.type === 'in' ? 'positive' : 'negative']">
                  {{ activity.type === 'in' ? '+' : '-' }}{{ activity.quantity }}
                </td>
              </tr>
              <tr v-if="recentActivities.length === 0">
                <td colspan="4" class="no-data">Henüz hareket kaydı bulunmuyor.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </v-card>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const emit = defineEmits(['back']);

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// State
const selectedAtmId = ref(1);
const stockData = ref({});
const atmLocation = ref('Yükleniyor...');
const isLoading = ref(true);
const errorMessage = ref('');
const searchQuery = ref('');

// Form state
const formData = ref({
  medicineName: '',
  quantity: 0,
  shelfCode: ''
});
const isSubmitting = ref(false);
const formError = ref('');
const formSuccess = ref('');

// Mock recent activities (since backend doesn't provide this yet)
const recentActivities = ref([
  { id: 1, date: '2024-12-12 14:30', medicineName: 'Parol 500mg', type: 'in', quantity: 50 },
  { id: 2, date: '2024-12-12 11:23', medicineName: 'Majezik 100mg', type: 'in', quantity: 20 },
  { id: 3, date: '2024-12-12 09:15', medicineName: 'Aspirin 100mg', type: 'out', quantity: 2 },
]);

// Computed
const filteredStock = computed(() => {
  if (!searchQuery.value) return stockData.value;
  
  const query = searchQuery.value.toLowerCase();
  const filtered = {};
  
  Object.entries(stockData.value).forEach(([name, quantity]) => {
    if (name.toLowerCase().includes(query)) {
      filtered[name] = quantity;
    }
  });
  
  return filtered;
});

// Methods
const fetchStock = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    const response = await axios.get(`${API_BASE_URL}/api/pharmacy/stock/${selectedAtmId.value}`);
    stockData.value = response.data.stock || {};
    atmLocation.value = response.data.location || 'Bilinmiyor';
  } catch (error) {
    console.error('Stok bilgileri alınamadı:', error);
    errorMessage.value = 'Stok bilgileri yüklenirken bir hata oluştu: ' + (error.response?.data || error.message);
  } finally {
    isLoading.value = false;
  }
};

const addStock = async () => {
  if (!formData.value.medicineName || formData.value.quantity <= 0) {
    formError.value = 'Lütfen geçerli bir ilaç adı ve miktar giriniz.';
    return;
  }

  isSubmitting.value = true;
  formError.value = '';
  formSuccess.value = '';

  try {
    const payload = {
      atmId: selectedAtmId.value,
      medicineName: formData.value.medicineName,
      quantity: formData.value.quantity
    };

    const response = await axios.post(`${API_BASE_URL}/api/pharmacy/add-stock`, payload);
    
    formSuccess.value = response.data.message || 'Stok başarıyla eklendi!';
    
    // Add to recent activities
    recentActivities.value.unshift({
      id: Date.now(),
      date: new Date().toLocaleString('tr-TR'),
      medicineName: formData.value.medicineName,
      type: 'in',
      quantity: formData.value.quantity
    });
    
    // Keep only last 10
    if (recentActivities.value.length > 10) {
      recentActivities.value = recentActivities.value.slice(0, 10);
    }

    // Reset form
    formData.value = {
      medicineName: '',
      quantity: 0,
      shelfCode: ''
    };

    // Refresh stock data
    await fetchStock();

    // Clear success message after 3 seconds
    setTimeout(() => {
      formSuccess.value = '';
    }, 3000);

  } catch (error) {
    console.error('Stok ekleme hatası:', error);
    formError.value = error.response?.data || 'Stok eklenirken bir hata oluştu. Lütfen tekrar deneyin.';
  } finally {
    isSubmitting.value = false;
  }
};

// Lifecycle
onMounted(() => {
  fetchStock();
});
</script>

<style scoped>
.stock-management-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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
  color: #252B61;
  margin: 0;
  font-weight: 600;
}

.atm-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.atm-selector label {
  font-weight: 500;
  color: #555;
}

.atm-select {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #fff;
  font-size: 0.95em;
  color: #333;
  cursor: pointer;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto auto;
  gap: 20px;
}

.stock-table-card {
  grid-column: 1;
  grid-row: 1 / 3;
}

.add-stock-card {
  grid-column: 2;
  grid-row: 1;
}

.activity-card {
  grid-column: 1 / 3;
  grid-row: 3;
}

/* Card Styles */
.stock-table-card,
.add-stock-card,
.activity-card {
  background-color: #fff !important;
  border-radius: 12px;
  padding: 25px;
}

.section-title {
  font-size: 1.4em;
  color: #252B61;
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 600;
}

/* Search */
.search-container {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95em;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #a2d6b8;
}

/* Tables */
.table-container {
  overflow-x: auto;
}

.stock-table,
.activity-table {
  width: 100%;
  border-collapse: collapse;
}

.stock-table th,
.stock-table td,
.activity-table th,
.activity-table td {
  text-align: left;
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
}

.stock-table th,
.activity-table th {
  background-color: #f8f9fa;
  color: #252B61;
  font-weight: 600;
  font-size: 0.9em;
  text-transform: uppercase;
}

.stock-table tbody tr:hover,
.activity-table tbody tr:hover {
  background-color: #f8f9fa;
}

.quantity-cell {
  font-weight: 600;
  color: #252B61;
  font-size: 1.1em;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 600;
}

.status-good {
  background-color: #d4edda;
  color: #155724;
}

.status-warning {
  background-color: #fff3cd;
  color: #856404;
}

.status-critical {
  background-color: #f8d7da;
  color: #721c24;
}

.activity-type {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 0.85em;
  font-weight: 500;
}

.type-in {
  background-color: #d4edda;
  color: #155724;
}

.type-out {
  background-color: #f8d7da;
  color: #721c24;
}

.quantity-change {
  font-weight: 600;
  font-size: 1.05em;
}

.quantity-change.positive {
  color: #28a745;
}

.quantity-change.negative {
  color: #dc3545;
}

.no-data {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 30px !important;
}

/* Form */
.stock-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #555;
  font-size: 0.9em;
}

.form-input {
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95em;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #a2d6b8;
}

.submit-btn {
  background-color: #252B61 !important;
  color: #fff !important;
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 1em;
  font-weight: 600;
  text-transform: none;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #1a1f4a !important;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Quick Stats */
.quick-stats {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 0.9em;
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #252B61;
  font-size: 1em;
}

/* Messages */
.loading-text {
  color: #666;
  text-align: center;
  padding: 20px;
}

.error-message {
  color: #721c24;
  background-color: #f8d7da;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.form-error {
  color: #721c24;
  background-color: #f8d7da;
  padding: 10px;
  border-radius: 6px;
  font-size: 0.9em;
}

.form-success {
  color: #155724;
  background-color: #d4edda;
  padding: 10px;
  border-radius: 6px;
  font-size: 0.9em;
}

/* Responsive */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .stock-table-card,
  .add-stock-card,
  .activity-card {
    grid-column: 1;
    grid-row: auto;
  }
}
</style>
