package back.sw.domain.admin.dto.response;

import java.util.List;

public record AdminPostReportListResponse(
        List<AdminPostReportItemResponse> items,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
    public AdminPostReportListResponse {
        items = List.copyOf(items);
    }
}
