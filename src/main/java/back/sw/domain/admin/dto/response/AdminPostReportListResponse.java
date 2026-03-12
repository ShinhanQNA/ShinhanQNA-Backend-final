package back.sw.domain.admin.dto.response;

import java.util.List;

public record AdminPostReportListResponse(
        List<AdminPostReportItemResponse> items
) {
    public AdminPostReportListResponse {
        items = List.copyOf(items);
    }
}
