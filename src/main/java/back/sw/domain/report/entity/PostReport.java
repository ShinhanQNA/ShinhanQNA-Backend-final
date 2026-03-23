package back.sw.domain.report.entity;

import back.sw.domain.member.entity.Member;
import back.sw.domain.post.entity.Post;
import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "post_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_post_reports_post_user", columnNames = {"post_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReport extends BaseEntity {
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    private PostReport(Post post, Member member, ReportReason reason, String description) {
        this.post = post;
        this.member = member;
        this.reason = reason;
        this.description = description;
    }

    public static PostReport create(Post post, Member member, ReportReason reason, String description) {
        return new PostReport(post, member, reason, description);
    }

    public int postId() {
        return post.getId();
    }

    public int reporterId() {
        return member.getId();
    }

    public boolean isPostDeleted() {
        return post.isDeleted();
    }
}
