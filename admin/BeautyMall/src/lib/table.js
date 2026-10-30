export function filterRows(rows, keyword = '', filters = {}) {
  const term = keyword.trim().normalize('NFKC').toLowerCase()
  return rows.filter(
    (row) =>
      (!term ||
        [row.name, row.id, row.description, row.category]
          .join(' ')
          .normalize('NFKC')
          .toLowerCase()
          .includes(term)) &&
      Object.entries(filters).every(([key, value]) => !value || row[key] === value),
  )
}
export function paginateRows(rows, requestedPage, size = 6) {
  const pageSize = Math.max(1, Math.floor(Number(size)) || 6)
  const page = Math.min(
    Math.max(1, Math.ceil(rows.length / pageSize)),
    Math.max(1, Math.floor(Number(requestedPage)) || 1),
  )
  return { items: rows.slice((page - 1) * pageSize, page * pageSize), page, total: rows.length }
}
