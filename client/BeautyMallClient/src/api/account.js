export function createAccountApi({
  base = '/api',
  fetcher = globalThis.fetch,
  token = () => '',
  onUnauthorized = () => {},
} = {}) {
  async function request(path, body, form = false) {
    const headers = {
      'Content-Type': form ? 'application/x-www-form-urlencoded' : 'application/json',
    }
    if (!form && token()) headers.Authorization = `Bearer ${token()}`
    let response
    try {
      response = await fetcher(`${base.replace(/\/$/, '')}/sys-user/${path}`, {
        method: form ? 'POST' : 'PUT',
        headers,
        body: form ? new URLSearchParams(body).toString() : JSON.stringify(body),
        signal: AbortSignal.timeout(15000),
      })
    } catch (error) {
      throw new Error(
        error.name === 'TimeoutError'
          ? '请求超时，请稍后重试。'
          : '无法连接服务，请检查网络或稍后重试。',
      )
    }
    if (response.status === 401 && !form) onUnauthorized()
    let result
    try {
      result = await response.json()
    } catch {
      throw new Error('服务暂时不可用，请稍后重试。')
    }
    if (Number(result.code) === 401 && response.status !== 401 && !form) onUnauthorized()
    if (!response.ok || Number(result.code) !== 200)
      throw new Error(result.info || '操作未完成，请稍后重试。')
    return result.data
  }
  return {
    login: (body) => request('login', body, true),
    register: (body) => request('reg', body, true),
    updateInfo: (body) => request('info', body),
    updatePassword: (body) => request('password', body),
  }
}
