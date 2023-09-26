package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.handler.IntoResponse;
import dev.mccue.microhttp.realworld.Auth;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.AuthContext;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class UpdateUserHandler
        extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public UpdateUserHandler(
            SQLiteDataSource db
    ) {
        super("PUT", Pattern.compile("/api/user"));
        this.db = db;
    }

    public record UpdateUserRequest(
            Optional<String> email,
            Optional<String> username,
            Optional<String> password,
            Optional<String> image,
            Optional<String> bio
    ) {
        public static UpdateUserRequest fromJson(Json json) {
            return new UpdateUserRequest(
                    JsonDecoder.optionalField(json, "email", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "username", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "password", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "image", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "bio", JsonDecoder::string)

            );
        }
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws SQLException {
        var updateUserRequest = RequestUtils.parseBody(request, UpdateUserRequest::fromJson);

        record Update(String setExpression, Object value) {}

        var updates = new ArrayList<Update>();
        updateUserRequest.username.ifPresent(username -> {
            updates.add(new Update("username = ?", username));
        });
        updateUserRequest.email.ifPresent(email -> {
            updates.add(new Update("email = ?", email));
        });
        updateUserRequest.bio.ifPresent(bio -> {
            updates.add(new Update("bio = ?", bio));
        });
        updateUserRequest.image.ifPresent(image -> {
            updates.add(new Update("image = ?", image));
        });
        try (var conn = this.db.getConnection()) {
            if (!updates.isEmpty()) {
                try (var stmt = conn.prepareStatement(
                        """
                        UPDATE user
                        SET
                           %s
                        WHERE id = ?
                        """.formatted(
                                updates.stream()
                                        .map(Update::setExpression)
                                        .collect(Collectors.joining(",\n    "))
                        )
                )) {
                    int i = 1;
                    for (var update : updates) {
                        stmt.setObject(i, update.value);
                        i++;
                    }

                    stmt.setLong(i, authContext.userId());
                    stmt.execute();
                }

            }

            try (var stmt = conn.prepareStatement(
                    """
                    SELECT
                       "user".id,
                       "user".email,
                       "user".username,
                       "user".bio,
                       "user".image
                       "user".password_hash
                    FROM "user"
                    WHERE "user".id = ?
                    """)) {
                stmt.setLong(1, authContext.userId());
                var rs = stmt.executeQuery();
                rs.next();
                return new JsonResponse(
                        Json.objectBuilder()
                                .put(
                                        "user",
                                        Json.objectBuilder()
                                                .put("email", rs.getString("email"))
                                                .put("username", rs.getString("username"))
                                                .put("bio", rs.getString("bio"))
                                                .put("image", rs.getString("image"))
                                                .put("token", Auth.jwtForUser(rs.getLong("id")))
                                )
                );
            }
        }
    }
}
