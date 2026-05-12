-- ════════════════════════════════════════════════════════════════
-- CampusFix — SGSITS Indore
-- PostgreSQL Schema v3.0
-- Architecture: Local Shadow Table (Zero ERP API cost)
-- Auth: Enrollment ID + DOB + Email OTP (passwordless)
-- Run: psql -U campusfix_user -d campusfix_db -f schema.sql
-- ════════════════════════════════════════════════════════════════

SET client_encoding = 'UTF8';
SET timezone = 'Asia/Kolkata';

-- ── DROP EXISTING ─────────────────────────────────────────────
DROP TABLE IF EXISTS ratings         CASCADE;
DROP TABLE IF EXISTS notifications   CASCADE;
DROP TABLE IF EXISTS complaint_logs  CASCADE;
DROP TABLE IF EXISTS comments        CASCADE;
DROP TABLE IF EXISTS co_reporters    CASCADE;
DROP TABLE IF EXISTS complaints      CASCADE;
DROP TABLE IF EXISTS otp_tokens      CASCADE;
DROP TABLE IF EXISTS user_sessions   CASCADE;
DROP TABLE IF EXISTS users           CASCADE;
DROP TABLE IF EXISTS departments     CASCADE;
DROP TABLE IF EXISTS locations       CASCADE;

-- ── AUTO UPDATED_AT TRIGGER ───────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

-- ════════════════════════════════════════════════════════════════
-- LOCATIONS  (20 SGSITS campus spots)
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS locations (
    id          SERIAL PRIMARY KEY,
    name_en     VARCHAR(120) NOT NULL,
    name_hi     VARCHAR(160),
    block       VARCHAR(60),
    floor       VARCHAR(20),
    zone        VARCHAR(30),        -- Academic | Hostel | Sports | Admin
    latitude    NUMERIC(10,7) DEFAULT 22.7196822,
    longitude   NUMERIC(10,7) DEFAULT 75.8577173
);

INSERT INTO locations(name_en,name_hi,block,floor,zone,latitude,longitude) VALUES
-- ── LT Building — Ground Floor ──────────────────────────────────
('LT Building — Room 001','LT भवन — कक्ष 001','LT Building','Ground','Academic',22.7196822,75.8577173),
('LT Building — Room 002','LT भवन — कक्ष 002','LT Building','Ground','Academic',22.7196900,75.8577200),
-- ── LT Building — First Floor ────────────────────────────────────
('LT Building — Room 101','LT भवन — कक्ष 101','LT Building','1st','Academic',22.7197000,75.8577300),
('LT Building — Room 102','LT भवन — कक्ष 102','LT Building','1st','Academic',22.7197050,75.8577350),
-- ── LT Building — Second Floor ───────────────────────────────────
('LT Building — Room 201','LT भवन — कक्ष 201','LT Building','2nd','Academic',22.7197100,75.8577400),
('LT Building — Room 202','LT भवन — कक्ष 202','LT Building','2nd','Academic',22.7197150,75.8577450),
-- ── LT Building — Third Floor ────────────────────────────────────
('LT Building — Room 301','LT भवन — कक्ष 301','LT Building','3rd','Academic',22.7197200,75.8577500),
('LT Building — Room 302','LT भवन — कक्ष 302','LT Building','3rd','Academic',22.7197250,75.8577550),
-- ── LT Building — Fourth Floor ───────────────────────────────────
('LT Building — Room 401','LT भवन — कक्ष 401','LT Building','4th','Academic',22.7197300,75.8577600),
('LT Building — Room 402','LT भवन — कक्ष 402','LT Building','4th','Academic',22.7197350,75.8577650),
-- ── ATC Building — Ground Floor (Rooms 101–108) ──────────────────
('ATC Building — Room 101','ATC भवन — कक्ष 101','ATC Building','Ground','Academic',22.7194500,75.8575000),
('ATC Building — Room 102','ATC भवन — कक्ष 102','ATC Building','Ground','Academic',22.7194550,75.8575050),
('ATC Building — Room 103','ATC भवन — कक्ष 103','ATC Building','Ground','Academic',22.7194600,75.8575100),
('ATC Building — Room 104','ATC भवन — कक्ष 104','ATC Building','Ground','Academic',22.7194650,75.8575150),
('ATC Building — Room 105','ATC भवन — कक्ष 105','ATC Building','Ground','Academic',22.7194700,75.8575200),
('ATC Building — Room 106','ATC भवन — कक्ष 106','ATC Building','Ground','Academic',22.7194750,75.8575250),
('ATC Building — Room 107','ATC भवन — कक्ष 107','ATC Building','Ground','Academic',22.7194800,75.8575300),
('ATC Building — Room 108','ATC भवन — कक्ष 108','ATC Building','Ground','Academic',22.7194850,75.8575350),
-- ── ATC Building — First Floor (Rooms 201–208) ───────────────────
('ATC Building — Room 201','ATC भवन — कक्ष 201','ATC Building','1st','Academic',22.7194900,75.8575400),
('ATC Building — Room 202','ATC भवन — कक्ष 202','ATC Building','1st','Academic',22.7194950,75.8575450),
('ATC Building — Room 203','ATC भवन — कक्ष 203','ATC Building','1st','Academic',22.7195000,75.8575500),
('ATC Building — Room 204','ATC भवन — कक्ष 204','ATC Building','1st','Academic',22.7195050,75.8575550),
('ATC Building — Room 205','ATC भवन — कक्ष 205','ATC Building','1st','Academic',22.7195100,75.8575600),
('ATC Building — Room 206','ATC भवन — कक्ष 206','ATC Building','1st','Academic',22.7195150,75.8575650),
('ATC Building — Room 207','ATC भवन — कक्ष 207','ATC Building','1st','Academic',22.7195200,75.8575700),
('ATC Building — Room 208','ATC भवन — कक्ष 208','ATC Building','1st','Academic',22.7195250,75.8575750),
-- ── ATC Building — Second Floor (Rooms 301–308) ──────────────────
('ATC Building — Room 301','ATC भवन — कक्ष 301','ATC Building','2nd','Academic',22.7195300,75.8575800),
('ATC Building — Room 302','ATC भवन — कक्ष 302','ATC Building','2nd','Academic',22.7195350,75.8575850),
('ATC Building — Room 303','ATC भवन — कक्ष 303','ATC Building','2nd','Academic',22.7195400,75.8575900),
('ATC Building — Room 304','ATC भवन — कक्ष 304','ATC Building','2nd','Academic',22.7195450,75.8575950),
('ATC Building — Room 305','ATC भवन — कक्ष 305','ATC Building','2nd','Academic',22.7195500,75.8576000),
('ATC Building — Room 306','ATC भवन — कक्ष 306','ATC Building','2nd','Academic',22.7195550,75.8576050),
('ATC Building — Room 307','ATC भवन — कक्ष 307','ATC Building','2nd','Academic',22.7195600,75.8576100),
('ATC Building — Room 308','ATC भवन — कक्ष 308','ATC Building','2nd','Academic',22.7195650,75.8576150),
-- ── Special / Standalone Locations ──────────────────────────────
('Central Library','केंद्रीय पुस्तकालय','Library','Ground','Academic',22.7198000,75.8580000),
('Advanced Computing Lab','उन्नत कंप्यूटिंग लैब','ATC Building','Ground','Academic',22.7194000,75.8574500),
-- ── Old Building — Second Floor (Rooms 201–220) ──────────────────
('Old Building — Room 201','पुरानी इमारत — कक्ष 201','Old Building','2nd','Academic',22.7192000,75.8574000),
('Old Building — Room 202','पुरानी इमारत — कक्ष 202','Old Building','2nd','Academic',22.7192050,75.8574050),
('Old Building — Room 203','पुरानी इमारत — कक्ष 203','Old Building','2nd','Academic',22.7192100,75.8574100),
('Old Building — Room 204','पुरानी इमारत — कक्ष 204','Old Building','2nd','Academic',22.7192150,75.8574150),
('Old Building — Room 205','पुरानी इमारत — कक्ष 205','Old Building','2nd','Academic',22.7192200,75.8574200),
('Old Building — Room 206','पुरानी इमारत — कक्ष 206','Old Building','2nd','Academic',22.7192250,75.8574250),
('Old Building — Room 207','पुरानी इमारत — कक्ष 207','Old Building','2nd','Academic',22.7192300,75.8574300),
('Old Building — Room 208','पुरानी इमारत — कक्ष 208','Old Building','2nd','Academic',22.7192350,75.8574350),
('Old Building — Room 209','पुरानी इमारत — कक्ष 209','Old Building','2nd','Academic',22.7192400,75.8574400),
('Old Building — Room 210','पुरानी इमारत — कक्ष 210','Old Building','2nd','Academic',22.7192450,75.8574450),
('Old Building — Room 211','पुरानी इमारत — कक्ष 211','Old Building','2nd','Academic',22.7192500,75.8574500),
('Old Building — Room 212','पुरानी इमारत — कक्ष 212','Old Building','2nd','Academic',22.7192550,75.8574550),
('Old Building — Room 213','पुरानी इमारत — कक्ष 213','Old Building','2nd','Academic',22.7192600,75.8574600),
('Old Building — Room 214','पुरानी इमारत — कक्ष 214','Old Building','2nd','Academic',22.7192650,75.8574650),
('Old Building — Room 215','पुरानी इमारत — कक्ष 215','Old Building','2nd','Academic',22.7192700,75.8574700),
('Old Building — Room 216','पुरानी इमारत — कक्ष 216','Old Building','2nd','Academic',22.7192750,75.8574750),
('Old Building — Room 217','पुरानी इमारत — कक्ष 217','Old Building','2nd','Academic',22.7192800,75.8574800),
('Old Building — Room 218','पुरानी इमारत — कक्ष 218','Old Building','2nd','Academic',22.7192850,75.8574850),
('Old Building — Room 219','पुरानी इमारत — कक्ष 219','Old Building','2nd','Academic',22.7192900,75.8574900),
('Old Building — Room 220','पुरानी इमारत — कक्ष 220','Old Building','2nd','Academic',22.7192950,75.8574950),
-- ── Campus Facilities ────────────────────────────────────────────
('Golden Jubilee Auditorium','गोल्डन जुबिली ऑडिटोरियम','Auditorium','Ground','Academic',22.7200000,75.8572000),
('Silveria (Cafeteria)','सिल्वेरिया (कैफेटेरिया)','Silveria','Ground','Academic',22.7201000,75.8573000),
('Director''s Building','निदेशक भवन','Director Building','Ground','Admin',22.7198500,75.8578500),
('Administrative Block','प्रशासनिक भवन','Admin Block','Ground','Admin',22.7197500,75.8578000),
('Student Section','छात्र अनुभाग','Admin Block','Ground','Admin',22.7197600,75.8578100),
('Examination Cell','परीक्षा कक्ष','Admin Block','Ground','Admin',22.7197700,75.8578200);

-- ════════════════════════════════════════════════════════════════
-- DEPARTMENTS
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS departments (
    id              SERIAL PRIMARY KEY,
    name_en         VARCHAR(120) NOT NULL,
    name_hi         VARCHAR(160),
    category_key    VARCHAR(50)  UNIQUE NOT NULL,
    head_name       VARCHAR(100),
    head_email      VARCHAR(120),
    contact_phone   VARCHAR(20),
    sla_hours       INTEGER DEFAULT 48   -- target resolution hours
);

INSERT INTO departments(name_en,name_hi,category_key,head_name,head_email,contact_phone,sla_hours) VALUES
('Electrical Maintenance','विद्युत अनुरक्षण','electrical','Er. Ramesh Gupta','electrical@sgsits.ac.in','0731-2570831',8),
('Civil & Plumbing','सिविल एवं नलसाज़ी','plumbing','Er. Sunil Verma','civil@sgsits.ac.in','0731-2570832',12),
('IT Cell','आईटी सेल','technical','Shri. Ajay Sharma','itcell@sgsits.ac.in','0731-2570833',24),
('Infrastructure & Stores','अवसंरचना एवं भंडार','furniture','Shri. Rakesh Patel','stores@sgsits.ac.in','0731-2570834',48),
('Housekeeping','गृह रक्षण','sanitation','Smt. Anita Joshi','housekeeping@sgsits.ac.in','0731-2570835',24),
('Administration','प्रशासन','other','Shri. Dinesh Malviya','admin@sgsits.ac.in','0731-2570836',72);

-- ════════════════════════════════════════════════════════════════
-- USERS  — Shadow Table  (Local copy, never touches live ERP)
-- Auth: Enrollment ID + DOB + Email OTP  (passwordless)
-- DOB stored as BCrypt hash for privacy
-- Data ingested via:
--   a) Bulk CSV import at semester start
--   b) Admin manual addition for faculty/staff
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS users (
    id               SERIAL PRIMARY KEY,
    -- Identity (from ERP CSV export — read-only shadow)
    enrollment_id    VARCHAR(60)  UNIQUE NOT NULL,
    name             VARCHAR(120) NOT NULL,
    email            VARCHAR(140) UNIQUE NOT NULL,
    -- dob_hash removed in v4: auth is enrollment_id + email OTP only. No DOB stored.
    role             VARCHAR(20)  NOT NULL CHECK (role IN ('student','faculty','staff','admin','super_admin')),
    department       VARCHAR(100),
    branch           VARCHAR(50),
    year_section     VARCHAR(20),
    phone            VARCHAR(20),
    whatsapp_number  VARCHAR(20),          -- Staff WhatsApp for direct notifications
    -- Gamification
    points           INTEGER DEFAULT 0,
    streak_days      INTEGER DEFAULT 0,
    badge            VARCHAR(50)  DEFAULT 'Newcomer',
    -- Prefs
    preferred_lang   VARCHAR(5)   DEFAULT 'en',
    is_active        BOOLEAN      DEFAULT TRUE,
    -- Audit
    created_at       TIMESTAMPTZ  DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  DEFAULT NOW(),
    last_login       TIMESTAMPTZ,
    -- Data source
    data_source      VARCHAR(30)  DEFAULT 'csv_import'  -- csv_import | admin_manual
);
CREATE TRIGGER users_upd BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ════════════════════════════════════════════════════════════════
-- OTP TOKENS  (Email OTP for passwordless login)
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS otp_tokens (
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users(id) ON DELETE CASCADE,
    email        VARCHAR(140) NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,   -- BCrypt(6-digit OTP)
    purpose      VARCHAR(30)  DEFAULT 'login',  -- login | closure
    expires_at   TIMESTAMPTZ  NOT NULL,
    used         BOOLEAN      DEFAULT FALSE,
    created_at   TIMESTAMPTZ  DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_otp_email ON otp_tokens(email, used, expires_at);

-- ════════════════════════════════════════════════════════════════
-- COMPLAINTS
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS complaints (
    id                  SERIAL PRIMARY KEY,
    complaint_no        VARCHAR(20)  UNIQUE NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT         NOT NULL,
    category            VARCHAR(50)  NOT NULL CHECK (category IN ('electrical','plumbing','technical','furniture','sanitation','other')),
    subcategory         VARCHAR(80),
    priority            VARCHAR(20)  NOT NULL DEFAULT 'medium' CHECK (priority IN ('low','medium','high','critical')),
    status              VARCHAR(30)  NOT NULL DEFAULT 'registered'
                        CHECK (status IN ('registered','forwarded','in_progress','resolved','closed','rejected')),
    -- Location
    location_id         INTEGER REFERENCES locations(id),
    location_text       VARCHAR(255),
    gps_lat             NUMERIC(10,7),
    gps_lng             NUMERIC(10,7),
    gps_verified        BOOLEAN      DEFAULT FALSE,
    -- Media & AI
    photo_url           VARCHAR(500),
    ai_category         VARCHAR(80),
    ai_confidence       NUMERIC(5,2),
    -- Ownership
    reporter_id         INTEGER REFERENCES users(id) ON DELETE SET NULL,
    assigned_to         INTEGER REFERENCES users(id) ON DELETE SET NULL,
    department_id       INTEGER REFERENCES departments(id),
    -- Scoring
    importance_score    INTEGER      DEFAULT 0 CHECK (importance_score BETWEEN 0 AND 100),
    reporter_count      INTEGER      DEFAULT 1,
    -- Escalation
    is_escalated        BOOLEAN      DEFAULT FALSE,
    escalated_at        TIMESTAMPTZ,
    escalation_reason   TEXT,
    -- CM-Helpline: Reminder tracking
    remind_count        INTEGER      DEFAULT 0,
    last_reminded_at    TIMESTAMPTZ,
    -- CM-Helpline: OTP-based closure (only reporter can close)
    closure_otp_hash    VARCHAR(255),
    closure_otp_sent_at TIMESTAMPTZ,
    closure_confirmed   BOOLEAN      DEFAULT FALSE,
    -- Resolution
    rejection_reason    TEXT,
    resolved_at         TIMESTAMPTZ,
    -- SLA
    sla_deadline        TIMESTAMPTZ,
    sla_breached        BOOLEAN      DEFAULT FALSE,
    -- Repair proof (uploaded by staff/admin after fix)
    proof_url           VARCHAR(500),
    proof_uploaded_at   TIMESTAMPTZ,
    -- Student reopen
    reopen_count        INTEGER      DEFAULT 0,
    -- Routing: TRUE = auto-routed directly to technician via WhatsApp
    --          FALSE = goes to admin for manual routing
    auto_routed         BOOLEAN      DEFAULT FALSE,
    -- Timestamps
    created_at          TIMESTAMPTZ  DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  DEFAULT NOW()
);
CREATE TRIGGER complaints_upd BEFORE UPDATE ON complaints FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_comp_status   ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_comp_cat      ON complaints(category);
CREATE INDEX IF NOT EXISTS idx_comp_reporter ON complaints(reporter_id);
CREATE INDEX IF NOT EXISTS idx_comp_score    ON complaints(importance_score DESC);
CREATE INDEX IF NOT EXISTS idx_comp_created  ON complaints(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comp_escalated ON complaints(is_escalated) WHERE is_escalated=TRUE;

-- ════════════════════════════════════════════════════════════════
-- CO-REPORTERS  (multiple people on same complaint)
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS co_reporters (
    id           SERIAL PRIMARY KEY,
    complaint_id INTEGER REFERENCES complaints(id) ON DELETE CASCADE,
    user_id      INTEGER REFERENCES users(id)      ON DELETE CASCADE,
    reported_at  TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(complaint_id, user_id)
);

-- ════════════════════════════════════════════════════════════════
-- COMPLAINT LOGS  (full audit trail / timeline)
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS complaint_logs (
    id           SERIAL PRIMARY KEY,
    complaint_id INTEGER REFERENCES complaints(id) ON DELETE CASCADE,
    action       VARCHAR(120) NOT NULL,
    action_hi    VARCHAR(160),
    performed_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    note         TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_logs_comp ON complaint_logs(complaint_id);

-- ════════════════════════════════════════════════════════════════
-- COMMENTS
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS comments (
    id           SERIAL PRIMARY KEY,
    complaint_id INTEGER REFERENCES complaints(id) ON DELETE CASCADE,
    user_id      INTEGER REFERENCES users(id)      ON DELETE SET NULL,
    message      TEXT    NOT NULL,
    is_admin     BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comments_comp ON comments(complaint_id);

-- ════════════════════════════════════════════════════════════════
-- NOTIFICATIONS
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS notifications (
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users(id)      ON DELETE CASCADE,
    complaint_id INTEGER REFERENCES complaints(id) ON DELETE SET NULL,
    type         VARCHAR(60)  NOT NULL,
    title_en     VARCHAR(200),
    title_hi     VARCHAR(200),
    message_en   TEXT,
    message_hi   TEXT,
    is_read      BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notif_user ON notifications(user_id, is_read);

-- ════════════════════════════════════════════════════════════════
-- RATINGS
-- ════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS ratings (
    id           SERIAL PRIMARY KEY,
    complaint_id INTEGER REFERENCES complaints(id) ON DELETE CASCADE,
    user_id      INTEGER REFERENCES users(id)      ON DELETE SET NULL,
    stars        INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment      TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(complaint_id, user_id)
);

-- ════════════════════════════════════════════════════════════════
-- USERS SEED DATA — MCA First Year 2025 Batch (SGSITS)
-- Auth: enrollment_id + email OTP only. No DOB stored.
-- Source: Admission List & Enrollment Records (150 students)
-- Import command: psql -U campusfix_user -d campusfix_db -f schema.sql
-- ════════════════════════════════════════════════════════════════

INSERT INTO users(enrollment_id,name,email,role,department,branch,year_section,points,streak_days,badge,data_source) VALUES
-- ── MCA 2025 Batch ──────────────────────────────────────────────
('0801CA251001','AAKANKSHA BRIJWANI','aakankshabrijwani@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251002','AARADHY PATHAK','aaradhypathak03@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251003','ABDUL REHMAN GORI','khanrehman28229@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251004','ABDUL TAYYAB MAKSIWALA','abdulmaksi@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251005','ABHISHEK CHANDRAWAT','abhishekchandrawat03@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251006','ABHISHEK MEENA','abhishekrbmeena@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251007','ABHISHEK NAGAR','abhisheknagar2030@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251008','ABHISHEK PRAJAPATI','abhishekprajapati1823@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251009','ADARSH DWIVEDI','dwivediadarsh487@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251010','ADITI JAGTAP','aditipraveenjagtap10671@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251011','ADITI RATHORE','rathoraditi1811@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251012','AJAY MEWADA','ajaymewada@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251013','AKSHAT VIJAYVARGIYA','akshatvijaywargiya963@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251014','AMAN JARYA','amanjarya03@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251015','AMAN LAHARPURE','amansahu2537@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251016','AMAN WANKHEDE','amanwankhede572@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251017','AMISHA KARARA','kararaamisha413@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251018','ANKIT CHOUDHARY','ankitchoudharjiat@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251019','ANKIT JAT','ankujat263@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251020','ANKIT KUSHWAHA','ankitkumarkushwaha@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251021','ANSHIKA TIWARI','anshikatiwari445@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251022','ANSHUL KILORIYA','anshulkiloriya@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251023','ANUSHKA SINGH CHOUHAN','anushkasinghchouhan4@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251024','ARCHI JAIN','archijain220@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251025','ARPIT JAYSHWAL','anshopjaiswal@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251026','ARYAN RATHORE','aryanrathore598@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251027','ASTHA SONI','aashasoni807@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251028','AVANTIKA UPASE','avani.upase@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251029','AYUSH BILWADIYA','aashusbilwadiya@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251030','AYUSH KUMAR','singhaashu392@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251031','AYUSH SUPATH','ayushsapath1829@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251032','AYUSHI NEMA','ayushinema2004@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251033','BHAGYASHRI CHATAKWAR','bhagyashrichatakwar@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251034','BHANU PRAKASH SHARMA','bs0850352@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251035','BHAVESH RATHORE','bhaveshrathore442@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251036','BHAWESH PANWAR','panwarbhawesh1112@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251037','BHUSHAN KHADE','khadebhaiya@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251038','CHANCHLESH AHARWAR','aharwar007@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251039','CHANDVI RANIWAL','chandviraniwal@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251040','CHIRAG RANE','chiragrane61@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251041','DEEKSHA PAL','deekshaa.pal10@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251042','DESHANT BRAHMAN','brahmandeshant@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251043','DEVYANI PATIDAR','devyanipatidar1504@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251044','DHANSHREE NIKUM','nikumdhanshree@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251045','DHARAMPREET SINGH KALRA','dharampreetsinghkalra@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251046','DHEERENDRA MISHRA','dheerendramishra64646@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251047','DIPESH','dhakaddipesh726@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251048','GARIMA SINGNATH','gsinghath14@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251049','GEETANJALI','geet061003@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251050','HARSH PAWAR','pawar.harsh.363@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251051','HARSH SONI','harshsoni07612@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251052','HARSH YADAV','hy844247@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251053','HUSAIN KHANDWAWALA','hkhandwal3@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251054','ISHIKA PATIDAR','ip5402213@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251055','JAHANVI GUPTA','jahanviguptav@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251056','JATIN BANSIYA','jatinbansiya@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251057','JIVISHA KUSHWAH','jkjivisha@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251058','KALPANA VERMA','kv70692@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251059','KANAK SHARMA','sharmakanak2905@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251060','KASAK MALVIYA','kasakmalviya384@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251061','KASHISH GIRI GOSWAMI','kashishgaswani2310@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251062','KASHISH NAGORI','kashishmaheshwari31@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251063','KAVISH MOTWANI','kaaaavish@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251064','KAVYA MANE','kavyamane11@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251065','KHILESHWAR MAGARDE','khileshwarmagarde@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251066','KHUSHBOO WASKALE','khushboowaskale3@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251067','KHUSHI MAHAJAN','khushi.mahajan26@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251068','KHUSHI VERMA','khushiv407@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251069','KOMAL SHARMA','komalsharma35387@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251070','LALIT SIMARIYA','laitsimariya14@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251071','LOKESH CHOUDHARY','lokeshcschoudhary4048@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251072','MAHIMA DESHMUKH','deshmukhmahima40@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251073','MANOJ BHARTI','manojbhart8080@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251074','MAYUR JAGTAP','mayurtejraj@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251075','MISHIKA PATIDAR','patidarmishi3004@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251076','MITESH MAVE','kumawatmitesh15@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251077','MOHAMMAD HASSAN KHAN','mohammadhassankhan03@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251078','MOHAMMED RATLAM WALA','mohammedratlam53@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251079','MOHIT GOUR','mohitgaur3020@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251080','MOHIT PAL','palmohit59342@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251081','MONIKA','manikapatidar.com@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251082','MUFADDAL DHARIWALA','mufaddaldhari08@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251083','MUSKAN SHARMA','muskansharmarajesh@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251084','NAMAN AGRAWAL','namanagrawal0402@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251085','NANDANI ASALKAR','nandaniasalkar2910@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251086','NANDANI BACHHANE','bachhanenandani@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251087','NAVDEEP KUSHWAH','navdeepkushwah55@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251088','NEETU SAHU','neetushahu072003@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251091','NITISH KUMAR','niteshkumar17481@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251092','NITYA DESHMUKH','nityadeshmukh016@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251094','PARTH JOSHI','parthjoshi0789@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251095','PIYUSH CHIROTE','piyushchirote343@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251096','POONAM GHOSWAL','poonamprajapat2306@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251097','POORNIMA YADAV','poornimayadav866@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251098','PRAGYA TIWARI','pragyatiwari26052001@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251099','PRANAV PRATAP SINGH SISODIYA','ppssisodiya@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251100','PRANAY PARMAR','pranayparmar2005@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251101','PRANJALI PALIWAL','pranjalipaliwal@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251102','PRASHANT BHUTESHWAR','prashantbhut168@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251103','PRASHANTI MEHRA','mehraprashanti02@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251104','PRATIK','pratikmakwana16@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251105','PRATIMA PATIDAR','pratimapatidar900@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251106','PRIYANSHI KUSHWAH','priyanshikush2-14@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251107','PUSHPRAJ KUSHWAHA','lucky482005kush@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251108','RACHANA KHEDE','rkhede2809@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251109','RAGHAV PATIDAR','raghavp0505@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251111','RAJ GUPTA','29.gupta.raj@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251112','RAJDEEP SINGH RATHORE','rathodrajdeepsingh12@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251113','RAMDEV THAPAK','ramthapak12@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251115','RISHABH PATEL','rishabhpatelpip143@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251116','RISHITA SHAH','rishitashah6263@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251117','RITIK GOVIND RAO','raoritik92@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251118','RITIK PARTE','ritikparte458@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251119','RIYA JOSHI','riyajoshi42230@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251120','ROHAN YADAV','rohan2004y@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251121','SABA NORIN PATEL','sabanorinpatel@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251122','SALONI PATIDAR','patidarsaloni780@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251123','SAMARTH THOMBRE','samarththombre00@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251124','SHIVANI VERMA','shivaniverma33086@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251125','SHIVANSHU GAUTAM','shivanshusingh4476@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251126','SHREE FALKE','kurashaarmy@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251127','SHRUTI','sj8808849@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251128','SIDDHI PANDEY','siddhipandey9399@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251129','SIFTY KAUR GANDHI','siftykaur12@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251130','SRAJAN TIWARI','srajantiwari0456@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251131','STUTI MAHAJAN','stutimahajan13@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251132','SUDARSHNA SHENGOKAR','sudarshanashengokar@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251133','SUDHANSHU SONI','sudhanshusoni2002@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251134','SUHANI JAT','suhanijatt19@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251135','SUMIT KHEDE','sumitkhede007@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251136','SUNIL JOSHI','sjs4s4s44@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251137','SUNITA MORYA','sm7117843@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251138','SURBHI LAHRI','surbhilahri830@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251139','TANISH JAIN','tanishjain0502@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251140','TANISHA MANDHANI','tanishamandhani03@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251141','TANMAY MISHRA','tanmaym55h@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251143','UTTAM CARPENTER','vtcarpenter030705@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251144','VAIBHAV PANCHAL','vaibhavpanchal029@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251145','VAIDEHI PATIDAR','vaidehi20patidar@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251146','VAISHNAVI PANDEY','vaishnavipandey052@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251147','VANSHIKA PRAJAPAT','vanshikapraj2004@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251148','VIKRAM SINGH','vikaramsingh271102@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251149','VISHAL NARWARIYA','vishalnarwariya4934@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251150','VISHNU BHURIYA','vbhuriya068@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251151','YASHBARDHAN SINGH','yashbardhansinghsikarwar@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251152','YASHDEEP','yashdipdhakad@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251153','YASHIKA SHARMA','yashikasharma15.11.24@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251154','YATHARTH MAKWANA','yatharthmakwana2003@icloud.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
('0801CA251155','ZAHARA RANGWALA','rangwalazahara8@gmail.com','student','MCA','MCA','1st',0,0,'Newcomer','csv_import'),
-- ⚠️  NOTE: Enrollment IDs 089,090,093,110,114,142 absent from admission records — add when available.
-- ── Faculty / Staff / Admin (initial test phase — Naman Agrawal only) ──────────
-- NOTE: Faculty/Admin use separate emails to avoid UNIQUE constraint violation.
-- In production, replace with real institutional email addresses.
('FAC-MCA-NAM','NAMAN AGRAWAL (Faculty)','naman.faculty@sgsits.ac.in','faculty','MCA',NULL,NULL,0,0,NULL,'admin_manual'),
('ADMIN-NAM','NAMAN AGRAWAL (Admin)','naman.admin@sgsits.ac.in','admin','MCA',NULL,NULL,0,0,NULL,'admin_manual');

-- Sample complaints
INSERT INTO complaints(complaint_no,title,description,category,priority,status,location_id,location_text,gps_lat,gps_lng,reporter_id,department_id,importance_score,reporter_count,is_escalated,created_at,sla_deadline) VALUES
('#EL-1001','Water Logging — Main Canteen','Severe water logging at canteen entrance. Drain blocked 48h. Slipping hazard — 2 students fell.','plumbing','critical','in_progress',8,'Main Canteen',22.7200,75.8572,84,2,86,7,true,NOW()-INTERVAL '2 days',NOW()-INTERVAL '6 hours'),
('#EL-1002','Short Circuit — Room 201','Sparks from switchboard Room 201. MCB trips every 15 min. Fire risk.','electrical','critical','registered',2,'Room 201, 1st Floor',22.7197,75.8577,111,1,72,3,false,NOW()-INTERVAL '1 day',NOW()+INTERVAL '2 hours'),
('#IT-1003','WiFi Down — Central Library','All 6 APs offline. 200+ students affected. Exams in 10 days.','technical','high','in_progress',7,'Central Library',22.7198,75.8580,50,3,61,4,false,NOW()-INTERVAL '3 days',NOW()-INTERVAL '1 day'),
('#FU-1004','10 Broken Chairs — Room 101','Detached backrests. Students standing during 2-hour lectures for 5 days.','furniture','medium','registered',1,'Room 101, Ground Floor',22.7196,75.8577,18,4,38,3,false,NOW()-INTERVAL '5 days',NOW()+INTERVAL '2 days'),
('#EL-1005','Fan Dead — Room 204','Fan motor stopped. 40 students, 38°C. Practicals disrupted.','electrical','medium','resolved',2,'Room 204, 1st Floor',22.7197,75.8577,120,1,42,4,false,NOW()-INTERVAL '7 days',NOW()-INTERVAL '3 days'),
('#SN-1006','Garbage Overflow — Hostel B','Bins overflow 3 days. Foul smell, insects. 80 residents affected.','sanitation','medium','in_progress',12,'Boys Hostel Block B',22.7189,75.8586,84,5,44,5,false,NOW()-INTERVAL '3 days',NOW()+INTERVAL '1 day');

-- Logs
INSERT INTO complaint_logs(complaint_id,action,action_hi,performed_by,note,created_at) VALUES
(1,'Complaint Registered','शिकायत दर्ज',1,'Filed via web portal',NOW()-INTERVAL '2 days'),
(1,'Forwarded to Civil & Plumbing','सिविल विभाग को अग्रेषित',11,NULL,NOW()-INTERVAL '1 day 22 hours'),
(1,'Status: In Progress','कार्यवाही जारी',11,'Maintenance team dispatched',NOW()-INTERVAL '1 day 20 hours'),
(1,'Auto-Escalated: 5+ reporters','स्वतः एस्केलेट: 5+ रिपोर्टर',NULL,NULL,NOW()-INTERVAL '1 day'),
(2,'Complaint Registered','शिकायत दर्ज',2,NULL,NOW()-INTERVAL '1 day'),
(3,'Complaint Registered','शिकायत दर्ज',4,NULL,NOW()-INTERVAL '3 days'),
(3,'Forwarded to IT Cell','IT सेल को अग्रेषित',11,NULL,NOW()-INTERVAL '2 days 22 hours'),
(3,'Status: In Progress','कार्यवाही जारी',11,'APs being replaced',NOW()-INTERVAL '2 days'),
(5,'Complaint Registered','शिकायत दर्ज',3,NULL,NOW()-INTERVAL '7 days'),
(5,'Status: Resolved — OTP Sent','OTP भेजा गया, पुष्टि प्रतीक्षित',10,'Fan motor replaced. OTP sent to reporter.',NOW()-INTERVAL '5 days'),
(5,'Closure Confirmed by Reporter','रिपोर्टर ने OTP से पुष्टि की',3,'OTP verified ✓',NOW()-INTERVAL '4 days 12 hours');

-- Comments
INSERT INTO comments(complaint_id,user_id,message,is_admin,created_at) VALUES
(1,1,'Canteen entrance flooded since Monday. Very dangerous — slippery floor all day.',false,NOW()-INTERVAL '2 days'),
(1,11,'Acknowledged. Civil team dispatched. Drain clearing in progress.',true,NOW()-INTERVAL '1 day 22 hours'),
(3,4,'WiFi down 3 days. Semester exams in 10 days. Urgent!',false,NOW()-INTERVAL '3 days'),
(3,11,'IT team replacing access points. Expected 2 days.',true,NOW()-INTERVAL '2 days'),
(5,3,'Fan stopped. Room 38°C. Heat exhaustion risk.',false,NOW()-INTERVAL '7 days'),
(5,10,'Motor replaced and tested. Sending OTP for closure confirmation.',true,NOW()-INTERVAL '5 days');

-- Co-reporters
INSERT INTO co_reporters(complaint_id,user_id) VALUES
(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
(3,1),(3,5),(3,3),(4,1),(4,3);

-- Notifications
INSERT INTO notifications(user_id,complaint_id,type,title_en,title_hi,message_en,message_hi,is_read) VALUES
(1,5,'resolved','Complaint Resolved ✓','शिकायत हल हुई','Fan issue #EL-1005 resolved. Enter OTP to close.','पंखा शिकायत हल हुई। बंद करने के लिए OTP दर्ज करें।',false),
(1,1,'escalated','Issue Escalated 🚨','शिकायत एस्केलेट','Water logging escalated — 7 reporters now.','जलभराव एस्केलेट — 7 रिपोर्टर हो गए।',false);

-- Rating
INSERT INTO ratings(complaint_id,user_id,stars,comment) VALUES(5,3,4,'Fan fixed in 2 days. Good response.');

-- ════════════════════════════════════════════════════════════════
-- VIEWS
-- ════════════════════════════════════════════════════════════════
CREATE OR REPLACE VIEW v_complaints AS
SELECT c.id, c.complaint_no, c.title, c.category, c.priority, c.status,
       c.importance_score, c.reporter_count, c.is_escalated, c.sla_breached,
       c.remind_count, c.created_at, c.resolved_at, c.sla_deadline,
       l.name_en AS location_name, l.name_hi AS location_name_hi,
       u.name AS reporter_name, u.role AS reporter_role, u.email AS reporter_email,
       d.name_en AS dept_name, d.name_hi AS dept_name_hi, d.head_email, d.sla_hours,
       EXTRACT(EPOCH FROM(NOW()-c.created_at))/3600 AS hours_open
FROM complaints c
LEFT JOIN locations l ON c.location_id=l.id
LEFT JOIN users u ON c.reporter_id=u.id
LEFT JOIN departments d ON c.department_id=d.id
ORDER BY c.is_escalated DESC, c.importance_score DESC, c.created_at DESC;

CREATE OR REPLACE VIEW v_stats AS
SELECT
    COUNT(*)                                              AS total,
    COUNT(*) FILTER(WHERE status='registered')           AS registered,
    COUNT(*) FILTER(WHERE status='in_progress')          AS in_progress,
    COUNT(*) FILTER(WHERE status IN('resolved','closed'))AS resolved,
    COUNT(*) FILTER(WHERE is_escalated AND status NOT IN('resolved','closed','rejected')) AS escalated,
    COUNT(*) FILTER(WHERE sla_breached AND status NOT IN('resolved','closed')) AS sla_breached,
    COUNT(*) FILTER(WHERE created_at>=NOW()-INTERVAL '24h') AS filed_today,
    ROUND(AVG(EXTRACT(EPOCH FROM(resolved_at-created_at))/3600)
        FILTER(WHERE resolved_at IS NOT NULL),1)         AS avg_resolve_hours,
    ROUND(100.0*COUNT(*) FILTER(WHERE status IN('resolved','closed'))/NULLIF(COUNT(*),0),1) AS resolution_pct
FROM complaints;

CREATE OR REPLACE VIEW v_leaderboard AS
SELECT u.id, u.name, u.branch, u.year_section, u.points, u.badge, u.streak_days,
       COUNT(DISTINCT c.id) AS filed,
       RANK() OVER(ORDER BY u.points DESC) AS rank
FROM users u
LEFT JOIN complaints c ON c.reporter_id=u.id
WHERE u.role IN('student','faculty') AND u.is_active
GROUP BY u.id ORDER BY u.points DESC;

SELECT '✓ CampusFix schema loaded successfully.' AS result;
