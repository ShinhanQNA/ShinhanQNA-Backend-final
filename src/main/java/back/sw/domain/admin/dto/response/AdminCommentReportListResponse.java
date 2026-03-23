package back.sw.domain.admin.dto.response;

import java.util.List;

public record AdminCommentReportListResponse(
        List<AdminCommentReportItemResponse> items,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
    public AdminCommentReportListResponse {
        items = List.copyOf(items);
    }
}
