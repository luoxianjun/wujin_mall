const fs = require("fs");
const path = require("path");
const assert = require("assert");

const html = fs.readFileSync(path.join(__dirname, "index.html"), "utf8");

function count(pattern) {
  return [...html.matchAll(pattern)].length;
}

assert.strictEqual(count(/data-role-option="product"/g), 1, "one selectable product role card is required");
assert.strictEqual(count(/data-role-option="material"/g), 1, "one selectable material role card is required");
assert.strictEqual(count(/data-role-form="product"/g), 1, "one product role form is required");
assert.strictEqual(count(/data-role-form="material"/g), 1, "one material role form is required");
assert.match(html, /selectedRoles:\s*new Set\(\["product"\]\)/, "state should track selected roles as a multi-select set");
assert.match(html, /function toggleMerchantRole\(role/, "role toggle function should exist");
assert.match(html, /function hasMerchantRole\(role/, "role membership helper should exist");
assert.match(html, /\[data-role-option\]/, "click handler should listen for role options");
assert.match(html, /hasMerchantRole\("product"\)/, "wizard validation should include product role");
assert.match(html, /hasMerchantRole\("material"\)/, "wizard validation should include material role");

console.log("role selection prototype contract ok");
