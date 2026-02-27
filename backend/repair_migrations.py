from django.db import connection

apps = [
    ('core', '0001_initial'),
    ('academics', '0001_initial'),
    ('academics', '0002_initial'),
    ('admission', '0001_initial'),
    ('admission', '0002_initial'),
    ('finance', '0001_initial'),
    ('support', '0001_initial'),
    ('voting', '0001_initial'),
]

with connection.cursor() as cursor:
    for app, name in apps:
        cursor.execute("INSERT OR IGNORE INTO django_migrations (app, name, applied) VALUES (%s, %s, CURRENT_TIMESTAMP)", [app, name])
print("Migrations repaired successfully.")
