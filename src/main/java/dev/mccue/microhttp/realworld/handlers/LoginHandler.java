package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.realworld.*;
import dev.mccue.microhttp.realworld.domain.PasswordHash;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LoginHandler extends RouteHandler {
    private final SQLiteDataSource db;

    public LoginHandler(SQLiteDataSource db) {
        super("POST", Pattern.compile("/api/users/login"));
        this.db = db;
    }

    public record LoginRequest(
            String email,
            String password
    ) {
        public static LoginRequest fromJson(Json json) {
            return JsonDecoder.field(json, "user", user -> new LoginRequest(
                    JsonDecoder.field(user, "email", JsonDecoder::string),
                    JsonDecoder.field(user, "password", JsonDecoder::string)
            ));
        }
    }


    @Override
    protected IntoResponse handleRoute(Matcher routeMatch, Request request) throws SQLException {
        var loginRequest = RequestUtils.parseBody(request, LoginRequest::fromJson);

        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT
                        "user".id,
                        "user".email,
                        "user".username,
                        "user".bio,
                        "user".image,
                        "user".password_hash
                     FROM "user"
                     WHERE "user".email = ?
                     """)) {
            stmt.setString(1, loginRequest.email());
            var rs = stmt.executeQuery();
            if (rs.next()) {
                var passwordHash = new PasswordHash(
                        rs.getString("password_hash")
                );

                if (passwordHash.isCorrectPassword(loginRequest.password)) {
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

            return new JsonResponse(
                    401,
                    Json.objectBuilder()
                            .put("errors", Json.objectBuilder()
                                    .put("body", Json.arrayBuilder()
                                            .add(Json.of("invalid email or password"))))
            );
        }
    }
}