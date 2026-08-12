import urllib.request
import urllib.parse
import os

filepath = "./app/build/outputs/apk/debug/app-debug.apk"
url = "https://catbox.moe/user/api.php"

boundary = '----WebKitFormBoundary7MA4YWxkTrZu0gW'
headers = {
    'Content-Type': f'multipart/form-data; boundary={boundary}'
}

with open(filepath, 'rb') as f:
    file_content = f.read()

data = []
data.append(f'--{boundary}')
data.append('Content-Disposition: form-data; name="reqtype"')
data.append('')
data.append('fileupload')
data.append(f'--{boundary}')
data.append('Content-Disposition: form-data; name="fileToUpload"; filename="app-debug.apk"')
data.append('Content-Type: application/vnd.android.package-archive')
data.append('')
data.append(file_content)
data.append(f'--{boundary}--')
data.append('')

body = b'\r\n'.join(
    d if isinstance(d, bytes) else d.encode('utf-8')
    for d in data
)

req = urllib.request.Request(url, data=body, headers=headers)
try:
    response = urllib.request.urlopen(req)
    print(response.read().decode('utf-8'))
except Exception as e:
    print("error:", e)
