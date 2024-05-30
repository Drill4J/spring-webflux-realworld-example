package com.realworld.springmongo.article;

import com.realworld.springmongo.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

import static java.util.Optional.ofNullable;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Comment {

    @Id
    @EqualsAndHashCode.Include
    @Getter
    private String id;

    @Getter
    @Setter
    private String body;

    @Getter
    @Setter
    private String authorId;

    @Getter
    private Instant createdAt;

    @Getter
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public Comment(String id, String body, String authorId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.body = body;
        this.authorId = authorId;
        this.createdAt = ofNullable(createdAt).orElse(Instant.now());
        this.updatedAt = ofNullable(updatedAt).orElse(this.createdAt);
    }

    public boolean isAuthor(User user) {
        return authorId.equals(user.getId());
    }
}
