#!/bin/bash
echo "Uploading to bashupload..."
curl -s -T ./app/build/outputs/apk/debug/app-debug.apk bashupload.com | grep -o 'http.*'
