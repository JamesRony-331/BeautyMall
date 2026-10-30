export function changedProfile(original, current) {
  return Object.fromEntries(
    ['realName', 'phone', 'avatar']
      .filter(
        (key) =>
          key in current &&
          String(current[key] ?? '').trim() !== String(original[key] ?? '').trim(),
      )
      .map((key) => [key, String(current[key] ?? '').trim()]),
  )
}

export function safeRedirect(value) {
  if (
    typeof value !== 'string' ||
    !value.startsWith('/') ||
    value.startsWith('//') ||
    /[\\\s]/.test(value)
  )
    return '/'
  const path = value.split(/[?#]/)[0]
  return ['/', '/profile', '/products'].includes(path) ? value : '/'
}

export function safeAvatar(value) {
  try {
    const url = new URL(value)
    return url.protocol === 'https:' ? url.href : ''
  } catch {
    return ''
  }
}
