# PROJECT PROPOSAL
## INTEGRATED CAMPUS MANAGEMENT SYSTEM (KCA STUDENT APP)

**SUBMITTED BY:**
**NAME:** DOANE WELLINGTONE OGWENO
**REG NO:** 22/04168
**EMAIL:** 2204168@students.kcau.ac.ke

**SUPERVISOR:**
DR. RUFUS GIKERA

**SUBMITTED IN PARTIAL FULFILLMENT OF THE REQUIREMENTS FOR THE AWARD OF A DEGREE IN BACHELOR OF SCIENCE IN SOFTWARE DEVELOPMENT AT KCA UNIVERSITY**

---

## CHAPTER ONE: INTRODUCTION

### 1.1 Background of the Study
In the contemporary academic landscape, the efficiency of higher education institutions relies heavily on the robustness of their Information Systems. KCA University, a premier institution in Kenya, currently operates multiple independent digital systems to manage student activities, virtual learning, fee payments, and grievance resolution. While such systems are functional, their lack of integration creates a fragmented user experience.

Students are often forced to toggle between separate platforms to access course materials, check fee balances, or cast votes in student elections. Furthermore, virtual learning typically relies on external, disjointed tools like Zoom or Google Meet links shared via WhatsApp, leading to security risks and tracking difficulties. The proposed **Integrated Campus Management System (ICMS)** seeks to bridge these gaps. By consolidating academic, administrative, financial, and co-curricular functions (such as voting and library access) into a single, unified mobile platform, the ICMS aims to create a seamless digital ecosystem.

### 1.2 Problem Statement
Despite the availability of digital tools, KCA University faces persistent operational inefficiencies due to the disjointed nature of its current systems. The specific challenges necessitating this study include:

1.  **Fragmented Digital Ecosystem:** Students must use different logins for the Portal (grades), LMS (learning), and Library, leading to "password fatigue" and disjointed workflows.
2.  **Lack of Transparent Digital Voting:** Student elections often rely on manual ballot boxes or insecure web forms, leading to distrust in the electoral process and delayed tallying.
3.  **Inefficient Virtual Classroom Experience:** Reliance on external video conferencing links makes attendance tracking difficult and disconnects the learning experience from the university's official platform.
4.  **Manual Complaint Resolution:** The current grievance handling mechanism is manual and opaque, making it difficult for students to track the status of their issues.
5.  **Information Latency:** Fee payments and unit registrations often take time to reflect across different departments due to lack of real-time database synchronization.

### 1.3 Objectives

#### 1.3.1 General Objective
The primary objective is to design, develop, and deploy a comprehensive **Integrated Campus Management System (ICMS)** application that unifies academic, financial, library, and democratic (voting) services into a secure, mobile-first platform using modern cloud-native technologies.

#### 1.3.2 Specific Objectives
1.  **To develop a unified Android application** using Jetpack Compose that serves as a central hub for Student Portal, Virtual Campus, and Library services.
2.  **To implement a secure, real-time E-Voting module** using Firebase to ensure transparent, accessible, and tamper-proof student elections.
3.  **To integrate an embedded Virtual Classroom** using the **Agora Video SDK**, allowing high-quality, in-app lectures without external links.
4.  **To engineer a Digital Library module** that facilitates easy searching, categorization, and retrieval of academic resources.
5.  **To automate the complaint management lifecycle**, allowing students to lodge tickets and receive real-time push notification updates.
6.  **To implementation a hybrid backend architecture** (Django + Firebase) to ensure robust data integrity (MySQL) and real-time responsiveness.

### 1.4 Research Questions
1.  How can real-time technologies (Firebase/Agora) be leveraged to improve student engagement in virtual learning and elections?
2.  What architectural patterns best support a hybrid system that requires both relational data (fees) and document-based data (voting/chats)?
3.  How does an integrated mobile app affect the turnaround time for administrative services like complaint resolution?
4.  To what extent can in-app video conferencing improve attendance tracking compared to external tools?

### 1.5 Significance of the Study
*   **For Students:** A "Campus in a Pocket" experience—voting, studying, and paying fees from one app.
*   **For Administration:** Automated attendance tracking and a transparent, audit-proof voting system for student leaders.
*   **For the Library:** Increased utilization of digital resources through easy mobile access.
*   **Technological Contribution:** Demonstrates the efficacy of a **Hybrid Architecture** (SQL + NoSQL) in managing complex educational ecosystems.

### 1.6 Scope and Limitations

#### 1.6.1 Scope
The project focuses on an Android Application developed with **Kotlin** and **Jetpack Compose**. The backend utilizes **Python (Django)** for business logic and **MySQL** for structured data, alongside **Google Firebase** for real-time features.
Key Modules:
*   **Authentication:** Secure Login/Sign-up.
*   **Academics:** Unit Registration, Exam Cards, Results.
*   **Virtual Campus:** Live Video Classes (Agora), Notes, Assignments.
*   **Digital Library:** Book search, categorization by School (Business, Tech, etc.).
*   **E-Voting:** Real-time polling for student elections.
*   **Finance:** Fee statements and M-Pesa integration.
*   **Support:** Complaint ticketing system.

#### 1.6.2 Limitations
*   **Mobile-Only:** The initial release is an Android application; iOS and Web versions are not part of this phase.
*   **Internet Dependency:** Real-time features (Video, Voting) require a stable internet connection.
*   **API Costs:** Extensive use of Agora and Firebase beyond the free tier may incur costs during scaling.

---

## CHAPTER TWO: LITERATURE REVIEW

### 2.1 Theoretical Framework
This study relies on the **Task-Technology Fit (TTF)** theory, which argues that IT is more likely to have a positive impact on individual performance and be used if the capabilities of the IT match the tasks that the user must perform. By integrating Voting, Learning, and Admin tasks (the Tasks) into a single optimized Mobile App (the Technology), the system maximizes the "Fit."

### 2.2 Empirical Review
Studies on E-Governance in universities show a shift towards mobile-first solutions. A 2023 study by *TechScience* indicated that universities implementing App-based voting saw a 40% increase in voter turnout compared to manual ballots. Similarly, integration of real-time chat and video (like Agora) within LMS platforms has been shown to increase student retention by reducing the friction of switching apps.

### 2.3 Review of Related Systems
| System | Strengths | Weaknesses |
| :--- | :--- | :--- |
| **Moodle Mobile** | Great for learning content. | No integration with Finance, Voting, or Library books. |
| **Zoom/Teams** | Excellent video quality. | External to the university ecosystem; no attendance tracking sync. |
| **Manual Ballot Box** | Trusted physical verification. | Slow counting, prone to ballot stuffing, requires physical presence. |

**The ICMS Advantage:** Combines the utility of an ERP, the interactivity of a Video App, and the security of an E-Voting system.

---

## CHAPTER THREE: METHODOLOGY

### 3.1 Research Design
The project follows the **Agile Methodology**. This iterative approach enables the development of distinct modules (e.g., Voting, Library) in "Sprints," allowing for continuous testing and refinement based on user feedback.

### 3.2 System Development Tools & Technologies
The system is built on a **Modern Tech Stack**:
*   **Frontend:** Android (Kotlin) using **Jetpack Compose** for UI.
*   **Backend API:** **Django REST Framework** (Python) for complex logic and integrations.
*   **Real-time Database:** **Google Firebase Firestore** (for Voting, Complaints, Chat).
*   **Relational Database:** **MySQL** (for Student Records, Fees).
*   **Video Engine:** **Agora RTC SDK** for high-definition, low-latency in-app video.
*   **Design:** Figma for UI/UX prototyping.

### 3.3 Data Collection Methods
*   **Interviews:** With Student Council members regarding voting pain points.
*   **Observation:** Analysis of the current manual registration and complaint processes.
*   **Document Review:** Study of current KCAU systems manuals (Portal, Moodle).

### 3.4 Ethical Considerations
*   **Data Privacy:** Compliance with the **Data Protection Act**, ensuring student grades and financial data are encrypted.
*   **Electoral Integrity:** The voting module is designed to ensure anonymity and "One Person, One Vote" integrity using unique student identifiers.

---

## CHAPTER FOUR: PROPOSED SYSTEM OVERVIEW

### 4.1 System Requirements Specification (SRS)

#### 4.1.1 Functional Requirements
*   **Modules:**
    *   **Voting:** View candidates, cast secure vote, view real-time results.
    *   **Library:** Browse books by category (Education, Technology, Business), view availability.
    *   **Classroom:** Join live video sessions directly in-app (Agora integration).
    *   **Admin:** Manage users, upload results, monitor election integrity.
*   **Integration:** The system must sync fee payment data from M-Pesa to the student's ledger.

#### 4.1.2 Non-Functional Requirements
*   **Responsiveness:** UI must adapt to various Android screen sizes.
*   **Latency:** Video streaming delay should be under 400ms (Agora standard).
*   **Security:** JWT Tokens for API access; End-to-end encryption for video.

### 4.2 System Design Specification (SDS)

#### 4.2.1 Architecture
The system utilizes a **Hybrid Cloud Architecture**:
1.  **Client:** Android App (Presentation Layer).
2.  **Middleware:** Django API (Business Logic) & Firebase Cloud Functions.
3.  **Data Layer:** MySQL (Structured Storage) + Firestore (Real-time Sync).

#### 4.2.2 Key Database Entities
*   **`Vote`**: {VoteID, StudentID (Hashed), ElectionID, CandidateID, Timestamp}
*   **`Book`**: {ISBN, Title, Author, Category, Status}
*   **`Meeting`**: {RoomID, LecturerID, CourseID, AgoraToken, StartTime}

---

## APPENDICES

### Appendix I: Research Schedule (Gantt Chart)
*   **Month 1:** Requirement Analysis & UI Design (Figma).
*   **Month 2:** Backend Setup (Django/MySQL) & Firebase Config.
*   **Month 3:** Core Features Dev (Auth, Academics, Library).
*   **Month 4:** Advanced Features (Agora Video, Voting Module).
*   **Month 5:** Testing, Bug Fixes, and Documentation.

### Appendix II: Project Budget

| Item | Description | Cost (KES) |
| :--- | :--- | :--- |
| **Hosting** | VPS for Django & MySQL | 15,000 |
| **Services** | Agora Video (Standard Tier) / Firebase (Blaze) | 10,000 |
| **Hardware** | Development Laptop & Test Devices | 90,000 |
| **Internet** | High-speed data for video testing | 12,000 |
| **Misc** | Printing, Binding, Stationery | 5,000 |
| **Total** | | **132,000** |

### Appendix III: Data Collection Instrument
**(Sample Questionnaire Questions)**
1.  How often do you miss classes due to lost/broken Zoom links?
2.  Would you vote in student elections if you could do it from your phone?
3.  How difficult is it to find physical books in the library currently?
4.  Rate your trust in the current manual complaint tracking system (1-5).
