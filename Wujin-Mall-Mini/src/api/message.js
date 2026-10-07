import request from "@/utils/request";

export function getLikeList(data) {
  return request({
    url: "/forum/message/likes",
    method: "GET",
    data,
  });
}

export function getCommentList(data) {
  return request({
    url: "/forum/message/comments",
    method: "GET",
    data,
  });
}

export function getFollowList(data) {
  return request({
    url: "/forum/message/follows",
    method: "GET",
    data,
  });
}

export function getUnreadCount() {
  return request({
    url: "/forum/message/unread-count",
    method: "GET",
  });
}

export function getSystemNoticePage(params) {
  return request({
    url: "/forum/message/notice/page",
    method: "GET",
    params,
  });
}

export function markNoticeAsRead(noticeId) {
  return request({
    url: "/forum/message/notice/mark-read",
    method: "POST",
    params: { noticeId },
  });
}

export function markAllNoticesAsRead() {
  return request({
    url: "/forum/message/notice/mark-all-read",
    method: "POST",
  });
}

export function getInteractionUnreadCount() {
  return request({
    url: "/forum/message/interaction/unread-count",
    method: "GET",
  });
}
