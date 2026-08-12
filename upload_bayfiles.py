import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    print("Uploading to filebin.net...")
    import time
    bin_id = f"skinlens-bin-{int(time.time())}"
    result = subprocess.check_output([
        "curl", "-s", "-T", filepath, f"https://filebin.net/{bin_id}/SkinLens-debug.apk"
    ])
    print(f"Filebin link: https://filebin.net/{bin_id}")
except Exception as e:
    print("error:", e)
