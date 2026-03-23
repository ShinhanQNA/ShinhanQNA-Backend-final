package back.sw.domain.comment.entity;

import back.sw.domain.member.entity.Member;
import back.sw.domain.post.entity.Post;
import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "comment_anonymous_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_comment_anon_profiles_post_user", columnNames = {"post_id", "user_id"}),
                @UniqueConstraint(name = "uq_comment_anon_profiles_post_no", columnNames = {"post_id", "anonymous_no"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAnonymousProfile extends BaseEntity {
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "anonymous_no", nullable = false)
    private int anonymousNo;

    private CommentAnonymousProfile(Post post, Member member, int anonymousNo) {
        this.post = post;
        this.member = member;
        this.anonymousNo = anonymousNo;
    }

    public static CommentAnonymousProfile create(Post post, Member member, int anonymousNo) {
        return new CommentAnonymousProfile(post, member, anonymousNo);
    }
}
