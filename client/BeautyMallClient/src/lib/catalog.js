import { sampleProducts } from '../data/catalog.js'
export function getCatalogPage(keyword = '', requestedPage = 1, requestedSize = 8) {
  const query = typeof keyword === 'string' ? keyword.trim().normalize('NFKC').toLowerCase() : ''
  const pageSize = [8, 12, 24].includes(Number(requestedSize)) ? Number(requestedSize) : 8
  const filtered = sampleProducts.filter((item) =>
    `${item.name} ${item.category}`.normalize('NFKC').toLowerCase().includes(query),
  )
  const total = filtered.length
  const count = Math.max(1, Math.ceil(total / pageSize))
  const page = Math.min(count, Math.max(1, Math.floor(Number(requestedPage)) || 1))
  return { items: filtered.slice((page - 1) * pageSize, page * pageSize), total, page, pageSize }
}
