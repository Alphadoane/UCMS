import requests

BASE_URL = "http://localhost:8000/api/"

def get_token(username, password):
    resp = requests.post(BASE_URL + "auth/login", json={"username": username, "password": password})
    if resp.status_code == 200:
        return resp.json()["access"]
    return None

def test_enrolled_courses(token):
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.get(BASE_URL + "academics/courses/enrolled", headers=headers)
    print(f"Status Code: {resp.status_code}")
    if resp.status_code == 200:
        courses = resp.json()
        print(f"Enrolled Courses Count: {len(courses)}")
        for c in courses:
            print(f"- {c['code']}: {c['name']} ({c['credit_units']} units)")
    else:
        print(f"Error: {resp.text}")

if __name__ == "__main__":
    # Updated to used student1@kcau.ac.ke
    token = get_token("student1@kcau.ac.ke", "password123")
    if token:
        print("Login successful.")
        test_enrolled_courses(token)
    else:
        print("Login failed. Check server and credentials.")
