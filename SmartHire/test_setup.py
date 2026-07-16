import requests

# 1. Login to get JWT
login_url = "http://127.0.0.1:8000/api/users/login/"
login_payload = {
    "username": "YaqoobNa4",
    "password": "password"
}
login_response = requests.post(login_url, json=login_payload)
print("Login Status:", login_response.status_code)
tokens = login_response.json()
access_token = tokens.get("access")

# 2. Setup Profile
setup_url = "http://127.0.0.1:8000/api/users/profile/setup/"
setup_payload = {
    "company_name": "Test Company",
    "company_size": "10-50",
    "industry": "IT",
    "designation": "HR"
}
headers = {
    "Authorization": f"Bearer {access_token}"
}
setup_response = requests.put(setup_url, json=setup_payload, headers=headers)
print("Setup Status:", setup_response.status_code)
print("Setup Body:", setup_response.text)
