import requests

url = "http://127.0.0.1:8000/api/users/register/"
payload = {
    "username": "YaqoobNa4",
    "password": "password",
    "email": "yaqoobnaqvi927@gmail.com",
    "role": "recruiter"
}
response = requests.post(url, json=payload)
print("Status Code:", response.status_code)
print("Body:", response.text)
