package dev.mccue.microhttp.realworld.test;

import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CreateArticleHandlerTest extends HandlerTest {
    @Test
    public void createBasicArticle() throws Exception {
        var user = AuthUser.create(rootHandler);
        var response = this.rootHandler.handle(new Request(
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

        System.out.println(new String(response.body(), StandardCharsets.UTF_8));
    }
}
