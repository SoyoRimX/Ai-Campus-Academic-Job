"""Generate tabBar icons for WeChat mini-program."""
import struct
import zlib
import os

def create_png(width, height, pixels):
    """Create a valid PNG file from RGBA pixel data."""
    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)
        return struct.pack(">I", len(data)) + c + crc

    # PNG signature
    sig = b"\x89PNG\r\n\x1a\n"

    # IHDR
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    ihdr = chunk(b"IHDR", ihdr_data)

    # IDAT - filter byte 0 for each row, then RGBA pixels
    raw = b""
    for y in range(height):
        raw += b"\x00"  # filter: None
        for x in range(width):
            raw += pixels[y * width + x]

    idat = chunk(b"IDAT", zlib.compress(raw))

    # IEND
    iend = chunk(b"IEND", b"")

    return sig + ihdr + idat + iend

def rgba(r, g, b, a=255):
    return struct.pack("BBBB", r, g, b, a)

def draw_circle(cx, cy, r, color, size=81):
    """Draw a filled circle on a transparent buffer."""
    pixels = [rgba(0, 0, 0, 0) for _ in range(size * size)]
    for y in range(size):
        for x in range(size):
            dx, dy = x - cx, y - cy
            if dx*dx + dy*dy <= r*r:
                pixels[y * size + x] = color
    return pixels

def draw_house(color, size=81):
    """Simple house shape."""
    pixels = [rgba(0, 0, 0, 0) for _ in range(size * size)]
    # Body
    for y in range(30, 75):
        for x in range(15, 66):
            pixels[y * size + x] = color
    # Roof
    for y in range(12, 35):
        roof_width = int((y - 12) * 50 / 23 + 10)
        for x in range(40 - roof_width, 41 + roof_width):
            if 0 <= x < size:
                pixels[y * size + x] = color
    # Door
    door_color = rgba(255, 255, 255, 255)
    for y in range(48, 75):
        for x in range(32, 49):
            pixels[y * size + x] = door_color
    return pixels

def draw_book(color, size=81):
    """Simple book shape for academic."""
    pixels = [rgba(0, 0, 0, 0) for _ in range(size * size)]
    # Book body
    for y in range(20, 65):
        for x in range(20, 61):
            pixels[y * size + x] = color
    # Spine line
    spine = rgba(255, 255, 255, 200)
    for y in range(20, 65):
        for x in range(38, 43):
            pixels[y * size + x] = spine
    # Lines on left page
    line_c = rgba(255, 255, 255, 150)
    for lx in range(24, 36):
        for ly in [28, 36, 44, 52]:
            pixels[ly * size + lx] = line_c
    # Lines on right page
    for lx in range(45, 57):
        for ly in [28, 36, 44, 52]:
            pixels[ly * size + lx] = line_c
    return pixels

def draw_document(color, size=81):
    """Simple document shape for resume."""
    pixels = [rgba(0, 0, 0, 0) for _ in range(size * size)]
    # Paper body
    for y in range(15, 70):
        for x in range(18, 63):
            pixels[y * size + x] = color
    # Fold corner
    for y in range(15, 30):
        for x in range(48, 63):
            if x - 48 >= (30 - y):
                pixels[y * size + x] = rgba(0, 0, 0, 0)
    # Lines
    line_c = rgba(255, 255, 255, 180)
    line_y = [28, 36, 44, 52, 60]
    for ly in line_y:
        for lx in range(26, 56):
            pixels[ly * size + lx] = line_c
    return pixels

def draw_person(color, size=81):
    """Simple person shape for profile."""
    pixels = [rgba(0, 0, 0, 0) for _ in range(size * size)]
    # Head (circle)
    for y in range(12, 38):
        for x in range(25, 56):
            dx, dy = x - 40.5, y - 25
            if dx*dx/(13*13) + dy*dy/(13*13) <= 1:
                pixels[y * size + x] = color
    # Body (trapezoid)
    for y in range(40, 72):
        body_width = 8 + (y - 40) * 6 // 16
        for x in range(40 - body_width, 41 + body_width):
            if 0 <= x < size:
                pixels[y * size + x] = color
    return pixels

def main():
    output_dir = os.path.join(os.path.dirname(__file__), "..", "images")
    os.makedirs(output_dir, exist_ok=True)

    icons = {
        "home": draw_house,
        "academic": draw_book,
        "resume": draw_document,
        "profile": draw_person,
    }

    normal_color = rgba(153, 153, 153, 255)   # #999
    active_color = rgba(74, 144, 217, 255)     # #4A90D9

    for name, draw_fn in icons.items():
        # Normal (gray)
        pixels_normal = draw_fn(normal_color)
        png_data = create_png(81, 81, pixels_normal)
        path = os.path.join(output_dir, f"{name}.png")
        with open(path, "wb") as f:
            f.write(png_data)
        print(f"Created: {path} ({len(png_data)} bytes)")

        # Active (blue)
        pixels_active = draw_fn(active_color)
        png_data = create_png(81, 81, pixels_active)
        path = os.path.join(output_dir, f"{name}-active.png")
        with open(path, "wb") as f:
            f.write(png_data)
        print(f"Created: {path} ({len(png_data)} bytes)")

if __name__ == "__main__":
    main()
