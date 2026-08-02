# SmartHire: AI-Powered CV Banking and Online Interview Platform
**Final Year Project (FYP) Documentation**

## Project Overview
SmartHire is a full-stack, AI-driven application designed to connect students/job seekers with recruiters. It features a robust Android frontend built with Kotlin and Jetpack Compose, and a powerful backend powered by Django (Python), PostgreSQL, and advanced NLP algorithms.

## Core Architecture
- **Frontend (Android/Kotlin):** Located in `app/`. Uses Jetpack Compose for a reactive, modern UI. MVVM architecture with Retrofit for API communication.
- **Backend (Django/Python):** Located in `SmartHire/`. Uses Django Rest Framework (DRF) to serve APIs. 
- **Database:** PostgreSQL (production on PythonAnywhere) and SQLite (local development).

## Key Features & FYP 2 Objectives Accomplished
1. **AI Resume Parsing:** Uses PyMuPDF and SpaCy to extract technical skills, education, and experience directly from uploaded PDF CVs.
2. **AI Job Matching Engine (Cosine Similarity):** Mathematically compares a candidate's extracted skills against a job's required skills to generate a highly accurate 0-100% "Match Score".
3. **Skill Gap Analysis:** Dynamically calculates which required skills a candidate is missing using set theory (Job Skills - Candidate Skills).
4. **Real-time Job Search:** Optimized backend `/api/jobs/search/?q=` endpoint using Django `Q()` objects to filter jobs by keywords across titles, descriptions, and companies.
5. **Dynamic UI Animations:** Android app features a realistic "AI Processing" progress loader and highly detailed Match Cards to visualize the NLP backend processes.
6. **Robust Authentication:** Secure JWT-based login for both Recruiter and Seeker roles, with crash-proof navigation flows.

## Environment Setup
**Backend:**
1. Navigate to the backend folder: `cd SmartHire`
2. Create a virtual environment: `python -m venv venv`
3. Activate the environment: `.\venv\Scripts\activate` (Windows)
4. Install requirements: `pip install -r requirements.txt`
5. Run migrations: `python manage.py migrate`
6. Run the server: `python manage.py runserver`

**Frontend:**
1. Open the `app` folder in Android Studio.
2. Ensure you have the latest Android SDK and Kotlin plugin installed.
3. Sync Gradle and click "Run". The app is currently configured to connect to the live PythonAnywhere server via `SmartHireApi.kt`.

## API Endpoints Summary
- **Auth:** `/api/users/login/`, `/api/users/register/`
- **CV Bank:** `/api/cv_bank/cvs/` (POST uploads and parses the PDF)
- **Jobs:** `/api/jobs/jobs/`, `/api/jobs/search/?q=keyword`
- **Demo Data:** `/api/populate-demo/` (Secret endpoint to instantly populate the DB with presentation data)
