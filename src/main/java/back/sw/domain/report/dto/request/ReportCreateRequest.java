package back.sw.domain.report.dto.request;

import back.sw.domain.report.entity.ReportReason;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,
        String description
) {
}
