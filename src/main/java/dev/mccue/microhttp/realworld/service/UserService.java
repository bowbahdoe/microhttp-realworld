package dev.mccue.microhttp.realworld.service;

import dev.mccue.microhttp.realworld.domain.PasswordHash;
import dev.mccue.microhttp.realworld.domain.User;
import org.sqlite.SQLiteDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class UserService {
    private final SQLiteDataSource db;


    public UserService(SQLiteDataSource db) {
        this.db = db;
    }

    private static final String SELECT_FIELDS = String.join(", ", List.of(
            "user.id",
            "user.email",
            "user.username",
            "user.bio",
            "user.image",
            "user.password_hash"
    ));

    private static User userFromRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                Optional.ofNullable(rs.getString(4)),
                Optional.ofNullable(rs.getString(5)),
                new PasswordHash(rs.getString(6))
        );
    }


    public Optional<User> findById(long userId) {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM "user"
                     WHERE "user".id = ?
                     """.formatted(SELECT_FIELDS))) {
            stmt.setLong(1, userId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(userFromRow(rs));
            }
            else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByUsername(String username) {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM "user"
                     WHERE "user".username = ?
                     """.formatted(SELECT_FIELDS))) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(userFromRow(rs));
            }
            else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByUsernameOrId(String usernameOrId) {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM "user"
                     WHERE "user".username = ? OR "user".id = ?
                     """.formatted(SELECT_FIELDS))) {
            stmt.setString(1, usernameOrId);
            stmt.setString(2, usernameOrId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(userFromRow(rs));
            }
            else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFollowing(long followerId, long followingId) {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     // language=SQL
                     """
                     SELECT 1
                     FROM follow
                     WHERE follow.follower_user_id = ? AND follow.following_user_id = ?
                     """)) {
            stmt.setLong(1, followerId);
            stmt.setLong(2, followingId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}