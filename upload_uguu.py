import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    result = subprocess.check_output([
        "curl",
        "-F", f"files[]=@{filepath}",
        "https://uguu.se/upload.php"
    ])
    response = json.loads(result.decode("utf-8"))
    print(response["files"][0]["url"])
except Exception as e:
    print("error:", e)
