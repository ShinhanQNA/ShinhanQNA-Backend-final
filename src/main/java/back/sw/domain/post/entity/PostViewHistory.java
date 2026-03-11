package back.sw.domain.post.entity;

import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_view_histories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_post_view_histories_post_viewer", columnNames = {"post_id", "viewer_key"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostViewHistory extends BaseEntity {
    @Column(name = "post_id", nullable = false)
    private int postId;

    @Column(name = "viewer_key", nullable = false, length = 120)
    private String viewerKey;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    private PostViewHistory(int postId, String viewerKey, LocalDateTime lastViewedAt) {
        this.postId = postId;
        this.viewerKey = viewerKey;
        this.lastViewedAt = lastViewedAt;
    }

    public static PostViewHistory firstView(int postId, String viewerKey, LocalDateTime viewedAt) {
        return new PostViewHistory(postId, viewerKey, viewedAt);
    }

    public boolean canIncreaseViewCount(LocalDateTime now, Duration coolDown) {
        return lastViewedAt.plus(coolDown).isBefore(now) || lastViewedAt.plus(coolDown).isEqual(now);
    }

    public void updateLastViewedAt(LocalDateTime viewedAt) {
        this.lastViewedAt = viewedAt;
    }
}
