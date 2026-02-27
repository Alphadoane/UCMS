import requests
import sys

def check_timetable():
    base_url = "http://127.0.0.1:8000/api/"
    login_url = base_url + "auth/login"
    timetable_url = base_url + "academics/timetable"
    
    # Login as a lecturer
    credentials = {"email": "peter.pan@kcau.ac.ke", "password": "password123"}
    
    try:
        response = requests.post(login_url, json=credentials)
        if response.status_code != 200:
            print(f"Login failed: {response.status_code}")
            print(response.text)
            return
        
        token = response.json().get("access")
        headers = {"Authorization": f"Bearer {token}"}
        
        response = requests.get(timetable_url, headers=headers)
        print(f"Timetable Response Code: {response.status_code}")
        print("Timetable Response Body:")
        print(response.text)
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check_timetable()
