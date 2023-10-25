package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.microhttp.handler.DelegatingHandler;
import dev.mccue.microhttp.handler.Handler;
import dev.mccue.microhttp.realworld.handlers.*;
import dev.mccue.microhttp.systemlogger.SystemLogger;
import org.microhttp.EventLoop;
import org.microhttp.OptionsBuilder;

import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public final class Main {
    private static final System.Logger LOGGER =
            System.getLogger(Main.class.getName());

    private final DelegatingHandler rootHandler;

    private Main() throws Exception {
        var db = DB.getDB(Path.of("test.db"));

        List<Handler> handlerList = List.of(
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
        );

        var defaultResponse = new JsonResponse(
                404,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("other", Json.arrayBuilder()
                                        .add("Route not found.")))
        );

        this.rootHandler = new DelegatingHandler(handlerList, defaultResponse);
    }

    public void start() throws Exception {
        var executor = Executors.newThreadPerTaskExecutor(
                Thread
                        .ofVirtual()
                        .name("handler-", 0)
                        .factory()
        );
        var eventLoop = new EventLoop(
                OptionsBuilder.newBuilder()
                        .withPort(Env.PORT)
                        .build(),
                new SystemLogger(),
                (req, cb) -> executor.submit(() -> {
                    try {
                        LOGGER.log(Level.INFO, req.method() + " " + req.uri());
                        cb.accept(rootHandler.handle(req).intoResponse());
                    } catch (Exception e) {
                        LOGGER.log(
                                Level.ERROR,
                                "Unhandled exception while handling " + req.method() + " " + req.uri(),
                                e
                        );
                        cb.accept(Responses.internalError().intoResponse());
                    }
                })
        );
        eventLoop.start();
        eventLoop.join();

    }

    public static void main(String[] args) throws Exception {
        new Main().start();
    }
}
