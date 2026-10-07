const BLOCKED_IMAGE_PATTERNS = [
  /^<url>$/i,
  /^(https?:)?\/\/([^/]+\.)?example\.com(?:[/:?#]|$)/i,
  /^(https?:)?\/\/([^/]+\.)?placeholder\.com(?:[/:?#]|$)/i,
  /^(https?:)?\/\/via\.placeholder\.com(?:[/:?#]|$)/i,
  /^(https?:)?\/\/lh3\.googleusercontent\.com\/aida-public\//i,
];

export function sanitizeWujinImageUrl(value) {
  const text = typeof value === "string" ? value.trim() : "";
  if (!text) {
    return "";
  }
  return BLOCKED_IMAGE_PATTERNS.some((pattern) => pattern.test(text)) ? "" : text;
}

export function sanitizeWujinImageList(values) {
  const list = Array.isArray(values) ? values : [values];
  return list
    .map(sanitizeWujinImageUrl)
    .filter(Boolean);
}
