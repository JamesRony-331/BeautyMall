import test from 'node:test'
import assert from 'node:assert/strict'
import { getCatalogPage } from '../src/lib/catalog.js'
test('catalog pages never overlap and return correct totals', () => {
  const first = getCatalogPage('', 1, 8),
    second = getCatalogPage('', 2, 8)
  assert.equal(first.total, 24)
  assert.equal(first.items.length, 8)
  assert.equal(second.items.length, 8)
  assert.equal(
    first.items.some((a) => second.items.some((b) => a.id === b.id)),
    false,
  )
})
test('search covers names and categories and empty results are valid', () => {
  const result = getCatalogPage('  精华 ', 1, 8)
  assert.ok(result.total > 0)
  assert.ok(result.items.every((item) => (item.name + item.category).includes('精华')))
  assert.equal(getCatalogPage('不存在的商品', 1, 8).total, 0)
})
test('invalid and excessive page inputs are clamped', () => {
  assert.equal(getCatalogPage('', 'oops', 8).page, 1)
  assert.equal(getCatalogPage('', 9999, 8).page, 3)
  assert.equal(getCatalogPage('', -1, 7).pageSize, 8)
})
