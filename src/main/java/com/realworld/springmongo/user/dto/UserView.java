package com.realworld.springmongo.user.dto;

import com.realworld.springmongo.user.User;
import com.realworld.springmongo.user.UserSessionProvider.UserSession;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserView {
    String email;

    String token;

    String username;

    String bio;

    String image;

    List<String> followingIds;

    String id;

    public static UserView fromUserAndToken(UserSession userSession) {
        var user = userSession.getUser();
        var token = userSession.getToken();
        return new UserView()
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setBio(user.getBio())
                .setImage(user.getImage())
                .setToken(token)
                .setFollowingIds(user.getFollowingIds())
                .setId(user.getId());
    }

    public static UserView fromUserAndToken(User user, String token) {
        return fromUserAndToken(new UserSession(user, token));
    }
}
