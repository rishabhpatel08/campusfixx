<div align="center">

```
 ██████╗ █████╗ ███╗   ███╗██████╗ ██╗   ██╗███████╗███████╗██╗██╗  ██╗
██╔════╝██╔══██╗████╗ ████║██╔══██╗██║   ██║██╔════╝██╔════╝██║╚██╗██╔╝
██║     ███████║██╔████╔██║██████╔╝██║   ██║███████╗█████╗  ██║ ╚███╔╝ 
██║     ██╔══██║██║╚██╔╝██║██╔═══╝ ██║   ██║╚════██║██╔══╝  ██║ ██╔██╗ 
╚██████╗██║  ██║██║ ╚═╝ ██║██║     ╚██████╔╝███████║██║     ██║██╔╝ ██╗
 ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝      ╚═════╝ ╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝
```

# Smart Campus Infrastructure Management Platform
### SGSITS Indore — MCA Department 2025

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17_LTS-orange?style=for-the-badge&logo=openjdk)](https://adoptium.net)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://postgresql.org)
[![GitHub Pages](https://img.shields.io/badge/Frontend-GitHub_Pages-black?style=for-the-badge&logo=github)](https://pages.github.com)
[![Render](https://img.shields.io/badge/Backend-Render.com-purple?style=for-the-badge)](https://render.com)

**Passwordless Login · Bilingual (हिंदी/English) · QR Reporting · WhatsApp Alerts · Gamified**

---

> *"From broken projectors to leaking pipes — CampusFix routes every campus issue to the right person, automatically."*

</div>

---

## 🎯 The Problem We Solved

Every college campus has the same frustrating cycle:

```
Student notices broken fan  →  Tells someone verbally  →  Gets forgotten
     ↓                                                          ↓
Complains again next week   ←  Nothing happens         ←  No accountability
```

**No tracking. No accountability. No resolution timeline. No data.**

CampusFix breaks this cycle entirely.

---

## ⚡ The Solution

```
Student scans QR on wall  →  Files complaint in 30 sec  →  AI classifies it
        ↓                                                         ↓
Reporter gets OTP email   ←  Issue resolved + proof      ←  Technician WhatsApp'd
        ↓                                                         ↓
Confirms closure (+pts)   →  Dashboard analytics updated  →  Admin sees patterns
```

**Full accountability loop. Zero paperwork. Real-time.**

---

## 🚀 What's New in v7 — Everything Changed

> v6 was a working prototype. v7 is a deployable platform.

### 🔴 Critical Bugs Fixed (would have crashed v6)

| Bug | Impact | Fix |
|-----|--------|-----|
| `OtpToken`, `Rating`, `CoReporter` were package-private | **Compile failure** — app wouldn't even build | Made all 3 `public class` |
| `updateStatus()` called `auth.me()` in WhatsApp webhook | **NullPointerException** — webhook has no JWT, crashes silently | Actor now passed explicitly as parameter |
| `ddl-auto=validate` on empty Render database | **App refuses to start** on fresh deploy | Changed to `none` + auto schema init |
| `schema.sql` not on Java classpath | Spring couldn't find schema to run | Copied to `src/main/resources/` |
| Render gives `postgres://` URL, Spring needs `jdbc:postgresql://` | **Database connection fails** on cloud | `render.yaml` startCommand converts URL via `sed` |
| Duplicate email in seed data (Naman Agrawal × 3) | **PostgreSQL UNIQUE constraint crash** on schema import | Faculty/admin use separate institutional emails |
| Hardcoded `localhost:8080` in both HTML files | Frontend talks to offline PC when deployed | `config.js` — change one line, both pages update |
| No `IF NOT EXISTS` on any CREATE TABLE | Schema fails on re-run | Added to all 10 tables + 10 indexes |
| No `.nojekyll` file | GitHub Pages Jekyll processor silently breaks paths | Added — tells GitHub to serve files as-is |
| No `index.html` at repo root | GitHub Pages 404 on base URL | Added redirect index at root |
| Missing `spring-boot-starter-actuator` | `/actuator/health` endpoint 404 — Render health check fails | Added to `pom.xml` + config |

### 🟡 Logic & Ambiguity Bugs Fixed

| Bug | Impact | Fix |
|-----|--------|-----|
| WhatsApp webhook picked "most recent" complaint | Wrong complaint updated when staff has multiple open | Now parses `#REF` from reply e.g. `DONE #EL-1001` |
| Phone number format mismatch | `findByWhatsappNumber()` always returned empty | Normalized to 12-digit, with 10-digit fallback |
| Webhook completely unauthenticated | Anyone could POST YES/DONE and change status | Token validation header added |
| `assign()` never sent WhatsApp | Staff never notified when admin assigns complaint | WhatsApp sent to assigned staff member's number |
| `@PreAuthorize` used lowercase roles | Spring Security stores `ROLE_ADMIN` — lowercase always returned 403 | All annotations updated to uppercase |

### 🟢 New Features Added

| Feature | What it does |
|---------|-------------|
| **Repair Proof Upload** | Staff uploads photo after fixing — student sees proof before confirming closure |
| **Student Reopen** | Reporter clicks Reopen if not satisfied — max 3 times, SLA extended 24h |
| **Location Hotspot Dashboard** | Bar chart of most problem-prone locations — color coded by severity |
| **Backend AI Classifier** | Server-side keyword classification — API-only calls now also get auto-categorized |
| **Smart WhatsApp Routing** | Electrical/IT/Plumbing/Sanitation → direct WhatsApp to technician. Furniture/Infrastructure → admin notified first |
| **62 Real SGSITS Locations** | All LT, ATC, Old Building rooms + Admin Block, Library, Auditorium, Silveria |
| **QR for all 62 locations** | Print-ready, grouped by building, with block filter, custom add, copy URL |
| **`config.js`** | Single file to change API URL — deploy without touching HTML files |
| **`setup-local.bat`** | Windows double-click setup — checks Java, builds, starts server |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (GitHub Pages)                   │
│  login.html ──→ app.html (2192 lines, 7 tabs, 80+ functions)    │
│  qr-admin.html (62 SGSITS locations, print-ready QR codes)      │
│  config.js ←── change ONE LINE to switch local ↔ production     │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS + JWT Bearer token
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND (Render)                  │
│                                                                  │
│  /api/auth/**          Passwordless OTP login (3-step)          │
│  /api/complaints/**    Submit · Status · Proof · Reopen · Rate  │
│  /api/admin/users/**   Add · Bulk CSV · Deactivate              │
│  /api/notifications/** Real-time alerts                         │
│  /api/locations        62 campus locations                      │
│  /api/whatsapp/webhook YES/DONE replies from staff              │
│                                                                  │
│  AuthService ──── OTP hash + JWT (24h) + points engine         │
│  ComplaintService ─ AI classify + duplicate detect + escalate   │
│  WhatsApp ──────── WATI outbound + inbound #REF parsing         │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC (auto schema init)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    POSTGRESQL DATABASE (Render)                  │
│                                                                  │
│  12 tables · 10 indexes · 2 views (v_stats, v_leaderboard)     │
│  150 MCA 2025 students pre-seeded · 62 SGSITS locations         │
│  Schema auto-runs on first startup — no manual SQL needed        │
└─────────────────────────────────────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
     GMAIL (OTP emails)           WATI / Twilio
     Login OTP · Closure OTP      WhatsApp to technicians
     6-digit · 10 min expiry      YES/DONE reply parsing
```

---

## 🧠 Smart Features Explained

### 1. Intelligent Ticket Routing
```
User types: "Projector not working in LT 101"
              ↓
    Backend classifyCategory() scans keywords
              ↓
    Matches: "projector" → category = technical
              ↓
    AUTO_ROUTE contains "technical" → WhatsApp sent to IT staff directly
              ↓
    Staff replies: "YES #IT-1001" → status = in_progress
    Staff replies: "DONE #IT-1001" → status = resolved → OTP emailed to student
```

### 2. Priority Detection
```
"sparking wire"  →  critical  (safety risk keywords)
"water leakage"  →  high      (damage risk keywords)  
"chair broken"   →  medium    (standard)
Faculty files any complaint → auto-elevated to HIGH minimum
5+ students report same issue → auto-escalated + admin alerted
3 reminders sent → auto-escalated
```

### 3. Smart WhatsApp Routing (NEW in v7)
```
Electrical ──→ WhatsApp to electrician directly   (auto-route)
Plumbing   ──→ WhatsApp to plumber directly        (auto-route)
IT/Technical → WhatsApp to IT staff directly       (auto-route)
Sanitation ──→ WhatsApp to sanitation team          (auto-route)
                   
Furniture  ──→ Admin notified → Admin assigns manually
Infrastructure → Admin notified → Admin assigns manually
Other      ──→ Admin notified → Admin assigns manually
```

### 4. Gamification Engine
```
File a complaint          → +10 points
Attach photo              → +5 points
Enable GPS location       → +5 points
Send a reminder           → +1 point
Rate the resolution       → +5 points
Confirm closure via OTP   → +5 points
Co-report existing issue  → +5 points
Faculty complaint filed   → +5 bonus points

Badges:  Newcomer (0) → Reporter (50) → Contributor (200) → Champion (500)
```

---

## 📁 Project Structure

```
campusfix/
│
├── 🖥️  frontend/pages/
│   ├── login.html          Passwordless login — 3 step OTP flow
│   ├── app.html            Main SPA — 7 tabs, 80+ JS functions, bilingual
│   ├── qr-admin.html       QR generator — 62 SGSITS locations, print ready
│   └── config.js           ← ONLY FILE TO CHANGE when deploying
│
├── ⚙️  backend/src/main/
│   ├── java/.../
│   │   ├── CampusFixApplication.java      Entry point
│   │   ├── config/
│   │   │   └── AppConfig.java             Security + CORS + JWT filter
│   │   ├── controller/
│   │   │   ├── AuthController.java        POST /api/auth/**
│   │   │   ├── ComplaintController.java   15 endpoints — full CRUD + proof + reopen
│   │   │   ├── AdminUserController.java   User management + bulk CSV
│   │   │   ├── NotificationController.java Real-time alerts
│   │   │   ├── LocationController.java    62 campus locations + QR URLs
│   │   │   └── WhatsAppWebhookController.java  YES/DONE + #REF parsing
│   │   ├── model/Models.java              9 JPA entities in one file
│   │   ├── repository/Repositories.java   9 Spring Data repos + native queries
│   │   ├── service/
│   │   │   ├── AuthService.java           OTP hash + JWT + points
│   │   │   └── ComplaintService.java      295 lines — core business logic
│   │   └── util/JwtUtil.java              HS256 token generate/validate
│   └── resources/
│       ├── application.properties         All config via env vars
│       └── schema.sql                     Auto-runs on startup (IF NOT EXISTS)
│
├── 🗄️  database/
│   └── schema.sql          12 tables · 10 indexes · 2 views · 150 students · 62 locations
│
├── 🚀  render.yaml          One-click cloud deploy config
├── 🪟  setup-local.bat      Windows double-click local setup
├── 🚫  .nojekyll            Tells GitHub Pages to skip Jekyll processing
└── 📖  README.md            You are here
```

---

## 🗄️ Database Schema

```
users ──────────────────┐
  enrollmentId (PK)     │
  name, email (unique)  │
  role, department      │        complaints ──────────────────────────┐
  whatsappNumber        │          complaintNo (#EL-1001)             │
  points, badge         │          title, description, category       │
  isActive, lastLogin   │          priority (critical/high/medium/low)│
                        │          status (registered→forwarded→      │
otp_tokens              │                  in_progress→resolved→      │
  tokenHash (bcrypt)    │                  closed/rejected)           │
  expiresAt, purpose    │          photoUrl, proofUrl ← NEW v7        │
                        │          gpsLat, gpsLng, gpsVerified        │
notifications           │          aiCategory, aiConfidence           │
  userId, complaintId   │          reopenCount ← NEW v7               │
  titleEn, titleHi      │          autoRouted ← NEW v7                │
  messageEn, messageHi  │          isEscalated, importanceScore       │
  isRead                │          slaDeadline, slaBreached            │
                        │                                             │
locations               │        complaint_logs                       │
  62 SGSITS rooms ←NEW  │          action, actionHi (bilingual)       │
  block, floor, zone    │          performedBy, note                  │
  lat, lng              │                                             │
                        │        co_reporters                         │
departments             │          complaintId + userId               │
  6 categories          │                                             │
  slaHours per dept     │        comments · ratings                   │
                        │                                             │
views:                  └────────────────────────────────────────────┘
  v_stats      → total, in_progress, escalated, SLA breached, avg resolution hours
  v_leaderboard → top 20 students ranked by points + badge
```

---

## 🏃 Run Locally (Windows 10 — ~20 min)

### Prerequisites
| Tool | Version | Download |
|------|---------|----------|
| JDK 17 (Temurin) | 17 LTS | [adoptium.net](https://adoptium.net) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org) |
| PostgreSQL | 15 or 16 | [postgresql.org](https://www.postgresql.org/download/windows/) |
| VS Code + Live Server | Latest | [code.visualstudio.com](https://code.visualstudio.com) |

### Setup

**1. Database**
```sql
-- Run in pgAdmin Query Tool
CREATE USER campusfix_user WITH PASSWORD 'campusfix_pass';
CREATE DATABASE campusfix_db OWNER campusfix_user;
GRANT ALL PRIVILEGES ON DATABASE campusfix_db TO campusfix_user;
```
Then open `database/schema.sql` in pgAdmin Query Tool and run it.

**2. Backend**
```bash
# Option A — double-click (easiest)
setup-local.bat

# Option B — manual CMD
cd backend
mvn clean package -DskipTests
java -Xms128m -Xmx400m -jar target\campusfix-1.0.0.jar
```
Backend live at → `http://localhost:8080`

**3. Frontend**
VS Code → open `frontend/pages/` → right-click `login.html` → **Open with Live Server**
Opens at → `http://127.0.0.1:5500/login.html` ✅

**4. Test Login**
| Enrollment ID | Role | Email |
|--------------|------|-------|
| `0801CA251084` | Student | namanagrawal0402@gmail.com |
| `FAC-MCA-NAM` | Faculty | naman.faculty@sgsits.ac.in |
| `ADMIN-NAM` | Admin | naman.admin@sgsits.ac.in |

> **Dev tip:** No Gmail setup needed locally. If `MAIL_PASSWORD` not set, OTP prints to console log.

---

## ☁️ Deploy Free Online (GitHub Pages + Render — ~20 min)

| Layer | Platform | Cost |
|-------|----------|------|
| Frontend | GitHub Pages | Free forever |
| Backend | Render.com (Web Service) | Free — 750 hrs/month |
| Database | Render.com (PostgreSQL) | Free — 1GB, 90 days |

### One change to deploy

Edit `frontend/pages/config.js` — change ONE line:
```javascript
// Local
window.CAMPUSFIX_API_URL = 'http://localhost:8080/api';

// Production — replace with your Render URL
window.CAMPUSFIX_API_URL = 'https://campusfix-backend.onrender.com/api';
```

Both `login.html` and `app.html` read from this file automatically.

> ⚠️ **Before demos:** Open `https://campusfix-backend.onrender.com/actuator/health` 5 minutes early. Free tier sleeps after 15 min idle — first wake takes ~30 sec.

---

## 🔑 Environment Variables

| Variable | Local default | Production |
|----------|--------------|------------|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/campusfix_db | Auto-set by Render |
| `DB_USER` | campusfix_user | From Render DB |
| `DB_PASS` | campusfix_pass | From Render DB |
| `JWT_SECRET` | (default 61-char key) | **Must change — set in Render dashboard** |
| `MAIL_USER` | campusfix.sgsits@gmail.com | Your Gmail |
| `MAIL_PASSWORD` | (prints OTP to console) | Gmail App Password (16 chars) |
| `FRONTEND_URL` | http://localhost:5500 | Your GitHub Pages URL |
| `WATI_API_URL` | (dev mode — logs to console) | Your WATI server URL |
| `WATI_TOKEN` | (dev mode) | Your WATI Bearer token |
| `WA_ELECTRICAL` | 919876543210 | Real electrician number |
| `WA_PLUMBING` | 919876543211 | Real plumber number |
| `WA_TECHNICAL` | 919876543212 | Real IT staff number |
| `WA_SANITATION` | 919876543214 | Real sanitation number |

---

## 🔧 Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| OTP not received | No Gmail config | Check console — OTP printed there in dev mode |
| Enrollment not found | Wrong ID format | IDs are uppercase e.g. `0801CA251084` |
| Frontend shows Network Error | Backend sleeping (Render free tier) | Wait 30 sec — or open health URL first |
| CORS error in browser | `FRONTEND_URL` env var mismatch | Must exactly match your GitHub Pages URL |
| Backend won't start on Render | Wrong DB URL format | Render gives `postgres://` — our `render.yaml` converts it automatically |
| 404 on GitHub Pages | Files uploaded in wrong folder structure | Use `campusfix_GITHUB_UPLOAD.zip` — files are at root level |
| Schema errors on first run | Tables already exist | Safe — all CREATE statements have `IF NOT EXISTS` |
| WhatsApp YES/DONE not working | Staff phone not in DB | Add whatsappNumber in Admin → Users tab |

---

## 👨‍💻 Built By

**Naman Agrawal** — MCA 2025 Batch, SGSITS Indore
Enrollment: `0801CA251084`

> *CampusFix is a real-world deployable solution — not just a college project.*
> *Every feature was designed around actual campus workflow, with practical constraints like non-technical technicians, slow PCs, and limited internet in mind.*

---

<div align="center">

**SGSITS Indore · Vallabh Nagar · Indore 452003 · Madhya Pradesh**

*Digitizing campus maintenance, one complaint at a time.*

</div>
