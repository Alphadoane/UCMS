import requests
import json

url = "http://127.0.0.1:8000/api/auth/login"
payload = {
    "username": "admin@kca.ac.ke",
    "password": "admin"
}

try:
    response = requests.post(url, json=payload)
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
        print("Login Successful")
        print("Tokens:", response.json().keys())
    else:
        print("Login Failed")
        print(response.text)
except Exception as e:
    print(f"Error: {e}")
