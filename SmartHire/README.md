# SmartHire 🚀

> AI-Powered Recruitment Platform — Final Year Project

SmartHire is a full-stack recruitment platform that connects job seekers with recruiters using AI-powered matching, resume parsing, and automated interview scheduling.

## Architecture

```
Android App (Kotlin) → Nginx → Django REST API → PostgreSQL
                                    ↓
                          AI Engine (spaCy + Gemini)
                                    ↓
                    Firebase FCM │ Zoom API │ Google Calendar │ AWS S3
```

**Architecture Style:** Modular Monolith (6 Django apps)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Kotlin, Jetpack Compose, Material 3 |
| Backend | Python 3.11, Django 4.2, Django REST Framework |
| Database | PostgreSQL 15 (production), SQLite (development) |
| AI/NLP | spaCy, Google Gemini 2.0, Cosine Similarity |
| Auth | JWT (SimpleJWT), Firebase Auth, Google OAuth |
| Real-time | Django Channels, WebSocket |
| Notifications | Firebase Cloud Messaging (FCM) |
| Video | Zoom Server-to-Server OAuth |
| Calendar | Google Calendar API |
| Storage | AWS S3 (production), local media (development) |
| Deployment | Docker, Nginx, Gunicorn, AWS EC2 |
| CI/CD | GitHub Actions |

## Project Structure

```
SmartHire/
├── smarthire_backend/     # Django project config (split settings)
├── users/                 # Auth, profiles, candidate search
├── cv_bank/               # CV upload & NLP parsing (707-line engine)
├── jobs/                  # Job postings, applications, ATS pipeline
├── interviews/            # Scheduling, Zoom, Google Calendar
├── communications/        # Chat, notifications, FCM push
├── ai_engine/             # Matching, recommendations, skill gap
├── core/                  # Shared permissions, utils, pagination
├── docker/                # Dockerfile, docker-compose, nginx.conf
├── tests/                 # pytest suite (26+ tests)
└── .github/workflows/     # CI/CD pipeline
```

## Quick Start (Development)

```bash
# 1. Clone
git clone https://github.com/yaqoobnaqvi927-a/SmartHire.git
cd SmartHire/SmartHire

# 2. Virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows

# 3. Install dependencies
pip install -r requirements/development.txt
python -m spacy download en_core_web_sm

# 4. Environment
cp .env.example .env
# Edit .env with your GEMINI_API_KEY

# 5. Database
python manage.py migrate
python seed_db.py  # Optional: seed with sample data

# 6. Run
python manage.py runserver
```

## Production Deployment (Docker)

```bash
# 1. Configure environment
cp .env.example .env
# Set DJANGO_SECRET_KEY, DB credentials, API keys

# 2. Build and run
cd docker
docker compose up --build -d

# 3. Migrate and setup
docker compose exec web python manage.py migrate --noinput
docker compose exec web python manage.py collectstatic --noinput
docker compose exec web python manage.py createsuperuser
```

## API Endpoints

| Module | Base Path | Key Endpoints |
|--------|-----------|---------------|
| Auth | `/api/users/` | register, login, google-login, profile |
| CVs | `/api/cv_bank/` | Upload & auto-parse with NLP |
| Jobs | `/api/jobs/` | CRUD, search, apply, ATS pipeline |
| Interviews | `/api/interviews/` | Schedule (auto Zoom + Calendar) |
| Chat | `/api/communications/` | Messages, notifications |
| AI | `/api/ai/` | Recommendations, skill gap, match score |

## AI Features

- **Resume Parsing**: spaCy NLP extracts skills, experience, education from PDFs
- **Job Matching**: Cosine similarity (75% skills + 25% experience weighting)
- **Skill Gap Analysis**: Gemini AI generates personalized career recommendations
- **Cover Letter Generation**: Gemini AI creates tailored cover letters
- **TF-IDF Search**: Multi-factor ranking (text similarity + skill overlap + recency)

## Testing

```bash
pytest tests/ -v --cov
```

## Live Server

- **API**: https://Yaqoob9227.pythonanywhere.com/
- **Admin**: https://Yaqoob9227.pythonanywhere.com/admin/

## Team

- **GitHub**: [@yaqoobnaqvi927-a](https://github.com/yaqoobnaqvi927-a)

## License

This project is part of a Final Year Project (FYP) submission.
