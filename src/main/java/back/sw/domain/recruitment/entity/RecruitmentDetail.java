package back.sw.domain.recruitment.entity;

import back.sw.domain.post.entity.Post;
import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "recruitment_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentDetail extends BaseEntity {
    @Getter(AccessLevel.NONE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "current_count", nullable = false)
    private int currentCount;

    @Column(name = "contact_method", nullable = false, length = 255)
    private String contactMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_status", nullable = false, length = 20)
    private RecruitStatus recruitStatus;

    @Column(nullable = false)
    private LocalDate deadline;

    private RecruitmentDetail(
            Post post,
            int capacity,
            int currentCount,
            String contactMethod,
            LocalDate deadline,
            RecruitStatus recruitStatus
    ) {
        this.post = post;
        this.capacity = capacity;
        this.currentCount = currentCount;
        this.contactMethod = contactMethod;
        this.deadline = deadline;
        this.recruitStatus = recruitStatus;
    }

    public static RecruitmentDetail createOpen(Post post, int capacity, String contactMethod, LocalDate deadline) {
        return new RecruitmentDetail(post, capacity, 0, contactMethod, deadline, RecruitStatus.OPEN);
    }

    public static RecruitmentDetail create(
            Post post,
            int capacity,
            int currentCount,
            String contactMethod,
            LocalDate deadline,
            RecruitStatus recruitStatus
    ) {
        return new RecruitmentDetail(post, capacity, currentCount, contactMethod, deadline, recruitStatus);
    }
}
