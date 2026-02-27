import psycopg2
import os
from dotenv import load_dotenv
from pathlib import Path

# Load environment variables
BASE_DIR = Path(__file__).resolve().parent
load_dotenv(BASE_DIR / ".env")

def fix_schema():
    try:
        conn = psycopg2.connect(
            dbname=os.getenv("POSTGRES_DB"),
            user=os.getenv("POSTGRES_USER"),
            password=os.getenv("POSTGRES_PASSWORD"),
            host=os.getenv("POSTGRES_HOST", "localhost"),
            port=os.getenv("POSTGRES_PORT", "5432")
        )
        cur = conn.cursor()
        
        # Add missing columns
        commands = [
            "ALTER TABLE payments ADD COLUMN IF NOT EXISTS checkout_request_id character varying(100) UNIQUE;",
            "ALTER TABLE payments ADD COLUMN IF NOT EXISTS merchant_request_id character varying(100);",
            "ALTER TABLE payments ADD COLUMN IF NOT EXISTS phone_number character varying(15);",
            "ALTER TABLE payments ADD COLUMN IF NOT EXISTS mpesa_receipt_number character varying(50);"
        ]
        
        for cmd in commands:
            print(f"Executing: {cmd}")
            try:
                cur.execute(cmd)
            except Exception as e:
                print(f"Error executing command: {e}")
                conn.rollback()
                continue
            conn.commit()
            
        print("Schema fix applied successfully.")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Connection Error: {e}")

if __name__ == "__main__":
    fix_schema()
