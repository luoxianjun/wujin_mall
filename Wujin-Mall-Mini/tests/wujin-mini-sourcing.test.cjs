const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.resolve(projectRoot, relativePath), "utf8");
}

function readJson(relativePath) {
  return JSON.parse(read(relativePath));
}

test("wujin mini sourcing API wraps supplier and lead endpoints", () => {
  const api = read("src/api/wujin/sourcing.js");

  assert.match(api, /from\s+["']@\/utils\/request["']/);
  assert.match(api, /export function getWujinSupplierCandidates/);
  assert.match(api, /\/wujin\/sourcing\/supplier-candidates/);
  assert.match(api, /keyword/);
  assert.match(api, /lane/);
  assert.match(api, /sourceKeyword/);
  assert.match(api, /export function submitWujinSourcingLead/);
  assert.match(api, /\/wujin\/sourcing\/lead\/submit/);
  assert.match(api, /method:\s*["']POST["']/);
  assert.match(api, /data/);
});

test("wujin mini sourcing page is registered", () => {
  const pages = readJson("src/pages.json");
  const sourcingPage = pages.pages.find(
    (item) => item.path === "pages/wujin/sourcing",
  );

  assert.ok(sourcingPage);
  assert.equal(sourcingPage.style.navigationBarTitleText, "一键寻源");
  assert.equal(sourcingPage.style.navigationStyle, "custom");
});

test("wujin mini search result page navigates upstream trace to sourcing page", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /handleTraceSearch/);
  assert.match(page, /uni\.navigateTo/);
  assert.match(page, /pages\/wujin\/sourcing/);
  assert.match(page, /keyword=\$\{encodeURIComponent\(lastKeyword\.value/);
  assert.match(page, /lane=MATERIAL/);
});

test("wujin mini sourcing page renders suppliers and lead form", () => {
  const page = read("src/pages/wujin/sourcing.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniSourcing["']\s*\}\)/);
  assert.match(page, /getWujinSupplierCandidates/);
  assert.match(page, /submitWujinSourcingLead/);
  assert.match(page, /const keyword = searchKeyword\.value \|\| ["']五金材料["']/);
  assert.match(page, /if \(!searchKeyword\.value\.trim\(\)\)/);
  assert.match(page, /supplierCandidates/);
  assert.match(page, /leadForm/);
  assert.match(page, /loadSupplierCandidates/);
  assert.match(page, /handleSupplierSelect/);
  assert.match(page, /handleLeadSubmit/);
  assert.match(page, /供应商候选/);
  assert.match(page, /需求说明/);
  assert.match(page, /联系方式/);
  assert.match(page, /提交寻源线索/);
});
