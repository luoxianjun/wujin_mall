# Wujin Mall one-click submit and deploy

Run from `E:\workspace\WuJin_Mall`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1
```

What it does:

- Checks the four child repos with `git -C`: Server, Platform Web, Merchant Web, Mini.
- Fails if a repo is behind its upstream, so conflicts are handled explicitly.
- Commits local changes in each dirty repo and pushes to its upstream.
- Builds and deploys only changed deployable components:
  - Server -> Maven SIT jar -> `/data/wujin-mall/server/wujin-mall-server.jar`
  - Platform Web -> `pnpm build:antd:sit` -> `/data/wujin-mall/web/platform`
  - Merchant Web -> `pnpm build:antd:sit` -> `/data/wujin-mall/web/merchant`
- Mini app changes are committed and pushed, but not deployed to this temporary nginx host.
- Verifies remote service, nginx, platform/merchant pages, unauthenticated auth API, and captcha API.

Useful options:

```powershell
# Use one commit message for every repo that has local changes
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1 -Message "feat: update wujin mall"

# Inspect repo state and show mutation/deploy commands without changing repos or uploading files
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1 -DryRun

# Commit and push only
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1 -SkipDeploy

# Deploy all web/server components even if only one changed
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1 -DeployAll

# Skip focused tests before the server package build
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\wujin-one-click-submit-deploy.ps1 -SkipTests
```

The original local script targets the project's SIT host. Its public copy is a sanitized example in `config-examples/scripts/wujin-one-click-submit-deploy.ps1.example`; restore it and supply your own SSH credentials before use. The script assumes four independent child Git repositories, so check the repository layout before running it from this combined source repository.

Static check:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-wujin-one-click-submit-deploy.ps1
```
