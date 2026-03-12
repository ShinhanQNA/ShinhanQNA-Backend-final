package back.sw.domain.recruitment.service;

import back.sw.domain.member.entity.Member;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.recruitment.dto.request.RecruitmentCreateRequest;
import back.sw.domain.recruitment.dto.response.RecruitmentDetailResponse;
import back.sw.domain.recruitment.entity.RecruitStatus;
import back.sw.domain.recruitment.entity.RecruitmentDetail;
import back.sw.domain.recruitment.repository.RecruitmentDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {
    @Mock
    private RecruitmentDetailRepository recruitmentDetailRepository;

    @InjectMocks
    private RecruitmentService recruitmentService;

    @Test
    void createForPostCreatesOpenDetail() {
        Post post = createRecruitPost(10);
        RecruitmentCreateRequest request = new RecruitmentCreateRequest(5, "오픈채팅", LocalDate.of(2026, 3, 20));

        recruitmentService.createForPost(post, request);

        ArgumentCaptor<RecruitmentDetail> captor = ArgumentCaptor.forClass(RecruitmentDetail.class);
        verify(recruitmentDetailRepository).save(captor.capture());
        RecruitmentDetail saved = captor.getValue();

        assertEquals(5, saved.getCapacity());
        assertEquals(0, saved.getCurrentCount());
        assertEquals("오픈채팅", saved.getContactMethod());
        assertEquals(LocalDate.of(2026, 3, 20), saved.getDeadline());
        assertEquals(RecruitStatus.OPEN, saved.getRecruitStatus());
    }

    @Test
    void getDetailResponseByPostIdReturnsMappedResponse() {
        Post post = createRecruitPost(11);
        RecruitmentDetail detail = RecruitmentDetail.create(
                post,
                6,
                2,
                "이메일",
                LocalDate.of(2026, 3, 25),
                RecruitStatus.CLOSED
        );

        when(recruitmentDetailRepository.findByPostId(11)).thenReturn(Optional.of(detail));

        Optional<RecruitmentDetailResponse> result = recruitmentService.getDetailResponseByPostId(11);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().capacity());
        assertEquals(2, result.get().currentCount());
        assertEquals("이메일", result.get().contactMethod());
        assertEquals(RecruitStatus.CLOSED, result.get().recruitStatus());
    }

    @Test
    void closeExpiredOpenRecruitmentsDelegatesToRepository() {
        when(recruitmentDetailRepository.closeExpiredOpenRecruitments(
                LocalDate.of(2026, 3, 12),
                RecruitStatus.OPEN,
                RecruitStatus.CLOSED
        )).thenReturn(3);

        int closedCount = recruitmentService.closeExpiredOpenRecruitments(LocalDate.of(2026, 3, 12));

        assertEquals(3, closedCount);
    }

    private Member createMember(int id) {
        Member member = Member.join("recruiter@univ.ac.kr", "20250010", "encoded", "recruiter");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Post createRecruitPost(int id) {
        Member member = createMember(2);
        Post post = Post.create(member, BoardType.PROJECT_RECRUIT, "모집", "내용");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
