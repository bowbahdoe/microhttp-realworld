package dev.mccue.microhttp.realworld.handlers;

import com.github.slugify.Slugify;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonWriteOptions;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CreateArticleHandler
    extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public CreateArticleHandler(SQLiteDataSource db) {
        super("POST", Pattern.compile("/api/articles"));
        this.db = db;
    }

    record CreateArticleRequest(
            String title,
            String description,
            String body,
            @Nullable List<String> tagList
    ) {
        static CreateArticleRequest fromJson(Json json) {
            return JsonDecoder.field(json, "article", article -> new CreateArticleRequest(
                    JsonDecoder.field(article, "title", JsonDecoder::string),
                    JsonDecoder.field(article, "description", JsonDecoder::string),
                    JsonDecoder.field(article, "body", JsonDecoder::string),
                    JsonDecoder.optionalNullableField(
                            article, "tagList", JsonDecoder.array(JsonDecoder::string),
                            null
                    )
            ));
        }
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        var createArticleRequest = CreateArticleRequest.fromJson(
                Json.readString(new String(request.body(), StandardCharsets.UTF_8))
        );

        try (var conn = db.getConnection()) {
            conn.setAutoCommit(false);

            long articleId;
            try (var stmt = conn.prepareStatement("""
                INSERT INTO article(user_id, title, description, body)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setObject(1, authContext.userId());
                stmt.setObject(2, createArticleRequest.title);
                stmt.setObject(3, createArticleRequest.description);
                stmt.setObject(4, createArticleRequest.body);

                var rs = stmt.executeQuery();
                articleId = rs.getLong("id");
            }

            if (createArticleRequest.tagList != null) {
                var tagIds = new LinkedHashSet<Long>();
                for (var tag : createArticleRequest.tagList) {
                    try (var stmt = conn.prepareStatement("""
                            INSERT INTO tag(name)
                            VALUES (?)
                            ON CONFLICT (name) DO UPDATE SET name=tag.name
                            RETURNING id
                            """)) {
                        stmt.setObject(1, tag);
                        var rs = stmt.executeQuery();
                        tagIds.add(rs.getLong("id"));
                    }
                }

                for (var tagId : tagIds) {
                    try (var stmt = conn.prepareStatement("""
                            INSERT INTO article_tag(article_id, tag_id)
                            VALUES (?, ?)
                            """)) {
                        stmt.setObject(1, articleId);
                        stmt.setObject(2, tagId);
                        stmt.execute();
                    }
                }
            }

            conn.commit();

            var articleJson = Json.objectBuilder();
            try (var stmt = conn.prepareStatement("""
                    SELECT
                        article.external_id,
                        article.title,
                        article.description,
                        article.body,
                        article.created_at,
                        article.updated_at,
                        json_group_array(tag.name) as tagList,
                        author.username as author_username,
                        author.bio as author_bio,
                        author.image as author_image,
                        follow.id IS NOT NULL as following,
                        user_favorite.user_id IS NOT NULL as favorited,
                        COUNT(article_favorite.user_id) as favorites_count
                    FROM article
                    LEFT JOIN article_tag
                        ON article_tag.article_id = article.id
                    LEFT JOIN tag
                        ON tag.id = article_tag.tag_id
                    LEFT JOIN user author
                        ON article.user_id = author.id
                    LEFT JOIN follow
                        ON follow.following_user_id = author.id
                            AND follow.follower_user_id = ?
                    LEFT JOIN article_favorite
                        ON article_favorite.article_id = article.id
                    LEFT JOIN article_favorite user_favorite
                        ON user_favorite.article_id = article.id
                            AND user_favorite.user_id = ?
                    GROUP BY article.id;
                    """)) {
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    articleJson.put(
                            "slug",
                            Slugify.builder()
                                    .build()
                                    .slugify(rs.getString("title"))
                    );
                    articleJson.put("title", rs.getString("title"));
                    articleJson.put("description", rs.getString("description"));
                    articleJson.put("body", rs.getString("body"));
                    articleJson.put(
                            "tagList",
                            Json.readString(rs.getString("tagList"))
                    );
                    articleJson.put("created_at", DateTimeFormatter.ISO_DATE_TIME.format(
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                    articleJson.put("updated_at", DateTimeFormatter.ISO_DATE_TIME.format(
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    ));
                    articleJson.put("favorited", rs.getInt("favorited") != 0);
                    articleJson.put("favoritesCount", rs.getInt("favorites_count"));
                    articleJson.put("author", Json.objectBuilder()
                            .put("username", rs.getString("author_username"))
                            .put("bio", rs.getString("author_bio"))
                            .put("image", rs.getString("author_image"))
                            .put("following", rs.getInt("following") != 0));
                }
            }

            System.out.println(Json.writeString(
                    articleJson,
                    new JsonWriteOptions()
                            .withIndentation(4)
            ));

            return new JsonResponse(Json.objectBuilder()
                    .put("article", articleJson));
        }



    }
}
