import requests
import json

BASE_URL = "http://localhost:8000/"

def get_token():
    # Assuming lecturer@kcau.ac.ke / admin123
    resp = requests.post(BASE_URL + "api/auth/jwt/login", json={"email": "lecturer@kcau.ac.ke", "password": "admin123"})
    if resp.status_code != 200:
        print(f"Login failed: {resp.status_code}")
        print(resp.text)
        return None
    return resp.json().get("access")

def test_post_material(token, course_id):
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    data = {
        "title": "Introduction to Python",
        "description": "Basic concepts of Python programming",
        "link": "https://python.org"
    }
    resp = requests.post(BASE_URL + f"api/staff/courses/{course_id}/materials", headers=headers, json=data)
    print(f"Post Material Status: {resp.status_code}")
    print(resp.json())

def test_post_work(token, course_id):
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    data = {
        "title": "Assignment 1",
        "description": "Variables and Data Types",
        "max_marks": 20,
        "due_date": "2026-03-15",
        "category": "assignment"
    }
    resp = requests.post(BASE_URL + f"api/staff/courses/{course_id}/work", headers=headers, json=data)
    print(f"Post Work Status: {resp.status_code}")
    print(resp.json())

if __name__ == "__main__":
    token = get_token()
    if token:
        # Using course_id = 2 (SE401)
        test_post_material(token, 2)
        test_post_work(token, 2)
    else:
        print("Failed to get token")
