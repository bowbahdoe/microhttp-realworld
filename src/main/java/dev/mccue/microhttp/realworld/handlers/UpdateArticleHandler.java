package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.handler.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.ArticleSlug;
import dev.mccue.microhttp.realworld.AuthContext;
import dev.mccue.microhttp.realworld.ExternalId;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class UpdateArticleHandler extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public UpdateArticleHandler(SQLiteDataSource db) {
        super("PUT", Pattern.compile("/api/articles/(?<slug>.+)"));
        this.db = db;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        var requestBody = RequestUtils.parseBody(request, RequestBody::fromJson);
        var slug = ArticleSlug.fromString(matcher.group("slug"))
                .orElse(null);
        if (slug == null) {
            return Responses.validationError(List.of("Invalid slug"));
        }

        record Update(String setExpression, Object value) {
        }

        var updates = new ArrayList<Update>();

        requestBody.title.ifPresent(title -> updates.add(
                new Update("title = ?", title)
        ));

        requestBody.description.ifPresent(description -> updates.add(
                new Update("description = ?", description)
        ));

        requestBody.body.ifPresent(body -> updates.add(
                new Update("body = ?", body)
        ));

        try (var conn = db.getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    UPDATE article
                    SET
                        %s
                    WHERE article.external_id = ? AND article.user_id = ?
                    RETURNING id
                    """.formatted(
                    updates.stream()
                            .map(Update::setExpression)
                            .collect(Collectors.joining(",\n    "))))) {
                int i = 1;
                for (var update : updates) {
                    stmt.setObject(i, update.value);
                    i++;
                }
                stmt.setString(i, slug.externalId().value());
                i++;
                stmt.setLong(i, authContext.userId());
                var rs = stmt.executeQuery();
                if (!rs.next()) {
                    return Responses.notTheAuthor();
                }
            }

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
                    WHERE article.external_id = ?
                    GROUP BY article.id;
                    """)) {
                stmt.setObject(1, authContext.userId());
                stmt.setObject(2, authContext.userId());
                stmt.setString(3, slug.externalId().value());

                var rs = stmt.executeQuery();
                while (rs.next()) {
                    articleJson.put(
                            "slug",
                            new ArticleSlug(
                                    new ExternalId(rs.getString("external_id")),
                                    rs.getString("title")
                            )
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
                    articleJson.put("favorited", rs.getBoolean("favorited"));
                    articleJson.put("favoritesCount", rs.getInt("favorites_count"));
                    articleJson.put("author", Json.objectBuilder()
                            .put("username", rs.getString("author_username"))
                            .put("bio", rs.getString("author_bio"))
                            .put("image", rs.getString("author_image"))
                            .put("following", rs.getBoolean("following")));
                }
            }

            return new JsonResponse(Json.objectBuilder()
                    .put("article", articleJson));
        }
    }

    record RequestBody(
            Optional<String> title,
            Optional<String> description,
            Optional<String> body
    ) {
        static RequestBody fromJson(Json json) {
            return JsonDecoder.field(json, "article", article -> new RequestBody(
                    JsonDecoder.optionalField(article, "title", JsonDecoder::string),
                    JsonDecoder.optionalField(article, "description", JsonDecoder::string),
                    JsonDecoder.optionalField(article, "body", JsonDecoder::string)
            ));
        }
    }
}
