package back.sw.domain.report.repository;

import back.sw.domain.report.entity.CommentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {
    boolean existsByCommentIdAndMemberId(int commentId, int memberId);

    @Override
    @EntityGraph(attributePaths = {"comment", "member"})
    Page<CommentReport> findAll(Pageable pageable);
}
