package back.sw.domain.admin.dto.response;

import java.util.List;

public record AdminCommentReportListResponse(
        List<AdminCommentReportItemResponse> items
) {
    public AdminCommentReportListResponse {
        items = List.copyOf(items);
    }
}
