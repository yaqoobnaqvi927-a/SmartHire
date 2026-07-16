"""
SmartHire Settings Package
Provides environment-specific settings modules:
  - base        : shared settings (imported by all environments)
  - development : local dev + test; uses SQLite, DEBUG=True
  - production  : cloud deployment; PostgreSQL, DEBUG=False, security hardened
"""
