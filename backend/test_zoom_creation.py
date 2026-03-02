import os
import django
import json
from datetime import datetime

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from django.test import RequestFactory
from student_api.core.api.virtual_campus import vc_zoom_rooms
from student_api.core.models import User
from student_api.academics.models import Lecturer, Course

def test_create_zoom():
    factory = RequestFactory()
    user = User.objects.get(email="lecturer@kcau.ac.ke")
    
    # Ensure course exists
    course, _ = Course.objects.get_or_create(code="TEST101", defaults={"name": "Test Course", "program_id": 1, "credit_units": 3})
    
    data = {
        "course_code": "TEST101",
        "title": "Test Zoom Room",
        "start_time": "2026-03-02 15:53" # The format Android sends
    }
    
    request = factory.post("/virtual/zoom-rooms", data=json.dumps(data), content_type="application/json")
    request.user = user
    # Manually set authentication for the function if needed, but usually request.user is enough for function views unless they check request.auth
    
    # We need to bypass the @permission_classes decorator for the test if it's acting up
    # or just use a real APIClient if available.
    from rest_framework.test import APIClient
    client = APIClient()
    client.force_authenticate(user=user)
    response = client.post("/api/virtual/zoom-rooms", data=data, format='json')
    print(f"Status Code: {response.status_code}")
    # Test GET List
    request_get = factory.get("/api/virtual/zoom-rooms")
    client.force_authenticate(user=user)
    response_get = client.get("/api/virtual/zoom-rooms")
    print(f"\nGET Status Code: {response_get.status_code}")
    # print(f"GET Response Data: {response_get.data}")
    if response_get.status_code == 200:
        rooms = response_get.data.get("rooms", [])
        print(f"Found {len(rooms)} rooms.")
        for r in rooms:
            print(f" - {r['id']}: {r['course_code']}: {r['course_title']} (Host: {r['is_host']})")
            
        # Test DELETE
        if rooms:
            last_room_id = rooms[-1]['id']
            print(f"\nTesting DELETE for room ID: {last_room_id}")
            response_delete = client.delete(f"/api/virtual/zoom-rooms/{last_room_id}")
            print(f"DELETE Status Code: {response_delete.status_code}")
            
            # Verify deletion
            response_verify = client.get("/api/virtual/zoom-rooms")
            remaining_rooms = response_verify.data.get("rooms", [])
            if any(str(r['id']) == str(last_room_id) for r in remaining_rooms):
                print("Verification FAILED: Room still exists.")
            else:
                print("Verification PASSED: Room deleted.")

if __name__ == "__main__":
    test_create_zoom()
