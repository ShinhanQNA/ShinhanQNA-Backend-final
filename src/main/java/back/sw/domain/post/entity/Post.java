package back.sw.domain.post.entity;

import back.sw.domain.member.entity.Member;
import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BoardType boardType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private Post(Member member, BoardType boardType, String title, String content) {
        this.member = member;
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.deleted = false;
    }

    public static Post create(Member member, BoardType boardType, String title, String content) {
        return new Post(member, boardType, title, content);
    }

    public boolean isWrittenBy(int memberId) {
        return member.getId() == memberId;
    }

    public void softDelete() {
        this.deleted = true;
    }

    public void increaseViewCount() {
        this.viewCount += 1;
    }
}
