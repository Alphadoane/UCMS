import sqlite3
conn = sqlite3.connect('db.sqlite3')
cursor = conn.cursor()
apps = ['core', 'academics', 'admission', 'finance', 'support', 'voting']
placeholders = ', '.join(['?'] * len(apps))
cursor.execute(f'DELETE FROM django_migrations WHERE app IN ({placeholders})', apps)
conn.commit()
print(f'Deleted {cursor.rowcount} entries')
conn.close()
