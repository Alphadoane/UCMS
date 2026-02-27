#!/usr/bin/env python
"""
Integration Diagnosis Script
Automatically checks database connection, API endpoints, and configuration
"""

import os
import sys
import subprocess
import requests
import json
from pathlib import Path

# Add the project directory to Python path
BASE_DIR = Path(__file__).resolve().parent
sys.path.append(str(BASE_DIR))

def print_header(title):
    """Print a formatted header"""
    print(f"\n{'='*60}")
    print(f"🔍 {title}")
    print(f"{'='*60}")

def print_success(message):
    """Print success message"""
    print(f"✅ {message}")

def print_error(message):
    """Print error message"""
    print(f"❌ {message}")

def print_warning(message):
    """Print warning message"""
    print(f"⚠️  {message}")

def check_python_environment():
    """Check Python environment and dependencies"""
    print_header("Python Environment Check")
    
    try:
        import django
        print_success(f"Django {django.get_version()} installed")
    except ImportError:
        print_error("Django not installed")
        return False
    
    try:
        import psycopg
        print_success("PostgreSQL adapter (psycopg) available")
    except ImportError:
        try:
            import psycopg2
            print_success("PostgreSQL adapter (psycopg2) available")
        except ImportError:
            print_error("PostgreSQL adapter not installed")
            return False
    
    try:
        import rest_framework
        print_success("Django REST Framework available")
    except ImportError:
        print_error("Django REST Framework not installed")
        return False
    
    return True

def check_database_connection():
    """Check PostgreSQL database connection"""
    print_header("Database Connection Check")
    
    try:
        os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'student_api.settings')
        import django
        django.setup()
        
        from django.db import connection
        with connection.cursor() as cursor:
            cursor.execute("SELECT 1")
            result = cursor.fetchone()
            if result:
                print_success("Database connection successful")
                return True
    except Exception as e:
        print_error(f"Database connection failed: {e}")
        return False
    
    return False

def check_database_tables():
    """Check if required database tables exist"""
    print_header("Database Tables Check")
    
    try:
        from django.db import connection
        with connection.cursor() as cursor:
            # PostgreSQL equivalent of SHOW TABLES
            cursor.execute("""
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = 'public'
            """)
            tables = [row[0] for row in cursor.fetchall()]
            
            required_tables = [
                'users',
                'students',
                'course_registrations',
                'elections',
                'votes'
            ]
            
            missing_tables = [table for table in required_tables if table not in tables]
            
            if missing_tables:
                print_error(f"Missing tables: {missing_tables}")
                return False
            else:
                print_success("All required tables exist")
                return True
    except Exception as e:
        print_error(f"Error checking tables: {e}")
        return False

def check_sample_data():
    """Check if sample data exists"""
    print_header("Sample Data Check")
    
    try:
        from student_api.core.models import User
        from student_api.academics.models import Student
        
        user_count = User.objects.count()
        student_count = Student.objects.count()
        
        if user_count == 0:
            print_error("No users found in database")
            return False
        
        if student_count == 0:
            print_warning("No students found (might be okay if only admin exists)")
        
        print_success(f"Found {user_count} users and {student_count} students")
        return True
    except Exception as e:
        print_error(f"Error checking sample data: {e}")
        return False

def check_django_server():
    """Check if Django server is running"""
    print_header("Django Server Check")
    
    try:
        response = requests.get("http://localhost:8000/api/", timeout=5)
        if response.status_code in [200, 404]:  # 404 is expected for root API
            print_success("Django server is running")
            return True
        else:
            print_warning(f"Server responded with status {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print_error("Django server is not running")
        return False
    except Exception as e:
        print_error(f"Error checking server: {e}")
        return False

def test_api_endpoints():
    """Test key API endpoints"""
    print_header("API Endpoints Test")
    
    base_url = "http://localhost:8000/api"
    endpoints = [
        ("/auth/login", "POST"),
        ("/profile/me", "GET"),
        ("/voting/elections", "GET"),
        ("/academics/course-registration", "GET")
    ]
    
    success_count = 0
    
    for endpoint, method in endpoints:
        try:
            if method == "POST" and endpoint == "/auth/login":
                # Test login endpoint
                data = {"username": "student1@kcau.ac.ke", "password": "student123"}
                response = requests.post(f"{base_url}{endpoint}", json=data, timeout=5)
            else:
                # Test GET endpoints (will fail without auth, but should not crash)
                response = requests.get(f"{base_url}{endpoint}", timeout=5)
            
            if response.status_code in [200, 401, 403]:  # 401/403 expected without auth
                print_success(f"{method} {endpoint} - Status {response.status_code}")
                success_count += 1
            else:
                print_warning(f"{method} {endpoint} - Unexpected status {response.status_code}")
        except Exception as e:
            print_error(f"{method} {endpoint} - Error: {e}")
    
    return success_count == len(endpoints)

def test_authentication():
    """Test authentication flow"""
    print_header("Authentication Test")
    
    try:
        # Test login
        login_data = {"username": "student1@kcau.ac.ke", "password": "student123"}
        response = requests.post("http://localhost:8000/api/auth/login", json=login_data, timeout=5)
        
        if response.status_code == 200:
            tokens = response.json()
            access_token = tokens.get('access')
            
            if access_token:
                print_success("Login successful, received access token")
                
                # Test protected endpoint
                headers = {"Authorization": f"Bearer {access_token}"}
                profile_response = requests.get("http://localhost:8000/api/profile/me", headers=headers, timeout=5)
                
                if profile_response.status_code == 200:
                    print_success("Protected endpoint accessible with token")
                    return True
                else:
                    print_error(f"Protected endpoint failed: {profile_response.status_code}")
            else:
                print_error("No access token in response")
        else:
            print_error(f"Login failed: {response.status_code}")
            print(f"Response: {response.text}")
    except Exception as e:
        print_error(f"Authentication test failed: {e}")
    
    return False

def check_cors_configuration():
    """Check CORS configuration"""
    print_header("CORS Configuration Check")
    
    try:
        response = requests.options("http://localhost:8000/api/auth/login", 
                                  headers={"Origin": "http://localhost:3000"}, timeout=5)
        
        cors_headers = {
            'Access-Control-Allow-Origin': response.headers.get('Access-Control-Allow-Origin'),
            'Access-Control-Allow-Methods': response.headers.get('Access-Control-Allow-Methods'),
            'Access-Control-Allow-Headers': response.headers.get('Access-Control-Allow-Headers')
        }
        
        if any(cors_headers.values()):
            print_success("CORS headers present")
            for header, value in cors_headers.items():
                if value:
                    print(f"  {header}: {value}")
            return True
        else:
            print_warning("CORS headers not found")
            return False
    except Exception as e:
        print_error(f"CORS check failed: {e}")
        return False

def generate_report(results):
    """Generate a summary report"""
    print_header("Integration Report")
    
    total_checks = len(results)
    passed_checks = sum(results.values())
    
    print(f"📊 Overall Status: {passed_checks}/{total_checks} checks passed")
    
    if passed_checks == total_checks:
        print_success("🎉 All checks passed! Integration is working correctly.")
        print("\nNext steps:")
        print("1. Start your Android app")
        print("2. Test login with: student1 / password123")
        print("3. Navigate through all screens")
    else:
        print_error("❌ Some checks failed. Please fix the issues above.")
        print("\nCommon fixes:")
        print("1. Start PostgreSQL: net start postgresql-x64-15 (Windows) or pg_ctl start")
        print("2. Run database setup: python setup_postgres.py")
        print("3. Run migrations: python manage.py migrate")
        print("4. Seed database: python scripts/seed_db.py")
        print("5. Start server: python manage.py runserver 0.0.0.0:8000")

def main():
    """Main diagnosis function"""
    print("🔍 Student Portal Integration Diagnosis")
    print("This script will check your database and API integration")
    
    results = {}
    
    # Run all checks
    results['python_env'] = check_python_environment()
    results['db_connection'] = check_database_connection()
    results['db_tables'] = check_database_tables()
    results['sample_data'] = check_sample_data()
    results['django_server'] = check_django_server()
    results['api_endpoints'] = test_api_endpoints()
    results['authentication'] = test_authentication()
    results['cors_config'] = check_cors_configuration()
    
    # Generate report
    generate_report(results)
    
    return all(results.values())

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
