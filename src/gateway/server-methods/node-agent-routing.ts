import {
  getNodeAgentId,
  listNodeAgentRoutings,
  setNodeAgentId,
} from "../../infra/node-agent-routing.js";
import { ErrorCodes, errorShape } from "../protocol/index.js";
import { respondUnavailableOnThrow } from "./nodes.helpers.js";
import type { GatewayRequestHandlers } from "./types.js";

export const nodeAgentRoutingHandlers: GatewayRequestHandlers = {
  "node.set-agent": async ({ params, respond }) => {
    const { nodeId, agentId } = (params ?? {}) as Record<string, unknown>;
    if (typeof nodeId !== "string" || !nodeId.trim()) {
      respond(false, undefined, errorShape(ErrorCodes.INVALID_REQUEST, "nodeId required"));
      return;
    }
    await respondUnavailableOnThrow(respond, async () => {
      const agent = typeof agentId === "string" && agentId.trim() ? agentId.trim() : null;
      await setNodeAgentId(nodeId.trim(), agent);
      respond(true, { nodeId: nodeId.trim(), agentId: agent }, undefined);
    });
  },

  "node.get-agent": async ({ params, respond }) => {
    const { nodeId } = (params ?? {}) as Record<string, unknown>;
    if (typeof nodeId !== "string" || !nodeId.trim()) {
      respond(false, undefined, errorShape(ErrorCodes.INVALID_REQUEST, "nodeId required"));
      return;
    }
    await respondUnavailableOnThrow(respond, async () => {
      const agentId = await getNodeAgentId(nodeId.trim());
      respond(true, { nodeId: nodeId.trim(), agentId }, undefined);
    });
  },

  "node.list-agent-routes": async ({ params: _params, respond }) => {
    await respondUnavailableOnThrow(respond, async () => {
      const routes = await listNodeAgentRoutings();
      respond(true, { routes }, undefined);
    });
  },
};
