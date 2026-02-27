# 🔗 Database Integration & API Connection Guide

This guide provides step-by-step instructions for integrating the MySQL database and ensuring successful API communication between the Django backend and Android frontend.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Backend Configuration](#backend-configuration)
4. [API Testing](#api-testing)
5. [Frontend Integration](#frontend-integration)
6. [End-to-End Testing](#end-to-end-testing)
7. [Troubleshooting](#troubleshooting)
8. [Verification Checklist](#verification-checklist)

---

## 🛠️ Prerequisites

### **Required Software**
- **MySQL Server 8.0+** - Database server
- **Python 3.10+** - Backend runtime
- **Android Studio Giraffe+** - Frontend development
- **JDK 11+** - Java development kit
- **Git** - Version control

### **System Requirements**
- **RAM**: 8GB+ recommended
- **Storage**: 5GB+ free space
- **OS**: Windows 10+, macOS 10.15+, or Ubuntu 18.04+

---

## 🗄️ Database Setup

### **Step 1: Install MySQL Server**

#### **Windows**
```bash
# Using Chocolatey
choco install mysql

# OR download from: https://dev.mysql.com/downloads/mysql/
# Install MySQL Installer for Windows
```

#### **macOS**
```bash
# Using Homebrew
brew install mysql

# Start MySQL service
brew services start mysql
```

#### **Ubuntu/Debian**
```bash
# Update package list
sudo apt update

# Install MySQL
sudo apt install mysql-server

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql
```

### **Step 2: Configure MySQL**

#### **Secure Installation**
```bash
# Run security script
sudo mysql_secure_installation

# Follow prompts:
# - Set root password
# - Remove anonymous users
# - Disable root login remotely
# - Remove test database
# - Reload privilege tables
```

#### **Create Database and User**
```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE student_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create dedicated user (optional but recommended)
CREATE USER 'student_portal_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON student_portal.* TO 'student_portal_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;
```

### **Step 3: Test Database Connection**

```bash
# Test connection
mysql -u root -p -e "SELECT VERSION();"

# Test database access
mysql -u root -p -e "USE student_portal; SHOW TABLES;"
```

---

## ⚙️ Backend Configuration

### **Step 1: Setup Python Environment**

```bash
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv .venv

# Activate virtual environment
# Windows:
.venv\Scripts\activate
# macOS/Linux:
source .venv/bin/activate

# Verify Python version
python --version
```

### **Step 2: Install Dependencies**

```bash
# Install required packages
pip install -r requirements.txt

# Verify installation
pip list | grep -E "(Django|mysql|djangorestframework)"
```

### **Step 3: Configure Database Settings**

#### **Update Database Configuration**
Edit `student_api/settings.py`:

```python
# MySQL configuration
MYSQL_DATABASE_NAME = "student_portal"
MYSQL_USER = "root"  # or "student_portal_user"
MYSQL_PASSWORD = "your_mysql_password"
MYSQL_HOST = "localhost"
MYSQL_PORT = "3306"

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.mysql",
        "NAME": MYSQL_DATABASE_NAME,
        "USER": MYSQL_USER,
        "PASSWORD": MYSQL_PASSWORD,
        "HOST": MYSQL_HOST,
        "PORT": MYSQL_PORT,
        "OPTIONS": {
            "init_command": "SET sql_mode='STRICT_TRANS_TABLES'",
            "charset": "utf8mb4",
        },
    }
}
```

### **Step 4: Run Database Migrations**

```bash
# Test database connection
python manage.py check --database default

# Create migrations
python manage.py makemigrations

# Apply migrations
python manage.py migrate

# Verify tables created
python manage.py dbshell
# In MySQL shell:
# USE student_portal;
# SHOW TABLES;
# EXIT;
```

### **Step 5: Create Sample Data**

```bash
# Create sample users and data
python manage.py create_sample_data

# Verify data creation
python manage.py shell
# In Django shell:
# from django.contrib.auth.models import User
# print(User.objects.count())
# exit()
```

### **Step 6: Start Backend Server**

```bash
# Start development server
python manage.py runserver 0.0.0.0:8000

# Server should start successfully
# Output: Starting development server at http://0.0.0.0:8000/
```

---

## 🧪 API Testing

### **Step 1: Test Server Health**

```bash
# Test server response
curl http://localhost:8000/api/

# Expected: JSON response or 404 (normal for root API path)
```

### **Step 2: Test Authentication Endpoint**

```bash
# Test login endpoint
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student1", "password": "password123"}'

# Expected response:
# {
#   "access": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
#   "refresh": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
# }
```

### **Step 3: Test Protected Endpoints**

```bash
# Get access token from previous response
ACCESS_TOKEN="your_access_token_here"

# Test profile endpoint
curl -X GET http://localhost:8000/api/profile/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Expected response:
# {
#   "id": 1,
#   "admission_no": "22/04168",
#   "full_name": "John Doe",
#   "email": "student1@kca.ac.ke",
#   ...
# }
```

### **Step 4: Test Academic Endpoints**

```bash
# Test course registration
curl -X GET http://localhost:8000/api/academics/course-registration \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Test voting system
curl -X GET http://localhost:8000/api/voting/elections \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### **Step 5: Test CORS Configuration**

```bash
# Test CORS headers
curl -I -X OPTIONS http://localhost:8000/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST"

# Expected headers:
# Access-Control-Allow-Origin: *
# Access-Control-Allow-Methods: GET, POST, OPTIONS
# Access-Control-Allow-Headers: Content-Type, Authorization
```

---

## 📱 Frontend Integration

### **Step 1: Configure Network Settings**

#### **Update Base URLs**
Edit `app/src/main/java/com/example/android/data/network/NetworkModule.kt`:

```kotlin
private val BASE_URLS: List<String> = listOf(
    "http://10.0.2.2:8000/api/",        // Android emulator
    "http://192.168.1.100:8000/api/",   // Your PC's IP address
    "http://127.0.0.1:8000/api/"        // Localhost (for testing)
)
```

#### **Find Your PC's IP Address**
```bash
# Windows
ipconfig | findstr "IPv4"

# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### **Step 2: Test Network Configuration**

#### **Update NetworkModule.kt**
```kotlin
// Add debug logging
private val loggingInterceptor = HttpLoggingInterceptor { message ->
    Timber.tag("OkHttp").d(message)
}.apply {
    level = HttpLoggingInterceptor.Level.BODY  // Enable full logging
}
```

### **Step 3: Build and Test Android App**

```bash
# In Android Studio
# 1. Clean Project: Build → Clean Project
# 2. Rebuild Project: Build → Rebuild Project
# 3. Run on Emulator: Run → Run 'app'
```

### **Step 4: Monitor API Calls**

#### **Enable Logging**
In `MainActivity.kt`, ensure Timber is initialized:

```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

#### **Check Android Logs**
```bash
# Monitor Android logs
adb logcat | grep -E "(OkHttp|StudentPortal|AuthRepository)"

# Look for successful API calls:
# D/OkHttp: --> POST http://10.0.2.2:8000/api/auth/login
# D/OkHttp: <-- 200 OK (1234ms)
```

---

## 🔄 End-to-End Testing

### **Step 1: Complete Login Flow**

1. **Launch Android App**
2. **Enter Credentials**:
   - Username: `student1`
   - Password: `password123`
3. **Tap Sign In**
4. **Verify Success**: Should navigate to main screen with drawer

### **Step 2: Test All Major Features**

#### **Profile Screen**
- Navigate to Profile
- Verify user information displays correctly
- Test logout functionality

#### **Academic Features**
- Navigate to Academics
- Test each sub-section:
  - Course Registration
  - Course Work
  - Exam Card
  - Exam Audit
  - Exam Result
  - Academic Leave
  - Clearance

#### **Finance Features**
- Navigate to Finance
- Test each sub-section:
  - Fee Payment
  - View Balance
  - Receipts

#### **Virtual Campus**
- Navigate to Virtual Campus
- Test Zoom Rooms functionality
- Verify attendance marking

#### **Voting System**
- Navigate to Voting System
- Create a new election
- Add candidates
- Cast a vote
- View results

### **Step 3: Test Offline Functionality**

1. **Disconnect Network**
2. **Verify App Behavior**:
   - Login should fail gracefully
   - Cached data should display
   - Error messages should be user-friendly

3. **Reconnect Network**
4. **Verify Sync**:
   - App should reconnect automatically
   - Data should refresh

---

## 🔧 Troubleshooting

### **Common Database Issues**

#### **Connection Refused**
```bash
# Check MySQL status
# Windows:
net start MySQL80
# macOS:
brew services start mysql
# Ubuntu:
sudo systemctl start mysql

# Test connection
mysql -u root -p -e "SELECT 1;"
```

#### **Authentication Failed**
```bash
# Reset MySQL root password
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

#### **Database Not Found**
```sql
-- Recreate database
DROP DATABASE IF EXISTS student_portal;
CREATE DATABASE student_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### **Common Backend Issues**

#### **Migration Errors**
```bash
# Reset migrations
rm -rf */migrations/0*.py
python manage.py makemigrations
python manage.py migrate
```

#### **Import Errors**
```bash
# Reinstall dependencies
pip uninstall -r requirements.txt -y
pip install -r requirements.txt
```

#### **Port Already in Use**
```bash
# Find process using port 8000
# Windows:
netstat -ano | findstr :8000
# macOS/Linux:
lsof -i :8000

# Kill process or use different port
python manage.py runserver 0.0.0.0:8001
```

### **Common Frontend Issues**

#### **Network Connection Failed**
- Check base URL configuration
- Verify backend server is running
- Test with curl commands
- Check firewall settings

#### **CORS Errors**
- Verify CORS settings in Django
- Check allowed origins
- Test with browser developer tools

#### **Authentication Issues**
- Check JWT token format
- Verify token expiration
- Clear app data and retry

### **Debug Commands**

```bash
# Backend debugging
python manage.py check --deploy
python manage.py runserver --verbosity=2

# Database debugging
python manage.py dbshell
python manage.py shell

# Frontend debugging
adb logcat | grep -E "(OkHttp|StudentPortal)"
```

---

## ✅ Verification Checklist

### **Database Integration**
- [ ] MySQL server running
- [ ] Database `student_portal` created
- [ ] Django migrations applied successfully
- [ ] Sample data created
- [ ] Database connection tested

### **Backend API**
- [ ] Django server starts without errors
- [ ] All API endpoints responding
- [ ] Authentication working
- [ ] CORS configured correctly
- [ ] Sample data accessible via API

### **Frontend Integration**
- [ ] Android app builds successfully
- [ ] Network configuration correct
- [ ] Login functionality working
- [ ] All screens loading data
- [ ] Error handling working

### **End-to-End Testing**
- [ ] Complete user journey works
- [ ] All features functional
- [ ] Offline behavior correct
- [ ] Performance acceptable
- [ ] No critical errors

### **Production Readiness**
- [ ] Environment variables configured
- [ ] Security settings applied
- [ ] Logging configured
- [ ] Backup strategy in place
- [ ] Monitoring setup

---

## 🎯 Success Indicators

### **Backend Success**
```bash
# Server starts successfully
python manage.py runserver 0.0.0.0:8000
# Output: Starting development server at http://0.0.0.0:8000/

# API responds correctly
curl http://localhost:8000/api/auth/login
# Output: {"detail": "Method not allowed"} (expected for GET)

# Database accessible
python manage.py shell
# >>> from django.contrib.auth.models import User
# >>> User.objects.count()
# Output: 3 (or number of sample users)
```

### **Frontend Success**
- Android app launches without crashes
- Login screen displays correctly
- Successful login navigates to main screen
- All drawer items accessible
- Data loads in each screen
- No network errors in logs

### **Integration Success**
- API calls show in backend logs
- Data flows from database to frontend
- Real-time updates work (voting system)
- Offline/online transitions smooth
- Error states handled gracefully

---

## 📞 Support

If you encounter issues not covered in this guide:

1. **Check Logs**: Review both backend and frontend logs
2. **Verify Configuration**: Ensure all settings are correct
3. **Test Components**: Isolate and test each component
4. **Check Network**: Verify connectivity and firewall settings
5. **Update Dependencies**: Ensure all packages are up to date

**Remember**: The key to successful integration is systematic testing and verification at each step!

---

**Happy Coding! 🚀**
