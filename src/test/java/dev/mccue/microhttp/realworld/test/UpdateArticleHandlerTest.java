package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class UpdateArticleHandlerTest extends HandlerTest {
    @Test
    public void createBasicArticle() throws Exception {
        var user = AuthUser.create(rootHandler);
        var createResponse = this.rootHandler.handle(new Request(
                "POST",
                "/api/articles",
                "",
                List.of(user.authHeader()),
                """
                        {
                          "article": {
                            "title": "How to train your dragon",
                            "description": "Ever wonder how?",
                            "body": "You have to believe",
                            "tagList": ["reactjs", "reactjs", "angularjs", "dragons"]
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        )).intoResponse();
        var createResponseJson = Json.readString(new String(
                createResponse.body(),
                StandardCharsets.UTF_8
        ));
        var slug = JsonDecoder.field(
                createResponseJson,
                "article",
                JsonDecoder.field("slug", JsonDecoder::string)
        );

        var updateResponse = this.rootHandler.handle(new Request(
                "PUT",
                "/api/articles/" + slug,
                "",
                List.of(user.authHeader()),
                Json.objectBuilder()
                        .put("article", Json.objectBuilder()
                                .put("title", "ABC"))
                        .build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        )).intoResponse();


        System.out.println(new String(updateResponse.body()));

    }
}
