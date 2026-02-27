import psycopg2
import os
from dotenv import load_dotenv

load_dotenv("backend/.env")

conn = psycopg2.connect(
    dbname=os.getenv("POSTGRES_DB"),
    user=os.getenv("POSTGRES_USER"),
    password=os.getenv("POSTGRES_PASSWORD"),
    host=os.getenv("POSTGRES_HOST", "localhost"),
    port=os.getenv("POSTGRES_PORT", "5432")
)

cur = conn.cursor()
cur.execute("SELECT id, application_id, first_name, current_phase FROM admission_applications ORDER BY application_date DESC LIMIT 5;")
print("Latest Applications:")
for row in cur.fetchall():
    print(row)

cur.execute("SELECT id, application_id, document_type, file, uploaded_at FROM admission_documents ORDER BY uploaded_at DESC LIMIT 10;")
print("\nLatest Documents:")
for row in cur.fetchall():
    print(row)

cur.close()
conn.close()
