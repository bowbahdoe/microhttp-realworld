package dev.mccue.microhttp.realworld.domain;

import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.service.ArticleService;
import dev.mccue.microhttp.realworld.service.UserService;
import org.microhttp.Response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public record ArticleResponse(
        Article article,
        FavoritedInfo favoritedInfo,
        boolean followingAuthor,
        User author,
        List<String> tags
) implements IntoResponse, JsonEncodable {
    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("article", Json.objectBuilder()
                        .put("slug", Json.of(article.slug().toString()))
                        .put("title", Json.of(article.title()))
                        .put("body", Json.of(article.body()))
                        .put("description", Json.of(article.description()))
                        .put("description", Json.of(article.body()))
                        .put("tagList", Json.of(tags.stream().map(Json::of).toList()))
                        .put("createdAt", Json.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(article.createdAt())))
                        .put("updatedAt", Json.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(article.updatedAt())))
                        .put("favorited", Json.of(favoritedInfo.favorited()))
                        .put("favoritesCount", Json.of(favoritedInfo.favoriteCount()))
                        .put("author", Json.objectBuilder()
                                .put("username", Json.of(author.username()))
                                .put("bio", Json.of(author.bio().orElse(null)))
                                .put("image", Json.of(author.image().orElse(null)))
                                .put("following", Json.of(followingAuthor))))
                .build();
    }
    public static List<ArticleResponse> forQuery(ArticleService articleService, UserService userService, ArticleSearchQuery query) {
        return articleService.all()
                .stream()
                .flatMap(article -> forArticleId(articleService, userService, article.userId(), article.articleId()).stream())
                .filter(articleResponse -> query.tag().map(articleResponse.tags::contains).orElse(true))

                .skip(query.offset())
                .limit(query.limit())
                .toList();
    }

    public static Optional<ArticleResponse> forArticleId(
            ArticleService articleService,
            UserService userService,
            long userId,
            long articleId
    ) {
        var article = articleService.forId(articleId).orElse(null);
        if (article == null) {
            return Optional.empty();
        }

        var favoritedInfo = articleService.favoritedInfoForId(userId, article.articleId());
        var author = userService.findById(article.userId()).orElse(null);
        if (author == null) {
            return Optional.empty();
        }

        boolean isFollowing = userService.isFollowing(userId, author.id());
        var tags = articleService.tags(article.articleId());

        return Optional.of(new ArticleResponse(
                article,
                favoritedInfo,
                isFollowing,
                author,
                tags
        ));
    }

    @Override
    public Response intoResponse() {
        return new JsonResponse(this).intoResponse();
    }
}
