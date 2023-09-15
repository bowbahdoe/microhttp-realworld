package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetTagsHandler extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public GetTagsHandler(SQLiteDataSource db) {
        super("GET", Pattern.compile("/api/tags"));
        this.db = db;
    }

    @Override
    protected JsonResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws SQLException {
        var names = new ArrayList<String>();
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement("""
                     SELECT name
                     FROM tag
                     """)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        }

        return new JsonResponse(
                Json.objectBuilder()
                    .put("tags", Json.of(names, Json::of))
        );
    }
}
