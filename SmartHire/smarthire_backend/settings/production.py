"""
SmartHire Production Settings
Inherits from base and applies security hardening, PostgreSQL, and
WhiteNoise static serving suitable for a Docker / AWS EC2 deployment.

Required environment variables (must be set in .env or container env):
  DJANGO_SECRET_KEY   — long random string, never commit to VCS
  ALLOWED_HOSTS       — comma-separated list of valid hostnames
  DATABASE_URL        — postgres://user:pass@host:5432/dbname
                        OR individual DB_* vars below
  DB_NAME / DB_USER / DB_PASSWORD / DB_HOST / DB_PORT
  GEMINI_API_KEY      — Google Generative AI key
"""

import os
import dj_database_url
from .base import *  # noqa: F401, F403

# ── DRF Throttling (rate limiting) ────────────────────────────────────────────
REST_FRAMEWORK = {
    **REST_FRAMEWORK,  # inherit from base
    'DEFAULT_THROTTLE_CLASSES': [
        'rest_framework.throttling.AnonRateThrottle',
        'rest_framework.throttling.UserRateThrottle',
    ],
    'DEFAULT_THROTTLE_RATES': {
        'anon': '100/hour',
        'user': '1000/hour',
    },
}

# ── Security hardening ────────────────────────────────────────────────────────
DEBUG = False

# Hosts must be explicitly whitelisted in production
_raw_hosts = os.environ.get('ALLOWED_HOSTS', '')
ALLOWED_HOSTS = [h.strip() for h in _raw_hosts.split(',') if h.strip()] or ['localhost']

SECRET_KEY = os.environ['DJANGO_SECRET_KEY']  # Hard fail if missing in production

# ── HTTPS / Security headers ──────────────────────────────────────────────────
SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')
SECURE_SSL_REDIRECT = os.environ.get('SECURE_SSL_REDIRECT', 'True').lower() in ('true', '1', 'yes')
SESSION_COOKIE_SECURE = True
CSRF_COOKIE_SECURE = True
SECURE_HSTS_SECONDS = 31536000          # 1 year
SECURE_HSTS_INCLUDE_SUBDOMAINS = True
SECURE_HSTS_PRELOAD = True
SECURE_CONTENT_TYPE_NOSNIFF = True

# ── Database — PostgreSQL (required in production) ────────────────────────────
DATABASE_URL = os.environ.get('DATABASE_URL', '')

if DATABASE_URL:
    DATABASES = {
        'default': dj_database_url.parse(DATABASE_URL, conn_max_age=600, ssl_require=False)
    }
else:
    # Explicit environment variables (Docker Compose / Kubernetes pattern)
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.postgresql',
            'NAME': os.environ.get('DB_NAME', 'smarthire_db'),
            'USER': os.environ.get('DB_USER', 'smarthire_user'),
            'PASSWORD': os.environ.get('DB_PASSWORD', ''),
            'HOST': os.environ.get('DB_HOST', 'db'),
            'PORT': os.environ.get('DB_PORT', '5432'),
            'CONN_MAX_AGE': 600,         # persistent connections
            'OPTIONS': {
                'connect_timeout': 10,
            },
        }
    }

# ── Static files — WhiteNoise with manifest hashing ──────────────────────────
STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'

# ── AWS S3 — Media file storage (CVs, uploads) ───────────────────────────────
_aws_key = os.environ.get('AWS_ACCESS_KEY_ID', '')
if _aws_key:
    INSTALLED_APPS += ['storages']  # noqa: F405
    DEFAULT_FILE_STORAGE = 'storages.backends.s3boto3.S3Boto3Storage'
    AWS_ACCESS_KEY_ID = _aws_key
    AWS_SECRET_ACCESS_KEY = os.environ.get('AWS_SECRET_ACCESS_KEY', '')
    AWS_STORAGE_BUCKET_NAME = os.environ.get('AWS_STORAGE_BUCKET_NAME', 'smarthire-cvs')
    AWS_S3_REGION_NAME = os.environ.get('AWS_S3_REGION_NAME', 'ap-south-1')
    AWS_S3_FILE_OVERWRITE = False
    AWS_DEFAULT_ACL = 'private'
    AWS_QUERYSTRING_AUTH = True
    AWS_QUERYSTRING_EXPIRE = 3600  # 1 hour signed URLs

# ── Email — SMTP via env vars ─────────────────────────────────────────────────
EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
EMAIL_HOST = os.environ.get('EMAIL_HOST', 'smtp.gmail.com')
EMAIL_PORT = int(os.environ.get('EMAIL_PORT', '587'))
EMAIL_USE_TLS = True
EMAIL_HOST_USER = os.environ.get('EMAIL_HOST_USER', '')
EMAIL_HOST_PASSWORD = os.environ.get('EMAIL_HOST_PASSWORD', '')
DEFAULT_FROM_EMAIL = os.environ.get('DEFAULT_FROM_EMAIL', 'noreply@smarthire.app')

# ── CORS — restrict to known frontend origins in production ───────────────────
_cors_origins = os.environ.get('CORS_ALLOWED_ORIGINS', '')
if _cors_origins:
    CORS_ALLOW_ALL_ORIGINS = False
    CORS_ALLOWED_ORIGINS = [o.strip() for o in _cors_origins.split(',') if o.strip()]
else:
    # If not explicitly set, allow all (mobile app support) — tighten per project needs
    CORS_ALLOW_ALL_ORIGINS = True

# ── Logging — structured logs to stdout (captured by Docker/CloudWatch) ───────
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'verbose': {
            'format': '{levelname} {asctime} {module} {process:d} {thread:d} {message}',
            'style': '{',
        },
    },
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
            'formatter': 'verbose',
        },
    },
    'root': {
        'handlers': ['console'],
        'level': 'WARNING',
    },
    'loggers': {
        'django': {
            'handlers': ['console'],
            'level': os.environ.get('DJANGO_LOG_LEVEL', 'ERROR'),
            'propagate': False,
        },
        'smarthire': {
            'handlers': ['console'],
            'level': 'INFO',
            'propagate': False,
        },
    },
}
