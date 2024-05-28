package com.realworld.springmongo.security;

import java.util.Objects;

public class TokenPrincipal {
    public final String userId;
    public final String token;

    public TokenPrincipal(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenPrincipal that = (TokenPrincipal) obj;
        return userId.equals(that.userId) && token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, token);
    }

    @Override
    public String toString() {
        return "TokenPrincipal{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}