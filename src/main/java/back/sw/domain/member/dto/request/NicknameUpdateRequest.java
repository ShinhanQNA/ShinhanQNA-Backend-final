package back.sw.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
