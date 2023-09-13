package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.handlers.*;
import dev.mccue.microhttp.systemlogger.SystemLogger;
import dev.mccue.reasonphrase.ReasonPhrase;
import org.microhttp.*;
import org.sqlite.SQLiteDataSource;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.concurrent.Executors;

public final class Main {
    private static final System.Logger LOGGER =
            System.getLogger(Main.class.getName());

    private final List<Handler> handlers;

    private Main() throws Exception {
        var db = getDB();
        this.handlers = List.of(
                new CorsHandler(),
                new FollowHandler(db),
                new GetCurrentUserHandler(db),
                new GetTagsHandler(db),
                new HealthHandler(),
                new ListArticlesHandler(db),
                new LoginHandler(db),
                new RegisterUserHandler(db),
                new UnfollowHandler(db),
                new UpdateUserHandler(db)
        );
    }

    static SQLiteDataSource getDB() throws Exception {
        SQLiteDataSource db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:test.db");
        try (var conn = db.getConnection()) {
            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS "user" (
                        id integer primary key autoincrement,
                        password_hash text,
                        email text unique,
                        username text unique,
                        image text,
                        bio text
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS follow (
                        id integer primary key autoincrement,
                        follower_user_id integer references user(id),
                        following_user_id integer references user(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article (
                        id integer primary key autoincrement,
                        external_id text unique,
                        title text,
                        description text,
                        body text,
                        created_at datetime not null default current_timestamp,
                        updated_at datetime not null default current_timestamp,
                        user_id integer references user(id)
                    );
                    """
            )) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS tag (
                        id integer primary key autoincrement,
                        name text unique
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article_tag (
                        article_id integer references article(id),
                        tag_id integer references tag(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article_favorite (
                        article_id integer references article(id),
                        user_id integer references user(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS comment (
                        id integer primary key autoincrement,
                        body text,
                        article_id integer references article(id),
                        user_id integer references user(id),
                        created_at datetime,
                        updated_at datetime
                    );
                    """)) {
                stmt.execute();
            }
        }
        return db;
    }

    public Response handleRequest(Request request) throws Exception {
        try {
            for (var handler : handlers) {
                var intoResponse = handler.handle(request);
                if (intoResponse != null) {
                    return intoResponse.intoResponse();
                }
            }
        } catch (Exception e) {
            if (e instanceof IntoResponse intoResponse) {
                return intoResponse.intoResponse();
            }
            throw e;
        }


        return new JsonResponse(
                404,
                Json.objectBuilder()
                    .put("errors", Json.objectBuilder()
                            .put("other", Json.arrayBuilder()
                                    .add("Route not found.")))
        ).intoResponse();
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
                        .withPort(5555)
                        .build(),
                new SystemLogger(),
                (req, cb) -> executor.submit(() -> {
                    try {
                        LOGGER.log(Level.INFO, req.method() + " " + req.uri());
                        cb.accept(handleRequest(req));
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
