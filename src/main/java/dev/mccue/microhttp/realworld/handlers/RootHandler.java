package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.Handler;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.util.List;

public final class RootHandler implements Handler {
    private final List<Handler> handlers;

    public RootHandler(SQLiteDataSource db) {
        this.handlers = List.of(
                new CorsHandler(),
                new FollowUserHandler(db),
                new GetCurrentUserHandler(db),
                new GetTagsHandler(db),
                new HealthHandler(),
                new ListArticlesHandler(db),
                new LoginHandler(db),
                new RegisterHandler(db),
                new UnfollowUserHandler(db),
                new UpdateUserHandler(db),
                new CreateArticleHandler(db),
                new UpdateArticleHandler(db)
        );
    }
    @Override
    public IntoResponse handle(Request request) throws Exception {
        try {
            for (var handler : handlers) {
                var intoResponse = handler.handle(request);
                if (intoResponse != null) {
                    return intoResponse;
                }
            }
        } catch (Exception e) {
            if (e instanceof IntoResponse intoResponse) {
                return intoResponse;
            }
            throw e;
        }


        return new JsonResponse(
                404,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("other", Json.arrayBuilder()
                                        .add("Route not found.")))
        );
    }
}
