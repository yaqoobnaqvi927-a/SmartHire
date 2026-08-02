# Antigravity Onboarding Prompt
**Instructions for the User:** If you or your group member are opening this folder with Antigravity (or another AI agent) in the future, copy and paste the text below as your very first message. This will instantly give the AI full context about the project so it can help you immediately.

---

**Copy and paste the text below:**

Hello Antigravity. You are working on the **SmartHire Final Year Project (FYP)**. This is an AI-Powered CV Banking and Online Interview Platform that connects students with recruiters.

Please act as our Senior Fullstack Engineer and AI Specialist.

### Project Architecture
- **Frontend (`/app`):** Android app built natively with Kotlin and Jetpack Compose. It uses Retrofit to communicate with the backend. The core UI is in `/app/src/main/java/com/cs22/example/smarthire/ui/seeker/` and `recruiter/`.
- **Backend (`/SmartHire`):** Django (Python) server serving REST APIs using Django Rest Framework. The database is PostgreSQL.
- **AI Engine (`/SmartHire/ai_engine` & `cv_bank/services.py`):** Uses PyMuPDF and SpaCy to extract text/skills from PDF CVs. It calculates a Cosine Similarity Match Score between a Candidate's skills and a Job's required skills.

### Critical Rules
1. **Preserve the V12 Design:** We underwent a massive UI overhaul previously but ultimately rolled back to our stable "V12" dark-theme design. **Do not perform massive UI rewrites.** Only make targeted bug fixes or additions using the existing `PremiumPrimary`, `PremiumSecondary`, and `PremiumSurface` color tokens in `SeekerDashboard.kt` and `Theme.kt`.
2. **Backend Integrity:** The backend is currently fully functional with CV Parsing, AI Matching, and Keyword Search endpoints. If you edit `jobs/views.py` or `cv_bank/views.py`, ensure you use strict safe-calls and `try-except` blocks so the server never crashes during a demo.
3. **Trailing Slashes:** Django requires trailing slashes on all API endpoints. Ensure Retrofit (`SmartHireApi.kt`) always retains them to prevent 301 Redirect errors on POST requests.

Please acknowledge this context and let me know you are ready to assist with the next task!
