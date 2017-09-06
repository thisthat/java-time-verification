import requests
import json

def push(message):
    header = {"Content-Type": "application/json; charset=utf-8",
          "Authorization": "Basic YWFiNmU5MjUtNWFjNi00MTU1LWEzNzMtOGU1ZDBjYjdiNGQz"}
    payload = {"app_id": "09dbf63c-a667-4326-b22e-685d6192b100",
           "include_player_ids": ["7ff03c80-932d-428f-95a1-755e12161296"],
           "contents": {"en": message}}
    requests.post("https://onesignal.com/api/v1/notifications", headers=header, data=json.dumps(payload))
