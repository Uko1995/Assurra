#!/usr/bin/env node

/**
 * Cursor-compatible wrapper around stitch-mcp-stdio / @google/stitch-sdk.
 *
 * Stitch returns tool schemas with outputSchema + $ref/#/$defs that AJV in the
 * MCP client cannot resolve (e.g. "#/$defs/ScreenInstance"). Cursor then shows
 * the server as connected with 0 tools.
 *
 * This proxy strips outputSchema/annotations and unresolved $ref/$defs from
 * inputSchema before returning tools/list.
 */

import { StitchProxy } from "/home/uko/.nvm/versions/node/v22.16.0/lib/node_modules/stitch-mcp-stdio/node_modules/@google/stitch-sdk/dist/src/index.js";
import { refreshTools } from "/home/uko/.nvm/versions/node/v22.16.0/lib/node_modules/stitch-mcp-stdio/node_modules/@google/stitch-sdk/dist/src/proxy/client.js";
import { StdioServerTransport } from "/home/uko/.nvm/versions/node/v22.16.0/lib/node_modules/stitch-mcp-stdio/node_modules/@modelcontextprotocol/sdk/dist/esm/server/stdio.js";
import { ListToolsRequestSchema } from "/home/uko/.nvm/versions/node/v22.16.0/lib/node_modules/stitch-mcp-stdio/node_modules/@modelcontextprotocol/sdk/dist/esm/types.js";

const apiKey = process.env.STITCH_API_KEY;
if (!apiKey) {
  process.stderr.write(
    "ERROR: STITCH_API_KEY environment variable is required.\n" +
      "Get your key from stitch.withgoogle.com -> Profile -> Settings -> API Key\n"
  );
  process.exit(1);
}

function stripBrokenSchemaNodes(node) {
  if (!node || typeof node !== "object") return node;
  if (Array.isArray(node)) return node.map(stripBrokenSchemaNodes);

  const out = {};
  for (const [key, value] of Object.entries(node)) {
    if (key === "$defs" || key === "definitions" || key === "$ref") continue;
    out[key] = stripBrokenSchemaNodes(value);
  }

  if (Object.keys(out).length === 0) {
    return { type: "object", additionalProperties: true };
  }
  return out;
}

function sanitizeTool(tool) {
  const { outputSchema: _outputSchema, annotations: _annotations, ...rest } =
    tool;
  return {
    ...rest,
    inputSchema: stripBrokenSchemaNodes(
      rest.inputSchema ?? { type: "object", additionalProperties: true }
    ),
  };
}

const proxy = new StitchProxy({ apiKey });
const transport = new StdioServerTransport();
const server = proxy.server.server;
const ctx = proxy.ctx;

server.setRequestHandler(ListToolsRequestSchema, async () => {
  try {
    await refreshTools(ctx);
  } catch (err) {
    process.stderr.write(
      `[stitch-sanitize] refresh failed, using cache: ${err.message}\n`
    );
    if (!ctx.remoteTools?.length) throw err;
  }

  const tools = (ctx.remoteTools || []).map(sanitizeTool);
  process.stderr.write(`[stitch-sanitize] returning ${tools.length} tools\n`);
  return { tools };
});

function shutdown() {
  proxy.close().catch(() => {}).finally(() => process.exit(0));
}
process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);

try {
  await proxy.start(transport);
} catch (err) {
  process.stderr.write(`Failed to start Stitch MCP server: ${err.message}\n`);
  process.exit(1);
}
