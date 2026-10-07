const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const filePath = path.resolve(__dirname, "../src/pages/invitation/register.vue");
const source = fs.readFileSync(filePath, "utf8");

test("invitation register page provides custom back navigation", () => {
  assert.match(source, /<view class="nav-bar" :style="navBarStyle">/);
  assert.match(source, /<BackButton \/>/);
  assert.match(source, /import BackButton from ['"]@\/components\/BackButton\.vue['"]/);
  assert.match(source, /import statusBarMixin from ['"]@\/mixins\/statusBar['"]/);
  assert.match(source, /mixins:\s*\[statusBarMixin\]/);
});

test("invitation register page keeps the white panel inset from screen edges", () => {
  assert.match(source, /\.register-page\s*\{[\s\S]*padding:\s*0 32rpx 56rpx;/);
  assert.match(source, /\.main-card\s*\{[\s\S]*width:\s*calc\(100% - 64rpx\);/);
  assert.match(source, /\.main-card\s*\{[\s\S]*box-sizing:\s*border-box;/);
});

test("invitation register page omits the logo block to keep the panel compact", () => {
  assert.doesNotMatch(source, /class="brand-logo-wrap"/);
  assert.doesNotMatch(source, /class="brand-logo-img"/);
  assert.doesNotMatch(source, /\.brand-logo-wrap\s*\{/);
  assert.match(source, /\.main-card\s*\{[\s\S]*padding:\s*48rpx 40rpx 48rpx;/);
});
