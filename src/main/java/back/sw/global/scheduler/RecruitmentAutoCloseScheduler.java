package back.sw.global.scheduler;

import back.sw.domain.recruitment.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RecruitmentAutoCloseScheduler {
    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private final RecruitmentService recruitmentService;
    private final Clock clock;

    @Scheduled(cron = "${custom.recruitment.auto-close-cron:0 0 0 * * *}", zone = "Asia/Seoul")
    public void closeExpiredRecruitments() {
        LocalDate todaySeoul = ZonedDateTime.now(clock)
                .withZoneSameInstant(ASIA_SEOUL)
                .toLocalDate();

        int closedCount = recruitmentService.closeExpiredOpenRecruitments(todaySeoul);
        if (closedCount > 0) {
            log.info("모집 자동 마감 처리 완료 - 처리 건수: {}, 기준일: {}", closedCount, todaySeoul);
        }
    }
}
