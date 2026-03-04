import requests
import json
import uuid

BASE_URL = "http://localhost:8000/api/"

def get_token(username, password):
    response = requests.post(f"{BASE_URL}auth/login", json={"username": username, "password": password})
    if response.status_code == 200:
        return response.json()["access"]
    return None

def test_health_system():
    # 1. Login as Admin
    admin_token = get_token("admin@kcau.ac.ke", "admin123")
    if not admin_token:
        # Try another admin
        admin_token = get_token("admin@example.com", "admin123")
    
    if not admin_token:
        print("Admin login failed")
        return

    headers = {"Authorization": f"Bearer {admin_token}"}

    # 2. Test Admin Health Stats
    print("\nTesting Admin Health Stats...")
    resp = requests.get(f"{BASE_URL}admin/health/stats", headers=headers)
    print(f"Status: {resp.status_code}")
    if resp.status_code == 200:
        stats = resp.json()
        print(f"Active Alerts: {stats.get('active_alerts_count')}")
        print(f"Pending Appointments: {stats.get('pending_appointments_count')}")
    else:
        print(f"Error: {resp.text}")

    # 3. Test Health Tips Endpoint
    # Wait, the URL in the implementation was support/campus-life/health_tips/
    # Router registers support/campus-life which means support/campus-life/health_tips/
    print("\nTesting Health Tips...")
    resp = requests.get(f"{BASE_URL}support/campus-life/health_tips/", headers=headers)
    print(f"Status: {resp.status_code}")
    if resp.status_code == 200:
        tips = resp.json()
        print(f"Fetched {len(tips)} tips")
    else:
        print(f"Error: {resp.text}")

    # 4. Login as Student
    student_token = get_token("student1@kcau.ac.ke", "password123")
    if not student_token:
        student_token = get_token("student1@kcau.ac.ke", "admin123")
        
    if not student_token:
        print("Student login failed")
        return
    
    student_headers = {"Authorization": f"Bearer {student_token}"}

    # 5. Test Appointment Booking
    print("\nTesting Appointment Booking...")
    appointment_data = {
        "appointment_type": "THERAPY",
        "reason": "Stress management session",
        "appointment_date": "2026-03-05T10:00:00Z"
    }
    resp = requests.post(f"{BASE_URL}support/appointments/", json=appointment_data, headers=student_headers)
    print(f"Status: {resp.status_code}")
    appointment_id = None
    if resp.status_code == 201:
        print("Appointment booked successfully")
        appointment_id = resp.json()['id']
    else:
        print(f"Error: {resp.text}")

    # 6. Test Emergency Alert
    print("\nTesting Emergency Alert...")
    alert_data = {
        "latitude": -1.28333,
        "longitude": 36.81667,
        "message": "Emergency near hostel A"
    }
    resp = requests.post(f"{BASE_URL}support/emergency/", json=alert_data, headers=student_headers)
    print(f"Status: {resp.status_code}")
    alert_id = None
    if resp.status_code == 201:
        print("Emergency alert sent successfully")
        alert_id = resp.json()['id']
    else:
        print(f"Error: {resp.text}")

    # 7. Test Admin Confirmation/Resolution
    if appointment_id:
        print(f"\nConfirming Appointment {appointment_id}...")
        resp = requests.post(f"{BASE_URL}support/appointments/{appointment_id}/confirm/", json={"admin_notes": "See you soon"}, headers=headers)
        print(f"Status: {resp.status_code}")
    
    if alert_id:
        print(f"\nResolving Alert {alert_id}...")
        resp = requests.post(f"{BASE_URL}support/emergency/{alert_id}/resolve/", headers=headers)
        print(f"Status: {resp.status_code}")

if __name__ == "__main__":
    test_health_system()
