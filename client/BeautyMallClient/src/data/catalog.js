// Layout demonstration only. Replace this source with the product API when available.
const groups = [
  {
    category: '面部护理',
    names: [
      '水润保湿精华',
      '轻盈面霜',
      '柔润洁面乳',
      '保湿化妆水',
      '夜间护理精华',
      '滋润眼霜',
      '日间护理乳',
      '清透防晒乳',
    ],
  },
  {
    category: '彩妆',
    names: [
      '柔雾唇膏',
      '轻透粉底液',
      '细腻定妆粉饼',
      '自然眉笔',
      '双色眼影',
      '柔光腮红',
      '润色唇釉',
      '轻盈妆前乳',
    ],
  },
  {
    category: '香氛与身体',
    names: [
      '花香淡香水',
      '木质调香水',
      '柑橘香氛喷雾',
      '柔润身体乳',
      '清新沐浴露',
      '滋润护手霜',
      '身体护理油',
      '便携香氛',
    ],
  },
]
export const sampleProducts = groups.flatMap((group, groupIndex) =>
  group.names.map((name, index) => ({
    id: `sample-${groupIndex}-${index}`,
    name,
    category: group.category,
    image: '/images/beauty-editorial.jpg',
    imagePosition: ['75% center', 'right bottom', 'center center'][groupIndex],
  })),
)
