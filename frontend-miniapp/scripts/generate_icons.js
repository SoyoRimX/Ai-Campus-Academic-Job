/** Generate tabBar icons for WeChat mini-program. No dependencies needed. */
const zlib = require("zlib");
const fs = require("fs");
const path = require("path");

function createPng(width, height, pixels) {
  function chunk(type, data) {
    const len = Buffer.alloc(4);
    len.writeUInt32BE(data.length);
    const typeAndData = Buffer.concat([Buffer.from(type, "ascii"), data]);
    const crc = Buffer.alloc(4);
    crc.writeUInt32BE(zlib.crc32(typeAndData));
    return Buffer.concat([len, typeAndData, crc]);
  }

  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  const ihdrData = Buffer.alloc(13);
  let off = 0;
  ihdrData.writeUInt32BE(width, off); off += 4;
  ihdrData.writeUInt32BE(height, off); off += 4;
  ihdrData[off++] = 8;  // bit depth
  ihdrData[off++] = 6;  // color type: RGBA
  ihdrData[off++] = 0;  // compression
  ihdrData[off++] = 0;  // filter
  ihdrData[off++] = 0;  // interlace

  // Build raw image data: filter byte 0 + RGBA for each row
  const rawRows = [];
  for (let y = 0; y < height; y++) {
    const row = Buffer.alloc(1 + width * 4);
    row[0] = 0; // filter: None
    for (let x = 0; x < width; x++) {
      const p = pixels[y * width + x];
      const base = 1 + x * 4;
      row[base] = p[0];
      row[base + 1] = p[1];
      row[base + 2] = p[2];
      row[base + 3] = p[3];
    }
    rawRows.push(row);
  }

  return Buffer.concat([
    sig,
    chunk("IHDR", ihdrData),
    chunk("IDAT", zlib.deflateSync(Buffer.concat(rawRows))),
    chunk("IEND", Buffer.alloc(0)),
  ]);
}

function transparent() { return [0, 0, 0, 0]; }
function color(r, g, b, a = 255) { return [r, g, b, a]; }

function drawIcon(drawFn, c, size = 81) {
  const pixels = [];
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      pixels.push(drawFn(x, y, size, c));
    }
  }
  return pixels;
}

function drawHouse(x, y, s, c) {
  // Roof triangle
  if (y >= 12 && y < 35) {
    const roofWidth = Math.floor((y - 12) * 50 / 23 + 10);
    if (x >= 40 - roofWidth && x <= 40 + roofWidth) return c;
  }
  // Body
  if (y >= 30 && y < 75 && x >= 15 && x < 66) {
    // Door
    if (y >= 48 && y < 75 && x >= 32 && x < 49) return color(255, 255, 255);
    return c;
  }
  return transparent();
}

function drawBook(x, y, s, c) {
  // Book body
  if (y >= 20 && y < 65 && x >= 20 && x < 61) {
    // Spine
    if (x >= 38 && x < 43) return color(255, 255, 255, 200);
    // Text lines
    if ([28, 36, 44, 52].includes(y) && ((x >= 24 && x < 36) || (x >= 45 && x < 57))) {
      return color(255, 255, 255, 150);
    }
    return c;
  }
  return transparent();
}

function drawDocument(x, y, s, c) {
  // Paper body
  if (y >= 15 && y < 70 && x >= 18 && x < 63) {
    // Fold corner
    if (y < 30 && x >= 48 && (x - 48) >= (30 - y)) return transparent();
    // Text lines
    if ([28, 36, 44, 52, 60].includes(y) && x >= 26 && x < 56) return color(255, 255, 255, 180);
    return c;
  }
  return transparent();
}

function drawPerson(x, y, s, c) {
  // Head (ellipse)
  const dx = x - 40.5, dy = y - 25;
  if (dx * dx / 169 + dy * dy / 169 <= 1) return c;
  // Body (trapezoid)
  if (y >= 40 && y < 72) {
    const bw = 8 + Math.floor((y - 40) * 6 / 16);
    if (x >= 40 - bw && x <= 40 + bw) return c;
  }
  return transparent();
}

const icons = {
  home: drawHouse,
  academic: drawBook,
  resume: drawDocument,
  profile: drawPerson,
};

const normalColor = color(153, 153, 153);  // #999
const activeColor = color(74, 144, 217);   // #4A90D9

const outputDir = path.join(__dirname, "..", "images");
fs.mkdirSync(outputDir, { recursive: true });

for (const [name, drawFn] of Object.entries(icons)) {
  for (const [suffix, clr] of [["", normalColor], ["-active", activeColor]]) {
    const pixels = drawIcon(drawFn, clr);
    const png = createPng(81, 81, pixels);
    const filePath = path.join(outputDir, `${name}${suffix}.png`);
    fs.writeFileSync(filePath, png);
    console.log(`Created: ${filePath} (${png.length} bytes)`);
  }
}
