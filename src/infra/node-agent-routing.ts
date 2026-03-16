import path from "node:path";
import { resolveStateDir } from "../config/paths.js";
import { createAsyncLock, readJsonFile, writeJsonAtomic } from "./json-files.js";

const ROUTING_FILE = "node-agent-routing.json";

type NodeAgentRoutingFile = {
  routes: Record<string, string>;
};

const withLock = createAsyncLock();

function resolveRoutingPath(baseDir?: string): string {
  return path.join(baseDir ?? resolveStateDir(), ROUTING_FILE);
}

async function loadRoutes(baseDir?: string): Promise<Record<string, string>> {
  const data = await readJsonFile<NodeAgentRoutingFile>(resolveRoutingPath(baseDir));
  return data?.routes ?? {};
}

export async function getNodeAgentId(nodeId: string, baseDir?: string): Promise<string | null> {
  const routes = await loadRoutes(baseDir);
  return routes[nodeId.trim()] ?? null;
}

export async function setNodeAgentId(
  nodeId: string,
  agentId: string | null,
  baseDir?: string,
): Promise<void> {
  await withLock(async () => {
    const routes = await loadRoutes(baseDir);
    const key = nodeId.trim();
    if (agentId) {
      routes[key] = agentId.trim();
    } else {
      delete routes[key];
    }
    await writeJsonAtomic(resolveRoutingPath(baseDir), { routes });
  });
}

export async function listNodeAgentRoutings(
  baseDir?: string,
): Promise<Array<{ nodeId: string; agentId: string }>> {
  const routes = await loadRoutes(baseDir);
  return Object.entries(routes).map(([nodeId, agentId]) => ({ nodeId, agentId }));
}
