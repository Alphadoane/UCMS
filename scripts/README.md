# How to Seed Data Manually (Backend Script)

If the Android app button isn't working, you can use this script to inject data directly.

## Prerequisites
1.  **Node.js**: You already have this installed (v24.11.0).
2.  **Service Account Key**: You need to download this from Firebase Console.

## Steps

1.  **Download Service Account Key**:
    *   Go to [Firebase Console](https://console.firebase.google.com/project/android-328ce/settings/serviceaccounts/adminsdk).
    *   Click **"Generate new private key"**.
    *   Save the file as `service-account.json` inside this folder (`android/scripts/`).

2.  **Install Dependencies**:
    Open your terminal in `android/scripts` and run:
    ```bash
    npm install firebase-admin
    ```

3.  **Run Seeder**:
    Run:
    ```bash
    node seed.js
    ```

4.  **Refresh Console**: Check your Firestore database.
