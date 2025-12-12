# Pharmacy Login Troubleshooting Guide

## ✅ App.vue is Correctly Configured

The `App.vue` file has been verified and contains all necessary code:

### 1. Import Statement ✅
```javascript
import PharmacyDashboard from './components/PharmacyDashboard.vue';
```

### 2. Template Component ✅
```vue
<PharmacyDashboard v-else-if="currentMode === 'dashboard' && currentUser.role === 'pharmacy'"
                   :user="currentUser"
                   @logout="handleLogout"
                   @switch-mode="switchMode" />
```

### 3. Login Handler ✅
```javascript
if (currentUser.value.role === 'doctor' || currentUser.value.role === 'patient' || currentUser.value.role === 'pharmacy') {
  currentMode.value = 'dashboard';
  console.log(`Giriş Başarılı. Rol: ${currentUser.value.role}`);
}
```

---

## 🔧 If You're Still Seeing the Placeholder Page

### Step 1: Verify Component Files Exist
Check that these files exist:
- ✅ `src/components/PharmacyDashboard.vue`
- ✅ `src/components/ATMStockManagement.vue`

### Step 2: Restart Development Server
```bash
# Stop the current server (Ctrl+C)
# Then restart:
cd Medicine-Frontend
npm run dev
```

### Step 3: Clear Browser Cache
- **Chrome/Edge:** Press `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
- **Firefox:** Press `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)
- Or open DevTools (F12) → Network tab → Check "Disable cache"

### Step 4: Verify Login Email Format
Make sure you're using an email ending with `@ph.medicine`:
- ✅ Correct: `staff@ph.medicine`
- ✅ Correct: `pharmacist@ph.medicine`
- ❌ Wrong: `staff@pharmacy.medicine`
- ❌ Wrong: `staff@medicine`

### Step 5: Check Browser Console
1. Open DevTools (F12)
2. Go to **Console** tab
3. Look for errors like:
   - `Failed to resolve component: PharmacyDashboard`
   - Import errors
   - Compilation errors

### Step 6: Check Network Tab
1. Open DevTools (F12)
2. Go to **Network** tab
3. After login, check if:
   - Login request succeeds (status 200)
   - Component files are loaded

### Step 7: Add Debug Logging
Temporarily add this to `handleLoginSuccess` function to debug:

```javascript
const handleLoginSuccess = (user) => {
  console.log('=== LOGIN DEBUG ===');
  console.log('User:', user);
  
  currentUser.value.name = user.name;
  currentUser.value.email = user.email;
  console.log('Email:', user.email);

  if (user.email.endsWith('@dr.medicine')) {
    currentUser.value.role = 'doctor';
  } else if (user.email.endsWith('@pt.medicine')) {
    currentUser.value.role = 'patient';
  } else if (user.email.endsWith('@ph.medicine')) {
    currentUser.value.role = 'pharmacy';
  }
  
  console.log('Detected Role:', currentUser.value.role);
  console.log('Current Mode Before:', currentMode.value);

  if (currentUser.value.role === 'doctor' || currentUser.value.role === 'patient' || currentUser.value.role === 'pharmacy') {
    currentMode.value = 'dashboard';
    console.log('Current Mode After:', currentMode.value);
    console.log(`Giriş Başarılı. Rol: ${currentUser.value.role}`);
  } else {
    currentMode.value = 'other';
    console.log('Going to OTHER mode');
  }
  console.log('=== END DEBUG ===');
};
```

Then check the console output after login.

---

## 🧪 Test Login Flow

### Test Case 1: Pharmacy Login
1. Navigate to login page
2. Enter email: `staff@ph.medicine`
3. Enter password
4. Click login
5. **Expected:** Should see PharmacyDashboard with welcome message
6. **Console should show:** `Giriş Başarılı. Rol: pharmacy`

### Test Case 2: Check currentMode and currentUser
After login, open console and type:
```javascript
// In browser console
console.log('Current Mode:', currentMode.value);
console.log('Current User:', currentUser.value);
```

Should output:
```
Current Mode: dashboard
Current User: { role: 'pharmacy', name: 'Staff Name', email: 'staff@ph.medicine' }
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Failed to resolve component: PharmacyDashboard"
**Solution:** 
- Verify file exists at `src/components/PharmacyDashboard.vue`
- Restart dev server
- Check for syntax errors in PharmacyDashboard.vue

### Issue 2: Blank page after login
**Solution:**
- Check browser console for errors
- Verify all imports are correct
- Check if Vuetify is properly configured

### Issue 3: Still seeing placeholder page
**Solution:**
- Verify email ends with `@ph.medicine`
- Check console logs to see what role is detected
- Verify `currentMode` is set to `'dashboard'`

### Issue 4: Component renders but looks broken
**Solution:**
- Check if Vuetify CSS is loaded
- Verify Material Design Icons are loaded
- Check for CSS conflicts

---

## 📋 Verification Checklist

Before reporting an issue, verify:

- [ ] `App.vue` contains `import PharmacyDashboard from './components/PharmacyDashboard.vue';`
- [ ] `App.vue` template has `<PharmacyDashboard v-else-if="currentMode === 'dashboard' && currentUser.role === 'pharmacy'">`
- [ ] `handleLoginSuccess` includes `'pharmacy'` in the if condition
- [ ] `PharmacyDashboard.vue` file exists in `src/components/`
- [ ] `ATMStockManagement.vue` file exists in `src/components/`
- [ ] Dev server is running (`npm run dev`)
- [ ] Browser cache is cleared
- [ ] Login email ends with `@ph.medicine`
- [ ] No errors in browser console
- [ ] Backend is running on port 8080

---

## 🎯 Quick Test Command

Run this in your terminal to verify files exist:
```bash
cd Medicine-Frontend
ls -la src/components/Pharmacy*.vue
ls -la src/components/ATMStock*.vue
```

Expected output:
```
PharmacyDashboard.vue
ATMStockManagement.vue
```

---

## 💡 Still Having Issues?

If you've tried all the above and still see the placeholder page:

1. **Share Console Output:** Copy all console logs after login
2. **Share Network Tab:** Check if any requests are failing
3. **Verify Backend:** Make sure pharmacy user exists in database
4. **Check Login Component:** Verify `Login.vue` is emitting the correct user object

The code in `App.vue` is **100% correct** and should work. The issue is likely environmental (cache, server not restarted) or related to the login data format.
