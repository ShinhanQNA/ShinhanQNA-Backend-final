package back.sw.domain.comment.dto.response;

import java.util.List;

public record CommentListResponse(
        List<CommentItemResponse> items
) {
    public CommentListResponse {
        items = List.copyOf(items);
    }
}
