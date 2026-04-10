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
