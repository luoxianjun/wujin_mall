const fs = require('fs');
const path = require('path');

async function convert() {
  const { marked } = await import('marked');
  const HTMLtoDOCX = (await import('html-to-docx')).default;

  const inputFile = process.argv[2] || 'PRD_三泳道动态分类与产业链溯源系统.md';
  const outputFile = process.argv[3] || inputFile.replace(/\.md$/, '.docx');

  console.log(`Reading: ${inputFile}`);
  const md = fs.readFileSync(inputFile, 'utf-8');

  // Convert Markdown to HTML
  const htmlBody = marked.parse(md);

  // Wrap with styling for better Word rendering
  const html = `
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  body {
    font-family: "Microsoft YaHei", "SimSun", Arial, sans-serif;
    font-size: 12pt;
    line-height: 1.6;
    color: #333;
  }
  h1 { font-size: 22pt; color: #1a1a2e; border-bottom: 2px solid #0066cc; padding-bottom: 8px; margin-top: 24px; }
  h2 { font-size: 18pt; color: #16213e; border-bottom: 1px solid #ddd; padding-bottom: 6px; margin-top: 20px; }
  h3 { font-size: 15pt; color: #0f3460; margin-top: 16px; }
  h4 { font-size: 13pt; color: #333; margin-top: 14px; }
  h5 { font-size: 12pt; color: #555; margin-top: 12px; }
  table { border-collapse: collapse; width: 100%; margin: 12px 0; }
  th, td { border: 1px solid #999; padding: 8px 12px; text-align: left; font-size: 10pt; }
  th { background-color: #e8eaf6; font-weight: bold; }
  tr:nth-child(even) { background-color: #f5f5f5; }
  code { font-family: Consolas, "Courier New", monospace; background-color: #f4f4f4; padding: 2px 4px; font-size: 10pt; }
  pre { background-color: #f4f4f4; padding: 12px; border-radius: 4px; overflow-x: auto; }
  pre code { background: none; padding: 0; }
  blockquote { border-left: 4px solid #0066cc; margin: 12px 0; padding: 8px 16px; background: #f0f4ff; }
  ul, ol { margin: 8px 0; padding-left: 24px; }
  li { margin: 4px 0; }
  hr { border: none; border-top: 1px solid #ddd; margin: 20px 0; }
  strong { color: #1a1a2e; }
  a { color: #0066cc; }
</style>
</head>
<body>
${htmlBody}
</body>
</html>
`;

  console.log('Converting to DOCX...');
  
  const docxBuffer = await HTMLtoDOCX(html, null, {
    table: { row: { cantSplit: true } },
    footer: true,
    pageNumber: true,
    font: 'Microsoft YaHei',
    fontSize: 24,
    title: path.basename(inputFile, '.md'),
    margins: {
      top: 1440,
      right: 1440,
      bottom: 1440,
      left: 1440,
    },
  });

  fs.writeFileSync(outputFile, docxBuffer);
  console.log(`✅ Word document saved: ${outputFile}`);
}

convert().catch(err => {
  console.error('❌ Conversion failed:', err);
  process.exit(1);
});
