# Build PRD HTML from template + markdown
# Usage: Run this script from the WuJin_Mall directory

$ErrorActionPreference = "Stop"
$scriptDir = "e:\workspace\WuJin_Mall"

Write-Host "🔧 Building PRD HTML..." -ForegroundColor Cyan

# Read source files
$mdPath = Join-Path $scriptDir "PRD_三泳道动态分类与产业链溯源系统.md"
$templatePath = Join-Path $scriptDir "_prd_template.html"
$outputPath = Join-Path $scriptDir "PRD_三泳道动态分类与产业链溯源系统.html"

$md = [System.IO.File]::ReadAllText($mdPath, [System.Text.Encoding]::UTF8)
$template = [System.IO.File]::ReadAllText($templatePath, [System.Text.Encoding]::UTF8)

Write-Host "  ✅ Read markdown: $($md.Length) chars" -ForegroundColor Gray

# Escape </script> to prevent breaking the embedded script tag
$md = $md.Replace("</script", "<\/script")

# Replace placeholder
$html = $template.Replace("{{MARKDOWN_CONTENT}}", $md)

# Write output
[System.IO.File]::WriteAllText($outputPath, $html, (New-Object System.Text.UTF8Encoding $false))

$fileSize = [math]::Round((Get-Item $outputPath).Length / 1024, 1)
Write-Host "  ✅ Generated: PRD_三泳道动态分类与产业链溯源系统.html ($fileSize KB)" -ForegroundColor Green
Write-Host "🎉 Done! Open the HTML file in your browser." -ForegroundColor Cyan
