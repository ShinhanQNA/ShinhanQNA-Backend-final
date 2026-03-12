package back.sw.domain.report.repository;

import back.sw.domain.report.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Integer> {
    boolean existsByPostIdAndMemberId(int postId, int memberId);

    List<PostReport> findAllByOrderByCreateDateDescIdDesc();
}
