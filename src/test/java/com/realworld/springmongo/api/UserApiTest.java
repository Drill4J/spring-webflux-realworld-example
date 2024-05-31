package com.realworld.springmongo.api;

import helpers.user.UserApiSupport;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class UserApiTest {
    private final static String baseUrl =
            System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:8080";
    private final WebTestClient client = WebTestClient.bindToServer().baseUrl(baseUrl).build();
    private final UserApiSupport api = new UserApiSupport(client);


    @Test
    void shouldSignupUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest()
                .setUsername("should-singup-user")
                .setEmail("should@signup.user");
        var signedUser = api.signup(userRegistrationRequest);

        assertThat(signedUser.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(signedUser.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(signedUser.getBio()).isNull();
        assertThat(signedUser.getImage()).isNull();
        assertThat(signedUser.getToken()).isNotEmpty();
    }

    @Test
    void shouldLoginRegisteredUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var result = api.login(UserSamples.sampleUserAuthenticationRequest());

        requireNonNull(result);
        assertThat(result.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(result.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(result.getBio()).isNull();
        assertThat(result.getImage()).isNull();
        assertThat(result.getToken()).isNotEmpty();
    }

    @Test
    void shouldGetCurrentUser() {
        var signedUser = api.signup();

        var currentUser = api.currentUser(signedUser.getToken());

        requireNonNull(currentUser);
        assertThat(currentUser.getUsername()).isEqualTo(signedUser.getUsername());
        assertThat(currentUser.getEmail()).isEqualTo(signedUser.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        var updateUserRequest = UserSamples.sampleUpdateUserRequest();
        var signedUser = api.signup();

        var updatedUser = api.updateUser(signedUser.getToken(), updateUserRequest);

        requireNonNull(updatedUser);
        assertThat(updatedUser.getBio()).isEqualTo(updateUserRequest.getBio());
        assertThat(updatedUser.getImage()).isEqualTo(updateUserRequest.getImage());
        assertThat(updatedUser.getUsername()).isEqualTo(updateUserRequest.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(updateUserRequest.getEmail());
    }

    @Test
    void shouldReturnProfileByNameWhenUnauthorizedUser() {
        var sampleUserRequest = UserSamples.sampleUserRegistrationRequest();
        api.signup();

        var result = api.getProfile(sampleUserRequest.getUsername());
        var body = requireNonNull(result);

        assertThat(body.getUsername()).isEqualTo(sampleUserRequest.getUsername());
        assertThat(body.isFollowing()).isFalse();
    }

    @Test
    void shouldFollowAndReturnRightProfile() {
        var followee = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("followeee@mail.com")
                .setUsername("Followee"));
        var follower = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("follower@mail.com")
                .setUsername("Follower"));

        api.follow(followee.getUsername(), follower.getToken());
        var profileDto = api.getProfile(followee.getUsername(), follower.getToken());

        assert profileDto != null;
        assertThat(profileDto.getUsername()).isEqualTo(followee.getUsername());
        assertThat(profileDto.isFollowing()).isTrue();
    }

    @Test
    void shouldFollowUser() {
        var followeeDto = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("followeee2@mail.com")
                .setUsername("Followee2"));
        var followerDto = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("follower2@mail.com")
                .setUsername("Follower2"));

        var profileDto = api.follow(followeeDto.getUsername(), followerDto.getToken());
        requireNonNull(profileDto);

        var follower = api.currentUser(followerDto.getToken());
        var followee = api.currentUser(followeeDto.getToken());
        assert follower != null;
        assert followee != null;
        assertThat(profileDto.getUsername()).isEqualTo(followeeDto.getUsername());
        assertThat(profileDto.isFollowing()).isTrue();
        assertThat(follower.getFollowingIds()).contains(followee.getId());
    }

    @Test
    void shouldUnfollowUser() {
        var followeeDto = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("followeee3@mail.com")
                .setUsername("Followee3"));
        var followerDto = api.signup(UserSamples.sampleUserRegistrationRequest()
                .setEmail("follower3@mail.com")
                .setUsername("Follower3"));
        api.follow(followeeDto.getUsername(), followerDto.getToken());

        var body = api.unfollow(followeeDto.getUsername(), followerDto.getToken());

        assert body != null;
        assertThat(body.getUsername()).isEqualTo(followeeDto.getUsername());
        assertThat(body.isFollowing()).isFalse();
        var follower = api.currentUser(followerDto.getToken());
        var followee = api.currentUser(followeeDto.getToken());
        assert follower != null;
        assert followee != null;
        assertThat(follower.getFollowingIds()).doesNotContain(followee.getId());
    }
}
