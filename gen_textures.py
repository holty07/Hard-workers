#!/usr/bin/env python3
"""
Generate template textures for the Hard Workers mod.

Block textures:  16x16, one per face per tier  (front / back / top / bottom / side)
Entity texture:  64x64 standard Minecraft skin layout (edit this in any pixel editor)

Run from the project root:  python3 gen_textures.py
"""

import struct, zlib, os

# ---------------------------------------------------------------------------
# Minimal PNG writer (no Pillow required)
# ---------------------------------------------------------------------------

def _chunk(tag, data):
    crc = zlib.crc32(tag + data) & 0xFFFFFFFF
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", crc)

def write_png(path, w, h, pixels):
    """pixels: flat list of (r,g,b,a) tuples, row-major."""
    raw = b"".join(
        b"\x00" + b"".join(bytes(pixels[y * w + x]) for x in range(w))
        for y in range(h)
    )
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(_chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)))
        f.write(_chunk(b"IDAT", zlib.compress(raw)))
        f.write(_chunk(b"IEND", b""))
    print(f"  wrote {path}")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def clamp(v): return max(0, min(255, v))
def shade(c, d): return tuple(clamp(v + d) for v in c)

def fill(pixels, w, x, y, fw, fh, color):
    for py in range(y, y + fh):
        for px in range(x, x + fw):
            if 0 <= px < w and 0 <= py < len(pixels) // w:
                pixels[py * w + px] = color

# ---------------------------------------------------------------------------
# Block face templates  (16x16)
# ---------------------------------------------------------------------------

TIERS = {
    "wood":      (139, 100,  40),
    "stone":     (115, 115, 115),
    "iron":      (175, 180, 190),
    "diamond":   ( 45, 195, 210),
    "netherite": ( 58,  28,  75),
}

def block_face(base, variant):
    r, g, b = base
    pix = []
    for y in range(16):
        for x in range(16):
            border = x == 0 or x == 15 or y == 0 or y == 15
            dark = shade(base, -45)
            if variant == "front":
                # two glowing "eye" pixels, dark border, small mouth line
                left_eye  = (x in (4, 5) and y in (5, 6))
                right_eye = (x in (10, 11) and y in (5, 6))
                mouth     = (5 <= x <= 10 and y == 11)
                if border:
                    pix.append(dark + (255,))
                elif left_eye or right_eye:
                    pix.append((220, 220, 20, 255))   # yellow "eyes"
                elif mouth:
                    pix.append(dark + (255,))
                else:
                    pix.append((r, g, b, 255))
            elif variant == "top":
                light = shade(base, +35)
                # cross marker so you can tell which way is up
                cross = (x == 8 or y == 8)
                if border:
                    pix.append(shade(base, -20) + (255,))
                elif cross:
                    pix.append(shade(base, +55) + (255,))
                else:
                    pix.append(light + (255,))
            elif variant == "bottom":
                if border:
                    pix.append(shade(base, -65) + (255,))
                else:
                    pix.append(shade(base, -40) + (255,))
            elif variant == "back":
                # diagonal stripe to distinguish back from side
                stripe = ((x + y) % 6 == 0)
                if border:
                    pix.append(dark + (255,))
                elif stripe:
                    pix.append(shade(base, -25) + (255,))
                else:
                    pix.append(shade(base, +10) + (255,))
            else:  # side
                if border:
                    pix.append(dark + (255,))
                else:
                    pix.append((r, g, b, 255))
    return pix

BLOCK_TEX = "src/main/resources/assets/hardworkers/textures/block"

for tier, color in TIERS.items():
    for face in ("front", "back", "top", "bottom", "side"):
        write_png(f"{BLOCK_TEX}/lumberjack_{tier}_{face}.png", 16, 16,
                  block_face(color, face))

# Miner block textures — same colour palette, front shows a pickaxe-hint
# (two small bright dots for drill bits instead of eyes)
MINER_TIERS = {
    "wood":      (120,  85,  40),   # warm brown, darker than lumberjack
    "stone":     ( 90,  90,  95),   # darker grey
    "iron":      (155, 160, 175),   # steely blue-grey
    "diamond":   ( 30, 170, 190),   # deeper cyan
    "netherite": ( 40,  18,  55),   # deep purple
}

def miner_face(base, variant):
    r, g, b = base
    pix = []
    for y in range(16):
        for x in range(16):
            border = x == 0 or x == 15 or y == 0 or y == 15
            dark = shade(base, -45)
            if variant == "front":
                # Two drill-bit dots + lower vent grill
                bit_l = (x in (4, 5) and y in (4, 5, 6))
                bit_r = (x in (10, 11) and y in (4, 5, 6))
                grill = (4 <= x <= 11 and y in (10, 12) and x % 2 == 0)
                if border:
                    pix.append(dark + (255,))
                elif bit_l or bit_r:
                    pix.append((210, 210, 255, 255))  # bright tip
                elif grill:
                    pix.append(shade(base, -30) + (255,))
                else:
                    pix.append((r, g, b, 255))
            elif variant == "top":
                cross = (x == 8 or y == 8)
                if border:
                    pix.append(shade(base, -20) + (255,))
                elif cross:
                    pix.append(shade(base, +55) + (255,))
                else:
                    pix.append(shade(base, +30) + (255,))
            elif variant == "bottom":
                if border:
                    pix.append(shade(base, -65) + (255,))
                else:
                    pix.append(shade(base, -40) + (255,))
            elif variant == "back":
                stripe = ((x + y) % 6 == 0)
                if border:
                    pix.append(dark + (255,))
                elif stripe:
                    pix.append(shade(base, -25) + (255,))
                else:
                    pix.append(shade(base, +10) + (255,))
            else:  # side
                if border:
                    pix.append(dark + (255,))
                else:
                    pix.append((r, g, b, 255))
    return pix

for tier, color in MINER_TIERS.items():
    for face in ("front", "back", "top", "bottom", "side"):
        write_png(f"{BLOCK_TEX}/miner_{tier}_{face}.png", 16, 16,
                  miner_face(color, face))

# ---------------------------------------------------------------------------
# Entity skin template  (64x64, standard Minecraft player UV layout)
#
#  Edit lumberjack.png in any pixel editor.  UV regions:
#
#  ┌─────────────────────────────────────────────────────────────────────────┐
#  │ HEAD                         │ HEAD OVERLAY (hat / hair)               │
#  │  top   (8,0  8×8)            │  top    (40,0  8×8)                     │
#  │  bot   (16,0  8×8)           │  bot    (48,0  8×8)                     │
#  │  right (0,8  8×8)            │  right  (32,8  8×8)                     │
#  │  front (8,8  8×8)  ← face   │  front  (40,8  8×8)  ← hat brim        │
#  │  left  (16,8  8×8)           │  left   (48,8  8×8)                     │
#  │  back  (24,8  8×8)           │  back   (56,8  8×8)                     │
#  ├─────────────────────────────────────────────────────────────────────────┤
#  │ RIGHT LEG        │ BODY                    │ RIGHT ARM                 │
#  │  top  (4,16 4×4) │  top  (20,16 8×4)       │  top  (44,16 4×4)        │
#  │  bot  (8,16 4×4) │  bot  (28,16 8×4)       │  bot  (48,16 4×4)        │
#  │  r    (0,20 4×12)│  r    (16,20 4×12)      │  r    (40,20 4×12)       │
#  │  f    (4,20 4×12)│  f    (20,20 8×12) ←shirt│  f   (44,20 4×12)       │
#  │  l    (8,20 4×12)│  l    (28,20 4×12)      │  l    (48,20 4×12)       │
#  │  b   (12,20 4×12)│  b    (32,20 8×12)      │  b    (52,20 4×12)       │
#  ├─────────────────────────────────────────────────────────────────────────┤
#  │ LEFT LEG (new-format 64px skin)            │ LEFT ARM                  │
#  │  top  (20,48 4×4)                          │  top  (36,48 4×4)         │
#  │  bot  (24,48 4×4)                          │  bot  (40,48 4×4)         │
#  │  r    (16,52 4×12)                         │  r    (32,52 4×12)        │
#  │  f    (20,52 4×12)                         │  f    (36,52 4×12)        │
#  │  l    (24,52 4×12)                         │  l    (40,52 4×12)        │
#  │  b    (28,52 4×12)                         │  b    (44,52 4×12)        │
#  └─────────────────────────────────────────────────────────────────────────┘
# ---------------------------------------------------------------------------

SKIN  = (200, 150, 100, 255)   # face / hands
SKIN2 = (185, 135,  88, 255)   # face side / darker skin
BEARD = ( 90,  55,  20, 255)   # beard / hair
EYE   = ( 40,  30,  20, 255)
HAT_F = ( 55,  90,  35, 255)   # green logger hat – front
HAT_S = ( 45,  75,  28, 255)   # hat sides
HAT_T = ( 65, 105,  42, 255)   # hat top
SHIRT_F = ( 70,  90, 150, 255) # blue shirt front
SHIRT_S = ( 55,  72, 125, 255) # shirt sides / back
SUSP  = ( 95,  65,  25, 255)   # brown suspenders
PANTS_F = ( 45,  60, 100, 255) # dark trousers
PANTS_S = ( 38,  50,  85, 255)
BOOT  = ( 48,  34,  18, 255)   # leather boots
BOOT2 = ( 38,  26,  12, 255)
TRANS = (  0,   0,   0,   0)   # transparent (unused skin area)

W = H = 64
pixels = [TRANS] * (W * H)

def f(x, y, fw, fh, color):
    fill(pixels, W, x, y, fw, fh, color)

# ── HEAD ────────────────────────────────────────────────────────────────────
f( 8, 0, 8, 8, SKIN)           # head top
f(16, 0, 8, 8, SKIN2)          # head bottom
f( 0, 8, 8, 8, SKIN2)          # head right
f( 8, 8, 8, 8, SKIN)           # head front  ← paint the face here
f(16, 8, 8, 8, SKIN2)          # head left
f(24, 8, 8, 8, SKIN2)          # head back
# face details on front (8,8)
f(10,10, 2, 2, EYE)            # left eye
f(13,10, 2, 2, EYE)            # right eye
f( 9,13, 6, 2, BEARD)          # beard

# ── HAT (head overlay) ──────────────────────────────────────────────────────
f(40, 0, 8, 8, HAT_T)          # hat top
f(48, 0, 8, 8, HAT_S)          # hat bottom inside
f(32, 8, 8, 8, HAT_S)          # hat right
f(40, 8, 8, 8, HAT_F)          # hat front  ← wide brim suggestion
f(48, 8, 8, 8, HAT_S)          # hat left
f(56, 8, 8, 8, HAT_S)          # hat back

# ── BODY ────────────────────────────────────────────────────────────────────
f(20,16, 8, 4, SHIRT_S)        # body top
f(28,16, 8, 4, SHIRT_S)        # body bottom
f(16,20, 4,12, SHIRT_S)        # body right
f(20,20, 8,12, SHIRT_F)        # body front  ← shirt
f(28,20, 4,12, SHIRT_S)        # body left
f(32,20, 8,12, SHIRT_S)        # body back
# suspenders drawn over shirt front
f(21,20, 1,12, SUSP)
f(26,20, 1,12, SUSP)

# ── RIGHT LEG ───────────────────────────────────────────────────────────────
f( 4,16, 4, 4, PANTS_S)        # top
f( 8,16, 4, 4, PANTS_S)        # bottom
f( 0,20, 4,12, PANTS_S)        # right
f( 4,20, 4,12, PANTS_F)        # front
f( 8,20, 4,12, PANTS_S)        # left
f(12,20, 4,12, PANTS_S)        # back
f( 0,28, 4, 4, BOOT2)          # boot right
f( 4,28, 4, 4, BOOT)           # boot front
f( 8,28, 4, 4, BOOT2)          # boot left
f(12,28, 4, 4, BOOT2)          # boot back

# ── LEFT LEG ────────────────────────────────────────────────────────────────
f(20,48, 4, 4, PANTS_S)
f(24,48, 4, 4, PANTS_S)
f(16,52, 4,12, PANTS_S)
f(20,52, 4,12, PANTS_F)
f(24,52, 4,12, PANTS_S)
f(28,52, 4,12, PANTS_S)
f(16,60, 4, 4, BOOT2)
f(20,60, 4, 4, BOOT)
f(24,60, 4, 4, BOOT2)
f(28,60, 4, 4, BOOT2)

# ── RIGHT ARM ───────────────────────────────────────────────────────────────
f(44,16, 4, 4, SHIRT_S)        # top
f(48,16, 4, 4, SHIRT_S)        # bottom
f(40,20, 4,12, SHIRT_S)        # right
f(44,20, 4,12, SHIRT_F)        # front
f(48,20, 4,12, SHIRT_S)        # left
f(52,20, 4,12, SHIRT_S)        # back
f(40,28, 4, 4, SKIN2)          # hand right
f(44,28, 4, 4, SKIN)           # hand front
f(48,28, 4, 4, SKIN2)          # hand left
f(52,28, 4, 4, SKIN2)          # hand back

# ── LEFT ARM ────────────────────────────────────────────────────────────────
f(36,48, 4, 4, SHIRT_S)
f(40,48, 4, 4, SHIRT_S)
f(32,52, 4,12, SHIRT_S)
f(36,52, 4,12, SHIRT_F)
f(40,52, 4,12, SHIRT_S)
f(44,52, 4,12, SHIRT_S)
f(32,60, 4, 4, SKIN2)
f(36,60, 4, 4, SKIN)
f(40,60, 4, 4, SKIN2)
f(44,60, 4, 4, SKIN2)

write_png("src/main/resources/assets/hardworkers/textures/entity/lumberjack.png",
          64, 64, pixels)

# ---------------------------------------------------------------------------
# Miner entity skin  (64x64)
# Colour scheme: grey hard-hat, brown leather jacket, dark trousers, boots
# Same UV layout as the lumberjack skin — edit in any pixel editor.
# ---------------------------------------------------------------------------

MHAT_F = ( 50,  50,  55, 255)   # hard hat front (dark grey)
MHAT_S = ( 40,  40,  45, 255)   # hard hat sides
MHAT_T = ( 60,  60,  65, 255)   # hard hat top
MJACK_F = ( 90,  65,  35, 255)  # leather jacket front (brown)
MJACK_S = ( 70,  50,  25, 255)  # jacket sides
MPANTS_F = ( 55,  55,  60, 255) # dark grey trousers
MPANTS_S = ( 45,  45,  50, 255)
MBOOT    = ( 35,  25,  15, 255)
MBOOT2   = ( 25,  18,  10, 255)
LAMP     = (220, 200,  50, 255)  # lamp dot on hard hat

mp = [TRANS] * (64 * 64)

def mf(x, y, fw, fh, color):
    fill(mp, 64, x, y, fw, fh, color)

# ── HEAD ────────────────────────────────────────────────────────────────────
mf( 8, 0, 8, 8, SKIN)
mf(16, 0, 8, 8, SKIN2)
mf( 0, 8, 8, 8, SKIN2)
mf( 8, 8, 8, 8, SKIN)       # face
mf(16, 8, 8, 8, SKIN2)
mf(24, 8, 8, 8, SKIN2)
mf(10,10, 2, 2, EYE)
mf(13,10, 2, 2, EYE)
mf( 9,13, 6, 2, BEARD)

# ── HARD HAT (head overlay) ──────────────────────────────────────────────────
mf(40, 0, 8, 8, MHAT_T)    # top — with lamp dot
mf(48, 0, 8, 8, MHAT_S)    # bottom inside
mf(32, 8, 8, 8, MHAT_S)    # right
mf(40, 8, 8, 8, MHAT_F)    # front
mf(48, 8, 8, 8, MHAT_S)    # left
mf(56, 8, 8, 8, MHAT_S)    # back
# headlamp on hat top (centre-ish)
mf(43, 2, 2, 2, LAMP)

# ── BODY — leather jacket ────────────────────────────────────────────────────
mf(20,16, 8, 4, MJACK_S)
mf(28,16, 8, 4, MJACK_S)
mf(16,20, 4,12, MJACK_S)
mf(20,20, 8,12, MJACK_F)
mf(28,20, 4,12, MJACK_S)
mf(32,20, 8,12, MJACK_S)

# ── RIGHT LEG ───────────────────────────────────────────────────────────────
mf( 4,16, 4, 4, MPANTS_S)
mf( 8,16, 4, 4, MPANTS_S)
mf( 0,20, 4,12, MPANTS_S)
mf( 4,20, 4,12, MPANTS_F)
mf( 8,20, 4,12, MPANTS_S)
mf(12,20, 4,12, MPANTS_S)
mf( 0,28, 4, 4, MBOOT2)
mf( 4,28, 4, 4, MBOOT)
mf( 8,28, 4, 4, MBOOT2)
mf(12,28, 4, 4, MBOOT2)

# ── LEFT LEG ────────────────────────────────────────────────────────────────
mf(20,48, 4, 4, MPANTS_S)
mf(24,48, 4, 4, MPANTS_S)
mf(16,52, 4,12, MPANTS_S)
mf(20,52, 4,12, MPANTS_F)
mf(24,52, 4,12, MPANTS_S)
mf(28,52, 4,12, MPANTS_S)
mf(16,60, 4, 4, MBOOT2)
mf(20,60, 4, 4, MBOOT)
mf(24,60, 4, 4, MBOOT2)
mf(28,60, 4, 4, MBOOT2)

# ── RIGHT ARM ───────────────────────────────────────────────────────────────
mf(44,16, 4, 4, MJACK_S)
mf(48,16, 4, 4, MJACK_S)
mf(40,20, 4,12, MJACK_S)
mf(44,20, 4,12, MJACK_F)
mf(48,20, 4,12, MJACK_S)
mf(52,20, 4,12, MJACK_S)
mf(40,28, 4, 4, SKIN2)
mf(44,28, 4, 4, SKIN)
mf(48,28, 4, 4, SKIN2)
mf(52,28, 4, 4, SKIN2)

# ── LEFT ARM ────────────────────────────────────────────────────────────────
mf(36,48, 4, 4, MJACK_S)
mf(40,48, 4, 4, MJACK_S)
mf(32,52, 4,12, MJACK_S)
mf(36,52, 4,12, MJACK_F)
mf(40,52, 4,12, MJACK_S)
mf(44,52, 4,12, MJACK_S)
mf(32,60, 4, 4, SKIN2)
mf(36,60, 4, 4, SKIN)
mf(40,60, 4, 4, SKIN2)
mf(44,60, 4, 4, SKIN2)

write_png("src/main/resources/assets/hardworkers/textures/entity/miner.png",
          64, 64, mp)

print("\nDone!  Edit the PNGs in any pixel editor (e.g. Aseprite, GIMP, Pixelorama).")
print("Block textures are 16x16; entity skins are 64x64 (standard Minecraft layout).")
