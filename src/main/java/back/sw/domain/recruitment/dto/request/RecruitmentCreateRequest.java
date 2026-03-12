package back.sw.domain.recruitment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RecruitmentCreateRequest(
        @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다.")
        int capacity,
        @NotBlank(message = "연락 방법은 필수입니다.")
        String contactMethod,
        @NotNull(message = "모집 마감일은 필수입니다.")
        LocalDate deadline
) {
}
