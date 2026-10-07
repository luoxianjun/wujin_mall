const fs = require('fs');
const path = require('path');

const dir = 'e:/workspace/WuJin_Mall';
const md = fs.readFileSync(path.join(dir, 'PRD_三泳道动态分类与产业链溯源系统.md'), 'utf-8');
const template = fs.readFileSync(path.join(dir, '_prd_template.html'), 'utf-8');

// Escape </script> tags in markdown content
const escaped = md.replace(/<\/script/gi, '<\\/script');

// Replace placeholder
const html = template.replace('{{MARKDOWN_CONTENT}}', escaped);

// Write output
const outPath = path.join(dir, 'PRD_三泳道动态分类与产业链溯源系统.html');
fs.writeFileSync(outPath, html, 'utf-8');

const size = (fs.statSync(outPath).size / 1024).toFixed(1);
console.log('Generated: ' + size + ' KB');
