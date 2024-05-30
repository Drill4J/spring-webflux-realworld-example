package com.realworld.springmongo.article.dto;

import com.realworld.springmongo.article.Article;
import com.realworld.springmongo.article.Comment;
import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.dto.ProfileView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleView {
    String slug;
    String title;
    String description;
    String body;
    List<String> tagList;
    List<Comment> comments;
    Instant createdAt;
    Instant updatedAt;
    Boolean favorited;
    Integer favoritesCount;
    ProfileView author;

    public static ArticleView toArticleView(Article article, ProfileView author, boolean favorited) {
        return new ArticleView()
                .setSlug(article.getSlug())
                .setTitle(article.getTitle())
                .setDescription(article.getDescription())
                .setBody(article.getBody())
                .setTagList(article.getTags())
                .setCreatedAt(article.getCreatedAt())
                .setUpdatedAt(article.getUpdatedAt())
                .setFavorited(favorited)
                .setFavoritesCount(article.getFavoritesCount())
                .setAuthor(author)
                .setComments(article.getComments());
    }

    public static ArticleView ofOwnArticle(Article article, User articleOwner) {
        return toArticleViewForViewer(article, ProfileView.toOwnProfile(articleOwner), articleOwner);
    }

    public static ArticleView toArticleViewForViewer(Article article, ProfileView author, User user) {
        return toArticleView(article, author, user.isFavoriteArticle(article));
    }

    public static ArticleView toUnfavoredArticleView(Article article, ProfileView author) {
        return toArticleView(article, author, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleView that = (ArticleView) o;
        return Objects.equals(slug, that.slug) && Objects.equals(title,
                that.title) && Objects.equals(description, that.description) && Objects.equals(body,
                that.body) && Objects.equals(tagList, that.tagList) && Objects.equals(comments,
                that.comments) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt,
                that.updatedAt) && Objects.equals(favorited, that.favorited) && Objects.equals(
                favoritesCount, that.favoritesCount) && Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug, title, description, body, tagList, comments, createdAt, updatedAt, favorited,
                favoritesCount, author);
    }
}
