import requests

BASE_URL = "http://localhost:8000/"

def get_token():
    resp = requests.post(BASE_URL + "api/auth/jwt/login", json={"email": "lecturer@kcau.ac.ke", "password": "admin123"})
    return resp.json().get("access")

def test_get_students(token, course_id):
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.get(BASE_URL + f"api/staff/courses/{course_id}/students", headers=headers)
    print(f"Status: {resp.status_code}")
    print(f"Response: {resp.text}")

if __name__ == "__main__":
    token = get_token()
    if token:
        test_get_students(token, 2)
    else:
        print("Failed to get token")
