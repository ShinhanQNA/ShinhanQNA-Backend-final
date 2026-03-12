package back.sw.domain.recruitment.repository;

import back.sw.domain.recruitment.entity.RecruitStatus;
import back.sw.domain.recruitment.entity.RecruitmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface RecruitmentDetailRepository extends JpaRepository<RecruitmentDetail, Integer> {
    Optional<RecruitmentDetail> findByPostId(int postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RecruitmentDetail r
            set r.recruitStatus = :closedStatus
            where r.recruitStatus = :openStatus
              and r.deadline < :today
            """)
    int closeExpiredOpenRecruitments(
            @Param("today") LocalDate today,
            @Param("openStatus") RecruitStatus openStatus,
            @Param("closedStatus") RecruitStatus closedStatus
    );
}
