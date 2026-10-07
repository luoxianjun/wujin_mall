const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

const quizActivityForm = read(
  'apps/web-antd/src/views/gamification/quiz/activity/modules/form.vue',
);

test('quiz activity form allows hiding leaderboard with size 0', () => {
  assert.match(
    quizActivityForm,
    /leaderboardSize:\s*Number\(props\.initialValue\?\.leaderboardSize\s*\?\?\s*10\)/,
  );

  const leaderboardField = quizActivityForm.match(
    /<FormItem label="排行榜人数">[\s\S]*?<\/FormItem>/,
  )?.[0];

  assert.ok(leaderboardField, 'leaderboard size form item should exist');
  assert.match(leaderboardField, /v-model:value="formState\.leaderboardSize"/);
  assert.match(leaderboardField, /:min="0"/);
  assert.doesNotMatch(leaderboardField, /:min="1"/);
});
