package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.AuthContext;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetCurrentUserHandler
        extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public GetCurrentUserHandler(SQLiteDataSource db) {
        super(
                "GET",
                Pattern.compile("/api/user")
        );
        this.db = db;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws SQLException {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT
                        "user".id,
                        "user".email,
                        "user".username,
                        "user".bio,
                        "user".image
                     FROM "user"
                     WHERE "user".id = ?
                     """)) {
            stmt.setLong(1, authContext.userId());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new JsonResponse(
                        Json.objectBuilder()
                                .put(
                                        "user",
                                        Json.objectBuilder()
                                                .put("email", rs.getString("email"))
                                                .put("username", rs.getString("username"))
                                                .put("bio", rs.getString("bio"))
                                                .put("image", rs.getString("image"))
                                )
                );
            }
            else {
                return Responses.validationError(List.of("no user found"));
            }

        }
    }
}
