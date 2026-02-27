import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Credentials
DB_USER = "postgres"
DB_PASS = "Doane40640666"
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "student_portal_db"

def reset_database():
    try:
        # Connect to 'postgres' db to drop target db
        conn = psycopg2.connect(
            user=DB_USER,
            password=DB_PASS,
            host=DB_HOST,
            port=DB_PORT,
            database="postgres"
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()

        # Drop User Database if exists
        print(f"💣 Dropping database '{DB_NAME}'...")
        cursor.execute(f"DROP DATABASE IF EXISTS {DB_NAME};")

        # Create User Database
        print(f"✨ Creating database '{DB_NAME}'...")
        cursor.execute(f"CREATE DATABASE {DB_NAME};")

        print("✅ Database reset successful!")
        cursor.close()
        conn.close()

    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    reset_database()
