# Infinispan AI Tutorials Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a top-level `infinispan-ai/` directory with four tutorial modules demonstrating Infinispan's AI capabilities — embedding stores via LangChain4j (standalone + Quarkus) and Spring AI, plus an MCP server setup guide.

**Architecture:** Each tutorial is an independent Maven module (except MCP which is config-only). Standalone LangChain4j uses a plain `main()` method. Quarkus and Spring AI tutorials are REST apps. All use in-process embedding models to avoid external API key requirements.

**Tech Stack:** Java 17, Maven, Infinispan 16.2.0-SNAPSHOT, LangChain4j 1.13.0, Quarkus 3.34.2, Spring Boot (via parent BOM), Spring AI 2.0.0-M4, Testcontainers, AllMiniLmL6V2 embedding model.

---

## File Structure

```
infinispan-ai/
  langchain4j/
    pom.xml
    src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4j.java
    src/main/resources/logging.properties
    src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4jTest.java
  langchain4j-quarkus/
    pom.xml
    src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResource.java
    src/main/resources/application.properties
    src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResourceTest.java
  spring-ai/
    pom.xml
    src/main/java/org/infinispan/tutorial/simple/ai/springai/InfinispanSpringAiApp.java
    src/main/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreController.java
    src/main/resources/application.properties
    src/test/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreIT.java
    src/test/resources/application-test.properties
  mcp-server/
    docker-compose.yml
    mcp.json
    README.md
```

Root `pom.xml` will be modified to add the three Java modules.

---

### Task 1: Standalone LangChain4j Embedding Store Tutorial

**Files:**
- Create: `infinispan-ai/langchain4j/pom.xml`
- Create: `infinispan-ai/langchain4j/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4j.java`
- Create: `infinispan-ai/langchain4j/src/main/resources/logging.properties`
- Create: `infinispan-ai/langchain4j/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4jTest.java`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infinispan-ai/langchain4j/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j
mkdir -p infinispan-ai/langchain4j/src/main/resources
mkdir -p infinispan-ai/langchain4j/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j
```

- [ ] **Step 2: Create pom.xml**

Create `infinispan-ai/langchain4j/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <artifactId>infinispan-simple-tutorials-ai-langchain4j</artifactId>
    <parent>
        <relativePath>../../pom.xml</relativePath>
        <version>16.2.0-SNAPSHOT</version>
        <groupId>org.infinispan.tutorial.simple</groupId>
        <artifactId>infinispan-simple-tutorials</artifactId>
    </parent>
    <name>Infinispan Simple Tutorials: AI LangChain4j Embedding Store</name>

    <properties>
        <langchain4j.version>1.13.0</langchain4j.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <executable>java</executable>
                    <arguments>
                        <argument>-Djava.net.preferIPv4Stack=true</argument>
                        <argument>-Djava.util.logging.config.file=src/main/resources/logging.properties</argument>
                        <argument>-classpath</argument>
                        <classpath />
                        <argument>org.infinispan.tutorial.simple.ai.langchain4j.InfinispanLangchain4j</argument>
                    </arguments>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.infinispan.tutorial.simple</groupId>
            <artifactId>connect-to-infinispan-server</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.infinispan</groupId>
            <artifactId>infinispan-client-hotrod</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-infinispan</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-embeddings-all-minilm-l6-v2-q</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```


- [ ] **Step 3: Create logging.properties**

Create `infinispan-ai/langchain4j/src/main/resources/logging.properties`:

```properties
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level=INFO
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
.level=INFO
org.infinispan.level=INFO
```

- [ ] **Step 4: Create the main Java class**

Create `infinispan-ai/langchain4j/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4j.java`:

```java
package org.infinispan.tutorial.simple.ai.langchain4j;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.List;

public class InfinispanLangchain4j {

   static InfinispanEmbeddingStore embeddingStore;
   static EmbeddingModel embeddingModel;

   public static void main(String[] args) {
      initEmbeddingModel();
      connectAndCreateStore();
      try {
         storeAndSearch();
      } finally {
         disconnect();
      }
   }

   static void initEmbeddingModel() {
      embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
      System.out.println("Embedding model initialized. Dimension: " + embeddingModel.dimension());
   }

   static void connectAndCreateStore() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      embeddingStore = InfinispanEmbeddingStore.builder()
            .cacheName("langchain4j-embeddings")
            .dimension(embeddingModel.dimension())
            .infinispanConfigBuilder(builder)
            .distance(3)
            .build();
      System.out.println("Connected to Infinispan and created embedding store.");
   }

   static void storeAndSearch() {
      // Store some text segments with metadata
      addEmbedding("Infinispan is a distributed in-memory key/value data store",
            Metadata.from("source", "docs").put("topic", "overview"));
      addEmbedding("Infinispan supports vector search for AI use cases",
            Metadata.from("source", "docs").put("topic", "ai"));
      addEmbedding("Infinispan can be used as an embedding store with LangChain4j",
            Metadata.from("source", "tutorial").put("topic", "ai"));

      System.out.println("Stored 3 text segments with embeddings.\n");

      // Search by similarity
      String query = "How can I use Infinispan with AI?";
      System.out.println("Query: \"" + query + "\"");

      Embedding queryEmbedding = embeddingModel.embed(query).content();
      EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(3)
            .build();

      EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
      List<EmbeddingMatch<TextSegment>> matches = result.matches();

      System.out.println("Found " + matches.size() + " results:");
      for (EmbeddingMatch<TextSegment> match : matches) {
         System.out.printf("  Score: %.4f | Text: %s%n",
               match.score(), match.embedded().text());
      }
   }

   static void addEmbedding(String text, Metadata metadata) {
      TextSegment segment = TextSegment.from(text, metadata);
      Embedding embedding = embeddingModel.embed(segment).content();
      embeddingStore.add(embedding, segment);
   }

   static void disconnect() {
      if (embeddingStore != null) {
         embeddingStore.removeAll();
      }
   }
}
```

- [ ] **Step 5: Create the test class**

Create `infinispan-ai/langchain4j/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/InfinispanLangchain4jTest.java`:

```java
package org.infinispan.tutorial.simple.ai.langchain4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanLangchain4jTest {

   @BeforeAll
   public static void start() {
      InfinispanLangchain4j.initEmbeddingModel();
      InfinispanLangchain4j.connectAndCreateStore();
   }

   @AfterAll
   public static void stop() {
      InfinispanLangchain4j.disconnect();
   }

   @Test
   public void testStoreAndSearch() {
      assertNotNull(InfinispanLangchain4j.embeddingStore);
      assertNotNull(InfinispanLangchain4j.embeddingModel);
      InfinispanLangchain4j.storeAndSearch();
   }
}
```

- [ ] **Step 6: Verify the module compiles**

Run:
```bash
cd infinispan-ai/langchain4j && ../../mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add infinispan-ai/langchain4j/
git commit -m "Add LangChain4j standalone embedding store tutorial"
```

---

### Task 2: Quarkus LangChain4j Embedding Store Tutorial

**Files:**
- Create: `infinispan-ai/langchain4j-quarkus/pom.xml`
- Create: `infinispan-ai/langchain4j-quarkus/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResource.java`
- Create: `infinispan-ai/langchain4j-quarkus/src/main/resources/application.properties`
- Create: `infinispan-ai/langchain4j-quarkus/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResourceTest.java`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infinispan-ai/langchain4j-quarkus/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus
mkdir -p infinispan-ai/langchain4j-quarkus/src/main/resources
mkdir -p infinispan-ai/langchain4j-quarkus/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus
```

- [ ] **Step 2: Create pom.xml**

Create `infinispan-ai/langchain4j-quarkus/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.infinispan.tutorial.simple</groupId>
    <version>16.2.0-SNAPSHOT</version>
    <artifactId>infinispan-simple-tutorials-ai-langchain4j-quarkus</artifactId>
    <name>Infinispan Simple Tutorials: AI LangChain4j Quarkus Embedding Store</name>

    <properties>
        <compiler-plugin.version>3.13.0</compiler-plugin.version>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
        <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
        <quarkus.platform.version>3.34.2</quarkus.platform.version>
        <skipITs>true</skipITs>
        <surefire-plugin.version>3.5.5</surefire-plugin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>${quarkus.platform.artifact-id}</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>io.quarkiverse.langchain4j</groupId>
                <artifactId>quarkus-langchain4j-bom</artifactId>
                <version>999-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkiverse.langchain4j</groupId>
            <artifactId>quarkus-langchain4j-infinispan</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-infinispan-client</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-embeddings-all-minilm-l6-v2-q</artifactId>
        </dependency>

        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>${quarkus.platform.group-id}</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.platform.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${compiler-plugin.version}</version>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${surefire-plugin.version}</version>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                        <maven.home>${maven.home}</maven.home>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> **Note:** Quarkus LangChain4j is still in development. Use the SNAPSHOT version built from main (`999-SNAPSHOT`). Add the Sonatype snapshots repository. A release is coming soon — update the version once released.

- [ ] **Step 3: Create application.properties**

Create `infinispan-ai/langchain4j-quarkus/src/main/resources/application.properties`:

```properties
# LangChain4j Infinispan embedding store configuration
quarkus.langchain4j.infinispan.dimension=384
quarkus.langchain4j.infinispan.cache-name=langchain4j-embeddings
quarkus.langchain4j.infinispan.distance=3
quarkus.langchain4j.infinispan.similarity=COSINE
quarkus.langchain4j.infinispan.create-cache=true

# Uncomment if you are running a server locally
# quarkus.infinispan-client.devservices.enabled=false
# quarkus.infinispan-client.hosts=localhost:11222
# quarkus.infinispan-client.username=admin
# quarkus.infinispan-client.password=password

# Assumes a server is running, with admin/password, locally
%prod.quarkus.infinispan-client.hosts=localhost:11222
%prod.quarkus.infinispan-client.username=admin
%prod.quarkus.infinispan-client.password=password
```

- [ ] **Step 4: Create the REST resource**

Create `infinispan-ai/langchain4j-quarkus/src/main/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResource.java`:

```java
package org.infinispan.tutorial.simple.ai.langchain4j.quarkus;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/embedding")
@Produces(MediaType.APPLICATION_JSON)
public class EmbeddingStoreResource {

   @Inject
   EmbeddingStore<TextSegment> embeddingStore;

   @Inject
   EmbeddingModel embeddingModel;

   @POST
   @Path("/add")
   public String addDocument(@QueryParam("text") String text) {
      TextSegment segment = TextSegment.from(text);
      Embedding embedding = embeddingModel.embed(segment).content();
      String id = embeddingStore.add(embedding, segment);
      return id;
   }

   @GET
   @Path("/search")
   public List<String> search(@QueryParam("query") String query,
                              @QueryParam("maxResults") int maxResults) {
      Embedding queryEmbedding = embeddingModel.embed(query).content();
      EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResults > 0 ? maxResults : 3)
            .build();

      EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
      return result.matches().stream()
            .map(match -> String.format("Score: %.4f | %s", match.score(), match.embedded().text()))
            .toList();
   }
}
```

- [ ] **Step 5: Create the test class**

Create `infinispan-ai/langchain4j-quarkus/src/test/java/org/infinispan/tutorial/simple/ai/langchain4j/quarkus/EmbeddingStoreResourceTest.java`:

```java
package org.infinispan.tutorial.simple.ai.langchain4j.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class EmbeddingStoreResourceTest {

   @Test
   public void testAddAndSearch() {
      // Add a document
      given()
            .queryParam("text", "Infinispan is a distributed in-memory data store")
            .when().post("/embedding/add")
            .then()
            .statusCode(200)
            .body(notNullValue());

      // Add another document
      given()
            .queryParam("text", "Infinispan supports vector search for AI workloads")
            .when().post("/embedding/add")
            .then()
            .statusCode(200);

      // Search
      given()
            .queryParam("query", "AI and vector search")
            .queryParam("maxResults", 2)
            .when().get("/embedding/search")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThan(0));
   }
}
```

- [ ] **Step 6: Verify the module compiles**

Run:
```bash
cd infinispan-ai/langchain4j-quarkus && ../../mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add infinispan-ai/langchain4j-quarkus/
git commit -m "Add Quarkus LangChain4j embedding store tutorial"
```

---

### Task 3: Spring AI Embedding Store Tutorial

**Files:**
- Create: `infinispan-ai/spring-ai/pom.xml`
- Create: `infinispan-ai/spring-ai/src/main/java/org/infinispan/tutorial/simple/ai/springai/InfinispanSpringAiApp.java`
- Create: `infinispan-ai/spring-ai/src/main/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreController.java`
- Create: `infinispan-ai/spring-ai/src/main/resources/application.properties`
- Create: `infinispan-ai/spring-ai/src/test/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreIT.java`
- Create: `infinispan-ai/spring-ai/src/test/resources/application-test.properties`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infinispan-ai/spring-ai/src/main/java/org/infinispan/tutorial/simple/ai/springai
mkdir -p infinispan-ai/spring-ai/src/main/resources
mkdir -p infinispan-ai/spring-ai/src/test/java/org/infinispan/tutorial/simple/ai/springai
mkdir -p infinispan-ai/spring-ai/src/test/resources
```

- [ ] **Step 2: Create pom.xml**

Create `infinispan-ai/spring-ai/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <relativePath>../../pom.xml</relativePath>
        <version>16.2.0-SNAPSHOT</version>
        <groupId>org.infinispan.tutorial.simple</groupId>
        <artifactId>infinispan-simple-tutorials</artifactId>
    </parent>

    <artifactId>infinispan-simple-tutorials-ai-spring-ai</artifactId>
    <name>Infinispan Simple Tutorials: AI Spring AI Embedding Store</name>

    <properties>
        <spring-ai.version>2.0.0-M4</spring-ai.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.infinispan</groupId>
                <artifactId>infinispan-bom</artifactId>
                <version>${version.infinispan}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>${version.spring.boot4}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>${version.junit5}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.infinispan</groupId>
            <artifactId>infinispan-spring-boot4-starter-remote</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-autoconfigure-vector-store-infinispan</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-onnx-transformers</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.infinispan</groupId>
            <artifactId>testcontainers-infinispan</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${version.spring.boot4}</version>
            </plugin>
        </plugins>
    </build>
</project>
```


- [ ] **Step 3: Create application.properties**

Create `infinispan-ai/spring-ai/src/main/resources/application.properties`:

```properties
# Infinispan connection
infinispan.remote.server-list=127.0.0.1:11222
infinispan.remote.auth-username=admin
infinispan.remote.auth-password=password

# Spring AI Infinispan vector store
spring.ai.vectorstore.infinispan.store-name=spring-ai-embeddings
spring.ai.vectorstore.infinispan.similarity=COSINE
spring.ai.vectorstore.infinispan.create-store=true
spring.ai.vectorstore.infinispan.distance=3
```

- [ ] **Step 4: Create the Spring Boot application class**

Create `infinispan-ai/spring-ai/src/main/java/org/infinispan/tutorial/simple/ai/springai/InfinispanSpringAiApp.java`:

```java
package org.infinispan.tutorial.simple.ai.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InfinispanSpringAiApp {

   public static void main(String... args) {
      SpringApplication.run(InfinispanSpringAiApp.class, args);
   }
}
```

- [ ] **Step 5: Create the REST controller**

Create `infinispan-ai/spring-ai/src/main/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreController.java`:

```java
package org.infinispan.tutorial.simple.ai.springai;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embedding")
public class EmbeddingStoreController {

   private final VectorStore vectorStore;

   public EmbeddingStoreController(VectorStore vectorStore) {
      this.vectorStore = vectorStore;
   }

   @PostMapping("/add")
   public String addDocument(@RequestParam String text) {
      Document document = new Document(text, Map.of("source", "tutorial"));
      vectorStore.add(List.of(document));
      return document.getId();
   }

   @GetMapping("/search")
   public List<String> search(@RequestParam String query,
                              @RequestParam(defaultValue = "3") int maxResults) {
      SearchRequest searchRequest = SearchRequest.query(query).withTopK(maxResults);
      List<Document> results = vectorStore.similaritySearch(searchRequest);
      return results.stream()
            .map(doc -> String.format("Score: %.4f | %s",
                  doc.getMetadata().getOrDefault("distance", 0.0), doc.getText()))
            .toList();
   }
}
```

- [ ] **Step 6: Create test properties**

Create `infinispan-ai/spring-ai/src/test/resources/application-test.properties`:

```properties
# Test properties — Infinispan connection is injected via @DynamicPropertySource
spring.ai.vectorstore.infinispan.store-name=spring-ai-embeddings
spring.ai.vectorstore.infinispan.create-store=true
spring.ai.vectorstore.infinispan.distance=3
spring.ai.vectorstore.infinispan.similarity=COSINE
```

- [ ] **Step 7: Create the integration test**

Create `infinispan-ai/spring-ai/src/test/java/org/infinispan/tutorial/simple/ai/springai/EmbeddingStoreIT.java`:

```java
package org.infinispan.tutorial.simple.ai.springai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class EmbeddingStoreIT {

   @Container
   static InfinispanContainer infinispan = new InfinispanContainer();

   @DynamicPropertySource
   static void infinispanProperties(DynamicPropertyRegistry registry) {
      registry.add("infinispan.remote.server-list",
            () -> infinispan.getHost() + ":" + infinispan.getMappedPort(11222));
      registry.add("infinispan.remote.auth-username", () -> "admin");
      registry.add("infinispan.remote.auth-password", () -> "password");
   }

   @Autowired
   VectorStore vectorStore;

   @Test
   void addAndSearchDocuments() {
      assertThat(vectorStore).isNotNull();

      // Add documents
      Document doc1 = new Document(
            "Infinispan is a distributed in-memory data store",
            Map.of("source", "docs"));
      Document doc2 = new Document(
            "Infinispan supports vector search for AI workloads",
            Map.of("source", "docs"));
      vectorStore.add(List.of(doc1, doc2));

      // Search
      SearchRequest searchRequest = SearchRequest.query("vector search AI").withTopK(2);
      List<Document> results = vectorStore.similaritySearch(searchRequest);

      assertThat(results).isNotEmpty();
      assertThat(results.get(0).getText()).contains("vector");
   }
}
```

- [ ] **Step 8: Verify the module compiles**

Run:
```bash
cd infinispan-ai/spring-ai && ../../mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add infinispan-ai/spring-ai/
git commit -m "Add Spring AI embedding store tutorial"
```

---

### Task 4: MCP Server Setup Tutorial

**Files:**
- Create: `infinispan-ai/mcp-server/docker-compose.yml`
- Create: `infinispan-ai/mcp-server/mcp.json`
- Create: `infinispan-ai/mcp-server/README.md`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infinispan-ai/mcp-server
```

- [ ] **Step 2: Create docker-compose.yml**

Create `infinispan-ai/mcp-server/docker-compose.yml`:

```yaml
version: '3'
services:
  infinispan:
    image: quay.io/infinispan/server:16.2
    ports:
      - "11222:11222"
    environment:
      - USER=admin
      - PASS=password
      - JAVA_OPTIONS=-Dorg.infinispan.feature.mcp=true
```

- [ ] **Step 3: Create mcp.json sample configuration**

Create `infinispan-ai/mcp-server/mcp.json`:

```json
{
  "mcpServers": {
    "infinispan": {
      "url": "http://localhost:11222/v3/mcp",
      "type": "streamable-http"
    }
  }
}
```

- [ ] **Step 4: Create README.md**

Create `infinispan-ai/mcp-server/README.md`:

```markdown
# Infinispan MCP Server Tutorial

This tutorial shows how to enable and use Infinispan's MCP (Model Context Protocol) endpoint.

Infinispan exposes an MCP endpoint that allows AI assistants and LLM-based tools to interact with
your Infinispan cluster — managing caches, counters, schemas, and more through natural language.

## Prerequisites

- Docker and Docker Compose
- An MCP client (e.g., Claude Desktop, an IDE with MCP support)

## Starting Infinispan with MCP enabled

The MCP feature must be explicitly enabled via a system property.

```bash
docker compose up -d
```

This starts Infinispan Server with the MCP endpoint enabled at `http://localhost:11222/v3/mcp`.

## Connecting an MCP client

### Claude Desktop

Add the following to your Claude Desktop MCP configuration:

```json
{
  "mcpServers": {
    "infinispan": {
      "url": "http://localhost:11222/v3/mcp",
      "type": "streamable-http"
    }
  }
}
```

### Claude Code

Add the Infinispan MCP server in your Claude Code settings or project `.mcp.json`:

```json
{
  "mcpServers": {
    "infinispan": {
      "url": "http://localhost:11222/v3/mcp",
      "type": "streamable-http"
    }
  }
}
```

## Available MCP Capabilities

Once connected, the Infinispan MCP server exposes:

### Tools
- Cache operations (create, list, get, put, remove, clear)
- Counter operations
- Schema management

### Resources
- Server information and configuration
- Audit and access logs

### Prompts
- Documentation search guidance

## Verifying the Connection

You can verify the MCP endpoint is running:

```bash
curl -s http://localhost:11222/v3/mcp -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":1}'
```

## Stopping Infinispan

```bash
docker compose down
```
```

- [ ] **Step 5: Commit**

```bash
git add infinispan-ai/mcp-server/
git commit -m "Add MCP server setup tutorial"
```

---

### Task 5: Register Modules in Root POM

**Files:**
- Modify: `pom.xml` (root)

- [ ] **Step 1: Add modules to root pom.xml**

In the root `pom.xml`, add the following three lines inside the `<modules>` section, after the existing integrations modules (after line 167):

```xml
        <module>infinispan-ai/langchain4j</module>
        <module>infinispan-ai/langchain4j-quarkus</module>
        <module>infinispan-ai/spring-ai</module>
```

- [ ] **Step 2: Verify the full build compiles**

Run:
```bash
./mvnw compile -pl infinispan-ai/langchain4j,infinispan-ai/langchain4j-quarkus,infinispan-ai/spring-ai -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "Register infinispan-ai modules in root pom"
```

---

### Task 6: Run Tests and Fix Issues

- [ ] **Step 1: Run langchain4j tests**

```bash
./mvnw test -pl infinispan-ai/langchain4j
```
Expected: Tests pass. If they fail, check the Infinispan container connection and embedding store builder configuration.

- [ ] **Step 2: Run quarkus tests**

```bash
./mvnw test -pl infinispan-ai/langchain4j-quarkus
```
Expected: Tests pass. Quarkus Dev Services should auto-start Infinispan.

- [ ] **Step 3: Run spring-ai tests**

```bash
./mvnw test -pl infinispan-ai/spring-ai
```
Expected: Tests pass with Testcontainers starting an Infinispan container.

- [ ] **Step 4: Fix any test failures**

Address compilation or runtime errors. Common issues:
- Wrong dependency versions — check BOM compatibility
- Missing transitive dependencies
- Container connection timeouts — increase timeout values
- Embedding model loading issues — verify ONNX runtime is on classpath

- [ ] **Step 5: Commit fixes if needed**

```bash
git add -u
git commit -m "Fix test issues in infinispan-ai tutorials"
```
