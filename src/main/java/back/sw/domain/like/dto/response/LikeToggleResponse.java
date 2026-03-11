package back.sw.domain.like.dto.response;

public record LikeToggleResponse(
        boolean liked,
        int likeCount
) {
}
