# 五金商城 WuJin Mall

本仓库汇总五金商城工作区的当前代码，包含本机尚未提交到原仓库的开发修改。

| 目录 | 内容 |
| --- | --- |
| `Wujin-Mall-Server` | Java 后端、五金业务模块、数据库迁移与测试 |
| `Wujin-Mall-Platform-Web` | 平台管理端 |
| `Wujin-Mall-Merchant-Web` | 商家管理端 |
| `Wujin-Mall-Mini` | uni-app 小程序 |
| `Wujin-Mini` | 工作区中的补充小程序页面 |
| `prototype` | 交互原型与原型测试 |
| `docs`、`设计规范包` | 产品与页面设计文档 |
| `tests`、`scripts` | 工作区级测试、SQL 与辅助脚本 |
| `config-examples` | 脱敏配置、SQL 与第三方接口示例 |

各应用的依赖安装与构建说明见对应目录中的 README 和 package.json / pom.xml。

## 本地配置

公开仓库不提交本机数据库、SSH 和第三方服务凭据。相关原文件在原工作区中保留，公开版本位于 `config-examples`，映射见 `config-examples/manifest.json`。依赖目录、编译产物、压缩包、临时运行文件和子仓库的 `.git` 元数据也未提交。

新克隆的工作区可在 PowerShell 中按映射恢复示例文件。此命令只创建缺失文件，不覆盖已有配置：

```powershell
$examples = Get-Content -LiteralPath .\config-examples\manifest.json -Raw | ConvertFrom-Json
foreach ($entry in $examples) {
    if (-not (Test-Path -LiteralPath $entry.original)) {
        $parent = Split-Path -Parent $entry.original
        if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
        Copy-Item -LiteralPath $entry.example -Destination $entry.original
    }
}
```

启动应用或运行第三方服务测试前，配置自己的服务地址和凭据，并替换示例中的 `REPLACE_WITH_CREDENTIAL` / `REPLACE_WITH_API_KEY`。基础 SQL 示例已移除外部集成的凭据种子行；五金业务 SQL 和增量迁移保留在后端目录中。

原工作区的四个子项目具有独立 Git 历史；本仓库按普通文件汇总源码，没有设置 Git 子模块。原有一键提交部署脚本面向四个独立仓库，使用前需检查 Git 结构、目标环境和远程配置。
