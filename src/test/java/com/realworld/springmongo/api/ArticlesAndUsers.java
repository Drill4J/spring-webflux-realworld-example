package com.realworld.springmongo.api;

import com.realworld.springmongo.article.dto.ArticleView;
import com.realworld.springmongo.user.dto.UserView;

import java.util.List;
import java.util.Objects;

public class ArticlesAndUsers {
    private final List<ArticleView> articles;
    private final List<UserView> users;

    public ArticlesAndUsers(List<ArticleView> articles, List<UserView> users) {
        this.articles = articles;
        this.users = users;
    }

    public List<ArticleView> getArticles() {
        return articles;
    }

    public List<UserView> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticlesAndUsers that = (ArticlesAndUsers) o;
        return Objects.equals(articles, that.articles) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articles, users);
    }

    @Override
    public String toString() {
        return "ArticlesAndUsers{" +
                "articles=" + articles +
                ", users=" + users +
                '}';
    }
}

