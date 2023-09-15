package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetProfileHandler
        extends MaybeAuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public GetProfileHandler(SQLiteDataSource db) {
        super(
                "GET",
                Pattern.compile("/api/profiles/(?<username>.+)")
        );
        this.db = db;
    }


    @Override
    protected IntoResponse handleMaybeAuthenticatedRoute(
            @Nullable AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        var username = matcher.group("username");

        var responseJson = Json.objectBuilder()
                .put("following", false);

        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement("""
                     SELECT "user".username,
                            "user".bio,
                            "user".image,
                            follow.id AS follow_id
                     FROM   "user"
                            LEFT JOIN follow
                                   ON follow.following_user_id = ?
                     WHERE  "user".username = ?
                     """)) {
            stmt.setObject(1, authContext == null ? null : authContext.userId());
            stmt.setObject(2, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                responseJson
                        .put("username", rs.getString("username"))
                        .put("bio", rs.getString("bio"))
                        .put("image", rs.getString("image"));
                if (rs.getObject("follow_id") != null) {
                    responseJson.put("following", true);
                }
            }
            else {
                return Responses.notFound();
            }
        }

        return new JsonResponse(responseJson);
    }
}
