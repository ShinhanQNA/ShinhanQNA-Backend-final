package back.sw.domain.recruitment.dto.response;

import back.sw.domain.recruitment.entity.RecruitStatus;

import java.time.LocalDate;

public record RecruitmentDetailResponse(
        int capacity,
        int currentCount,
        String contactMethod,
        RecruitStatus recruitStatus,
        LocalDate deadline
) {
}
