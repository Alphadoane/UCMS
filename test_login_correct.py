import requests
import json

# Test login endpoint without trailing slash
url = "http://10.54.63.91:8000/api/auth/login"
data = {
    "username": "student1",
    "password": "password123"
}

print(f"Testing login with username: {data['username']}")
print(f"Sending POST request to: {url}")
print(f"Request data: {json.dumps(data, indent=2)}")

try:
    # First, try with Content-Type: application/json
    headers = {"Content-Type": "application/json"}
    print("\nTrying with Content-Type: application/json")
    response = requests.post(url, json=data, headers=headers)
    print(f"Response status code: {response.status_code}")
    print("Response body:")
    try:
        print(json.dumps(response.json(), indent=2))
    except ValueError:
        print(response.text)
    
    # Then try with form data
    print("\nTrying with form data")
    response = requests.post(url, data=data)
    print(f"Response status code: {response.status_code}")
    print("Response body:")
    try:
        print(json.dumps(response.json(), indent=2))
    except ValueError:
        print(response.text)

except requests.exceptions.RequestException as e:
    print(f"\nRequest failed: {e}")
    if hasattr(e, 'response') and e.response is not None:
        print(f"Response status: {e.response.status_code}")
        print(f"Response text: {e.response.text}")
