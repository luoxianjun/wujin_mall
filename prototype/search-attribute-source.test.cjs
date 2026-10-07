const fs = require("fs");
const path = require("path");
const assert = require("assert");

const html = fs.readFileSync(path.join(__dirname, "index.html"), "utf8");

assert.match(html, /平台属性字典/, "merchant and admin UI should explain the platform attribute dictionary source");
assert.match(html, /标准属性由平台维护/, "merchant UI should clarify standard attributes are maintained by platform");
assert.match(html, /商家只选择或填写属性值/, "merchant UI should clarify merchants fill values instead of creating standard attributes");
assert.match(html, /自定义标签.*商家可添加.*需审核/, "merchant custom tags should be distinct from standard attributes and need review");
assert.match(html, /data-attribute-source="platform-dictionary"/, "attribute cards should expose platform dictionary source");
assert.match(html, /data-attribute-source="category-inherit"/, "attribute cards should expose category inherited source");
assert.match(html, /data-attribute-source="template-role"/, "attribute cards should expose template or role source");
assert.match(html, /data-admin-nav="attribute"/, "admin sidebar should expose attribute dictionary config");
assert.match(html, /data-admin-section="attribute"/, "admin page should include attribute dictionary section");
assert.match(html, /data-admin-action="attribute-create"/, "admin attribute create action should exist");
assert.match(html, /data-admin-action="attribute-save"/, "admin attribute save action should exist");
assert.match(html, /data-admin-action="attribute-approve-tag"/, "admin custom tag approval action should exist");

console.log("search attribute source contract ok");
