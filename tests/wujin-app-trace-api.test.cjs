const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.resolve(__dirname, "..");

function read(relativePath) {
  const absolutePath = path.join(root, relativePath);
  assert.ok(fs.existsSync(absolutePath), `Expected ${relativePath} to exist`);
  return fs.readFileSync(absolutePath, "utf8");
}

test("wujin app trace graph endpoint is exposed for mini program", () => {
  const controller = read(
    "Wujin-Mall-Server/yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/app/trace/WujinAppTraceController.java",
  );
  const reqVO = read(
    "Wujin-Mall-Server/yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/app/trace/vo/WujinTraceGraphReqVO.java",
  );
  const respVO = read(
    "Wujin-Mall-Server/yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/app/trace/vo/WujinTraceGraphRespVO.java",
  );
  const service = read(
    "Wujin-Mall-Server/yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/service/trace/WujinAppTraceServiceImpl.java",
  );

  assert.match(controller, /@RequestMapping\("\/wujin\/trace"\)/);
  assert.match(controller, /@GetMapping\("\/graph"\)/);
  assert.match(controller, /CommonResult<WujinTraceGraphRespVO>/);
  assert.match(reqVO, /private String keyword;/);
  assert.match(reqVO, /private String lane;/);
  assert.match(reqVO, /private String sourceKeyword;/);
  assert.match(respVO, /private List<TraceNode> nodes;/);
  assert.match(respVO, /private List<TraceEdge> edges;/);
  assert.match(respVO, /private TraceBatch currentBatch;/);
  assert.match(service, /WujinChainEntityAdminService/);
  assert.match(service, /WujinChainEntityRelationAdminService/);
});
