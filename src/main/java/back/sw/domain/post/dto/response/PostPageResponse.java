package back.sw.domain.post.dto.response;

import java.util.List;

public record PostPageResponse(
        List<PostSummaryResponse> items,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
    public PostPageResponse {
        items = List.copyOf(items);
    }
}
