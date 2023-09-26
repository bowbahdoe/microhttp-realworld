package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.AuthContext;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FollowUserHandler extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public FollowUserHandler(SQLiteDataSource db) {
        super(
                "POST",
                Pattern.compile("/api/profiles/(?<username>.+)/follow")
        );
        this.db = db;
    }

    @Override
    public IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws SQLException {
        var username = URLDecoder.decode(
                matcher.group("username"),
                StandardCharsets.UTF_8
        );

        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT
                        "user".id,
                        "user".username,
                        "user".bio,
                        "user".image
                     FROM "user"
                     WHERE "user".username = ?
                     """)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                long followerId = authContext.userId();
                long followingId = rs.getLong("id");
                try (var insert = conn.prepareStatement(
                        """
                        INSERT OR IGNORE INTO follow(follower_user_id, following_user_id)
                        VALUES (?, ?)
                        """)) {
                    insert.setLong(1, followerId);
                    insert.setLong(2, followingId);
                    insert.execute();
                }

                return new JsonResponse(
                        Json.objectBuilder()
                                .put("profile", Json.objectBuilder()
                                        .put("username", username)
                                        .put("bio", rs.getString("bio"))
                                        .put("image", rs.getString("image"))
                                        .put("following", false))
                );
            }
        }

        return Responses.validationError(List.of("invalid user"));
    }

}
