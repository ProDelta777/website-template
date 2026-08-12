import urllib.request
import os

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
size = os.path.getsize(filepath)

def p(s): print(s)

p("Starting direct binary chunk upload...")
# Let's try fileio api
req = urllib.request.Request("https://file.io", method="POST")
req.add_header('Content-Type', 'multipart/form-data; boundary=---BOUNDARY')

boundary = b"-----BOUNDARY"
crlf = b"\r\n"

body = boundary + crlf
body += b'Content-Disposition: form-data; name="file"; filename="app-debug.apk"' + crlf
body += b'Content-Type: application/vnd.android.package-archive' + crlf + crlf
with open(filepath, "rb") as f:
    body += f.read()
body += crlf + boundary + b"--" + crlf

req.data = body

try:
    res = urllib.request.urlopen(req)
    p(res.read().decode())
except Exception as e:
    p(str(e))
