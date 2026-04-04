import type { Command } from "commander";
import { defaultRuntime } from "../../runtime.js";
import { getNodesTheme, runNodesCommand } from "./cli-utils.js";
import { callGatewayCli, nodesCallOpts } from "./rpc.js";

export function registerNodesAgentRoutingCommands(nodes: Command) {
  nodesCallOpts(
    nodes
      .command("set-agent")
      .description("Route a node's messages to a specific agent")
      .argument("<nodeId>", "Node ID")
      .argument("<agentId>", "Agent ID to route to (use 'none' to clear)")
      .action(async (nodeId: string, agentId: string, opts) => {
        await runNodesCommand("set-agent", async () => {
          const agent = agentId.trim().toLowerCase() === "none" ? null : agentId.trim();
          const result = await callGatewayCli("node.set-agent", opts, {
            nodeId: nodeId.trim(),
            agentId: agent,
          });
          if (opts.json) {
            defaultRuntime.log(JSON.stringify(result, null, 2));
            return;
          }
          const { ok, muted } = getNodesTheme();
          if (agent) {
            defaultRuntime.log(ok(`Node ${nodeId} → agent "${agent}"`));
          } else {
            defaultRuntime.log(muted(`Node ${nodeId} agent routing cleared.`));
          }
        });
      }),
  );

  nodesCallOpts(
    nodes
      .command("list-agent-routes")
      .description("List node-to-agent routing assignments")
      .action(async (opts) => {
        await runNodesCommand("list-agent-routes", async () => {
          const result = (await callGatewayCli("node.list-agent-routes", opts, {})) as {
            routes: Array<{ nodeId: string; agentId: string }>;
          };
          if (opts.json) {
            defaultRuntime.log(JSON.stringify(result, null, 2));
            return;
          }
          const { heading, muted } = getNodesTheme();
          const routes = result?.routes ?? [];
          if (routes.length === 0) {
            defaultRuntime.log(muted("No agent routing assignments."));
            return;
          }
          defaultRuntime.log(heading("Node agent routing:"));
          for (const { nodeId, agentId } of routes) {
            defaultRuntime.log(`  ${nodeId}  →  ${agentId}`);
          }
        });
      }),
  );
}
