const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

const quizApi = read('apps/web-antd/src/api/gamification/quiz.ts');
const attemptsPage = read(
  'apps/web-antd/src/views/gamification/quiz/attempts/index.vue',
);

test('quiz attempt API exposes enriched fields and export endpoint', () => {
  assert.match(quizApi, /activityTitle\?: string/);
  assert.match(quizApi, /uid\?: string/);
  assert.match(quizApi, /activityRank\?: number/);
  assert.match(quizApi, /signUpRemark\?: string/);
  assert.match(
    quizApi,
    /export function exportQuizAttempt\(\s*params\?: Omit<QuizAttemptApi\.QuizAttemptPageReq, 'pageNo' \| 'pageSize'>/,
  );
  assert.match(
    quizApi,
    /requestClient\.download\('\/gamification\/quiz\/attempt\/export'/,
  );
});

test('quiz attempt page shows enriched columns and exports current filters', () => {
  assert.match(attemptsPage, /downloadFileFromBlobPart/);
  assert.match(attemptsPage, /exportQuizAttempt/);
  assert.match(attemptsPage, /const exporting = ref\(false\)/);
  assert.match(attemptsPage, /function buildQueryParams\(\)/);
  assert.match(
    attemptsPage,
    /const data = await exportQuizAttempt\(buildQueryParams\(\)\)/,
  );
  assert.match(attemptsPage, /fileName: '答题记录\.xlsx'/);
  assert.match(attemptsPage, /dataIndex: 'activityTitle'/);
  assert.match(attemptsPage, /dataIndex: 'uid'/);
  assert.match(attemptsPage, /dataIndex: 'activityRank'/);
  assert.match(attemptsPage, /dataIndex: 'signUpRemark'/);
});
