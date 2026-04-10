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
