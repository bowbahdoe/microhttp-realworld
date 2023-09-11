package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import org.microhttp.Header;
import org.microhttp.Request;
import org.microhttp.Response;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CorsHandler extends RouteHandler {
    public CorsHandler() {
        super("OPTIONS", Pattern.compile(".+"));
    }

    @Override
    protected IntoResponse handleRoute(
            Matcher routeMatch,
            Request request
    ) {
        return () -> new Response(
                200,
                "OK",
                List.of(
                        new Header("Access-Control-Allow-Origin", "*"),
                        new Header("Access-Control-Allow-Headers", "*")
                ),
                new byte[0]
        );
    }
}
