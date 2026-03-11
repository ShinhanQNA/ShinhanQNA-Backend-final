package back.sw.global.scheduler;

import back.sw.domain.recruitment.service.RecruitmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecruitmentAutoCloseSchedulerTest {
    @Mock
    private RecruitmentService recruitmentService;

    @Test
    void closeExpiredRecruitmentsUsesAsiaSeoulDate() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-11T15:30:00Z"), ZoneOffset.UTC);
        RecruitmentAutoCloseScheduler scheduler = new RecruitmentAutoCloseScheduler(recruitmentService, fixedClock);

        scheduler.closeExpiredRecruitments();

        verify(recruitmentService).closeExpiredOpenRecruitments(LocalDate.of(2026, 3, 12));
    }
}
