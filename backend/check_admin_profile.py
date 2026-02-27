import requests
import json

# authenticate first
login_url = "http://127.0.0.1:8000/api/auth/login"
payload = {
    "username": "admin@kca.ac.ke",
    "password": "admin"
}

try:
    s = requests.Session()
    response = s.post(login_url, json=payload)
    if response.status_code == 200:
        tokens = response.json()
        access_token = tokens['access']
        
        # get profile
        profile_url = "http://127.0.0.1:8000/api/profile/me"
        headers = {"Authorization": f"Bearer {access_token}"}
        resp = s.get(profile_url, headers=headers)
        print("Profile Response Code:", resp.status_code)
        print("Profile Response:", json.dumps(resp.json(), indent=2))
    else:
        print("Login Failed")
except Exception as e:
    print(f"Error: {e}")
