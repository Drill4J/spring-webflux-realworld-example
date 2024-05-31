package com.realworld.springmongo.api;

import com.realworld.springmongo.api.wrappers.ArticleWrapper.ArticleViewWrapper;
import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.article.dto.CreateCommentRequest;
import com.realworld.springmongo.article.dto.UpdateArticleRequest;
import com.realworld.springmongo.user.dto.UserView;
import helpers.article.ArticleApiSupport;
import helpers.article.ArticleSamples;
import helpers.article.FindArticlesRequest;
import helpers.user.UserApiSupport;
import helpers.user.UserSamples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleApiTest {
    private final static String baseUrl =
            System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
    private final static WebTestClient client = WebTestClient.bindToServer().baseUrl(baseUrl).build();
    private final static UserApiSupport userApi = new UserApiSupport(client);
    private final static UserView user = userApi.signup();
    private final static ArticleApiSupport articleApi = new ArticleApiSupport(client);

    @BeforeAll
    static void setUp() {
        System.out.println(baseUrl);
    }
    @Test
    void shouldCreateArticle() {
        var createArticleRequest = ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-create-article");

        var result = articleApi.createArticle(createArticleRequest, user.getToken());
        assert result != null;
        var author = result.getAuthor();

        //assertThatCreatedArticleIsRight
        assertThat(result.getBody()).isEqualTo(createArticleRequest.getBody());
        assertThat(result.getDescription()).isEqualTo(createArticleRequest.getDescription());
        assertThat(result.getTitle()).isEqualTo(createArticleRequest.getTitle());
        assertThat(result.getTagList()).isEqualTo(createArticleRequest.getTagList());
        //assertThatCreatedArticleHasRightAuthor
        assertThat(author.getUsername()).isEqualTo(user.getUsername());
        assertThat(author.getBio()).isEqualTo(user.getBio());
        assertThat(author.getImage()).isEqualTo(user.getImage());
        assertThat(author.isFollowing()).isFalse();

        var createdArticle = articleApi.getArticle(createArticleRequest.getTitle(), user.getToken());
        Assertions.assertNotNull(createdArticle);
    }

    @Test
    void shouldFindArticles() {
        var expectedTag = "tag";
        var preparation = create2UsersAnd2Articles(expectedTag);

        var findArticlesRequest1 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.getUsers().get(0).getUsername());
        var findArticlesRequest2 = new FindArticlesRequest()
                .setTag(expectedTag)
                .setAuthor(preparation.getUsers().get(1).getUsername());

        var articles1 = articleApi.findArticles(findArticlesRequest1).getResponseBody();
        var articles2 = articleApi.findArticles(findArticlesRequest2).getResponseBody();

        assert articles1 != null;
        assert articles2 != null;
        assertThat(articles1.getArticlesCount()).isEqualTo(1);
        assertThat(articles2.getArticlesCount()).isEqualTo(1);

        var article1 = articles1.getArticles().get(0);
        var article2 = articles2.getArticles().get(0);

        assertThat(article1)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.getArticles().get(0));
        assertThat(article2)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Instant.class)
                .isEqualTo(preparation.getArticles().get(1));
    }

    @Test
    void shouldReturnFeed() {
        var follower = userApi.signup();
        var followingUser = userApi.signup(UserSamples.sampleUserRegistrationRequest()
                .setUsername("following username")
                .setEmail("following@gmail.com"));
        assert followingUser != null;

        userApi.follow(followingUser.getUsername(), follower.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-return-feed-1"), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-return-feed-2"), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-return-feed-3"), followingUser.getToken());
        articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-return-feed-4"), follower.getToken());

        var resultBody = articleApi.feed(follower.getToken(), 1, 2).getResponseBody();

        assert resultBody != null;
        assertThat(resultBody.getArticlesCount()).isEqualTo(2);
        var hasRightAuthor = resultBody.getArticles().stream()
                .map(ArticleView::getAuthor)
                .allMatch(it -> it.getUsername().equals(followingUser.getUsername()));
        assertThat(hasRightAuthor).isTrue();
    }

    @Test
    void shouldReturnArticle() {
        var expected = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("article-title"), user.getToken());
        assert expected != null;

        var actual = articleApi.getArticle("article-title", user.getToken());
        assert actual != null;

        assertThat(actual.getContent().getSlug()).isEqualTo(expected.getSlug());
    }

    @Test
    void shouldUpdateArticle() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-update-article"), user.getToken());
        assert article != null;
        var updateArticleRequest = new UpdateArticleRequest()
                .setBody("new body")
                .setDescription("new description")
                .setTitle("new title");

        var updatedArticle = articleApi.updateArticle(article.getSlug(), updateArticleRequest, user.getToken());
        assert updatedArticle != null;

        assertThat(updatedArticle.getAuthor()).isEqualTo(article.getAuthor());
        assertThat(updatedArticle.getBody()).isEqualTo(updateArticleRequest.getBody());
        assertThat(updatedArticle.getDescription()).isEqualTo(updateArticleRequest.getDescription());
        assertThat(updatedArticle.getTitle()).isEqualTo(updateArticleRequest.getTitle());
    }

    @Test
    void shouldDeleteArticle() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-be-deleted"), user.getToken());
        assert article != null;

        articleApi.deleteArticle(article.getSlug(), user.getToken());

        ArticleViewWrapper restoredArticle = articleApi.getArticle(article.getSlug(), user.getToken());
        assertThat(restoredArticle).isNull();
    }

    @Test
    void shouldAddComment() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-add-comment"), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");

        var commentView = articleApi.addComment(article.getSlug(), request, user.getToken());

        assert commentView != null;
        assertThat(commentView.getBody()).isEqualTo(request.getBody());
        assertThat(commentView.getAuthor().getUsername()).isEqualTo(user.getUsername());
        ArticleView savedArticle = articleApi.getArticle(article.getSlug(), user.getToken()).getContent();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isNotEmpty();
    }

    @Test
    void shouldDeleteComment() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-delete-comment"), user.getToken());
        assert article != null;
        var request = new CreateCommentRequest("test comment");
        var commentView = articleApi.addComment(article.getSlug(), request, user.getToken());
        assert commentView != null;

        articleApi.deleteComment(article.getSlug(), commentView.getId(), user.getToken());

        ArticleView savedArticle = articleApi.getArticle(article.getSlug(), user.getToken()).getContent();
        assert savedArticle != null;
        assertThat(savedArticle.getComments()).isEmpty();
    }

    @Test
    void shouldGetComments() {
        userApi.follow(user.getUsername(), user.getToken());
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-get-comments"), user.getToken());
        var comment1 = articleApi.addComment(article.getSlug(), "comment 1", user.getToken());
        var comment2 = articleApi.addComment(article.getSlug(), "comment 2", user.getToken());
        var expectedComments = Set.of(comment1, comment2);

        var actualComments = articleApi.getComments(article.getSlug(), user.getToken()).getResponseBody();

        assertThat(new HashSet<>(actualComments.getComments())).isEqualTo(expectedComments);
    }

    @Test
    void shouldFavoriteArticle() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-favorite-article"), user.getToken());
        var favoritedArticle = articleApi.favoriteArticle(article.getSlug(), user);
        assertThat(article.getFavorited()).isFalse();
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(favoritedArticle.getFavoritesCount()).isEqualTo(1);
    }

    @Test
    void shouldUnfavoriteArticle() {
        var article = articleApi.createArticle(ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-unfavorite-article"), user.getToken());
        var favoritedArticle = articleApi.favoriteArticle(article.getSlug(), user);
        var unfavoritedArticle = articleApi.unfavoriteArticle(article.getSlug(), user);
        assertThat(favoritedArticle.getFavorited()).isTrue();
        assertThat(unfavoritedArticle.getFavorited()).isFalse();
    }

    @Test
    void shouldGetTags() {
        var request1 = ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-get-tags-1")
                .setTagList(List.of("tag1", "tag2", "tag2"));
        var request2 = ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-get-tags-2")
                .setTagList(List.of("tag3", "tag4", "tag3"));
        articleApi.createArticle(request1, user.getToken());
        articleApi.createArticle(request2, user.getToken());
        var tagListView = articleApi.getTags().getResponseBody();

        assertThat(tagListView.getTags()).contains("tag1", "tag2", "tag3", "tag4");
    }

    private ArticlesAndUsers create2UsersAnd2Articles(String tag) {
        var user1 = userApi.signup();
        var user2 = userApi.signup(UserSamples.sampleUserRegistrationRequest()
                .setUsername("testarticle-user")
                .setEmail("testarticle@gmail.com"));

        var createArticleRequest1 = ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-find-articles-2")
                .setTagList(List.of(tag));
        var createArticleRequest2 = ArticleSamples.sampleCreateArticleRequest()
                .setTitle("should-find-articles-2")
                .setTagList(List.of(tag));

        var article1 = articleApi.createArticle(createArticleRequest1, user1.getToken());
        var article2 = articleApi.createArticle(createArticleRequest2, user2.getToken());
        assert article1 != null;
        assert article2 != null;
        return new ArticlesAndUsers(List.of(article1, article2), List.of(user1, user2));
    }
}
