import requests
import os

BASE_URL = "http://localhost:8000/api/"

def get_token(username, password):
    response = requests.post(f"{BASE_URL}auth/login", json={"username": username, "password": password})
    if response.status_code == 200:
        return response.json()["access"]
    return None

def test_library_upload():
    # 1. Login as Admin
    admin_token = get_token("admin@kcau.ac.ke", "admin123")
    if not admin_token:
        print("Admin login failed")
        return

    headers = {"Authorization": f"Bearer {admin_token}"}

    # 2. Test Book Upload
    print("\nTesting Book Upload...")
    
    # Create a dummy PDF content
    dummy_pdf_content = b"%PDF-1.4\n1 0 obj\n<< /Title (Test Book) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF"
    
    files = {
        "pdf_file": ("test_book.pdf", dummy_pdf_content, "application/pdf")
    }
    data = {
        "title": "Agentic AI Handbook",
        "author": "Antigravity",
        "category": "TECH"
    }
    
    resp = requests.post(f"{BASE_URL}admin/books/", data=data, files=files, headers=headers)
    print(f"Status: {resp.status_code}")
    if resp.status_code == 201:
        print("Book uploaded successfully")
        book_id = resp.json()['id']
        
        # 3. Verify in student list
        print("\nVerifying in Student Book List...")
        # Login as student
        student_token = get_token("student1@kcau.ac.ke", "password123")
        if not student_token:
            student_token = get_token("student1@kcau.ac.ke", "admin123")
            
        student_headers = {"Authorization": f"Bearer {student_token}"}
        
        list_resp = requests.get(f"{BASE_URL}student/books/", headers=student_headers)
        print(f"Status: {list_resp.status_code}")
        if list_resp.status_code == 200:
            books = list_resp.json()
            found = any(b['id'] == book_id for b in books)
            if found:
                print(f"Success: Book {book_id} found in student list")
            else:
                print(f"Failure: Book {book_id} NOT found in student list")
        else:
            print(f"Error fetching books: {list_resp.text}")
            
    else:
        print(f"Upload Error: {resp.text}")

if __name__ == "__main__":
    test_library_upload()
