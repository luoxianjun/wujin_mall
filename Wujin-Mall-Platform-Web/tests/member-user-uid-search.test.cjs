const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');
const workspaceRoot = path.resolve(webRoot, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(workspaceRoot, relativePath), 'utf8');
}

const memberUserData = read(
  'Student-Forum-Web/apps/web-antd/src/views/member/user/data.ts',
);
const memberUserApi = read(
  'Student-Forum-Web/apps/web-antd/src/api/member/user/index.ts',
);
const pageReqVo = read(
  'Student-Forum-Server/yudao-module-member/src/main/java/cn/iocoder/yudao/module/member/controller/admin/user/vo/MemberUserPageReqVO.java',
);
const controller = read(
  'Student-Forum-Server/yudao-module-member/src/main/java/cn/iocoder/yudao/module/member/controller/admin/user/MemberUserController.java',
);
const mapper = read(
  'Student-Forum-Server/yudao-module-member/src/main/java/cn/iocoder/yudao/module/member/dal/mysql/user/MemberUserMapper.java',
);

test('member user grid exposes UID search field', () => {
  assert.match(memberUserData, /fieldName:\s*'uid'[\s\S]*label:\s*'会员UID'[\s\S]*component:\s*'Input'/);
});

test('member user page request accepts uid and internal user ids', () => {
  assert.match(memberUserApi, /export interface UserPageReq extends PageParam/);
  assert.match(memberUserApi, /uid\?: string/);
  assert.match(pageReqVo, /private String uid;/);
  assert.match(pageReqVo, /private Set<Long> userIds;/);
});

test('member user backend resolves uid to user ids before paging', () => {
  assert.match(controller, /getUserIdsByUidLike\(pageVO\.getUid\(\)\.trim\(\)\)/);
  assert.match(controller, /pageVO\.setUserIds\(new HashSet<>\(userIds\)\)/);
  assert.match(mapper, /\.inIfPresent\(MemberUserDO::getId, reqVO\.getUserIds\(\)\)/);
});
