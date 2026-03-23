package back.sw.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,
        Integer parentId
) {
    public CommentCreateRequest(String content) {
        this(content, null);
    }
}
