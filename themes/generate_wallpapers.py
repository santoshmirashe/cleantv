import os
import math
import random
from PIL import Image, ImageDraw, ImageFilter

W, H = 1920, 1080
out_dir = r"d:\Santosh\Workspace\cleanyourtv\themes\heroes\wallpapers"
os.makedirs(out_dir, exist_ok=True)

def create_base(top_color, bottom_color):
    img = Image.new("RGB", (W, H))
    draw = ImageDraw.Draw(img)
    for y in range(H):
        t = y / H
        r = int(top_color[0] + (bottom_color[0] - top_color[0]) * t)
        g = int(top_color[1] + (bottom_color[1] - top_color[1]) * t)
        b = int(top_color[2] + (bottom_color[2] - top_color[2]) * t)
        draw.line([(0, y), (W, y)], fill=(r, g, b))
    return img

def add_vignette(img, intensity=0.7):
    mask = Image.new("L", (W, H), 0)
    draw = ImageDraw.Draw(mask)
    cx, cy = W / 2, H / 2
    max_dist = math.sqrt(cx*cx + cy*cy)
    # Radial mask
    for r in range(int(max_dist), 0, -20):
        t = r / max_dist
        alpha = int(255 * (t ** 1.8) * intensity)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=alpha)
    mask = mask.filter(ImageFilter.GaussianBlur(40))
    black = Image.new("RGB", (W, H), (0, 0, 0))
    return Image.composite(black, img, mask)

# 1. Itachi Uchiha - Blood Moon & Crows
print("Generating Itachi wallpaper...")
img1 = create_base((12, 10, 16), (4, 4, 6))
draw1 = ImageDraw.Draw(img1)
# Dark Crimson Moon
moon_cx, moon_cy, moon_r = W // 2, 380, 260
for r in range(moon_r + 60, moon_r, -2):
    t = (r - moon_r) / 60
    a = int(60 * (1 - t))
    draw1.ellipse([moon_cx - r, moon_cy - r, moon_cx + r, moon_cy + r], outline=(180, 20, 30))
draw1.ellipse([moon_cx - moon_r, moon_cy - moon_r, moon_cx + moon_r, moon_cy + moon_r], fill=(140, 16, 24))
# Crows silhouette
for _ in range(45):
    cx = random.randint(300, 1620)
    cy = random.randint(150, 700)
    size = random.randint(12, 35)
    # Simple bird silhouette
    draw1.arc([cx - size, cy - size//2, cx, cy + size//2], 180, 360, fill=(2, 2, 3), width=3)
    draw1.arc([cx, cy - size//2, cx + size, cy + size//2], 180, 360, fill=(2, 2, 3), width=3)
img1 = add_vignette(img1, 0.8)
img1.save(os.path.join(out_dir, "01-itachi-uchiha.jpg"), quality=95)

# 2. Batman - The Dark Knight Gotham Noir
print("Generating Batman wallpaper...")
img2 = create_base((10, 14, 20), (2, 3, 5))
draw2 = ImageDraw.Draw(img2)
# Bat signal in clouds
bat_cx, bat_cy = W // 2, 360
for r in range(240, 0, -5):
    t = r / 240
    c = int(50 * (1 - t))
    draw2.ellipse([bat_cx - r, bat_cy - r, bat_cx + r, bat_cy + r], fill=(15 + c, 22 + c*2, 35 + c*3))
# City Skyline silhouette
for i in range(0, W, 40):
    bw = random.randint(30, 80)
    bh = random.randint(200, 500)
    draw2.rectangle([i, H - bh, i + bw, H], fill=(5, 7, 10))
    # Some tiny glowing office windows
    for wy in range(H - bh + 20, H - 40, 25):
        if random.random() > 0.6:
            draw2.rectangle([i + 8, wy, i + 14, wy + 8], fill=(40, 60, 80))
img2 = add_vignette(img2, 0.75)
img2.save(os.path.join(out_dir, "02-batman-dark-knight.jpg"), quality=95)

# 3. Naruto - Sage Mode Dark Aesthetic
print("Generating Naruto Sage wallpaper...")
img3 = create_base((18, 12, 8), (4, 3, 2))
draw3 = ImageDraw.Draw(img3)
# Dark warm sun / toad mountain mist
for r in range(320, 0, -8):
    t = r / 320
    draw3.ellipse([W//2 - r, 400 - r, W//2 + r, 400 + r], fill=(int(45*(1-t)), int(25*(1-t)), int(10*(1-t))))
# Mountain silhouettes
points = [(0, H), (0, 750), (400, 550), (800, 700), (1200, 520), (1600, 680), (W, 600), (W, H)]
draw3.polygon(points, fill=(6, 5, 4))
img3 = add_vignette(img3, 0.8)
img3.save(os.path.join(out_dir, "03-naruto-sage.jpg"), quality=95)

# 4. Spider-Man - Noir Skyscraper Rain
print("Generating Spider-Man wallpaper...")
img4 = create_base((8, 12, 18), (3, 4, 6))
draw4 = ImageDraw.Draw(img4)
# Webbing pattern in background
for ang in range(0, 360, 30):
    rad = math.radians(ang)
    ex = W//2 + math.cos(rad) * 800
    ey = H//2 + math.sin(rad) * 600
    draw4.line([(W//2, H//2), (ex, ey)], fill=(20, 30, 45), width=2)
for wr in range(100, 700, 90):
    draw4.ellipse([W//2 - wr, H//2 - wr//2, W//2 + wr, H//2 + wr//2], outline=(18, 26, 40), width=1)
# Rain streaks
for _ in range(200):
    rx = random.randint(0, W)
    ry = random.randint(0, H - 80)
    draw4.line([(rx, ry), (rx - 15, ry + 35)], fill=(30, 45, 65), width=1)
img4 = add_vignette(img4, 0.75)
img4.save(os.path.join(out_dir, "04-spiderman-noir.jpg"), quality=95)

# 5. Sasuke - Rinnegan & Chidori Dark Lightning
print("Generating Sasuke wallpaper...")
img5 = create_base((14, 8, 22), (3, 2, 5))
draw5 = ImageDraw.Draw(img5)
# Violet Rinnegan aura center
for r in range(280, 0, -6):
    t = r / 280
    draw5.ellipse([W//2 - r, 380 - r, W//2 + r, 380 + r], fill=(int(50*(1-t)), int(15*(1-t)), int(75*(1-t))))
# Chidori lightning bolts
for _ in range(8):
    lx, ly = W//2, 380
    for _ in range(12):
        nlx = lx + random.randint(-60, 60)
        nly = ly + random.randint(20, 60)
        draw5.line([(lx, ly), (nlx, nly)], fill=(180, 120, 255), width=2)
        lx, ly = nlx, nly
img5 = add_vignette(img5, 0.8)
img5.save(os.path.join(out_dir, "05-sasuke-rinnegan.jpg"), quality=95)

# 6. Zoro - King of Hell Green Flame
print("Generating Zoro wallpaper...")
img6 = create_base((6, 18, 12), (2, 5, 3))
draw6 = ImageDraw.Draw(img6)
# Green spectral flame aura
for r in range(300, 0, -6):
    t = r / 300
    draw6.ellipse([W//2 - r, 420 - r, W//2 + r, 420 + r], fill=(int(15*(1-t)), int(65*(1-t)), int(35*(1-t))))
# Katana blade slash gleam across screen
draw6.line([(100, 200), (W - 100, H - 200)], fill=(80, 255, 160), width=3)
draw6.line([(100, 200), (W - 100, H - 200)], fill=(200, 255, 230), width=1)
img6 = add_vignette(img6, 0.8)
img6.save(os.path.join(out_dir, "06-zoro-demon-aura.jpg"), quality=95)

# Captions file
with open(os.path.join(out_dir, "captions.txt"), "w", encoding="utf-8") as f:
    f.write("01-itachi-uchiha.jpg\tItachi Uchiha · Mangekyo Sharingan\n")
    f.write("02-batman-dark-knight.jpg\tBatman · The Dark Knight\n")
    f.write("03-naruto-sage.jpg\tNaruto Uzumaki · Sage Mode\n")
    f.write("04-spiderman-noir.jpg\tSpider-Man · Noir Vigilante\n")
    f.write("05-sasuke-rinnegan.jpg\tSasuke Uchiha · Rinnegan & Chidori\n")
    f.write("06-zoro-demon-aura.jpg\tRoronoa Zoro · King of Hell\n")

print("All hero wallpapers successfully generated!")

