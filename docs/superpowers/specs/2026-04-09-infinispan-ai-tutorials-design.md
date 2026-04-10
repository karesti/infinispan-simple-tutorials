# Infinispan AI Tutorials - Design Spec

## Overview

Add a new top-level `infinispan-ai/` directory to the simple tutorials repository containing tutorials that demonstrate Infinispan's AI capabilities. The primary focus is **embedding stores** (vector stores for RAG use cases), plus an MCP server setup tutorial.

## Structure

```
infinispan-ai/
  langchain4j/                    # Plain Java - embedding store
  langchain4j-quarkus/            # Quarkus app - embedding store
  spring-ai/                      # Spring Boot app - embedding store
  mcp-server/                     # Config/setup - Infinispan MCP endpoint
```

Each subfolder is a standalone Maven module registered in the root `pom.xml`.

## Module Details

### 1. `langchain4j/` - Standalone LangChain4j Embedding Store

**Type:** Plain Java application with `main()` method.

**Purpose:** Demonstrate the simplest way to use Infinispan as an embedding store with LangChain4j. No framework overhead.

**Key dependencies:**
- `dev.langchain4j:langchain4j-infinispan` (embedding store)
- `dev.langchain4j:langchain4j` (core)
- An embedding model provider (e.g., `langchain4j-embeddings-all-minilm-l6-v2` for in-process, or an OpenAI/Ollama provider)
- `org.infinispan:infinispan-client-hotrod`

**What it does:**
- Connects to an Infinispan server
- Creates an `InfinispanEmbeddingStore`
- Stores document embeddings
- Runs similarity searches
- Prints results

**Test:** JUnit 5 + Testcontainers (Infinispan container).

**Pattern:** Follows `infinispan-remote/cache` style — simple class with `main()`, minimal config.

### 2. `langchain4j-quarkus/` - Quarkus LangChain4j App

**Type:** Quarkus REST application.

**Purpose:** Demonstrate Infinispan embedding store in a Quarkus application using the Quarkus LangChain4j extension and Quarkus Infinispan extension.

**Key dependencies:**
- `io.quarkiverse.langchain4j:quarkus-langchain4j-infinispan` (or equivalent Quarkus extension)
- Quarkus Infinispan client extension
- Quarkus REST (Jakarta REST)
- An embedding model provider

**What it does:**
- REST endpoints to ingest documents and query by similarity
- CDI-injected `EmbeddingStore` bean
- `application.properties` for Infinispan connection config
- Demonstrates the Quarkus-native way of wiring LangChain4j + Infinispan

**Test:** `@QuarkusTest` + Quarkus Dev Services (auto-starts Infinispan container).

**Pattern:** Follows `integrations/quarkus/infinispan-client-example` style.

### 3. `spring-ai/` - Spring Boot Spring AI App

**Type:** Spring Boot REST application.

**Purpose:** Demonstrate Infinispan as a vector store using Spring AI's `InfinispanVectorStore` with auto-configuration.

**Key dependencies:**
- `org.springframework.ai:spring-ai-autoconfigure-vector-store-infinispan`
- `org.springframework.ai:spring-ai-infinispan-store`
- Spring Boot Starter Web
- An embedding model provider

**Configuration properties** (in `application.properties`):
- `spring.ai.vectorstore.infinispan.storeName`
- `spring.ai.vectorstore.infinispan.similarity=COSINE`
- `spring.ai.vectorstore.infinispan.createStore=true`
- Infinispan connection properties

**What it does:**
- REST endpoints to add documents and search by similarity
- Auto-configured `InfinispanVectorStore` bean
- Demonstrates Spring AI's `VectorStore` abstraction with Infinispan backend

**Test:** `@SpringBootTest` + Testcontainers.

**Pattern:** Follows `integrations/spring-boot/cache-remote` style.

### 4. `mcp-server/` - Infinispan MCP Server Setup

**Type:** Configuration/documentation tutorial (not a Java project).

**Purpose:** Show how to enable Infinispan's MCP endpoint and connect an MCP client to it.

**Contents:**
- `docker-compose.yml` — starts Infinispan server with MCP endpoint enabled
- `infinispan.xml` (or equivalent server config) — server configuration enabling the MCP endpoint
- Sample MCP client configuration (e.g., `mcp.json` for Claude Desktop or similar)
- `README.md` — step-by-step instructions

**What it demonstrates:**
- Enabling the MCP endpoint on Infinispan server
- Connecting an MCP client
- Example interactions (cache operations exposed as MCP tools)

**No Maven module** — this is a config-only tutorial.

## Root POM Integration

Add the three Java modules to the root `pom.xml` `<modules>` section:
```xml
<module>infinispan-ai/langchain4j</module>
<module>infinispan-ai/langchain4j-quarkus</module>
<module>infinispan-ai/spring-ai</module>
```

The `mcp-server/` folder is not a Maven module.

## Embedding Model Choice

For the standalone `langchain4j/` tutorial, use an in-process embedding model (`langchain4j-embeddings-all-minilm-l6-v2`) to avoid requiring external API keys. This keeps the tutorial self-contained.

For the Quarkus and Spring AI tutorials, make the embedding model configurable via properties so users can swap in their preferred provider (OpenAI, Ollama, etc.). Default to an in-process model if one is available for the framework.

## Testing Strategy

- **`langchain4j/`**: JUnit 5 + Testcontainers with Infinispan container
- **`langchain4j-quarkus/`**: `@QuarkusTest` with Dev Services (auto-provisions Infinispan)
- **`spring-ai/`**: `@SpringBootTest` + Testcontainers with Infinispan container
- **`mcp-server/`**: No automated tests — manual verification via README instructions

## Future Work (Out of Scope)

- Agentic example storing conversation context — to be added later
- Infinispan AI skills tutorials (private repo for now)
- Tutorial documentation/guides (to be written once code examples are in place)
- Chat memory / conversation store tutorials
- LLM response caching tutorials
