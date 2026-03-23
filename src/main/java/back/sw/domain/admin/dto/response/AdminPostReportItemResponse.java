package back.sw.domain.admin.dto.response;

import back.sw.domain.report.entity.ReportReason;

import java.time.LocalDateTime;

public record AdminPostReportItemResponse(
        int reportId,
        int postId,
        int reporterId,
        ReportReason reason,
        String description,
        boolean postDeleted,
        LocalDateTime reportedAt
) {
}
