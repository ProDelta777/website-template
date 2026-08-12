import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    print("Uploading to gofile...")
    # Get server
    server_res = subprocess.check_output(["curl", "-s", "https://api.gofile.io/servers"])
    server = json.loads(server_res.decode("utf-8"))["data"]["servers"][0]["name"]

    # Upload
    result = subprocess.check_output([
        "curl", "-s", "-F", f"file=@{filepath}", f"https://{server}.gofile.io/contents/uploadfile"
    ])
    response = json.loads(result.decode("utf-8"))
    print("Gofile link:", response["data"]["downloadPage"])
except Exception as e:
    print("Gofile error:", e)
