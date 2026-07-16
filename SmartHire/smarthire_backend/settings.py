"""
DEPRECATED — This monolithic settings file has been replaced by the
smarthire_backend/settings/ package (base.py / development.py / production.py).

This file is kept as a compatibility shim. Django will always prefer the
settings/ package over this file when resolving smarthire_backend.settings.

If you need the old settings directly, see settings_monolithic_backup.py
(or restore from VCS history).
"""
# Re-export everything from the development settings so any tool that directly
# imports this module still gets valid Django settings.
from smarthire_backend.settings.development import *  # noqa: F401, F403
