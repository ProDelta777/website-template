import os
from PIL import Image, ImageDraw

def make_icon(size, filename, round_icon=False):
    img = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    draw = ImageDraw.Draw(img)
    if round_icon:
        draw.ellipse((0, 0, size, size), fill="#3DDC84")
    else:
        draw.rectangle((0, 0, size, size), fill="#3DDC84")
    img.save(filename)

sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

os.system("rm -rf app/src/main/res/mipmap-*")

for dens, size in sizes.items():
    folder = f"app/src/main/res/mipmap-{dens}"
    os.makedirs(folder, exist_ok=True)
    make_icon(size, f"{folder}/ic_launcher.png", round_icon=False)
    make_icon(size, f"{folder}/ic_launcher_round.png", round_icon=True)

print("Icons generated successfully.")
