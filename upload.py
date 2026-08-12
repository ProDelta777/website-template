import urllib.request
import json
import os

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
with open(filepath, "rb") as f:
    req = urllib.request.Request(
        "https://api.anonfiles.com/upload",
        data=f,
        headers={"Content-Type": "application/octet-stream"}
    )
    try:
        response = urllib.request.urlopen(req)
        print(response.read().decode("utf-8"))
    except Exception as e:
        print("error", e)
