const fs = require("fs");
const path = require("path");
const assert = require("assert");

const html = fs.readFileSync(path.join(__dirname, "index.html"), "utf8");

assert.match(html, /const merchantMaterialRoleScenarios = /, "merchant material-role scenario dataset should exist");
assert.match(html, /tire:\s*{/, "tire demo should have its own material-role scenarios");
assert.match(html, /整车配套件/, "tire material role should show downstream assembly use case");
assert.match(html, /维修替换耗材/, "tire material role should show replacement/service use case");
assert.match(html, /rubber:\s*{/, "rubber demo should keep rubber-specific scenarios");
assert.match(html, /function renderMerchantMaterialRoleScenarios/, "scenario renderer should exist");
assert.match(html, /id="merchantMaterialRoleScenarioGrid"/, "scenario grid should be dynamic");
assert.match(html, /id="merchantMaterialRoleRelationList"/, "relation list should be dynamic");

console.log("merchant material-role scenario contract ok");
