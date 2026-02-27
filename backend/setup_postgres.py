import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Credentials
DB_USER = "postgres"
DB_PASS = "Doane40640666"
DB_HOST = "localhost"
NEW_DB_NAME = "student_portal_db"

def create_database():
    print(f"🔌 Connecting to PostgreSQL as '{DB_USER}'...")
    try:
        # Connect to 'postgres' default database to create usage db
        conn = psycopg2.connect(user=DB_USER, password=DB_PASS, host=DB_HOST)
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()
        
        # Check if DB exists
        cursor.execute(f"SELECT 1 FROM pg_catalog.pg_database WHERE datname = '{NEW_DB_NAME}'")
        exists = cursor.fetchone()
        
        if not exists:
            print(f"✨ Creating database '{NEW_DB_NAME}'...")
            cursor.execute(f"CREATE DATABASE {NEW_DB_NAME}")
            print("✅ Database created successfully!")
        else:
            print(f"ℹ️ Database '{NEW_DB_NAME}' already exists.")
            
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"❌ Error creating database: {e}")

if __name__ == "__main__":
    create_database()
