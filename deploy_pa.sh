#!/bin/bash
set -e

# Get PythonAnywhere username from directory path
USER=$(whoami)
echo "Deploying SmartHire for user: $USER"

# 1. Clone repository if not already present
if [ ! -d "/home/$USER/SmartHireRepo" ]; then
    echo "Cloning repository..."
    git clone https://github.com/yaqoobnaqvi927-a/SmartHire.git /home/$USER/SmartHireRepo
else
    echo "Repository already exists, pulling latest changes..."
    cd /home/$USER/SmartHireRepo
    git pull origin main
fi

# 2. Setup Virtual Environment
echo "Setting up virtual environment..."
export WORKON_HOME=$HOME/.virtualenvs
mkdir -p $WORKON_HOME
if [ ! -d "$WORKON_HOME/smarthire-env" ]; then
    virtualenv --python=/usr/bin/python3.10 $WORKON_HOME/smarthire-env
fi

# Activate virtual environment
source $WORKON_HOME/smarthire-env/bin/activate

# 3. Install requirements
echo "Installing dependencies..."
pip install -r /home/$USER/SmartHireRepo/SmartHire/requirements.txt

# 4. Create WSGI file
echo "Configuring WSGI file..."
WSGI_FILE="/var/www/${USER}_pythonanywhere_com_wsgi.py"
cat << EOF > $WSGI_FILE
import os
import sys

# Add project path to sys.path
path = '/home/$USER/SmartHireRepo/SmartHire'
if path not in sys.path:
    sys.path.insert(0, path)

# Set Django settings module
os.environ['DJANGO_SETTINGS_MODULE'] = 'smarthire_backend.settings.development'

# Import WSGI application
from django.core.wsgi import get_wsgi_application
application = get_wsgi_application()
EOF

# 5. Migrate Database & Seed Data
echo "Running migrations..."
cd /home/$USER/SmartHireRepo/SmartHire
python manage.py migrate

echo "Seeding database with 150+ jobs and demo data..."
python seed_db.py

echo "Collecting static files..."
python manage.py collectstatic --noinput

echo "=========================================================="
echo "Deployment setup complete!"
echo "Now go to the 'Web' tab on PythonAnywhere and do the following:"
echo "1. Click 'Add a new web app'."
echo "2. Select 'Manual Configuration' (DO NOT select Django here)."
echo "3. Choose 'Python 3.10'."
echo "4. In the Web App settings:"
echo "   - Virtualenv path: /home/$USER/.virtualenvs/smarthire-env"
echo "   - Source code path: /home/$USER/SmartHireRepo/SmartHire"
echo "5. Click the green 'Reload' button at the top."
echo "Your server will be online at: http://$USER.pythonanywhere.com"
echo "=========================================================="
