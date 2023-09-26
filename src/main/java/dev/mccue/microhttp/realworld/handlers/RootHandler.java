package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.handler.DelegatingHandler;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.util.List;

public final class RootHandler extends DelegatingHandler {
    public RootHandler(SQLiteDataSource db) {
        super(List.of(
                new CorsHandler(),
                new CreateArticleHandler(db),
                new FollowUserHandler(db),
                new GetCurrentUserHandler(db),
                new GetProfileHandler(db),
                new GetTagsHandler(db),
                new HealthHandler(),
                new ListArticlesHandler(db),
                new LoginHandler(db),
                new RegisterHandler(db),
                new UnfollowUserHandler(db),
                new UpdateArticleHandler(db),
                new UpdateUserHandler(db)
        ), new JsonResponse(
                404,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("other", Json.arrayBuilder()
                                        .add("Route not found.")))
        ));
    }
}
