package dev.mccue.microhttp.realworld;

import dev.mccue.microhttp.realworld.handlers.RootHandler;
import dev.mccue.microhttp.systemlogger.SystemLogger;
import org.microhttp.EventLoop;
import org.microhttp.OptionsBuilder;

import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public final class Main {
    private static final System.Logger LOGGER =
            System.getLogger(Main.class.getName());

    private final RootHandler rootHandler;

    private Main() throws Exception {
        var db = DB.getDB(Path.of("test.db"));
        this.rootHandler = new RootHandler(db);
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
