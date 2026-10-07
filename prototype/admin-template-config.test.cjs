const fs = require("fs");
const path = require("path");
const assert = require("assert");

const html = fs.readFileSync(path.join(__dirname, "index.html"), "utf8");

assert.match(html, /data-admin-nav="template"/, "admin sidebar should expose relation template config");
assert.match(html, /data-admin-section="template"/, "admin page should include relation template section");
assert.match(html, /乘用车轮胎模板 v3\.2/, "platform template should match merchant-side tire template");
assert.match(html, /模板基础信息/, "template editor should expose base information");
assert.match(html, /模板材料配置/, "template editor should expose material config");
assert.match(html, /模板工艺配置/, "template editor should expose process config");
assert.match(html, /自动通过规则/, "template editor should expose auto approval rules");
assert.match(html, /data-admin-action="template-create"/, "template create action should exist");
assert.match(html, /data-admin-action="template-save"/, "template save action should exist");
assert.match(html, /data-admin-action="template-preview"/, "template preview action should exist");

console.log("admin template config contract ok");
