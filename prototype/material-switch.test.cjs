const fs = require("fs");
const path = require("path");
const assert = require("assert");

const html = fs.readFileSync(path.join(__dirname, "index.html"), "utf8");

function count(pattern) {
  return [...html.matchAll(pattern)].length;
}

assert.strictEqual(count(/data-material-option="/g), 3, "overview needs three clickable material options");
assert.match(html, /id="overviewMaterialProducts"/, "overview product strip should be rendered dynamically");
assert.match(html, /id="overviewMaterialDetailBtn"/, "detail button should follow selected material");
assert.match(html, /const overviewMaterialDemos = /, "material demo dataset should exist");
assert.match(html, /function setOverviewMaterial\(materialKey/, "material switching function should exist");
assert.match(html, /\[data-material-option\]/, "click handler should listen for material options");
assert.match(html, /synthetic-rubber/, "synthetic rubber demo data should exist");
assert.match(html, /recycled-rubber/, "recycled rubber demo data should exist");

console.log("material switch prototype contract ok");
