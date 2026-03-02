import requests

BASE_URL = "https://entreatingly-commonable-georgann.ngrok-free.dev/api/"
endpoints = [
    "auth/login",
    "auth/login/",
    "profile/me",
    "profile/me/",
    "academics/timetable",
    "academics/timetable/",
    "finance/view-balance",
    "finance/view-balance/",
    "support/tickets",
    "support/tickets/",
]

for ep in endpoints:
    url = BASE_URL + ep
    try:
        # Using GET for most, but login needs POST (will be 405 or 401, which is fine)
        if "login" in ep or "stk-push" in ep:
            resp = requests.post(url, json={})
        else:
            resp = requests.get(url)
        print(f"{url} -> {resp.status_code}")
    except Exception as e:
        print(f"{url} -> Error: {e}")
