import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace QuizApi {
  export interface RewardRule {
    id?: number;
    rankStart: number;
    rankEnd: number;
    rewardType: 'PHYSICAL' | 'POINTS';
    pointAmount: number;
    rewardName: string;
  }

  export interface QuizActivity {
    id?: number;
    activityId: number;
    activityTitle?: string;
    activityStartTime?: string;
    activityEndTime?: string;
    questionBankId: number;
    questionBankName?: string;
    questionBankQuestionCount?: number;
    questionBankCreateTime?: string;
    questionBankUpdateTime?: string;
    questionCount: number;
    maxAttempts: number;
    durationSeconds: number;
    randomQuestionOrder: boolean;
    randomOptionOrder: boolean;
    leaderboardSize: number;
    answerRevealMode:
      | 'AFTER_ACTIVITY_END'
      | 'AFTER_SUBMIT'
      | 'HIDDEN'
      | 'PER_QUESTION';
    status: 'DISABLED' | 'DRAFT' | 'ENABLED';
    rewardRules?: RewardRule[];
  }

  export interface QuizActivityPageReq extends PageParam {
    activityId?: number;
    questionBankId?: number;
    status?: string;
  }

  export interface QuestionOption {
    id?: number;
    optionKey: string;
    content: string;
    isCorrect?: boolean;
    sort?: number;
  }

  export interface Question {
    id?: number;
    questionType: 'MULTIPLE_CHOICE' | 'SINGLE_CHOICE' | 'TRUE_FALSE';
    content: string;
    imageUrl?: string;
    score: number;
    explanation?: string;
    sort?: number;
    options: QuestionOption[];
  }

  export interface QuestionBank {
    id?: number;
    name: string;
    description?: string;
    enabled?: boolean;
    questionCount?: number;
    questionCountDisplay?: number;
    createTime?: string;
    updateTime?: string;
    questions: Question[];
  }

  export interface QuestionBankPageReq extends PageParam {
    name?: string;
    enabled?: boolean;
  }

  export interface ImportResult {
    successCount: number;
    failureCount: number;
    failureMessages: string[];
  }
}

export function createQuizActivity(data: QuizApi.QuizActivity) {
  return requestClient.post<number>('/gamification/quiz/activity/create', data);
}

export function updateQuizActivity(data: QuizApi.QuizActivity) {
  return requestClient.put<boolean>('/gamification/quiz/activity/update', data);
}

export function getQuizActivity(id: number) {
  return requestClient.get<QuizApi.QuizActivity>(
    `/gamification/quiz/activity/get?id=${id}`,
  );
}

export function getQuizActivityPage(params: QuizApi.QuizActivityPageReq) {
  return requestClient.get<PageResult<QuizApi.QuizActivity>>(
    '/gamification/quiz/activity/page',
    { params },
  );
}

export function createQuestionBank(data: QuizApi.QuestionBank) {
  return requestClient.post<number>(
    '/gamification/quiz/question-bank/create',
    data,
  );
}

export function updateQuestionBank(data: QuizApi.QuestionBank) {
  return requestClient.put<boolean>(
    '/gamification/quiz/question-bank/update',
    data,
  );
}

export function getQuestionBank(id: number) {
  return requestClient.get<QuizApi.QuestionBank>(
    `/gamification/quiz/question-bank/get?id=${id}`,
  );
}

export function getQuestionBankPage(params: QuizApi.QuestionBankPageReq) {
  return requestClient.get<PageResult<QuizApi.QuestionBank>>(
    '/gamification/quiz/question-bank/page',
    { params },
  );
}

export function importQuestionBankQuestions(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return requestClient.post<QuizApi.ImportResult>(
    '/gamification/quiz/question-bank/import',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 60_000,
    },
  );
}

export function downloadQuestionBankImportTemplate() {
  return requestClient.download(
    '/gamification/quiz/question-bank/get-import-template',
  );
}

export namespace QuizAttemptApi {
  export interface QuizAttempt {
    id: number;
    quizActivityId: number;
    activityId: number;
    activityTitle?: string;
    userId: number;
    uid?: string;
    userNickname?: string;
    userMobile?: string;
    activityRank?: number;
    signUpRemark?: string;
    attemptNo: number;
    status: string;
    score: number;
    elapsedMillis: number;
    startedAt?: string;
    submittedAt?: string;
    invalidatedReason?: string;
    createTime?: string;
  }

  export interface QuizAttemptPageReq extends PageParam {
    quizActivityId?: number;
    activityId?: number;
    userId?: number;
    status?: string;
    userMobile?: string;
    userNickname?: string;
    submittedAt?: string[];
  }
}

export function getQuizAttemptPage(params: QuizAttemptApi.QuizAttemptPageReq) {
  return requestClient.get<PageResult<QuizAttemptApi.QuizAttempt>>(
    '/gamification/quiz/attempt/page',
    { params },
  );
}

export function exportQuizAttempt(
  params?: Omit<QuizAttemptApi.QuizAttemptPageReq, 'pageNo' | 'pageSize'>,
) {
  return requestClient.download('/gamification/quiz/attempt/export', {
    params,
  });
}
