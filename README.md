---

# 🎓 Student Record Finder

### AI-Enabled Academic Record & Institutional Control System

<p align="center">
  <b>Secure • Role-Based • AI Integrated • Enterprise Structured</b>
</p>

---

## 🏷 Badges

<p align="center">

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Java](https://img.shields.io/badge/Language-Java-blue?logo=java)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)
![Realtime DB](https://img.shields.io/badge/Database-RealtimeDB-yellow)
![AI Powered](https://img.shields.io/badge/AI-Google%20Gemini-purple)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle)
![Architecture](https://img.shields.io/badge/Architecture-Role--Based%20Control-critical)
![Status](https://img.shields.io/badge/Project-Production%20Ready-brightgreen)

</p>

---

# 📌 Overview

**Student Record Finder** is a secure, role-based academic management system built during my Android development learning journey.

It integrates structured institutional control with AI-powered analytics to assist:

* 👨‍🎓 Students
* 👨‍🏫 Faculty
* 🧑‍💼 Administrators

The application demonstrates enterprise-style system design using Firebase backend and modular Android architecture.

---

# 🚀 Core Features

## 👨‍🎓 Student Module

* Attendance tracking (subject-wise)
* GPA calculation
* Risk detection indicators
* AI academic assistant
* Personalized performance insights

## 👨‍🏫 Faculty Module

* Manage assigned students only
* Update attendance & marks
* At-risk student identification
* Class-level analytics
* Academic performance dashboard

## 🧑‍💼 Admin Module

* Faculty approval system
* Institutional analytics dashboard
* System health monitoring
* Academic risk overview
* Global monitoring access

---

# 🧠 AI Integration

Integrated with **Google Gemini API** to provide:

* Context-aware academic suggestions
* Attendance-based recommendations
* Risk alerts & improvement guidance
* Faculty-level performance insights
* Institutional analytics evaluation

Strict role-context enforcement ensures:

```
Students → Single-user context
Faculty → Class-level context
Admin → Institutional context
```

---

# 🏗 Architecture & Design

### 🔐 Role-Based Access Control (RBAC)

| Role    | Permissions                  |
| ------- | ---------------------------- |
| Student | Read-only personal data      |
| Faculty | Manage assigned students     |
| Admin   | Global monitoring & approval |

Firebase rules enforce:

```
faculty_id === auth.uid
```

---

### 📦 Modular Controller-Based Architecture

```
controller/
model/
ui/
```

Separation of concerns ensures:

* Maintainability
* Scalability
* Clean logic separation
* Industry-level structuring

---

# 🛠 Tech Stack

| Layer        | Technology                       |
| ------------ | -------------------------------- |
| Frontend     | Android (Java, XML)              |
| Backend      | Firebase Realtime Database       |
| Auth         | Firebase Authentication          |
| AI           | Google Gemini API                |
| Networking   | OkHttp                           |
| Build        | Gradle                           |
| Architecture | Controller-Based Modular Pattern |

---

# 🗄 Firebase Database Design

```
users/
students/
faculty/
```

Structured ownership:

* Faculty linked via `faculty_id`
* Approval gating for faculty
* Role-driven navigation logic
* Secure real-time sync

---

# 📊 Analytical Dashboards

### Student

* Attendance %
* GPA
* Subject risk indicators
* AI suggestions

### Faculty

* Total students
* At-risk count
* Subject analytics

### Admin

* Total students
* Total faculty
* Pending approvals
* System Health Score

---

# ⚙ Installation Guide

## 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/student-record-finder.git
```

---

## 2️⃣ Open in Android Studio

* Open Android Studio
* Select **Open Existing Project**

---

## 3️⃣ Firebase Setup

* Enable Email/Password Authentication
* Enable Realtime Database
* Download `google-services.json`
* Place inside:

```
app/
```

---

## 4️⃣ Add API Keys

In `local.properties`:

```
GEMINI_API_KEY=your_gemini_key_here
```

---

## 5️⃣ Build & Run

* Sync Gradle
* Run on emulator or device

---

# 🔐 Security Highlights

* Strict role validation
* Faculty approval gating
* Active/inactive faculty checks
* Secure key injection via `BuildConfig`
* Firebase ownership rules
* No hardcoded credentials

---

# 📚 What This Project Demonstrates

✔ Android lifecycle mastery
✔ Firebase integration
✔ Secure database rule design
✔ Role-based navigation logic
✔ Modular architecture
✔ AI API integration
✔ Real-time analytics dashboards
✔ Structured enterprise thinking

---

# 🚀 Future Enhancements

* ML-based predictive risk scoring
* Firestore migration
* Cloud Functions AI proxy
* Email alert automation
* Multi-institution support
* Advanced academic planner

---

# 👨‍💻 Developer

**Pradhumnya Changdev Kalsait**
Computer Engineering Student
Android Developer (Learning Phase)

---

# 🌟 Portfolio Note

This project reflects:

* My growth in Android development
* Practical backend integration experience
* Understanding of secure system architecture
* Early adoption of AI in academic systems
* Structured enterprise-style thinking

