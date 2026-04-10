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
