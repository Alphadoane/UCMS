# ⚡ Quick Integration Checklist

## 🚀 5-Minute Setup

### **1. Database Setup (2 minutes)**
```bash
# Start MySQL
# Windows: net start MySQL80
# macOS: brew services start mysql
# Ubuntu: sudo systemctl start mysql

# Create database
mysql -u root -p
CREATE DATABASE student_portal;
EXIT;
```

### **2. Backend Setup (2 minutes)**
```bash
cd backend
python -m venv .venv
.venv\Scripts\activate  # Windows
# source .venv/bin/activate  # macOS/Linux
pip install -r requirements.txt
python migrate_to_mysql.py
python manage.py runserver 0.0.0.0:8000
```

### **3. Frontend Setup (1 minute)**
```bash
# Open Android Studio
# File → Open → Select 'android' folder
# Update NetworkModule.kt with your PC's IP
# Build and Run
```

## ✅ Quick Verification

### **Backend Test**
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student1", "password": "password123"}'
```
**Expected**: JSON with access/refresh tokens

### **Frontend Test**
1. Launch Android app
2. Login with: `student1` / `password123`
3. Navigate through all screens
4. Check Android logs: `adb logcat | grep OkHttp`

## 🔧 Common Fixes

| Issue | Solution |
|-------|----------|
| **Connection refused** | Check MySQL is running |
| **401 Unauthorized** | Verify sample data created |
| **CORS errors** | Check Django CORS settings |
| **App won't connect** | Update base URL in NetworkModule.kt |
| **Build fails** | Clean and rebuild project |

## 📱 Sample Credentials
- **student1** / **password123** (John Doe)
- **student2** / **password123** (Jane Smith)
- **admin** / **admin123** (Admin User)

## 🌐 Network URLs
- **Emulator**: `http://10.0.2.2:8000/api/`
- **Physical Device**: `http://YOUR_PC_IP:8000/api/`
- **Localhost**: `http://127.0.0.1:8000/api/`

---
**Need help? Check the full [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md)**
