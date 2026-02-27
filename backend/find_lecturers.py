from student_api.core.models import User, Role

try:
    lecturer_role = Role.objects.get(name='LECTURER')
    lecturers = User.objects.filter(userrole__role=lecturer_role)
    
    print(f"Found {lecturers.count()} lecturers:")
    for lecturer in lecturers:
        print(f"Email: {lecturer.email}, Name: {lecturer.first_name} {lecturer.last_name}")

except Role.DoesNotExist:
    print("Role 'LECTURER' not found.")
except Exception as e:
    print(f"An error occurred: {e}")
