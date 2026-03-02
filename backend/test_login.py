import requests
import json

url = "https://entreatingly-commonable-georgann.ngrok-free.dev/api/auth/login"
payload = {
    "username": "alpha@gmail.com",
    "password": "password"
}

try:
    response = requests.post(url, json=payload)
    print(f"Status (no slash): {response.status_code}")
except Exception as e:
    print(f"Error (no slash): {e}")

# Also try WITH slash
url_slash = "http://localhost:8000/api/auth/login/"
try:
    response = requests.post(url_slash, json=payload)
    print(f"Status (with slash): {response.status_code}")
except Exception as e:
    print(f"Error (with slash): {e}")
