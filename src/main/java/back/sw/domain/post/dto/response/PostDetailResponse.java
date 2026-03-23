package back.sw.domain.post.dto.response;

import back.sw.domain.post.entity.BoardType;
import back.sw.domain.recruitment.dto.response.RecruitmentDetailResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        int postId,
        BoardType boardType,
        String title,
        String content,
        int likeCount,
        int commentCount,
        List<String> imageUrls,
        RecruitmentDetailResponse recruitment,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PostDetailResponse {
        imageUrls = List.copyOf(imageUrls);
    }
}
