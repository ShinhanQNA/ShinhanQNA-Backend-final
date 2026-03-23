package back.sw.domain.report.repository;

import back.sw.domain.report.entity.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PostReportRepository extends JpaRepository<PostReport, Integer> {
    boolean existsByPostIdAndMemberId(int postId, int memberId);

    @Override
    @EntityGraph(attributePaths = {"post", "member"})
    Page<PostReport> findAll(Pageable pageable);
}
