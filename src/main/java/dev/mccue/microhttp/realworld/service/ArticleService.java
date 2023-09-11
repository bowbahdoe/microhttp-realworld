package dev.mccue.microhttp.realworld.service;

import dev.mccue.microhttp.realworld.domain.Article;
import dev.mccue.microhttp.realworld.domain.ArticleBuilder;
import dev.mccue.microhttp.realworld.domain.ExternalId;
import dev.mccue.microhttp.realworld.domain.FavoritedInfo;
import org.sqlite.SQLiteDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public final class ArticleService {
    private final SQLiteDataSource db;

    public ArticleService(SQLiteDataSource db) {
        this.db = db;
    }

    private static final String SELECT_FIELDS =
            String.join(", ", List.of(
                    "article.id",
                    "article.external_id",
                    "article.title",
                    "article.description",
                    "article.body",
                    "article.created_at",
                    "article.updated_at",
                    "article.user_id")
            );

    private static Article articleFromRow(ResultSet rs) throws SQLException {
        return ArticleBuilder.builder()
                .articleId(rs.getLong(1))
                .externalId(new ExternalId(rs.getString(2)))
                .title(rs.getString(3))
                .description(rs.getString(4))
                .body(rs.getString(5))
                .createdAt(rs.getTimestamp(6).toLocalDateTime())
                .updatedAt(rs.getTimestamp(7).toLocalDateTime())
                .userId(rs.getLong(8))
                .build();
    }

    public Optional<Article> forId(long articleId) {
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM article
                     WHERE article.article_id = ?
                     """.formatted(SELECT_FIELDS))) {
            stmt.setLong(1, articleId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(articleFromRow(rs));
            }
            else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Article> all() {
        var articles = new ArrayList<Article>();
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT %s
                     FROM article
                     """.formatted(SELECT_FIELDS))) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                articles.add(articleFromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Collections.unmodifiableList(articles);
    }

    public FavoritedInfo favoritedInfoForId(long userId, long articleId) {

        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT article_favorite.user_id
                     FROM article_favorite
                     WHERE article_favorite.article_id = ?
                     """)) {
            stmt.setLong(1, articleId);
            var rs = stmt.executeQuery();

            var userIds = new HashSet<Long>();
            while (rs.next()) {
                userIds.add(rs.getLong(1));
            }

            return new FavoritedInfo(
                    userIds.contains(userId),
                    userIds.size()
            );


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> tags(long articleId) {
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement(
                     """
                     SELECT tag.name
                     FROM article_tag
                     JOIN tag ON tag.tag_id = article_tag.tag_id
                     WHERE article_tag.article_id = ?
                     """)) {
            stmt.setLong(1, articleId);
            var rs = stmt.executeQuery();

            var userIds = new LinkedHashSet<String>();
            while (rs.next()) {
                userIds.add(rs.getString(1));
            }

            return List.copyOf(userIds);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long createArticle(
            long userId,
            String title,
            String description,
            String body,
            List<String> tagList,
            ExternalId externalId
    ) {
        try (var conn = db.getConnection()) {
            conn.setAutoCommit(false);

            var tagIds = new HashSet<Long>();
            try (var stmt = conn.prepareStatement(
                    """
                    SELECT tag.tag_id
                    FROM tag
                    WHERE tag.name IN (%s)
                    """.formatted(
                            tagList
                                    .stream()
                                    .map(__ -> "?")
                                    .collect(Collectors.joining(","))
                    ))) {
                for (int i = 0; i < tagList.size(); i++) {
                    stmt.setString(i + 1, tagList.get(i));
                }
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    tagIds.add(rs.getLong(1));
                }
            }

            long articleId;
            try (var stmt = conn.prepareStatement(
                    """
                    INSERT INTO article(title, description, body, user_id, external_article_id)
                    VALUES (?, ?, ?, ?, ?);
                    RETURNING article_id
                    """,
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setString(3, body);
                stmt.setLong(4, userId);
                stmt.setString(5, externalId.value());

                stmt.execute();
                var rs = stmt.getGeneratedKeys();
                rs.next();
                articleId = rs.getLong(1);
            }

            for (var tagId : tagIds) {
                try (var stmt = conn.prepareStatement(
                        """
                        INSERT INTO article_tag(article_id, tag_id)
                        VALUES (?, ?)
                        """)) {
                    stmt.setLong(1, articleId);
                    stmt.setLong(2, tagId);
                    stmt.execute();
                }
            }

            conn.commit();

            return articleId;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}