package com.realworld.springmongo.user;

import com.realworld.springmongo.security.TokenPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserSessionProvider {

    private final UserRepository userRepository;

    public Mono<User> getCurrentUserOrEmpty() {
        return getCurrentUserSessionOrEmpty().map(UserSession::getUser);
    }

    public Mono<UserSession> getCurrentUserSessionOrEmpty() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    var authentication = context.getAuthentication();
                    if (authentication == null) {
                        return Mono.empty();
                    }
                    var tokenPrincipal = (TokenPrincipal) authentication.getPrincipal();
                    return userRepository
                            .findById(tokenPrincipal.getUserId())
                            .map(user -> new UserSession(user, tokenPrincipal.getToken()));
                });
    }

    public static class UserSession {
        public final User user;
        public final String token;

        public UserSession(User user, String token) {
            this.user = user;
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UserSession that = (UserSession) obj;
            return user.equals(that.user) && token.equals(that.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, token);
        }
    }

}
