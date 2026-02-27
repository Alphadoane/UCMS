import os
import django

# Setup Django environment
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "student_api.settings")
django.setup()

from student_api.academics.models import Program, Course

def seed_real_units():
    print("Beginning real-life unit seeding...")

    # Map Program Code -> List of (Course Code, Course Name, Units)
    program_units = {
        # Tech
        "BSc.SE": [
            ("SE110", "Introduction to Software Engineering", 3),
            ("SE111", "Requirements Engineering", 3),
            ("SE112", "Programming Fundamentals (Java)", 4),
            ("SE120", "Discrete Structures", 3),
            ("SE210", "Software Architecture & Design", 3),
            ("SE211", "User Interface Design", 3),
            ("SE310", "Software Testing & Quality Assurance", 3),
            ("SE410", "Mobile Application Development", 4),
        ],
        "BBIT": [
            ("BBIT101", "Introduction to Business Information Systems", 3),
            ("BBIT102", "Fundamentals of Computer Programming", 4),
            ("BBIT103", "Microeconomics", 3),
            ("BBIT201", "Object-Oriented Programming I", 4),
            ("BBIT202", "Database Management Systems", 4),
            ("BBIT301", "Business Intelligence & Analytics", 3),
            ("BBIT302", "E-Commerce Strategy", 3),
            ("BBIT401", "Information Systems Audit", 3),
        ],
        "BSIT": [
            ("BIT111", "Information Technology Essentials", 3),
            ("BIT112", "Computer Networks I", 4),
            ("BIT113", "Operating Systems", 4),
            ("BIT211", "System Analysis and Design", 3),
            ("BIT212", "Web Technologies I", 4),
            ("BIT311", "Network Security", 3),
            ("BIT312", "Cloud Computing", 3),
        ],
         "BSc.CS": [ # Targeting the ID 66 or similar if exists
            ("CSC111", "Introduction to Computer Systems", 3),
            ("CSC112", "Structured Programming (C)", 4),
            ("CSC121", "Digital Logic", 3),
            ("CSC211", "Data Structures & Algorithms in C++", 4),
            ("CSC221", "Computer Organization & Architecture", 3),
            ("CSC311", "Artificial Intelligence", 3),
            ("CSC321", "Automata Theory", 3),
        ],
        
        # Data Science
        "BSDS": [
            ("DS101", "Introduction to Data Science", 3),
            ("DS102", "Calculus for Data Science", 3),
            ("DS103", "Linear Algebra", 3),
            ("DS201", "Statistical Methods", 3),
            ("DS202", "Data Visualization", 3),
            ("DS301", "Machine Learning", 4),
            ("DS302", "Big Data Analytics", 4),
        ],

        # Business
        "B.Com": [
            ("BCOM101", "Business Communication", 3),
            ("BCOM102", "Principles of Macroeconomics", 3),
            ("BCOM201", "Cost Accounting", 3),
            ("BCOM202", "Organizational Behavior", 3),
            ("BCOM301", "Financial Management", 3),
            ("BCOM302", "Taxation", 3),
        ],
        "BIBM": [
            ("IBM101", "Globalization & Business", 3),
            ("IBM102", "International Economics", 3),
            ("IBM201", "Cross-Cultural Management", 3),
        ],
        
        # Education
        "BEA": [ # Bachelor of Education (Arts)
            ("EDU110", "History of Education", 3),
            ("EDU111", "Sociology of Education", 3),
            ("GEO110", "Introduction to Physical Geography", 3),
            ("HIS110", "Themes in African History", 3),
            ("EDU210", "Psychology of Human Development", 3),
            ("EDU310", "Educational Measurement & Evaluation", 3),
        ],
        
        # Media
        "BAJDM": [
            ("JDM101", "Introduction to Mass Communication", 3),
            ("JDM102", "English for Journalists", 3),
            ("JDM201", "News Writing and Reporting", 3),
            ("JDM202", "Media Law and Ethics", 3),
            ("JDM301", "Broadcast Journalism (Radio/TV)", 4),
            ("JDM302", "Digital Media Production", 4),
        ],

        # Psychology
        "BACP": [ # Counselling Psychology
            ("PSY101", "Introduction to Psychology", 3),
            ("PSY102", "Theories of Personality", 3),
            ("PSY201", "Developmental Psychology", 3),
            ("PSY202", "Abnormal Psychology", 3),
            ("PSY301", "Counselling Skills & Techniques", 3),
        ],

        # Criminology
        "BAC": [
             ("CRIM101", "Introduction to Criminology", 3),
             ("CRIM102", "Criminal Justice System", 3),
             ("CRIM201", "Psychology of Criminal Behavior", 3),
             ("CRIM202", "Correctional Services", 3),
        ]
    }

    for prog_code, courses in program_units.items():
        try:
            # We filter because there might be duplicates, we presume to attach to the first valid one
            # or we can iterate if multiple programs have same code (unlikely due to unique constraint, but good safety)
            program = Program.objects.get(code=prog_code)
            print(f"Seeding units for: {program.name} ({prog_code})")
            
            for code, name, credits in courses:
                course, created = Course.objects.get_or_create(
                    program=program,
                    code=code,
                    defaults={
                        "name": name,
                        "credit_units": credits
                    }
                )
                if created:
                    print(f"  + Added: {code} - {name}")
                else:
                    print(f"  . Exists: {code}")
                    
        except Program.DoesNotExist:
            print(f"!! Program not found with code: {prog_code}. Skipping.")
        except Exception as e:
            print(f"!! Error processing {prog_code}: {e}")

    print("Real-life unit seeding completed.")

if __name__ == "__main__":
    seed_real_units()
