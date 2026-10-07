import { describe, expect, it } from 'vitest';

import { buildInvitationDetailPageParams } from './data';

describe('buildInvitationDetailPageParams', () => {
  it('returns pagination params when form values are absent', () => {
    expect(
      buildInvitationDetailPageParams({
        currentPage: 1,
        pageSize: 20,
      }),
    ).toEqual({
      pageNo: 1,
      pageSize: 20,
    });
  });

  it('maps filters and flattens the date range into begin and end times', () => {
    expect(
      buildInvitationDetailPageParams(
        {
          currentPage: 2,
          pageSize: 50,
        },
        {
          inviterNickname: '用户784787',
          inviteeNickname: '用户049459',
          status: 1,
          registerTime: ['2026-04-01 00:00:00', '2026-04-10 23:59:59'],
        },
      ),
    ).toEqual({
      pageNo: 2,
      pageSize: 50,
      inviterNickname: '用户784787',
      inviteeNickname: '用户049459',
      status: 1,
      beginTime: '2026-04-01 00:00:00',
      endTime: '2026-04-10 23:59:59',
    });
  });
});
