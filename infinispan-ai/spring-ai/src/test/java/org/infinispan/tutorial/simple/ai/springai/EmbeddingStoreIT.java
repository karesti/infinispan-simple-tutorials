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
