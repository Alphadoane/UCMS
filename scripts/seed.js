const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seed() {
  console.log(`🌱 Starting database seed for Project: ${serviceAccount.project_id}...`);

  try {
    // DIAGNOSTIC CORE: Try to list collections first to verify connection
    console.log("... Testing connection to Firestore...");
    const collections = await db.listCollections();
    console.log(`✅ Connection Successful! Found ${collections.length} existing collections.`);

    // --- HELPER: Create Auth User ---
    async function ensureAuthUser(uid, email, password, displayName) {
      try {
        await admin.auth().updateUser(uid, { email, password, displayName });
        console.log(`   - Auth User Updated: ${email}`);
      } catch (e) {
        if (e.code === 'auth/user-not-found') {
          await admin.auth().createUser({ uid, email, password, displayName });
          console.log(`   - Auth User Created: ${email}`);
        } else {
          console.warn(`   - Auth Warning for ${email}: ${e.message}`);
        }
      }
    }

    // 0. Create Auth Accounts (So you can actually Login!)
    // Password for all: "password123"
    console.log("... Seeding Authentication Accounts...");

    // Student 1 (25/00001)
    const email1 = "2500001@kcau.ac.ke";
    await ensureAuthUser("student_1", email1, "password123", "Jane Doe");

    // Student 2 (25/00002)
    const email2 = "2500002@kcau.ac.ke";
    await ensureAuthUser("student_2", email2, "password123", "John Student");

    // Staff 1
    await ensureAuthUser("staff_1", "dr.smith@kca.ac.ke", "password123", "Dr. Smith");

    // Admin 1
    await ensureAuthUser("admin_1", "admin@kca.ac.ke", "password123", "System Administrator");

    // Support 1
    await ensureAuthUser("support_1", "support@kca.ac.ke", "password123", "IT Support");

    console.log("✅ Auth Accounts Ready! Password: password123");

    // 1. Elections
    const electionRef = await db.collection('elections').add({
      title: "KCA Student Council 2025",
      created_at: admin.firestore.Timestamp.now(),
      status: "OPEN"
    });
    console.log(`✅ Created Election: ${electionRef.id}`);

    // 2. Candidates
    const candidates = ["Jane Doe", "John Smith", "Alice Wonder"];
    for (const name of candidates) {
      await db.collection('candidates').add({
        election_id: electionRef.id,
        name: name,
        vote_count: Math.floor(Math.random() * 100) + 10
      });
      console.log(`   - Added Candidate: ${name}`);
    }

    // 3. Courses (Sample)
    const courses = [
      { code: "CS101", name: "Introduction to Java" },
      { code: "MATH201", name: "Calculus II" }
    ];
    for (const course of courses) {
      await db.collection('courses').add(course);
    }
    console.log('✅ Added Sample Courses');

    // 4. Users (Firestore Profiles linking to Auth UIDs)
    // We explicitly used UIDs: student_1, student_2, staff_1 above.

    const users = [
      {
        id: "student_1", email: "2500001@kcau.ac.ke", firstName: "Jane", lastName: "Doe",
        role: "STUDENT", regNumber: "25/00001", course: "BSD", department: "Technology"
      },
      {
        id: "student_2", email: "2500002@kcau.ac.ke", firstName: "John", lastName: "Student",
        role: "STUDENT", regNumber: "25/00002", course: "BIT", department: "Technology"
      },
      {
        id: "staff_1", email: "dr.smith@kca.ac.ke", firstName: "Dr. Smith", lastName: "Lecturer",
        role: "STAFF", regNumber: "EMP001", department: "Technology"
      },
      {
        id: "admin_1", email: "admin@kca.ac.ke", firstName: "System", lastName: "Admin",
        role: "ADMIN", regNumber: "ADM001", department: "Administration"
      },
      {
        id: "support_1", email: "support@kca.ac.ke", firstName: "IT", lastName: "Support",
        role: "TECHNICAL_SUPPORT", regNumber: "SUP001", department: "IT Department"
      }
    ];

    for (const u of users) {
      await db.collection('users').doc(u.id).set(u);
      console.log(`   - Linked User Profile: ${u.email} (${u.role})`);
    }

    // 5. Relations (Finance, Support - mirroring Kotlin logic)
    await db.collection('finance_transactions').add({
      studentId: "student_1", amount: 50000, type: "Tuition Fee", date: "2024-01-15", status: "PAID"
    });

    await db.collection('support_tickets').add({
      userId: "student_1", subject: "Login Issue", description: "Sample Ticket", status: "OPEN", priority: "HIGH"
    });

    console.log('🎉 Seeding Complete!');
  } catch (error) {
    console.error('❌ Seeding Failed:', error);
  }
}

seed();
