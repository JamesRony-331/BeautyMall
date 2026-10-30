import test from 'node:test'
import assert from 'node:assert/strict'
import { filterRows, paginateRows } from '../src/lib/table.js'
const rows = [
  { name: '柔雾唇膏', category: '彩妆', status: '已上架' },
  { name: '保湿精华', category: '护肤', status: '待上架' },
]
test('search and filters intersect without changing source', () => {
  assert.deepEqual(filterRows(rows, ' 精华 ', { status: '待上架' }), [rows[1]])
  assert.equal(filterRows(rows, '精华', { status: '已上架' }).length, 0)
  assert.equal(rows.length, 2)
})
test('pagination clamps empty and out-of-range pages', () => {
  assert.equal(paginateRows(rows, 99, 1).page, 2)
  assert.deepEqual(paginateRows([], 2, 10).items, [])
  assert.equal(paginateRows(rows, 1, 1).items[0].name, '柔雾唇膏')
})
