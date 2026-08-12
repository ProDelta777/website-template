import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    print("Uploading to WeTransfer format...")
    # There is no direct public API for WeTransfer without auth. We'll try file.io again with curl.
    result = subprocess.check_output([
        "curl", "-s", "-F", f"file=@{filepath}", "https://file.io"
    ])
    response = json.loads(result.decode("utf-8"))
    print("Link:", response.get("link"))
except Exception as e:
    print("error:", e)
