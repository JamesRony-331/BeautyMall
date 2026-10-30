export function createUsersApi({
  fetcher = globalThis.fetch,
  token = () => '',
  unauthorized = () => {},
} = {}) {
  async function request(path, method = 'GET', body) {
    const login = ['/login', '/reg'].includes(path)
    const headers = login
      ? { 'Content-Type': 'application/x-www-form-urlencoded' }
      : { 'Content-Type': 'application/json', Authorization: `Bearer ${token()}` }
    let response
    try {
      response = await fetcher(`/api/sys-user${path}`, {
        method,
        headers,
        body:
          body === undefined
            ? undefined
            : login
              ? new URLSearchParams(body).toString()
              : JSON.stringify(body),
        signal: AbortSignal.timeout(15000),
      })
    } catch {
      throw new Error('无法连接服务，请稍后重试。')
    }
    let result
    try {
      result = await response.json()
    } catch {
      throw new Error('服务响应异常，请稍后重试。')
    }
    if (
      !login &&
      ([401, 403].includes(response.status) || [401, 403].includes(Number(result.code)))
    )
      unauthorized()
    if (!response.ok || Number(result.code) !== 200)
      throw new Error(result.info || '操作失败，请重试。')
    return result.data
  }
  return {
    register: (body) => request('/reg', 'POST', body),
    login: (body) => request('/login', 'POST', body),
    page: (pageNum, pageSize) => request(`/page?pageNum=${pageNum}&pageSize=${pageSize}`),
    edit: (id, body) => request(`/${encodeURIComponent(id)}/info`, 'PUT', body),
    disable: (id) => request(`/${encodeURIComponent(id)}/disable`, 'PUT'),
  }
}
