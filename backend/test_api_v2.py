import requests
import json

BASE_URL = "http://127.0.0.1:8000/api"

def run_tests():
    # 1. Login
    print("Trying Login...")
    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "username": "student1@kca.ac.ke",
        "password": "password123"
    })
    
    if resp.status_code != 200:
        print(f"Login Failed: {resp.text}")
        return
        
    data = resp.json()
    token = data['access']
    print("Login Success! Token obtained.")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # 2. Profile
    print("\nFetching Profile...")
    resp = requests.get(f"{BASE_URL}/profile/me", headers=headers)
    print(f"Profile ({resp.status_code}): {resp.text}")
    
    # 3. Balance
    print("\nFetching Balance...")
    resp = requests.get(f"{BASE_URL}/finance/view-balance", headers=headers)
    print(f"Balance ({resp.status_code}): {resp.text}")
    
    # 4. Registration
    print("\nFetching Registration...")
    resp = requests.get(f"{BASE_URL}/academics/course-registration", headers=headers)
    print(f"Reg ({resp.status_code}): {resp.text}")

if __name__ == "__main__":
    run_tests()
