import requests

login_url = "http://127.0.0.1:8000/api/users/login/"
login_payload = {
    "username": "YaqoobNa4",
    "password": "password"
}
login_response = requests.post(login_url, json=login_payload)
tokens = login_response.json()
access_token = tokens.get("access")

jobs_url = "http://127.0.0.1:8000/api/jobs/jobs/?skills=python"
headers = {
    "Authorization": f"Bearer {access_token}"
}
print("Calling Jobs API...")
jobs_response = requests.get(jobs_url, headers=headers)
print("Jobs API Status:", jobs_response.status_code)
print("Jobs API Response:", jobs_response.text[:200])
