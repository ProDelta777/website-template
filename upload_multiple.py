import subprocess
import json
import os

filepath = "./app/build/outputs/apk/debug/app-debug.apk"

def upload_pixeldrain():
    try:
        print("Uploading to pixeldrain...")
        result = subprocess.check_output([
            "curl", "-s", "-T", filepath, "https://pixeldrain.com/api/file"
        ])
        response = json.loads(result.decode("utf-8"))
        if response.get("success"):
            print("Pixeldrain link: https://pixeldrain.com/u/" + response["id"])
    except Exception as e:
        print("Pixeldrain error:", e)

def upload_file_coffee():
    try:
        print("Uploading to file.coffee...")
        result = subprocess.check_output([
            "curl", "-s", "-F", f"file=@{filepath}", "https://file.coffee/api/file/upload"
        ])
        response = json.loads(result.decode("utf-8"))
        print("File.coffee link: " + response["url"])
    except Exception as e:
        print("File.coffee error:", e)

upload_pixeldrain()
upload_file_coffee()
