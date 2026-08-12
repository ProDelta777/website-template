import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    print("Uploading to anonfiles clone...")
    result = subprocess.check_output([
        "curl", "-s", "-F", f"file=@{filepath}", "https://api.anonfiles.com/upload"
    ])
    response = json.loads(result.decode("utf-8"))
    print("Anonfiles link:", response["data"]["file"]["url"]["full"])
except Exception as e:
    print("error:", e)
