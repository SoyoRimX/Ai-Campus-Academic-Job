// 从 .env 文件读取配置并注入到 app.json
// 用法: node scripts/inject-config.js
const fs = require('fs');
const path = require('path');

const envPath = path.join(__dirname, '..', '.env');
const appJsonPath = path.join(__dirname, '..', 'app.json');

if (!fs.existsSync(envPath)) {
  console.error('❌ 未找到 .env 文件，请从 .env.example 复制并填写配置');
  console.error('   cp .env.example .env');
  process.exit(1);
}

// 读取 .env
const envContent = fs.readFileSync(envPath, 'utf-8');
const env = {};
envContent.split('\n').forEach(line => {
  const trimmed = line.trim();
  if (!trimmed || trimmed.startsWith('#')) return;
  const eqIdx = trimmed.indexOf('=');
  if (eqIdx === -1) return;
  env[trimmed.slice(0, eqIdx)] = trimmed.slice(eqIdx + 1);
});

// 读取 app.json
let appJson = fs.readFileSync(appJsonPath, 'utf-8');

// 替换占位符
const placeholders = {
  YOUR_WECHATSI_PLUGIN_ID: env.WECHATSI_PLUGIN_ID || 'wx069ba97219f66d99',
};

for (const [placeholder, value] of Object.entries(placeholders)) {
  appJson = appJson.replace(new RegExp(placeholder, 'g'), value);
}

fs.writeFileSync(appJsonPath, appJson, 'utf-8');
console.log('✅ 配置已注入到 app.json');
