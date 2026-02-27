import requests
import json

# Test login endpoint
url = "http://10.54.63.91:8000/api/auth/login/"
data = {
    "username": "student1",
    "password": "password123"
}

print(f"Testing login with username: {data['username']}")
print(f"Sending POST request to: {url}")
print(f"Request data: {json.dumps(data, indent=2)}")

try:
    response = requests.post(url, json=data)
    print(f"\nResponse status code: {response.status_code}")
    print("Response headers:")
    for header, value in response.headers.items():
        print(f"  {header}: {value}")
    
    try:
        print("\nResponse body:")
        print(json.dumps(response.json(), indent=2))
    except ValueError:
        print("\nResponse body (raw):")
        print(response.text)

except requests.exceptions.RequestException as e:
    print(f"\nRequest failed: {e}")
    if hasattr(e, 'response') and e.response is not None:
        print(f"Response status: {e.response.status_code}")
        print(f"Response text: {e.response.text}")
