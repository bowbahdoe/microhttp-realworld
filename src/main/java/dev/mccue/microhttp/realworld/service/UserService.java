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

    public sealed interface RegistrationResult {
        record EmailTaken() implements RegistrationResult {

        }

        record UsernameTaken() implements RegistrationResult {

        }

        record Success(User user) implements RegistrationResult {
        }
    }

    public RegistrationResult register(
            String username,
            String email,
            String password
    ) {
        username = username.toLowerCase();
        email = email.toLowerCase();

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
                    return new RegistrationResult.EmailTaken();
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
                    return new RegistrationResult.UsernameTaken();
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
                    SELECT %s
                    FROM "user"
                    WHERE "user".email = ?
                    """.formatted(SELECT_FIELDS))) {
                findByEmail.setString(1, email);

                var rs = findByEmail.executeQuery();
                rs.next();
                var user = userFromRow(rs);

                return new RegistrationResult.Success(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public Optional<User> findByEmail(String email) {
        try (var conn = this.db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM "user"
                     WHERE "user".email = ?
                     """.formatted(SELECT_FIELDS))) {
            stmt.setString(1, email);
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

    public void update(User user) {
        try (var conn = this.db.getConnection();
             var update = conn.prepareStatement(
                     """
                     UPDATE user
                     SET
                         username = ?,
                         email = ?,
                         bio = ?,
                         image = ?,
                         password_hash = ?
                     WHERE id = ?
                     """)) {
            update.setString(1, user.username());
            update.setString(2, user.email());
            update.setString(3, user.bio().orElse(null));
            update.setString(4, user.image().orElse(null));
            update.setString(5, user.passwordHash().value());
            update.setLong(6, user.id());
            update.execute();
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

    public void follow(long followerId, long followingId) {
        try (var conn = this.db.getConnection();
             var insert = conn.prepareStatement(
                     """
                     INSERT OR IGNORE INTO follow(follower_user_id, following_user_id)
                     VALUES (?, ?)
                     """)) {
            insert.setLong(1, followerId);
            insert.setLong(2, followingId);
            insert.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unfollow(long followerId, long followingId) {
        try (var conn = this.db.getConnection();
             var insert = conn.prepareStatement(
                     """
                     DELETE FROM follow
                     WHERE follower_user_id = ? AND following_user_id = ?
                     """)) {
            insert.setLong(1, followerId);
            insert.setLong(2, followingId);
            insert.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}