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

public final class RegisterHandler extends RouteHandler {
    private final SQLiteDataSource db;

    public RegisterHandler(
            SQLiteDataSource db
    ) {
        super("POST", Pattern.compile("/api/users"));
        this.db = db;
    }

    public record RegisterUserRequest(String email, String username, String password) {
        static RegisterUserRequest fromJson(Json json){
            return JsonDecoder.field(
                    json,
                    "user",
                    userFields -> new RegisterUserRequest(
                            JsonDecoder.field(userFields, "email", JsonDecoder::string),
                            JsonDecoder.field(userFields, "username", JsonDecoder::string),
                            JsonDecoder.field(userFields, "password", JsonDecoder::string)
                    ));
        }
    }

    @Override
    protected IntoResponse handleRoute(
            Matcher routeMatch,
            Request request
    ) throws SQLException {
        var registerUserRequest = RequestUtils.parseBody(
                request,
                RegisterUserRequest::fromJson
        );


        var username = registerUserRequest.username.toLowerCase();
        var email = registerUserRequest.email.toLowerCase();
        var password = registerUserRequest.password;

        try (var conn = this.db.getConnection()) {
            conn.setAutoCommit(false);
            try (var selectByEmail = conn.prepareStatement(
                    """
                    SELECT 1
                    FROM "user"
                    WHERE "user".email = ?
                    """)) {
                selectByEmail.setString(1, email);
                if (selectByEmail.executeQuery().next()) {
                    return Responses.validationError(List.of("email taken"));
                }
            }

            try (var selectByUsername = conn.prepareStatement(
                    """
                    SELECT 1
                    FROM "user"
                    WHERE "user".username = ?
                    """)) {
                selectByUsername.setString(1, username);
                if (selectByUsername.executeQuery().next()) {
                    return Responses.validationError(List.of("username taken"));
                }
            }

            try (var insert = conn.prepareStatement(
                    """
                    INSERT INTO "user"(username, email, password_hash)
                    VALUES (?, ?, ?)
                    """)) {
                insert.setString(1, username);
                insert.setString(2, email);
                insert.setString(3, PasswordHash.fromUnHashedPassword(password).value());
                insert.execute();
            }

            conn.commit();

            try (var findByEmail = conn.prepareStatement(
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
                findByEmail.setString(1, email);

                var rs = findByEmail.executeQuery();
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