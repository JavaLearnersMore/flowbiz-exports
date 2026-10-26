# Flowbiz Exports Pvt. Ltd. – Website

Frontend: HTML / CSS / JavaScript (inside `src/main/resources/static`)
Backend: Java 17 + Spring Boot 3 (REST API, Spring Data JPA)
Database: MySQL

Pages: Home, About Us, Products, Services & Quality, Contact Us (+ Login, Register)

## MySQL tables (created automatically on first run)
- `users` – registered users (id, name, email, phone, company, password_hash (BCrypt), created_at)
- `enquiries` – messages from the Contact form (id, name, email, phone, company, product, message, created_at)

---------------------------------------------------------------------

## 1. Run on your computer

Install: JDK 17, Maven, MySQL Server 8 (and optionally MySQL Workbench).

1. Start MySQL. Default settings expect user `root`, password `root`, host `localhost:3306`.
   To use different credentials, edit `src/main/resources/application.properties`
   or set environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. The database `flowbiz` is created automatically. (Or create it yourself:
   `CREATE DATABASE flowbiz;`)
3. Run:
```bash
mvn spring-boot:run
```
4. Open http://localhost:8080

To see saved data, open MySQL Workbench (or the mysql CLI):
```sql
USE flowbiz;
SELECT * FROM users;
SELECT * FROM enquiries;
```

---------------------------------------------------------------------

## 2. Edit your real company details
Search and replace these placeholders:
- `info@flowbizexports.com`, `+91 00000 00000` → `js/main.js` (footer) and `contact.html`
- `[Your street address], [City]...` and the Google Maps link → `contact.html`
- `[Year]` history items → `about.html`
- Logo → replace `images/logo.svg` with your real logo
- Product pictures → replace the files in `images/` with real photos (same names)

---------------------------------------------------------------------

## 3. Host the website

You need two things online: a **MySQL database** and the **Java app**.

### Step A – Cloud MySQL database
Pick one provider (many have a free or low-cost plan): Aiven, Railway, PlanetScale-compatible
MySQL hosts, AWS RDS, DigitalOcean Managed MySQL, or the MySQL included with your hosting plan.
Create a MySQL 8 database named `flowbiz` and note the host, port, username and password.
Your JDBC URL will look like:
`jdbc:mysql://HOST:PORT/flowbiz?useSSL=true&serverTimezone=UTC`

### Step B – Put code on GitHub
```bash
git init
git add .
git commit -m "Flowbiz website"
# create an empty repo on github.com, then:
git remote add origin https://github.com/YOUR_USERNAME/flowbiz-exports.git
git push -u origin main
```

### Step C – Deploy the Java app (Render example)
1. https://render.com → sign in with GitHub → **New → Web Service** → select your repo.
2. Runtime: **Docker** (uses the included `Dockerfile`).
3. Add Environment Variables:
   - `DB_URL` = your JDBC URL from Step A
   - `DB_USERNAME` = database user
   - `DB_PASSWORD` = database password
4. **Create Web Service**. You get a URL like `https://flowbiz-exports.onrender.com`.
   (Free plan sleeps when idle; first load may take ~1 minute.)

### Step D – Own domain (e.g. flowbizexports.com)
1. Buy a domain (GoDaddy, Namecheap, etc.).
2. Render → your service → Settings → Custom Domains → add domain.
3. Add the DNS records Render shows in your domain provider. HTTPS is automatic.

### Alternative: VPS (DigitalOcean / AWS / Hostinger) with MySQL on the same server
```bash
sudo apt install openjdk-17-jre mysql-server
sudo mysql -e "CREATE DATABASE flowbiz; CREATE USER 'flowbiz'@'localhost' IDENTIFIED BY 'STRONG_PASSWORD'; GRANT ALL ON flowbiz.* TO 'flowbiz'@'localhost';"
mvn clean package            # builds target/flowbiz-exports-1.0.0.jar
DB_URL="jdbc:mysql://localhost:3306/flowbiz?serverTimezone=UTC" DB_USERNAME=flowbiz DB_PASSWORD=STRONG_PASSWORD \
  java -jar target/flowbiz-exports-1.0.0.jar
```
Then use Nginx as a reverse proxy to port 8080 and Certbot for HTTPS.

---------------------------------------------------------------------

## API
| Method | URL | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register (saves in MySQL) |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Current login status |
| POST | `/api/enquiries` | Save enquiry form |
