import urllib.request
import time

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
bin_id = f"skinlens-{int(time.time())}"
filename = "app-debug.apk"
url = f"https://filebin.net/{bin_id}/{filename}"

try:
    with open(filepath, "rb") as f:
        req = urllib.request.Request(url, data=f.read(), method="POST")
        req.add_header("bin", bin_id)
        req.add_header("filename", filename)
        req.add_header("Content-Type", "application/octet-stream")
        urllib.request.urlopen(req)
        print(f"Filebin link: https://filebin.net/{bin_id}/{filename}")
except Exception as e:
    pass
