package back.sw.domain.like.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostLikeId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    public static PostLikeId of(int postId, int userId) {
        return new PostLikeId(postId, userId);
    }
}
