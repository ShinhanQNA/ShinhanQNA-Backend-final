package back.sw.domain.post.dto.request;

import back.sw.domain.post.entity.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotNull(message = "게시판 타입은 필수입니다.")
        BoardType boardType,
        @NotBlank(message = "제목은 필수입니다.")
        String title,
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}
