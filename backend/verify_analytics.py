import requests
import json

BASE_URL = "http://127.0.0.1:8000/api"

def test_reports():
    # 1. Login
    login_url = f"{BASE_URL}/auth/login"
    payload = {
        "username": "admin@kcau.ac.ke",
        "password": "password123"
    }
    
    print(f"Logging in at {login_url}...")
    response = requests.post(login_url, json=payload)
    if response.status_code != 200:
        print(f"Login failed: {response.text}")
        return

    data = response.json()
    token = data.get('access') or data.get('token')
    if not token:
        print(f"No token found in response: {data}")
        return
        
    print("Login successful.")
    
    # 2. Call Reports
    reports_url = f"{BASE_URL}/admin/reports"
    headers = {"Authorization": f"Bearer {token}"}
    
    print(f"Fetching reports from {reports_url}...")
    response = requests.get(reports_url, headers=headers)
    
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
        print("Success! Report Data:")
        print(json.dumps(response.json(), indent=2))
    else:
        print("Failed!")
        print(response.text)

if __name__ == "__main__":
    test_reports()
