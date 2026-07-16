"""
SmartHire Development / Test Settings
Inherits everything from base and overrides for local development and CI.
Uses SQLite so tests run without a Postgres server unless DATABASE_URL is set.
"""

import os
from .base import *  # noqa: F401, F403

# ── Developer ergonomics ──────────────────────────────────────────────────────
DEBUG = True

# Allow all hosts in development (restrict in production)
ALLOWED_HOSTS = ['*']

# ── Database ──────────────────────────────────────────────────────────────────
# CI sets DATABASE_URL to a live Postgres service; locally falls back to SQLite.
DATABASE_URL = os.environ.get('DATABASE_URL', '')

if DATABASE_URL:
    import dj_database_url
    DATABASES = {
        'default': dj_database_url.parse(DATABASE_URL, conn_max_age=0)
    }
elif all(os.environ.get(k) for k in ['DB_NAME', 'DB_USER', 'DB_PASSWORD', 'DB_HOST']):
    # CI-style explicit env vars (used by the GitHub Actions test job)
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.postgresql',
            'NAME': os.environ['DB_NAME'],
            'USER': os.environ['DB_USER'],
            'PASSWORD': os.environ['DB_PASSWORD'],
            'HOST': os.environ.get('DB_HOST', 'localhost'),
            'PORT': os.environ.get('DB_PORT', '5432'),
            # Disable conn_max_age in tests — each test gets a fresh connection
            'CONN_MAX_AGE': 0,
        }
    }
else:
    # Pure local: SQLite (zero-config)
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.sqlite3',
            'NAME': BASE_DIR / 'db.sqlite3',  # noqa: F405
        }
    }

# ── Email — console backend for development ───────────────────────────────────
EMAIL_BACKEND = 'django.core.mail.backends.console.EmailBackend'

# ── Static files — use simple storage (no manifest hashing) in dev ────────────
STATICFILES_STORAGE = 'django.contrib.staticfiles.storage.StaticFilesStorage'

# ── Security — relaxed for local dev ─────────────────────────────────────────
CORS_ALLOW_ALL_ORIGINS = True
