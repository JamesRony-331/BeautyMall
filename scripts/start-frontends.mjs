import { spawn } from 'node:child_process'
import { existsSync } from 'node:fs'
import net from 'node:net'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const projects = [
  { name: '客户端', directory: 'client/BeautyMallClient', port: Number(process.env.CLIENT_PORT || 5173) },
  { name: '管理端', directory: 'admin/BeautyMall', port: Number(process.env.ADMIN_PORT || 5174) },
]
const children = new Set()
let stopping = false
function stop(code = 0) {
  if (stopping) return
  stopping = true
  console.log('\n正在停止本次启动的服务……')
  for (const child of children) child.kill('SIGTERM')
  const timer = setTimeout(() => {
    for (const child of children) child.kill('SIGKILL')
    process.exit(code)
  }, 3000)
  if (!children.size) { clearTimeout(timer); process.exit(code) }
  const poll = setInterval(() => {
    if (!children.size) { clearInterval(poll); clearTimeout(timer); process.exit(code) }
  }, 100)
}
process.on('SIGINT', () => stop())
process.on('SIGTERM', () => stop())
function run(command, args, options) {
  const child = spawn(command, args, { stdio: 'inherit', ...options })
  children.add(child)
  child.once('exit', () => children.delete(child))
  child.once('error', () => children.delete(child))
  return child
}
async function freePort(start, reserved) {
  if (!Number.isInteger(start) || start < 1024 || start > 65515) throw new Error('起始端口必须是 1024–65515 之间的整数')
  for (let port = start; port < start + 20; port++) {
    if (reserved.has(port)) continue
    const available = await new Promise((resolve, reject) => {
      const server = net.createServer()
      server.once('error', error => error.code === 'EADDRINUSE' ? resolve(false) : reject(error))
      server.listen(port, '127.0.0.1', () => server.close(() => resolve(true)))
    })
    if (available) return port
  }
  throw new Error(`${start} 附近没有可用端口，请关闭旧服务或更换起始端口`)
}
async function prepare(project) {
  project.cwd = path.join(root, project.directory)
  project.vite = path.join(project.cwd, 'node_modules/vite/bin/vite.js')
  if (!existsSync(path.join(project.cwd, 'package.json'))) throw new Error(`找不到${project.name}项目：${project.cwd}`)
  if (existsSync(project.vite)) return
  console.log(`\n${project.name}尚未安装依赖，正在安装（需要网络）……`)
  await new Promise((resolve, reject) => {
    const child = run('npm', ['install', '--no-audit', '--no-fund'], { cwd: project.cwd })
    child.once('error', reject)
    child.once('exit', code => code === 0 ? resolve() : reject(new Error(`${project.name}依赖安装失败`)))
  })
}
async function start(project) {
  const url = `http://127.0.0.1:${project.port}/`
  const child = run(process.execPath, [project.vite, '--host', '127.0.0.1', '--port', String(project.port), '--strictPort'], { cwd: project.cwd })
  child.once('error', error => { console.error(`${project.name}启动失败：${error.message}`); stop(1) })
  child.once('exit', () => { if (!stopping) { console.error(`${project.name}服务已退出`); stop(1) } })
  for (let attempt = 0; attempt < 100 && !stopping; attempt++) {
    try {
      const response = await fetch(url, { signal: AbortSignal.timeout(800) })
      if (response.ok) {
        console.log(`\n✓ ${project.name}：${url}`)
        if (process.platform === 'darwin' && process.env.NO_OPEN !== '1') {
          const browser = spawn('open', [url], { stdio: 'ignore' })
          browser.on('error', () => console.log(`请手动打开：${url}`))
        }
        return
      }
    } catch { /* Wait briefly for Vite to start listening. */ }
    await new Promise(resolve => setTimeout(resolve, 200))
  }
  if (!stopping) throw new Error(`${project.name}启动超时`)
}
try {
  const [major, minor] = process.versions.node.split('.').map(Number)
  if (!(major > 22 || (major === 22 && minor >= 12) || (major === 20 && minor >= 19))) throw new Error('请使用 Node.js 20.19+、22.12+ 或更新版本')
  console.log('美妆项目 · 管理端与客户端一键启动\n按 Ctrl+C 停止本次启动的两个服务。后端 5050 请单独启动。')
  const reserved = new Set()
  for (const project of projects) {
    await prepare(project)
    if (stopping) break
    const requested = project.port
    project.port = await freePort(requested, reserved)
    reserved.add(project.port)
    if (requested !== project.port) console.log(`${project.name}默认端口被占用，使用 ${project.port}`)
  }
  if (!stopping) {
    await Promise.all(projects.map(start))
    if (!stopping) console.log('\n两端均已就绪，请保留此终端窗口。')
  }
} catch (error) {
  console.error(`\n启动失败：${error.message}`)
  stop(1)
}
