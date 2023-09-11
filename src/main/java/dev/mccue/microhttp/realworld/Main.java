package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.handlers.*;
import dev.mccue.microhttp.realworld.service.ArticleService;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.TagService;
import dev.mccue.microhttp.realworld.service.UserService;
import dev.mccue.microhttp.systemlogger.SystemLogger;
import org.microhttp.EventLoop;
import org.microhttp.Options;
import org.microhttp.Request;
import org.microhttp.Response;
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
        var tagService = new TagService(db);
        var articleService = new ArticleService(db);
        var userService = new UserService(db);
        var authService = new AuthService(userService);
        this.handlers = List.of(
                new CorsHandler(),
                new FollowUserHandler(authService, userService),
                new GetCurrentUserHandler(authService),
                new GetTagsHandler(tagService, authService),
                new HelloHandler(),
                new ListArticlesHandler(authService, articleService, userService),
                new LoginHandler(authService, userService),
                new RegisterUserHandler(authService, userService),
                new UnfollowUserHandler(authService, userService),
                new UpdateUserHandler(authService, userService)
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


        return new JsonResponse(Json.of("Unhandled route")).intoResponse();
    }

    public void start() throws Exception {
        var executor = Executors.newFixedThreadPool(10);
        var eventLoop = new EventLoop(
                new Options()
                        .withPort(5555),
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
