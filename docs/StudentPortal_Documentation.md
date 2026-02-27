## Student Portal — Android + Django REST + MongoDB

Version 1.0

### 1. Overview
This document provides comprehensive technical and product documentation for the Student Portal project. The system comprises an Android mobile application built with Kotlin and Jetpack Compose, and a backend powered by Django REST Framework with MongoDB for domain data and SQLite for Django internals. The app supports authentication, profile, academics, finance, library, virtual campus (Google Meet/Zoom), and a full voting system.

### 2. Architecture
- **Android App**: Kotlin, Jetpack Compose, Navigation-Compose, Room, DataStore, Retrofit/OkHttp, kotlinx.serialization, Timber.
- **Backend**: Django 5, Django REST Framework, JWT (SimpleJWT), CORS, SQLite (Django metadata), MongoDB (domain data).
- **Data**: MongoDB collections for users, profiles, academics, finance, voting; Room for offline cache; DataStore for auth token.
- **Video**: Google Meet (Intent-based) integrated by default; optional Zoom SDK via gradle properties.

High-level flow:
- Android UI interacts with repositories that call `ApiService` (Retrofit).
- Backend exposes REST endpoints under `/api/` and integrates with MongoDB through a service wrapper.
- Auth tokens are stored in DataStore and attached to requests by an OkHttp interceptor.
- Room caches key entities (e.g., profile) for offline access.

### 3. Repository Structure
```
android/              # Android app root
  app/                # App module (Compose UI, navigation, data layer)
backend/              # Django REST backend (DRF + MongoDB)
docs/                 # Generated documentation (this file, HTML)
```

### 4. Environments & Dependencies
- **Android key versions**: AGP 8.9.1, Kotlin 2.0.21, Compose BOM 2024.09.00, compileSdk 35, minSdk 24.
- **Libraries**: Room 2.6.1, DataStore 1.1.1, Retrofit 2.11.0, OkHttp 4.12.0, kotlinx-serialization 1.7.3, Navigation-Compose 2.8/2.9.
- **Backend**: Django 5.1.1, DRF 3.15.2, django-cors-headers 4.4.0, SimpleJWT 5.3.1, PyMongo 4.6.1, argon2-cffi 23.1.0.

### 5. Setup & Local Development
Prerequisites: Android Studio (Giraffe+), JDK 11, Python 3.10+, MongoDB Community Server on `localhost:27017`.

Backend setup (Windows PowerShell):
1. `python -m venv backend\.venv`
2. `backend\.venv\Scripts\Activate.ps1`
3. `pip install -r backend\requirements.txt`
4. `cd backend`
5. `python manage.py migrate`
6. (Optional) `python manage.py create_sample_data`
7. (Once) `python manage.py create_voting_indexes`
8. `python manage.py runserver 0.0.0.0:8000`

Android app:
1. Ensure backend URL is reachable from device/emulator.
2. In `NetworkModule.kt` base URLs include `http://10.0.2.2:8000/api/`, `http://<your-ip>:8000/api/`, `http://127.0.0.1:8000/api/`.
3. Open project in Android Studio and Run on emulator or device.

### 6. Configuration
- **Android Manifest**: INTERNET, ACCESS_NETWORK_STATE, CAMERA, RECORD_AUDIO; cleartext enabled for local dev via `networkSecurityConfig`.
- **Backend settings**: `DEBUG=True`, permissive CORS for local development, JWT authentication, SQLite for Django, MongoDB for domain collections. Logging configured for console with environment-driven level.

### 7. Android Application
Key layers:
- **UI**: Jetpack Compose screens for Login, Home, Profile, Academics (Course Registration, Course Work, Exam Card, Exam Audit, Exam Result, Academic Leave, Clearance), Finance, Library, Virtual Campus (Zoom Rooms), Voting System.
- **Data**:
  - `ApiService`: typed Retrofit interface with kotlinx.serialization DTOs.
  - `NetworkModule`: OkHttp client with auth and logging interceptors; fallback base URLs.
  - Repositories: `AuthRepository`, `ProfileRepository`, `VotingRepository`.
- **Storage**: Room (Profile cache), DataStore (auth token).

Networking specifics:
- Fallback base URLs: `http://10.0.2.2:8000/api/`, `http://<LAN-IP>:8000/api/`, `http://127.0.0.1:8000/api/`.
- Authorization header automatically injected: `Authorization: Bearer <token>` when present.

Video integrations:
- Google Meet: Intent-based integration, opens in Google Meet app if installed, otherwise browser. No SDK required.
- Optional Zoom SDK: supply `ZOOM_SDK_KEY`, `ZOOM_SDK_SECRET`, `ZOOM_DOMAIN` via gradle properties for native meeting experience.

### 8. Backend API
Base path: `/api/`

Authentication:
- `POST /api/auth/jwt/login` — obtain access/refresh tokens.
- `POST /api/auth/jwt/refresh` — refresh access token.

Custom Auth + Profile:
- `POST /api/auth/login` — MongoDB-backed login, returns JWT via service.
- `POST /api/auth/logout` — placeholder.
- `GET /api/profile/me` — profile document from MongoDB with default bootstrap if absent.

Academics:
- `GET /api/academics/course-registration`
- `GET /api/academics/course-work`
- `GET /api/academics/exam-card`
- `GET /api/academics/exam-audit`
- `GET /api/academics/exam-result`
- `GET /api/academics/academic-leave`
- `GET /api/academics/clearance`
- `GET /api/academics/insights` — illustrative analytics stub.

Finance:
- `GET /api/finance/fee-payment`
- `GET /api/finance/view-balance`
- `GET /api/finance/receipts`

Virtual Campus:
- `GET /api/virtual/dashboard`, `GET /api/virtual/my-courses`, `GET /api/virtual/lectures`
- `GET /api/virtual/zoom-rooms` — list rooms
- `POST /api/virtual/zoom-rooms/{roomId}/attendance/mark`
- `GET /api/virtual/zoom-rooms/{roomId}/attendance/summary`

Voting System:
- `GET /api/voting/elections` — list elections
- `POST /api/voting/elections` — create election `{ title }`
- `GET /api/voting/elections/{id}` — election details
- `POST /api/voting/elections/{id}/candidates` — add candidate `{ name }`
- `POST /api/voting/elections/{id}/vote` — cast vote `{ candidate }` (one per user)
- `GET /api/voting/elections/{id}/results` — aggregated results

### 9. Data Model (MongoDB)
- `voting_elections`: `{ _id, title, candidates: [ { name } ], results: { <candidate>: votes }, created_at, updated_at }`
- `voting_votes`: `{ election_id, user_id, candidate, created_at }` with unique index `(election_id, user_id)`
- `attendance_records`: `{ room_id, course_id, user_id, date, marked_at }`
- User-centric collections (by convention): profiles and module-specific documents under user-scoped namespaces.

### 10. Security & Authentication
- JWT via SimpleJWT; attach `Authorization: Bearer <token>` from DataStore.
- Passwords hashed with Argon2 (preferred), PBKDF2 fallbacks configured.
- CORS configured for local development and common emulator/host addresses.

### 11. Observability & Logging
- Backend: structured console logging with environment-driven level (`LOG_LEVEL`).
- Android: OkHttp network logs bridged to Timber in DEBUG builds; keep sensitive data out of logs.

### 12. Testing
- Android: `app/src/test` and `app/src/androidTest` with sample tests.
- Backend: Django management commands for sample data and indexes; sample test scripts (e.g., `test_login.py`).

### 13. Deployment
- Backend:
  - Containerize with Python 3.10 base; configure env vars for `MONGODB_CONNECTION_STRING`, `SECRET_KEY`, `ALLOWED_HOSTS`, `LOG_LEVEL`.
  - Prefer managed MongoDB; ensure indexes are created on deploy.
  - Use Gunicorn/Uvicorn behind Nginx; enable HTTPS and secure headers.
- Android:
  - Configure environment-aware base URLs (build flavors or runtime config).
  - Sign release builds; enable ProGuard/R8 as needed; review cleartext policies.

### 14. Roadmap
- Wire Android screens to backend APIs for Academics, Finance, Library beyond stubs.
- Implement offline-first flows and error states; background sync for profile/academics.
- Add role-based access control, audit logging, and rate limiting to backend endpoints.
- Add pagination, filtering, and sorting to list endpoints; standardize error schema.
- Optional Zoom SDK feature flag; robust fallback to Google Meet or browser join.
- CI/CD (GitHub Actions): lint, test, build, artifact publishing, release notes.

### 15. Troubleshooting
- Emulator cannot reach backend: use `http://10.0.2.2:8000/api/`.
- 401 on login: ensure MongoDB is running and `create_sample_data` executed.
- 409 on vote: unique index `(election_id, user_id)` prevents duplicate votes.
- CORS errors: verify `CORS_ALLOWED_ORIGINS` and device/emulator host are permitted.

### 16. License
MIT (or update accordingly).

---

Conversion to Word (.docx):
- Option 1: Open this Markdown in Microsoft Word and Save As `.docx`.
- Option 2: Use `pandoc` if available: `pandoc StudentPortal_Documentation.md -o StudentPortal_Documentation.docx`.



