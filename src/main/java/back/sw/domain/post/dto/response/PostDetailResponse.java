package back.sw.domain.post.dto.response;

import back.sw.domain.post.entity.BoardType;

import java.time.LocalDateTime;

public record PostDetailResponse(
        int postId,
        BoardType boardType,
        String title,
        String content,
        int viewCount,
        int likeCount,
        int commentCount,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
