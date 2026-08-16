import urllib.request
import urllib.error
import urllib.parse
import json
import ssl
import sys

def upload_gofile(filepath):
    try:
        req = urllib.request.Request("https://api.gofile.io/servers", method="GET")
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read())
            server = res["data"]["servers"][0]["name"]

        print(f"Uploading to server {server}...")

        import subprocess
        result = subprocess.run(
            ["curl", "-F", f"file=@{filepath}", f"https://{server}.gofile.io/contents/uploadfile"],
            capture_output=True, text=True, check=True
        )
        res_json = json.loads(result.stdout)
        if res_json["status"] == "ok":
            print("Download Link:", res_json["data"]["downloadPage"])
        else:
            print("Failed:", result.stdout)
    except Exception as e:
        print(f"Failed: {e}")

upload_gofile("release/SkinLens-Release.apk")
