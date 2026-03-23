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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {
    public static final String DELETED_CONTENT = "삭제된 댓글입니다.";

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_profile_id", nullable = false)
    private CommentAnonymousProfile anonymousProfile;

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private Comment(
            Post post,
            Member member,
            CommentAnonymousProfile anonymousProfile,
            Comment parent,
            String content
    ) {
        this.post = post;
        this.member = member;
        this.anonymousProfile = anonymousProfile;
        this.parent = parent;
        this.content = content;
        this.deleted = false;
    }

    public static Comment create(Post post, Member member, CommentAnonymousProfile anonymousProfile, String content) {
        return new Comment(post, member, anonymousProfile, null, content);
    }

    public static Comment createReply(
            Post post,
            Member member,
            CommentAnonymousProfile anonymousProfile,
            Comment parent,
            String content
    ) {
        return new Comment(post, member, anonymousProfile, parent, content);
    }

    public void softDelete() {
        this.deleted = true;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isWrittenBy(int memberId) {
        return member.getId() == memberId;
    }

    public boolean isPostAuthor() {
        return post.isWrittenBy(member.getId());
    }

    public int anonymousNo() {
        return anonymousProfile.getAnonymousNo();
    }

    public String displayContent() {
        return deleted ? DELETED_CONTENT : content;
    }

    public int postId() {
        return post.getId();
    }

    public Integer parentId() {
        return parent == null ? null : parent.getId();
    }

    public void decreasePostCommentCount() {
        post.decreaseCommentCount();
    }
}
