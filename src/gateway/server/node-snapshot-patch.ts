import { getNodeAgentId } from "../../infra/node-agent-routing.js";
import type { Snapshot } from "../protocol/index.js";

/**
 * If the connecting device has a per-node agent routing assignment, override
 * `sessionDefaults.mainSessionKey` in the snapshot so the Android app uses
 * the routed session key for all `chat.send` calls (text, images, files).
 */
export async function patchSnapshotForNode(
  snapshot: Snapshot,
  nodeId: string | undefined,
): Promise<Snapshot> {
  if (!nodeId || !snapshot.sessionDefaults) {
    return snapshot;
  }
  const agentId = await getNodeAgentId(nodeId);
  if (!agentId) {
    return snapshot;
  }
  const base = snapshot.sessionDefaults.mainSessionKey;
  return {
    ...snapshot,
    sessionDefaults: {
      ...snapshot.sessionDefaults,
      mainSessionKey: `agent:${agentId}:${base}`,
    },
  };
}
