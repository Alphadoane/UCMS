# Student Portal (Android + Django REST + PostgreSQL)

A comprehensive student portal application built with Kotlin and Jetpack Compose, backed by a Django REST Framework API with a PostgreSQL database. Features offline storage via Room and DataStore, real-time voting system, video conferencing, and complete academic management.

## ✨ Features

### 📱 **Android App**
- **Modern UI**: Jetpack Compose with Material 3 design
- **Authentication**: Secure JWT-based login system
- **Navigation**: Side drawer with intuitive navigation
- **Offline Support**: Room database + DataStore for offline functionality
- **Auto-Email Generation**: Student emails are automatically generated based on the configured domain (`@kcau.ac.ke`) upon enrollment.


### 🎓 **Academic Management**
- **Course Registration**: Enroll in courses and track status
- **Course Work**: Assignment tracking and grade management
- **Exam System**: Exam cards (blocked if fee balance > 5000), audit trails, and result tracking

- **Academic Leave**: Request and track leave applications
- **Clearance**: Department clearance status tracking

### 💰 **Financial Management**
- **Fee Payment**: Multiple payment methods and tracking
- **Balance View**: Real-time account balance
- **Receipts**: Digital receipt management

### 🏛️ **Virtual Campus**
- **Dashboard**: Personalized student dashboard
- **My Courses**: Course management and tracking
- **Lectures**: Lecture scheduling and materials
- **Video Conferencing**: Integrated Google Meet for online classes
- **Attendance**: Digital attendance tracking

### 🗳️ **Voting System**
- **Elections**: Create and manage student elections
- **Candidates**: Add and manage election candidates
- **Voting**: Secure one-vote-per-user system
- **Results**: Real-time election results and analytics

### 🛡️ **Admin Portal**
- **Dashboard**: System-wide analytics and quick stats
- **User Management**: Manage Students, Staff, and Administrators
- **Course Management**: Create/Edit courses, semesters, and academics
- **Finance**: Monitor all fee payments and financial records
- **Zoom Management**: Create and manage virtual classrooms
- **Audit Logs**: Track system usage and sensitive actions
- **Broadcast**: Send system-wide announcements

### 👨‍🏫 **Staff Portal**
- **Course Management**: Manage assigned courses and content
- **Content Upload**: Upload lecture notes, videos, and resources
- **Grading**: Grade student assignments and exams
- **Student Lookup**: View student profiles and performance
- **Teaching Schedule**: View upcoming classes and exams

### 🎧 **Support Portal**
- **Ticket Management**: View and resolve support tickets
- **User Diagnostics**: Lookup user status and troubleshoot issues
- **System Health**: Monitor basic system parameters

### 📚 **Library**
- **Resource Access**: Library materials and resources
- **Digital Services**: Online library services

## 🛠️ Tech Stack

### **Frontend (Android)**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **Database**: Room (offline storage) + StudentRepository (PostgreSQL Backend)
- **Preferences**: DataStore
- **Networking**: Retrofit + OkHttp + Kotlinx Serialization
- **Video/Media**: 
  - **Agora RTC SDK** (In-app video conferencing)
  - **CameraX** (Camera integration)
  - **Google Meet** (Intent-based fallback)
- **Background Tasks**: WorkManager

### **Backend (Django)**
- **Language**: Python 3.10+
- **Framework**: Django 5.1.1 + Django REST Framework
- **Database**: PostgreSQL 13+
- **Authentication**: JWT (SimpleJWT)
- **CORS**: django-cors-headers
- **Password Hashing**: Argon2

## 📁 Project Structure
```
.                       # Project Root
├── app/                    # Android App (Kotlin + Compose)
│   ├── src/main/java/com/example/android/
│   │   ├── data/          # Data layer (network, local, repository)
│   │   ├── ui/screens/    # Compose UI screens
│   │   ├── navigation/    # Navigation setup
│   │   └── MainActivity.kt
│   └── build.gradle.kts
├── backend/               # Django REST API
│   ├── student_api/       # Django Project Configuration
│   │   ├── core/         # Core Models & Views
│   │   ├── settings.py   # App settings (PostgreSQL config)
│   │   └── urls.py
│   ├── admission/         # Admission App
│   ├── manage.py
│   └── requirements.txt
└── docs/                  # Documentation
```

## 📋 Prerequisites

### **Development Environment**
- **Android Studio**: Giraffe+ (2023.1.1+)
- **JDK**: 11 or higher
- **Python**: 3.10+ with pip
- **PostgreSQL**: 13+ running on `localhost:5432`

### **System Requirements**
- **RAM**: 8GB+ recommended
- **Storage**: 5GB+ free space
- **OS**: Windows 10+, macOS 10.15+, or Ubuntu 18.04+

## 🚀 Quick Start Guide

### **1. Android App Setup**

#### **Step 1: Open in Android Studio**
```bash
# Clone the repository
git clone <repository-url>
cd android

# Open in Android Studio
# File → Open → Select the 'android' folder
```

#### **Step 2: Configure Network**
Update the base URL in `app/src/main/java/com/example/android/data/network/NetworkModule.kt`:

```kotlin
private val BASE_URLS: List<String> = listOf(
    "http://10.0.2.2:8000/api/",        // Android emulator
    "http://<your-pc-ip>:8000/api/",    // Physical device
    "http://127.0.0.1:8000/api/"        // Localhost
)
```

#### **Step 3: Build and Run**
- **Target SDK**: 35 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Build**: Gradle will automatically download dependencies
- **Run**: Deploy to emulator or physical device

### **2. Backend Setup**

#### **Step 1: Setup Database**
Ensure PostgreSQL is running and create the database:
```sql
CREATE DATABASE student_portal_db;
```
*Note: Update credentials in `backend/student_api/settings.py` if different from `postgres`/`Doane40640666`.*

#### **Step 2: Setup Python Environment**
```bash
cd backend

# Create virtual environment
python -m venv .venv

# Activate virtual environment
# Windows:
.venv\Scripts\activate
# macOS/Linux:
source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

#### **Step 3: Run Migration**
```bash
python manage.py makemigrations
python manage.py migrate
```

#### **Step 4: Start Server**
```bash
python manage.py runserver 0.0.0.0:8000
```

### **3. Test the Application**

1.  **Start Backend**: Server running on `http://localhost:8000`
2.  **Launch Android App**: Deploy to emulator/device
3.  **Login**: Use credentials from your database (or create a superuser via `python manage.py createsuperuser`)
4.  **Explore Features**: Navigate through all sections using the side drawer

## 🐳 Docker Setup

### **Prerequisites**
- **Docker**: Installed and running

### **Run with Docker Compose**
1. **Build and Start**:
   ```bash
   docker-compose up --build
   ```
2. **Apply Migrations** (First run only):
   ```bash
   docker-compose exec backend python manage.py migrate
   ```
3. **Create Superuser** (Optional):
   ```bash
   docker-compose exec backend python manage.py createsuperuser
   ```
4. **Services**:
   - Backend: `http://localhost:8000`
   - Database: `postgres:15` (internal)

### **Automation Scripts (Windows)**
- **Start Backend**: Double-click `start_docker.bat` to build, start, and migrate automatically.
- **Stop Backend**: Double-click `stop_docker.bat` to shutdown services.
- **Start Desktop App**: Double-click `start_desktop.bat` (Sets JAVA_HOME and runs).


### **Stop Services**
```bash
docker-compose down
```

## 🔧 Backend API Documentation

### **API Base URL**
```
http://localhost:8000/api/
```

### **Authentication Endpoints**
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### **Profile Endpoints**
- `GET /api/profile/me` - Get user profile

### **Academic Endpoints**
- `GET /api/academics/course-registration` - Course enrollment
- `GET /api/academics/course-work` - Assignments and grades
- `GET /api/academics/exam-card` - Exam information
- `GET /api/academics/exam-audit` - Exam audit records
- `GET /api/academics/exam-result` - Exam results
- `GET /api/academics/academic-leave` - Leave applications
- `GET /api/academics/clearance` - Clearance status

### **Finance Endpoints**
- `GET /api/finance/fee-payment` - Payment methods
- `GET /api/finance/view-balance` - Account balance
- `GET /api/finance/receipts` - Payment receipts

### **Virtual Campus Endpoints**
- `GET /api/virtual/dashboard` - Student dashboard
- `GET /api/virtual/my-courses` - Enrolled courses
- `GET /api/virtual/lectures` - Lecture schedule
- `GET /api/virtual/zoom-rooms` - Video conference rooms
- `POST /api/virtual/zoom-rooms/{id}/attendance/mark` - Mark attendance
- `GET /api/virtual/zoom-rooms/{id}/attendance/summary` - Attendance summary

### **Voting System Endpoints**
- `GET /api/voting/elections` - List elections
- `POST /api/voting/elections` - Create election
- `GET /api/voting/elections/{id}` - Get election details
- `POST /api/voting/elections/{id}/candidates` - Add candidate
- `POST /api/voting/elections/{id}/vote` - Cast vote
- `GET /api/voting/elections/{id}/results` - Get results

### **Library Endpoints**
- `GET /api/library` - Library resources

## 🔄 Offline Strategy
- **Authentication**: JWT tokens stored in DataStore
- **Profile Data**: Cached in Room database for offline access
- **Course Data**: Local storage for offline viewing
- **Sync**: Automatic sync when network is available

## 🛠️ Build Configuration

### **Android Build**
- **Gradle**: AGP 8.9.1
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00
- **Target SDK**: 35 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Java Target**: 11

### Optional: Zoom SDK integration
To enable native join (camera/mic, in-app UI similar to Meet) using the Zoom SDK:
1. Obtain Zoom SDK credentials (SDK Key/Secret) and set gradle properties (do not commit secrets):
   - In `~/.gradle/gradle.properties` or project `gradle.properties`:
     - `ZOOM_SDK_KEY=xxxx`
     - `ZOOM_SDK_SECRET=yyyy`
     - `ZOOM_DOMAIN=zoom.us`
2. The app exposes these via `BuildConfig.ZOOM_SDK_KEY`, `BuildConfig.ZOOM_SDK_SECRET`, `BuildConfig.ZOOM_DOMAIN`.
3. Dependency coordinates (to add when enabling SDK):
   - Add Zoom SDK repo and dependency per Zoom docs (Android Meeting SDK). Example (check latest in Zoom docs):
     - Maven repo: `maven { url 'https://maven.zoom.us/artifactory/zoom-repo/' }`
     - Dependency: `implementation 'us.zoom.sdk:mzoom-sdk-android:<version>'`
4. Implement native join using provided SDK key/secret and `ZoomClient` (see `util/ZoomClient.kt`). The current implementation falls back to opening the join URL.

### Google Meet (default in-app video)
- No SDK or keys required. Uses Intent-based integration.
- Opens Google Meet links in the Google Meet app if installed, otherwise falls back to browser.
- Permissions: CAMERA and RECORD_AUDIO are requested at runtime.
- Meeting URLs should be in format: `https://meet.google.com/[meeting-code]` or stored in backend `join_url` field.

## 🧪 Testing Guide

### **End-to-End Test**
1. **Start Backend**: `python manage.py runserver 0.0.0.0:8000`
2. **Configure Android**: Update base URL in `NetworkModule.kt`
3. **Test Login**: Use valid PostgreSQL user credentials.
4. **Test Features**:
   - Navigate through all screens
   - Test voting system (create election, add candidates, vote)
   - Check academic data display
   - Verify virtual campus features

## 🔧 Troubleshooting

### **Common Issues**

#### **Connection Issues**
- **Emulator**: Use `http://10.0.2.2:8000/api/` as base URL
- **Physical Device**: Use your PC's IP address
- **CORS Errors**: Ensure CORS is enabled in Django settings

#### **Authentication Issues**
- **401 Unauthorized**: Check if user exists in PostgreSQL
- **Login Fails**: Verify database connection and user exists
- **Token Issues**: Clear app data and login again

#### **Database Issues**
- **Connection Failed**: Check PostgreSQL credentials in `student_api/settings.py`
- **Migration Errors**: Run `python manage.py migrate` manually

#### **Android Build Issues**
- **Gradle Sync**: Clean and rebuild project
- **Dependencies**: Check internet connection for downloads
- **SDK Issues**: Ensure correct Android SDK is installed

### **Debug Commands**
```bash
# Check Django setup
python manage.py check

# View Django logs
python manage.py runserver --verbosity=2

# Check Android logs
adb logcat | grep "StudentPortal"
```

## 🚀 Future Enhancements

### **Planned Features**
- **Push Notifications**: Real-time updates for grades, assignments, announcements
- **File Upload**: Assignment submission and document management
- **Advanced Analytics**: Student performance insights and predictions
- **Multi-language Support**: Internationalization for diverse student body
- **Dark Mode**: Enhanced UI with theme switching
- **Offline Sync**: Advanced offline capabilities with conflict resolution

### **Technical Improvements**
- **API Optimization**: Caching and performance improvements
- **Security Enhancements**: Advanced authentication and data encryption
- **Mobile Optimization**: Better performance on low-end devices
- **Testing Coverage**: Comprehensive unit and integration tests
- **CI/CD Pipeline**: Automated testing and deployment

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support and questions:
- **Issues**: Create an issue on GitHub
- **Documentation**: Check the `/docs` folder
- **Email**: [alphadoane@gmail.com](mailto:alphadoane@gmail.com)

---

**Built with ❤️ for students, by alphadoane**
