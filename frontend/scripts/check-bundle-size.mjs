import { gzipSync } from "node:zlib";
import { readdir, readFile } from "node:fs/promises";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";

const distributionDirectory = fileURLToPath(
  new URL("../dist/", import.meta.url),
);
const maximumJavaScriptChunkGzipBytes = 120 * 1024;
const maximumTotalGzipBytes = 180 * 1024;

async function collectFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const files = await Promise.all(
    entries.map((entry) => {
      const path = join(directory, entry.name);
      return entry.isDirectory() ? collectFiles(path) : path;
    }),
  );

  return files.flat();
}

const files = await collectFiles(distributionDirectory);
const compressedAssets = await Promise.all(
  files.map(async (path) => {
    const source = await readFile(path);
    return {
      path: relative(distributionDirectory, path),
      gzipBytes: gzipSync(source).byteLength,
    };
  }),
);

const oversizedChunks = compressedAssets.filter(
  ({ path, gzipBytes }) =>
    path.endsWith(".js") && gzipBytes > maximumJavaScriptChunkGzipBytes,
);
const totalGzipBytes = compressedAssets.reduce(
  (total, { gzipBytes }) => total + gzipBytes,
  0,
);

if (oversizedChunks.length > 0 || totalGzipBytes > maximumTotalGzipBytes) {
  for (const asset of oversizedChunks) {
    console.error(
      `${asset.path} is ${asset.gzipBytes} gzip bytes; limit is ${maximumJavaScriptChunkGzipBytes}.`,
    );
  }
  if (totalGzipBytes > maximumTotalGzipBytes) {
    console.error(
      `Total bundle is ${totalGzipBytes} gzip bytes; limit is ${maximumTotalGzipBytes}.`,
    );
  }
  process.exitCode = 1;
} else {
  console.log(
    `Bundle budget passed: ${totalGzipBytes} total gzip bytes across ${compressedAssets.length} files.`,
  );
}
