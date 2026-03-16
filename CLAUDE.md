# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

AGENTS.md

---

## Commands

### Build & Dev

```bash
pnpm install          # Install all dependencies
pnpm build            # Compile TypeScript → dist/
pnpm dev              # Run CLI via tsx (no build required)
pnpm openclaw <cmd>   # Run any CLI command (tsx, no build)
pnpm gateway:watch    # Auto-reload gateway on source changes
```

### Quality Checks

```bash
pnpm check            # Run format + lint + typecheck + tests (CI gate)
pnpm tsgo             # Fast type-only check
pnpm format           # Check formatting (oxfmt)
pnpm format:fix       # Fix formatting
pnpm lint             # Lint with oxlint
pnpm lint:fix         # Auto-fix lint errors
pnpm deadcode:report  # Find unused exports
```

### Tests

```bash
pnpm test             # All tests (vitest)
pnpm test:fast        # Unit tests only
pnpm test:gateway     # Gateway/RPC tests (pool=forks, isolated)
pnpm test:channels    # Channel plugin tests
pnpm test:e2e         # End-to-end tests
pnpm test:coverage    # Coverage report (70% line/function threshold)
```

Run a single test file:
```bash
pnpm exec vitest run src/path/to/file.test.ts
```

---

## Architecture

### Application Topology

**OpenClaw** is a personal AI assistant platform. A **Gateway** server runs locally (default port 18789) as a WebSocket control plane. **Nodes** (iOS/Android apps) pair with the gateway over local network. **Channels** (Telegram, Discord, WhatsApp, Slack, etc.) are managed as plugins.

```
Mobile Node (iOS/Android)
  ↓ WebSocket RPC
Gateway Server (src/gateway/server.impl.ts)
  ├── Node Event Handlers (server-node-events.ts)   — voice.transcript, agent.request
  ├── RPC Method Handlers (server-methods/*.ts)     — 40+ registered methods
  └── Channels Manager (server-channels.ts)         — messaging platform integrations
          ↓
      Agent Sessions (src/agents/)                  — Pi agent runtime, tools, skills
```

### Key Modules

| Path | Role |
|------|------|
| `src/cli/` | Commander.js wiring; lazily loads subcommands |
| `src/gateway/` | WebSocket server, RPC dispatch, node event handling |
| `src/gateway/server-methods/` | One file per domain of RPC methods |
| `src/agents/` | Agent session lifecycle, tools, skills |
| `src/channels/` | Channel plugin adapters (one per platform) |
| `src/infra/` | Path resolution, JSON state files, ports, updates |
| `src/config/` | Config loading, validation, migration |
| `src/plugins/` | Plugin runtime, hook system, registry |
| `src/routing/` | Session key routing, multi-agent isolation |
| `src/media/` | Audio/video/image pipeline, transcription |
| `src/memory/` | LanceDB vector memory |
| `extensions/` | Channel plugin workspace packages |

### RPC Pattern

CLI commands communicate with a running gateway over WebSocket using `callGatewayCli()`. RPC handlers are registered in `server-methods.ts` as spreads of domain-specific handler maps from `server-methods/*.ts`.

### Session Keys

- Default session: `"main"`
- Multi-agent isolation: `"agent:{agentId}:{base}"` — the agent prefix is applied by `applyNodeAgentRouting()` in `server-node-events.ts` when a node has a routing assignment.

### Module System

ESM-only (`"module": "NodeNext"`). No CJS. Imports need explicit `.js` extensions even for `.ts` source files. Build tool is **tsdown** (not tsc).

### Linting & Formatting

- Formatter: **oxfmt** (Rust-based)
- Linter: **oxlint** (Rust ESLint replacement, type-aware)
- No `@ts-nocheck`; no disabling `no-explicit-any`
- Files should stay under ~700 LOC

---

## Fork-Specific: Per-device Agent Routing

This fork adds routing of each paired mobile node to a specific agent (multi-user isolation).

**New files (no upstream conflicts):**
- `src/infra/node-agent-routing.ts` — persists nodeId→agentId in `{stateDir}/node-agent-routing.json`
- `src/gateway/server-methods/node-agent-routing.ts` — RPC: `node.set-agent`, `node.get-agent`, `node.list-agent-routes`
- `src/cli/nodes-cli/register.agent-routing.ts` — CLI: `nodes set-agent`, `nodes list-agent-routes`

**Modified files (watch during upstream rebase):**
- `src/gateway/server-node-events.ts` — `applyNodeAgentRouting()` + 2 await calls in `voice.transcript` and `agent.request`
- `src/gateway/server-methods.ts` — import + spread of `nodeAgentRoutingHandlers`
- `src/cli/nodes-cli/register.ts` — import + call to `registerNodesAgentRoutingCommands`

**CLI usage:**
```bash
openclaw nodes set-agent <nodeId> <agentId>   # assign node to agent
openclaw nodes set-agent <nodeId> none         # clear assignment
openclaw nodes list-agent-routes               # show all assignments
```

### Syncing Upstream

```bash
git fetch upstream
git checkout main && git rebase upstream/main && git push origin main

git checkout feature/device-agent-routing
git rebase main
git push origin feature/device-agent-routing --force-with-lease
```

**Conflict hotspots after rebase:** `server-node-events.ts`, `server-methods.ts`, `src/cli/nodes-cli/register.ts`
