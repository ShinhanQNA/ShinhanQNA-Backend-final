package back.sw.domain.admin.dto.response;

import back.sw.domain.report.entity.ReportReason;

import java.time.LocalDateTime;

public record AdminCommentReportItemResponse(
        int reportId,
        int commentId,
        int postId,
        int reporterId,
        ReportReason reason,
        String description,
        boolean commentDeleted,
        LocalDateTime reportedAt
) {
}
