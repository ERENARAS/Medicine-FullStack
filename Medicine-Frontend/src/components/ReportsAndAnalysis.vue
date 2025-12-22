<template>
  <div class="reports-container">
    <!-- Header -->
    <div class="reports-header">
      <div class="header-content">
        <h1 class="page-title">Raporlar ve Analiz</h1>
        <p class="page-subtitle">ATM stok hareketleri, kullanım verileri ve operasyonel metrikler.</p>
      </div>
      
      <div class="header-actions">
      </div>
    </div>

    <!-- Top Stats Cards -->
    <div class="stats-grid">
      <!-- Critical Stock Alerts (Dynamic) -->
      <v-card class="stat-card elevation-2">
        <div class="stat-icon-wrapper red-tint">
          <v-icon color="#FF5252">mdi-alert-outline</v-icon>
        </div>
        <div class="stat-info">
          <p class="stat-label">Kritik Stok Uyarısı</p>
          <h3 class="stat-value">{{ criticalStockCount }} ATM</h3>
          <span class="stat-badge error">Aksiyon Gerekli</span>
        </div>
      </v-card>
    </div>

    <!-- Charts Section -->
    <div class="charts-grid">
      <!-- Stock Levels Chart -->
      <v-card class="chart-card large elevation-2">
        <div class="card-header">
            <h3>Stok Seviyeleri (Haftalık)</h3>
            <p>Günlük bazda toplam ilaç stok durumu</p>
        </div>
        <div class="chart-container">
            <Bar v-if="stockChartData.labels" :data="stockChartData" :options="stockChartOptions" />
        </div>
      </v-card>

      <!-- Best Sellers Chart -->
      <v-card class="chart-card small elevation-2">
        <div class="card-header">
            <h3>En Çok Satılanlar</h3>
            <p>İlaç bazlı dağılım</p>
        </div>
        <div class="chart-container donut-container">
             <Doughnut v-if="salesChartData.labels" :data="salesChartData" :options="salesChartOptions" />
             <div class="chart-center-text" v-if="salesChartData.datasets">
                 <h3>{{ formattedTotalSales }}</h3>
                 <p>Toplam</p>
             </div>
        </div>
        <!-- Custom Legend -->
        <div class="custom-legend" v-if="salesChartData.labels">
            <div v-for="(label, index) in salesChartData.labels" :key="index" class="legend-item">
                <span class="dot" :style="{ backgroundColor: salesChartData.datasets[0].backgroundColor[index] }"></span>
                <span class="label">{{ label }}</span>
                <span class="value">{{ salesChartData.datasets[0].data[index] }}%</span>
            </div>
        </div>
      </v-card>
    </div>

    <!-- Low Stock Alerts Table (Full width now) -->
    <div class="bottom-grid">
        <v-card class="table-card elevation-2" style="grid-column: 1 / -1;">
            <div class="card-header">
                <h3>Düşük Stok Uyarıları</h3>
                <p>Aksiyon gerektiren ATM üniteleri listesi.</p>
            </div>
            <v-table>
                <thead>
                    <tr>
                        <th class="text-left">ATM ID</th>
                        <th class="text-left">Lokasyon</th>
                        <th class="text-left">İlaç Adı</th>
                        <th class="text-center">Stok</th>
                        <th class="text-right">Durum</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="alert in lowStockAlerts" :key="alert.id">
                        <td>ATM-{{ String(alert.atmId).padStart(3, '0') }}</td>
                        <td>{{ alert.location }}</td>
                        <td>{{ alert.medicine }}</td>
                        <td class="text-center text-red font-weight-bold">{{ alert.stock }}</td>
                        <td class="text-right">
                            <v-chip color="error" size="x-small" variant="flat">Kritik</v-chip>
                        </td>
                    </tr>
                     <tr v-if="lowStockAlerts.length === 0">
                        <td colspan="5" class="text-center pa-4 text-grey">Kritik stok uyarısı bulunmuyor.</td>
                    </tr>
                </tbody>
            </v-table>
        </v-card>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js';
import { Bar, Doughnut } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

const props = defineProps({
    userId: [Number, String]
});

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// State
const criticalStockCount = ref(0);
const lowStockAlerts = ref([]);
const totalSalesCount = ref(0); // For center text of Best Sellers

// Charts State
const stockChartData = ref({});
const salesChartData = ref({});

// Data Fetching
const fetchATMData = async () => {
    try {
        const params = props.userId ? { staffId: props.userId } : {};
        const response = await axios.get(`${API_BASE_URL}/api/atm/all`, { params });
        const atms = response.data;
        
        // Process for Low Stock Alerts
        const alerts = [];
        let count = 0;
        
        atms.forEach(atm => {
            if (atm.stock) {
                Object.entries(atm.stock).forEach(([medicine, qty]) => {
                    if (qty < 30) { // Threshold < 30
                        count++;
                        if (alerts.length < 5) { // Show max 5
                             alerts.push({
                                id: `${atm.id}-${medicine}`,
                                atmId: atm.id,
                                location: atm.location.split(',')[0], // Shorten location
                                medicine: medicine,
                                stock: qty
                            });
                        }
                    }
                });
            }
        });
        
        criticalStockCount.value = count;
        lowStockAlerts.value = alerts;

        // Process for Stock Levels Chart (Mocking Weekly Data based on current)
        // In real world, we'd need historical stock data endpoint.
        // We will simulate a slight variation for previous days based on current total.
        
        // Calculate current total stock
        let totalCurrentStock = 0;
        atms.forEach(atm => {
             if (atm.stock) {
                 totalCurrentStock += Object.values(atm.stock).reduce((a, b) => a + b, 0);
             }
        });

        const days = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
        const mockWeeklyData = days.map(() => {
             // Random variance +/- 20%
             return Math.floor(totalCurrentStock); 
        });
        
        // Set last day to actual total (assuming today is the last day)
        mockWeeklyData[days.length - 1] = totalCurrentStock;

        stockChartData.value = {
            labels: days,
            datasets: [{
                label: 'Toplam Stok',
                backgroundColor: ['#4CC9F0', '#4CC9F0', '#4CC9F0', '#4CC9F0', '#00B4D8', '#4CC9F0', '#4CC9F0'], // Highlight current day maybe?
                borderRadius: 4,
                data: mockWeeklyData
            }]
        };

    } catch (error) {
        console.error("ATM verisi hatası:", error);
    }
};

const fetchSalesData = async () => {
     try {
        const response = await axios.get(`${API_BASE_URL}/api/data/historical-sales`);
        const sales = response.data;
        
        // Group by Medicine Name
        const salesByMedicine = {};
        let total = 0;
        
        sales.forEach(record => {
             const name = record.medicineName;
             const qty = record.quantity || 1;
             salesByMedicine[name] = (salesByMedicine[name] || 0) + qty;
             total += qty;
        });

        totalSalesCount.value = total;

        // Convert to array and sort
        let sortedSales = Object.entries(salesByMedicine)
            .map(([name, qty]) => ({ name, qty }))
            .sort((a, b) => b.qty - a.qty);

        // Take top 7 and group the rest
        let topSales = sortedSales.slice(0, 7);
        const otherSales = sortedSales.slice(7);
        
        if (otherSales.length > 0) {
            const otherQty = otherSales.reduce((sum, item) => sum + item.qty, 0);
            topSales.push({ name: 'Diğer', qty: otherQty });
        }

        const labels = topSales.map(item => item.name);
        // Calculate percentages
        const percentages = topSales.map(item => ((item.qty / total) * 100).toFixed(0));

        // Extended color palette
        const backgroundColors = [
            '#F72585', // Pink
            '#4CC9F0', // Light Blue
            '#7209B7', // Purple
            '#3A0CA3', // Dark Blue
            '#4361EE', // Blue
            '#4895EF', // Sky Blue
            '#56CFE1', // Teal
            '#E0E0E0'  // Grey for 'Other'
        ];

        salesChartData.value = {
            labels: labels,
            datasets: [{
                backgroundColor: backgroundColors.slice(0, labels.length),
                data: percentages,
                borderWidth: 0
            }]
        };

     } catch (error) {
         console.error("Satış verisi hatası:", error);
     }
}

// Chart Options
const stockChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: { display: false },
        tooltip: {
             backgroundColor: '#252B61',
             padding: 10,
             cornerRadius: 8,
        }
    },
    scales: {
        y: { display: false, beginAtZero: true },
        x: { 
            grid: { display: false },
            ticks: { color: '#888' }
        }
    }
};

const salesChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    plugins: {
        legend: { display: false },
        tooltip: {
            callbacks: {
                label: function(context) {
                    let label = context.label || '';
                    if (label) {
                        label += ': ';
                    }
                    if (context.parsed !== null) {
                        label += context.parsed + '%';
                    }
                    return label;
                }
            }
        }
    }
};

const formattedTotalSales = computed(() => {
    const total = totalSalesCount.value;
    return total >= 1000 ? (total / 1000).toFixed(1) + 'k' : total;
});

onMounted(() => {
    fetchATMData();
    fetchSalesData();
});
</script>

<style scoped>
.reports-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  color: #252B61;
}

/* Header */
.reports-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 2em;
  margin: 0;
  font-weight: 700;
}

.page-subtitle {
  margin: 5px 0 0;
  color: #666;
  font-size: 0.95em;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  color: #fff !important; 
  border-color: #ddd !important;
  color: #555 !important;
  text-transform: none;
}

.export-btn {
  background-color: #4CC9F0 !important;
  color: #fff !important;
  text-transform: none;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(76, 201, 240, 0.4);
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #fff;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blue-tint { background-color: rgba(76, 201, 240, 0.1); }
.red-tint { background-color: rgba(255, 82, 82, 0.1); }
.green-tint { background-color: rgba(76, 175, 80, 0.1); }
.purple-tint { background-color: rgba(156, 39, 176, 0.1); }

.stat-info .stat-label {
  font-size: 0.85em;
  color: #888;
  margin: 0;
}

.stat-info .stat-value {
  font-size: 1.5em;
  font-weight: 700;
  margin: 2px 0;
  color: #252B61;
}

.stat-badge {
    font-size: 0.75em;
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: 600;
}

.stat-badge.error { background-color: rgba(255, 82, 82, 0.1); color: #FF5252; }
.stat-badge.success { background-color: rgba(76, 175, 80, 0.1); color: #4CAF50; }

.stat-trend {
    font-size: 0.8em;
    font-weight: 500;
}
.stat-trend.positive { color: #4CAF50; }

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

@media(max-width: 900px) {
    .charts-grid { grid-template-columns: 1fr; }
}

.chart-card {
    padding: 24px;
    border-radius: 16px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.card-header h3 {
    margin: 0;
    font-size: 1.2em;
    font-weight: 600;
    width: 100%;
    color: #252B61;
}

.card-header p {
    margin: 5px 0 0;
    font-size: 0.9em;
    color: #888;
}

.chart-container {
    position: relative;
    flex: 1;
    min-height: 250px;
}

.donut-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 200px;
    position: relative;
}

.chart-center-text {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
}

.chart-center-text h3 {
    margin: 0;
    font-size: 1.8em;
    font-weight: 700;
    line-height: 1;
}

.chart-center-text p {
    margin: 0;
    font-size: 0.8em;
    color: #888;
}

.custom-legend {
    margin-top: 20px;
}

.legend-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 0.9em;
}

.legend-item .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-right: 10px;
}

.legend-item .label {
    flex: 1;
    color: #666;
}

.legend-item .value {
    font-weight: 600;
    color: #252B61;
}

/* Bottom Grid */
.bottom-grid {
    display: grid;
    grid-template-columns: 1fr; /* Changed to single column */
    gap: 20px;
}

.table-card {
    padding: 24px;
    border-radius: 16px;
    background-color: #fff;
    min-height: 300px;
}

.text-red { color: #FF5252 !important; }

.trend-icon {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 5px;
    font-size: 0.85em;
    color: #666;
}
.trend-icon.up { color: #4CAF50; }

.loader {
    width: 100%;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.error-msg {
    color: #FF5252;
    padding: 20px;
    text-align: center;
}
</style>
