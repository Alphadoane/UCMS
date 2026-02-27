# Backend (Node.js + MySQL)

This directory contains the backend API powering the Android app.

## Render Deployment

**Start Command for Render:**
```
cd backend && node server.js
```

Or use npm script:
```
npm start
```

## Prerequisites
- Node.js 18+
- MySQL running locally (port 3306) - OR use Render's managed MySQL

## Configure
Set environment variables in Render dashboard:
- `DB_HOST` - MySQL host
- `DB_PORT` - MySQL port (usually 3306)
- `DB_USER` - MySQL username
- `DB_PASSWORD` - MySQL password
- `DB_NAME` - Database name
- `PORT` - Server port (Render sets this automatically)

## Initialize database
From a MySQL client, run `backend/setup.sql` to create schema and seed data.

## Local Development

```powershell
cd backend
npm install
$env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_USER="root"; $env:DB_PASSWORD=""; $env:DB_NAME="test"; $env:PORT="3000"; node server.js
```

Test:
```powershell
curl http://localhost:3000/health
```

## Note
This backend is being migrated to Firebase. The Android app now uses Firebase Auth and Firestore for authentication and data storage.
