const { spawnSync } = require('node:child_process');
const { dirname, join } = require('node:path');

const mode = process.env.NODE_ENV || 'production';
const vitePackagePath = require.resolve('vite/package.json');
const viteBinPath = join(dirname(vitePackagePath), 'bin', 'vite.js');
const env = {
  ...process.env,
  npm_lifecycle_script: `vite build --mode ${mode}`,
};
const result = spawnSync(process.execPath, [viteBinPath, 'build', '--mode', mode], {
  env,
  stdio: 'inherit',
});

process.exit(result.status ?? 1);
