import 'dotenv/config';
import mysql from 'mysql2/promise';
import bcrypt from 'bcrypt';

async function main() {
  const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'test',
    waitForConnections: true,
    connectionLimit: 5,
  });

  const defaults = [
    { username: 'student1', password: 'S1!doane2025' },
    { username: 'student2', password: 'S2!doane2025' },
    { username: 'admin', password: 'Admin#Doane2025' },
  ];

  for (const { username, password } of defaults) {
    const [rows] = await pool.query('SELECT id, password_hash FROM users WHERE username=?', [username]);
    if (!rows || rows.length === 0) continue;
    const { id, password_hash } = rows[0];
    if (password_hash && String(password_hash).length > 0) continue;
    const hash = await bcrypt.hash(password, 10);
    await pool.query('UPDATE users SET password_hash=? WHERE id=?', [hash, id]);
    // eslint-disable-next-line no-console
    console.log(`Set password for ${username}`);
  }

  await pool.end();
}

main().catch((e) => {
  // eslint-disable-next-line no-console
  console.error(e);
  process.exit(1);
});


