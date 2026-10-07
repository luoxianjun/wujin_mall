const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const repoRoot = path.resolve(__dirname, '..');
const registerVue = fs.readFileSync(
  path.join(repoRoot, 'src/pages/events/register.vue'),
  'utf8',
);
const activityDataTs = fs.readFileSync(
  path.resolve(repoRoot, '../Student-Forum-Web/apps/web-antd/src/views/forum/activity/data.ts'),
  'utf8',
);
const exportControllerJava = fs.readFileSync(
  path.resolve(
    repoRoot,
    '../Student-Forum-Server/yudao-module-forum/src/main/java/cn/iocoder/yudao/module/forum/controller/admin/activity/AdminActivityController.java',
  ),
  'utf8',
);
const exportServiceJava = fs.readFileSync(
  path.resolve(
    repoRoot,
    '../Student-Forum-Server/yudao-module-forum/src/main/java/cn/iocoder/yudao/module/forum/service/activity/ForumActivityServiceImpl.java',
  ),
  'utf8',
);

function extractMethodBody(source, methodName) {
  const signatureMatch = new RegExp(`\\n\\s{4}(?:async\\s+)?${methodName}\\s*\\(`).exec(source);
  assert.ok(signatureMatch, `${methodName} should exist`);
  const signatureIndex = signatureMatch.index;
  const bodyStart = source.indexOf('{', signatureIndex);
  assert.notEqual(bodyStart, -1, `${methodName} should have a body`);

  let depth = 0;
  for (let index = bodyStart; index < source.length; index += 1) {
    const char = source[index];
    if (char === '{') depth += 1;
    if (char === '}') {
      depth -= 1;
      if (depth === 0) {
        return source.slice(bodyStart + 1, index);
      }
    }
  }

  assert.fail(`${methodName} body should close`);
}

test('activity custom field editor exposes upload file field type', () => {
  assert.match(activityDataTs, /label:\s*'上传文件'[\s\S]*value:\s*'file'/);
  assert.match(activityDataTs, /type:\s*'checkbox' \| 'date' \| 'file' \| 'input'/);
});

test('registration page uploads allowed file fields before submit', () => {
  assert.match(registerVue, /import\s+\{\s*uploadFile\s*\}\s+from\s+["']@\/api\/file["']/);
  assert.match(registerVue, /field\.type === 'file'/);
  assert.match(registerVue, /chooseRegistrationFile\(field\)/);
  assert.match(registerVue, /const allowedExtensions = \[[^\]]*'jpg'[^\]]*'png'[^\]]*'pdf'[^\]]*'doc'[^\]]*'docx'[^\]]*\]/);
  assert.match(registerVue, /await uploadFile\(/);
});

test('registration page stores and displays file links as structured values', () => {
  assert.match(registerVue, /url:\s*uploadedUrl/);
  assert.match(registerVue, /getRegisteredFileName\(field\)/);
  assert.match(registerVue, /openRegisteredFile\(field\)/);
  assert.match(registerVue, /val\.url/);
});

test('registration page chooses image attachments from the album', () => {
  const chooseRegistrationFileBody = extractMethodBody(registerVue, 'chooseRegistrationFile');
  assert.match(chooseRegistrationFileBody, /uni\.showActionSheet/);
  assert.doesNotMatch(chooseRegistrationFileBody, /uni\.chooseMessageFile/);

  const chooseRegistrationImageBody = extractMethodBody(registerVue, 'chooseRegistrationImage');
  assert.match(chooseRegistrationImageBody, /uni\.chooseImage/);
  assert.match(chooseRegistrationImageBody, /sourceType:\s*\[\s*'album'\s*\]/);
  assert.match(chooseRegistrationImageBody, /await this\.uploadRegistrationAttachment\(/);

  const uploadRegistrationAttachmentBody = extractMethodBody(registerVue, 'uploadRegistrationAttachment');
  assert.match(uploadRegistrationAttachmentBody, /await uploadFile\(/);
});

test('sign-up export writes dynamic custom field columns with file urls', () => {
  assert.match(exportControllerJava, /writeDynamicSignUpExcel/);
  assert.match(exportServiceJava, /buildSignUpExportRows/);
  assert.match(exportServiceJava, /extractSignUpRemarkValue/);
  assert.match(exportServiceJava, /url/);
});
