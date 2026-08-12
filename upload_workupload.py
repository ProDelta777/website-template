import subprocess
import json

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
try:
    print("Uploading to workupload...")
    # Get server
    server_res = subprocess.check_output(["curl", "-s", "https://workupload.com/api/file/getServer"])
    server = json.loads(server_res.decode("utf-8"))["data"]["server"]

    # Upload
    result = subprocess.check_output([
        "curl", "-s", "-F", f"file=@{filepath}", f"{server}/upload"
    ])
    response = json.loads(result.decode("utf-8"))

    if response["success"]:
        print("Workupload link: https://workupload.com/file/" + response["data"]["id"])
    else:
        print("Workupload upload failed.")
except Exception as e:
    print("Workupload error:", e)
