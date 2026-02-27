from django.core.management.base import BaseCommand
from student_api.core.models import Program, Department, Faculty

class Command(BaseCommand):
    help = 'Populates KCA University Programs'

    def handle(self, *args, **options):
        # 1. Ensure basic structure
        faculty, _ = Faculty.objects.get_or_create(name="KCA University")
        
        # Helper to get/create dept
        def get_dept(name):
            d, _ = Department.objects.get_or_create(faculty=faculty, name=name)
            return d

        data = [
            # Doctoral
            ("Doctoral", "PhD in Business Management", "Master’s degree in relevant field from a recognised university; research proposal; additional criteria per Board of Postgraduate Studies.", "Business"),
            ("Doctoral", "PhD in Finance", "Master’s in Finance/Economics/related discipline; other qualifications subject to Senate approval.", "Business"),
            ("Doctoral", "PhD in Information Systems", "Master’s in relevant ICT/Management field; additional requirements set by academic unit.", "ICT"),
            
            # Masters
            ("Masters", "MBA (General & Specializations)", "Bachelor’s degree in any field plus relevant work experience; exact requirements vary by track and may include work experience.", "Business"),
            ("Masters", "Master of Arts in Counselling Psychology", "Bachelor’s degree; specific prerequisites may depend on discipline.", "Psychology"),
            ("Masters", "Master of Education", "Bachelor’s degree; teaching experience may be required for certain tracks.", "Education"),
            ("Masters", "MSc in Commerce", "Bachelor’s degree (Upper Second or Lower Second + 2 years relevant work experience; Pass + 5 years and aptitude test possible).", "Business"),
            ("Masters", "MSc in Data Science", "Bachelor’s degree in relevant field; Senate approval for equivalents.", "ICT"),
            ("Masters", "MSc in Data Analytics", "Bachelor’s degree; equivalent qualifications considered.", "ICT"),
             ("Masters", "MSc in Development Finance", "Bachelor’s degree in relevant field; further criteria may apply.", "Finance"),
            ("Masters", "MSc in Information Systems Management", "Bachelor’s degree; relevant IT background recommended.", "ICT"),
             ("Masters", "MSc in Knowledge Management & Innovation", "Bachelor’s degree; additional criteria set by department.", "ICT"),
            ("Masters", "MSc in Supply Chain Management", "Bachelor’s degree; related background preferred.", "Business"),

            # Postgraduate Diploma
            ("Postgraduate Diploma", "Post Graduate Diploma in Education", "First degree from recognised university; studied at least two secondary-school subjects.", "Education"),

            # Undergraduate
            ("Undergraduate", "Bachelor of Education (Arts)", "KCSE mean grade C+; C+ in specialization subjects; C in English; D+ in Mathematics.", "Education"),
            ("Undergraduate", "Bachelor of Education (Early Childhood)", "Typically KCSE C+ or equivalent; contact admissions for specifics.", "Education"),
            ("Undergraduate", "Bachelor of Arts (Counselling Psychology)", "KCSE C+ or equivalent qualification.", "Psychology"),
            ("Undergraduate", "Bachelor of Arts (Criminology)", "KCSE C+ or equivalent.", "Arts"),
             ("Undergraduate", "Bachelor of Arts (Economics & Business Studies)", "KCSE C+ or equivalent.", "Business"),
            ("Undergraduate", "Bachelor of Arts (Film Tech & Performing Arts)", "KCSE C+ or equivalent.", "Arts"),
            ("Undergraduate", "Bachelor of Arts (Journalism & Digital Media)", "KCSE C+ or equivalent.", "Arts"),
            ("Undergraduate", "Bachelor of Business Information Technology", "KCSE C+ or equivalent.", "ICT"),
            ("Undergraduate", "Bachelor of Commerce", "KCSE C+ or equivalent.", "Business"),
            ("Undergraduate", "Bachelor of International Business Management", "KCSE C+ or equivalent.", "Business"),
            ("Undergraduate", "Bachelor of Procurement & Logistics", "KCSE C+ or equivalent.", "Business"),
             ("Undergraduate", "BSc in Actuarial Science", "KCSE C+ or equivalent with strong mathematics expected.", "Business"),
            ("Undergraduate", "BSc in Applied Computing", "KCSE C+ or equivalent.", "ICT"),
             ("Undergraduate", "BSc in Data Science", "KCSE C+ or equivalent.", "ICT"),
            ("Undergraduate", "BSc in Economics & Statistics", "KCSE C+ or equivalent.", "Business"),
             ("Undergraduate", "BSc in Forensic Accounting", "KCSE C+ or equivalent.", "Business"),
            ("Undergraduate", "BSc in Gaming & Animation Tech", "KCSE C+ or equivalent.", "ICT"),
             ("Undergraduate", "BSc in ICT", "KCSE C+ or equivalent.", "ICT"),
            ("Undergraduate", "BSc in Information Security & Forensics", "KCSE C+ or equivalent.", "ICT"),
             ("Undergraduate", "BSc in Information Technology", "KCSE C+ or equivalent.", "ICT"),
            ("Undergraduate", "BSc in Software Development", "KCSE C+ or equivalent.", "ICT"),
            ("Undergraduate", "BSc in Public Management", "KCSE C+ or equivalent.", "Business"),

            # Diploma
            ("Diploma", "Diploma in Business Management", "KCSE C- (minus) or equivalent.", "Business"),
             ("Diploma", "Diploma in Information Technology", "KCSE C- (minus) or equivalent.", "ICT"),
            ("Diploma", "Diploma in Counselling Psychology", "KCSE C- or equivalent.", "Psychology"),
             ("Diploma", "Diploma in Criminology & Criminal Justice", "KCSE C- or equivalent.", "Arts"),
            ("Diploma", "Diploma in Data Management & Analytics", "KCSE C- or equivalent (or IGCSE grade D).", "ICT"),
             ("Diploma", "Diploma in Computer Networks & Systems Admin", "KCSE C- or equivalent.", "ICT"),
            ("Diploma", "Diploma in Film Technology", "KCSE C- or equivalent.", "Arts"),
             ("Diploma", "Diploma in Journalism & Digital Media", "KCSE C- or equivalent.", "Arts"),
            ("Diploma", "Diploma in Education", "KCSE C- or equivalent.", "Education"),
             ("Diploma", "Diploma in Procurement & Logistics", "KCSE C- or equivalent.", "Business"),
            ("Diploma", "Diploma in Project Management", "KCSE C- or equivalent.", "Business"),
             ("Diploma", "Diploma in Public Management", "KCSE C- or equivalent.", "Business"),
             ("Diploma", "Diploma in Banking", "KCSE C- or equivalent.", "Business"),

            # Certificate
            ("Certificate", "Certificate in Banking", "KCSE D+ (plus) or equivalent.", "Business"),
            ("Certificate", "Certificate in Project Management", "KCSE D+ (plus) or equivalent.", "Business"),
             ("Certificate", "Certificate in Procurement & Logistics", "KCSE C- (minus) or equivalent.", "Business"),
            ("Certificate", "Certificate in Computer Applications", "KCSE C- (minus) or equivalent.", "ICT"),

            # Professional
            ("Professional", "Accounting Technicians Diploma (ATD)", "KCSE C- (minus) or equivalent (e.g., IGCSE grade D).", "Professional"),
            ("Professional", "Certified Public Accountants (CPA)", "KCSE C+ or equivalent (including KACE with principals & credits); degree holders eligible.", "Professional"),
            ("Professional", "ACCA (UK)", "KCSE/IGCSE with English/Maths credits or equivalent; exemptions with prior qualifications (general professional norms).", "Professional"),
            ("Professional", "Certified Human Resource Professional (CHRP)", "KCSE or equivalent; subject to professional body criteria.", "Professional"),
            ("Professional", "Certified Information System Auditor (CISA)", "Prior qualifications and/or professional experience typical; check admissions.", "Professional"),
            ("Professional", "Certified Investment & Financial Analysts (CIFA)", "Professional prerequisites vary; contact KCAU PTTI.", "Professional"),
            ("Professional", "Certified Forensic Fraud Examiner (CFFE)", "Professional prerequisites vary; contact KCAU.", "Professional"),
            ("Professional", "Certified Secretaries (CS)", "KCSE or equivalent; specific prerequisites per secretarial body.", "Professional"),
            
            # Short
             ("Short Course", "Short courses (Excel, AWS, CCNA, Python...)", "Typically open to KCSE holders of any grade or equivalent.", "Professional"),
             
             # KNEC
              ("KNEC", "Craft and Artisan (Business, ICT, Food & Beverages)", "KCSE D or equivalent; KNEC qualifications considered.", "Technical"),

        ]

        count = 0
        for category, name, reqs, dept_name in data:
            dept = get_dept(dept_name)
            # Simple code generation
            code = "".join([c for c in name if c.isupper() or c.isdigit()]).replace("(", "").replace(")", "")
            if len(code) < 3: code = name[:3].upper()
            
            # Unique code hack
            base_code = code
            counter = 1
            while Program.objects.filter(code=code).exists():
                code = f"{base_code}{counter}"
                counter += 1
                
            Program.objects.get_or_create(
                name=name,
                defaults={
                    'code': code.upper(),
                    'department': dept,
                    'duration_years': 4 if category == "Undergraduate" else 2,
                    'category': category,
                    'entry_requirements': reqs
                }
            )
            count += 1
            
        self.stdout.write(self.style.SUCCESS(f'Successfully populated {count} programmes'))
