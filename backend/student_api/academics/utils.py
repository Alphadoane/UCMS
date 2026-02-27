from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from django.conf import settings
import io
import os
from datetime import datetime

def generate_pdf_slip(student, semester_name, results, gpa, total_credits):
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=letter)
    elements = []
    
    styles = getSampleStyleSheet()
    title_style = styles['Title']
    body_style = styles['BodyText']
    
    # Header - KCA Logo and University Name
    # Assuming logo exists, otherwise skip image
    logo_path = os.path.join(settings.BASE_DIR, 'static', 'admin', 'img', 'kca_logo.png') # Adjust path as needed
    if os.path.exists(logo_path):
        img = Image(logo_path, width=1.5*inch, height=1.5*inch)
        elements.append(img)

    elements.append(Paragraph("KCA UNIVERSITY", title_style))
    elements.append(Paragraph("OFFICIAL ACADEMIC TRANSCRIPT", ParagraphStyle(name='Subtitle', parent=styles['Heading2'], alignment=1)))
    elements.append(Spacer(1, 0.2*inch))
    
    # Student Details
    details_data = [
        ["Name:", f"{student.user.first_name} {student.user.last_name}"],
        ["Admission No:", student.admission_number],
        ["Program:", student.program.name],
        ["Semester:", semester_name],
        ["Date Generated:", datetime.now().strftime("%Y-%m-%d")]
    ]
    
    details_table = Table(details_data, colWidths=[1.5*inch, 4*inch])
    details_table.setStyle(TableStyle([
        ('FONTNAME', (0,0), (0,-1), 'Helvetica-Bold'),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    elements.append(details_table)
    elements.append(Spacer(1, 0.3*inch))
    
    # Results Table
    # Headers
    table_data = [['Course Code', 'Course Title', 'Credits', 'Grade', 'Points']]
    
    # Data Rows
    for result in results:
        points = 0.0
        grade = result['grade']
        if grade == 'A': points = 4.0
        elif grade == 'B': points = 3.0
        elif grade == 'C': points = 2.0
        elif grade == 'D': points = 1.0
        else: points = 0.0
        
        table_data.append([
            result['course_code'],
            result['course_title'][:30] + ('...' if len(result['course_title']) > 30 else ''), # Truncate title
            str(result['credits']),
            grade,
            f"{points * result['credits']:.1f}"
        ])
        
    # Footer Row (GPA)
    table_data.append(['', '', 'Total Credits:', str(total_credits), f"GPA: {gpa:.2f}"])

    results_table = Table(table_data, colWidths=[1.2*inch, 2.5*inch, 0.8*inch, 0.8*inch, 0.8*inch])
    results_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.grey),
        ('TEXTCOLOR', (0,0), (-1,0), colors.whitesmoke),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0,0), (-1,0), 12),
        ('BACKGROUND', (0,-1), (-1,-1), colors.lightgrey),
        ('GRID', (0,0), (-1,-1), 1, colors.black),
    ]))
    
    elements.append(results_table)
    elements.append(Spacer(1, 0.5*inch))
    
    # Footer Remarks
    elements.append(Paragraph("Remarks:", styles['Heading4']))
    remarks = "Congratulations on your performance." if gpa >= 2.0 else "Please consult your academic advisor."
    elements.append(Paragraph(remarks, body_style))
    
    elements.append(Spacer(1, 0.5*inch))
    elements.append(Paragraph("Registrar (Academic Affairs)", ParagraphStyle(name='Footer', parent=body_style, alignment=2)))
    
    # Build PDF
    doc.build(elements)
    buffer.seek(0)
    return buffer
