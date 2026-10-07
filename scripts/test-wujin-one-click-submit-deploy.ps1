param(
  [string]$ScriptPath = (Join-Path $PSScriptRoot 'wujin-one-click-submit-deploy.ps1')
)

$ErrorActionPreference = 'Stop'

function Assert-True {
  param(
    [bool]$Condition,
    [string]$Message
  )
  if (-not $Condition) {
    throw $Message
  }
}

Assert-True (Test-Path -LiteralPath $ScriptPath) "script does not exist: $ScriptPath"

$tokens = $null
$parseErrors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile(
  $ScriptPath,
  [ref]$tokens,
  [ref]$parseErrors
)
Assert-True ($parseErrors.Count -eq 0) "PowerShell parse errors: $($parseErrors | ForEach-Object Message -join '; ')"

$content = Get-Content -LiteralPath $ScriptPath -Raw
$functionNames = $ast.FindAll({
  param($node)
  $node -is [System.Management.Automation.Language.FunctionDefinitionAst]
}, $true).Name

$paramNames = @()
if ($ast.ParamBlock) {
  $paramNames = $ast.ParamBlock.Parameters | ForEach-Object { $_.Name.VariablePath.UserPath }
}

foreach ($name in @(
  'Message',
  'DeployAll',
  'SkipDeploy',
  'SkipTests',
  'DryRun',
  'NoPush',
  'WorkspaceRoot',
  'SshHost',
  'SshUser',
  'SshPassword'
)) {
  Assert-True ($paramNames -contains $name) "missing parameter: $name"
}

foreach ($name in @(
  'Invoke-CheckedCommand',
  'Get-RepoChangeState',
  'Invoke-RepoCommitPush',
  'Invoke-BackendBuild',
  'Invoke-WebBuild',
  'Compress-WebDist',
  'Invoke-RemoteDeploy',
  'Invoke-RemoteVerify'
)) {
  Assert-True ($functionNames -contains $name) "missing function: $name"
}

foreach ($repo in @(
  'Wujin-Mall-Server',
  'Wujin-Mall-Platform-Web',
  'Wujin-Mall-Merchant-Web',
  'Wujin-Mall-Mini'
)) {
  Assert-True ($content.Contains($repo)) "missing repo mapping: $repo"
}

foreach ($expected in @(
  'yudao-server',
  'maven.test.skip=true',
  'build:antd:sit',
  'wujin-mall-server.jar.new',
  '/data/wujin-mall/web/platform',
  '/data/wujin-mall/web/merchant',
  'systemctl restart wujin-mall-server',
  'admin-api/system/captcha/get'
)) {
  Assert-True ($content.Contains($expected)) "missing expected content: $expected"
}

Assert-True ($content -match 'paramiko') 'script should use Paramiko for password SSH/SFTP'
Assert-True ($content -match 'argparse') 'embedded Python should use argparse instead of string interpolation for file paths'
Assert-True ($content -match 'sha256sum') 'remote deploy should print jar checksum evidence'
Assert-True ($content -match "FilePath 'git'") 'script should invoke git explicitly'
Assert-True ($content -match '''-C'',\s*\$repoPath') 'script should operate on child repos via git -C'
Assert-True ($content -match 'Push-Location') 'commands should run in their requested WorkingDirectory'
Assert-True ($content -match 'SkipOnDryRun') 'dry-run should skip mutations while still allowing read-only git inspection'

Write-Host "Static checks passed for $ScriptPath"
