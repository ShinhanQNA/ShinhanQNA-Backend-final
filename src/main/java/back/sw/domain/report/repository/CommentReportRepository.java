package back.sw.domain.report.repository;

import back.sw.domain.report.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {
    boolean existsByCommentIdAndMemberId(int commentId, int memberId);
}
