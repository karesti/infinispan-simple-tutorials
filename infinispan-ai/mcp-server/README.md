# Infinispan MCP Server Tutorial

This tutorial shows how to enable and use Infinispan's MCP (Model Context Protocol) endpoint.

Infinispan exposes an MCP endpoint that allows AI assistants and LLM-based tools to interact with
your Infinispan cluster:
* managing caches
* counters
* schemas

... and more through natural language.

## Prerequisites

- Docker and Docker Compose
- An MCP client (e.g., Claude Code, Claude Desktop, an IDE with MCP support)
- Python 3 (only required for the `digest-auth` example)

## Choose Your Authentication Method

This tutorial provides two examples, each in its own folder:

| Folder | Auth Method | Helper Script | Setup Complexity |
|--------|------------|---------------|-----------------|
| `basic-auth/` | HTTP Basic | Not needed | Simpler |
| `digest-auth/` | HTTP Digest | `get-mcp-headers.py` | Default Infinispan auth |

### Basic Auth (`basic-auth/`)

Uses HTTP Basic authentication. A custom Infinispan configuration (`infinispan-basic-auth.xml`)
switches the REST connector from the default Digest authentication to Basic. This makes the setup
simpler because MCP clients can send credentials directly in the `Authorization` header without
any helper script.

### Digest Auth (`digest-auth/`)

Uses HTTP Digest authentication, which is the default for Infinispan. Digest auth is more secure
because credentials are never sent in plain text — instead, a challenge-response mechanism is used.
However, most MCP clients don't support Digest natively, so a Python helper script
(`get-mcp-headers.py`) is included to compute the required headers for each request.

## Running the Example

Pick one of the two folders and run all commands from inside it.

### Basic Auth

```bash
cd basic-auth
docker compose up -d
```

### Digest Auth

```bash
cd digest-auth
docker compose up -d
```

Both start Infinispan Server with the MCP endpoint enabled at `http://localhost:11222/rest/v3/mcp`.

## Connecting Claude Code

Run the following command from inside the chosen folder.

### Basic Auth

```bash
claude mcp add infinispan --transport http http://localhost:11222/rest/v3/mcp
```

### Digest Auth

```bash
claude mcp add infinispan --transport http http://localhost:11222/rest/v3/mcp \
  -e INFINISPAN_USER=admin -e INFINISPAN_PASS=password \
  --headers-helper "python3 get-mcp-headers.py"
```

### Using the `.mcp.json` file

Alternatively, each folder contains an `mcp.json` file that you can copy to your project root:

```bash
cp mcp.json /path/to/your/project/.mcp.json
```

For the Digest example, you can configure the connection with environment variables:
- `INFINISPAN_URL` — MCP endpoint URL (default: `http://localhost:11222/rest/v3/mcp`)
- `INFINISPAN_USER` — Username (default: `admin`)
- `INFINISPAN_PASS` — Password (default: `password`)

## Connecting Claude Desktop

Add the following to your Claude Desktop MCP configuration (`claude_desktop_config.json`).

### Basic Auth

```json
{
  "mcpServers": {
    "infinispan": {
      "type": "http",
      "url": "http://localhost:11222/rest/v3/mcp"
    }
  }
}
```

### Digest Auth

```json
{
  "mcpServers": {
    "infinispan": {
      "type": "http",
      "url": "http://localhost:11222/rest/v3/mcp",
      "headersHelper": "python3 /absolute/path/to/digest-auth/get-mcp-headers.py"
    }
  }
}
```

## Testing Locally

Once Infinispan is running and your MCP client is connected, you can interact with the server
using natural language. Try asking your AI assistant to:

**Create a cache:**
> Create a cache called "my-cache"

**Put an entry:**
> Put the key "greeting" with value "hello world" in my-cache

**Read an entry:**
> Get the value for key "greeting" from my-cache

**List all caches:**
> List all caches

**Create a counter:**
> Create a strong counter called "visitor-count"

You can also verify the MCP endpoint directly with `curl`:

### Basic Auth

```bash
curl http://localhost:11222/rest/v3/mcp \
  -u admin:password \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":1}'
```

### Digest Auth

```bash
curl http://localhost:11222/rest/v3/mcp \
  --digest -u admin:password \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":1}'
```

## Available MCP Capabilities

Once connected, the Infinispan MCP server exposes:

### Tools
- Cache operations (create, list, get, put, remove, query)
- Counter operations (get, increment, decrement)
- Schema management

### Resources
- Server information and configuration
- Audit and access logs

### Prompts
- Documentation search guidance

## Stopping Infinispan

From the folder you started:

```bash
docker compose down
```