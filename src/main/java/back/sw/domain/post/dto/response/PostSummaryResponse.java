package back.sw.domain.post.dto.response;

import back.sw.domain.post.entity.BoardType;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        int postId,
        BoardType boardType,
        String title,
        int likeCount,
        int commentCount,
        String authorName,
        LocalDateTime createdAt
) {
}
