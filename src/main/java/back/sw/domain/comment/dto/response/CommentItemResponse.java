package back.sw.domain.comment.dto.response;

import java.time.LocalDateTime;

public record CommentItemResponse(
        int commentId,
        String content,
        String anonymousLabel,
        boolean isPostAuthor,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
