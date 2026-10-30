#!/bin/zsh
# 使用登录 shell 的 PATH，支持 Finder 双击。
cd -- "${0:A:h}" || exit 1
if ! command -v node >/dev/null 2>&1; then
  for node_dir in /opt/homebrew/bin /usr/local/bin; do
    if [[ -x "$node_dir/node" ]]; then
      export PATH="$node_dir:$PATH"
      break
    fi
  done
fi
if ! command -v node >/dev/null 2>&1; then
  print '没有找到 Node.js。请先安装 Node.js 22.12+，再双击此文件。'
  read '?按回车关闭窗口……'
  exit 1
fi
node scripts/start-frontends.mjs
start_status=$?
if (( start_status != 0 )); then
  read '?启动未完成，请查看上方错误。按回车关闭窗口……'
fi
exit $start_status
