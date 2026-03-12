package back.sw.domain.recruitment.service;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.recruitment.entity.RecruitStatus;
import back.sw.domain.recruitment.entity.RecruitmentDetail;
import back.sw.domain.recruitment.repository.RecruitmentDetailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecruitmentServiceIntegrationTest {
    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private RecruitmentDetailRepository recruitmentDetailRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void closeExpiredOpenRecruitmentsClosesOnlyExpiredOpenRowsAndIsIdempotent() {
        Member member = memberRepository.save(Member.join("recruit-int@univ.ac.kr", "20250011", "encoded", "intnick"));
        Post expiredPost = postRepository.save(Post.create(member, BoardType.PROJECT_RECRUIT, "expired", "content"));
        Post todayPost = postRepository.save(Post.create(member, BoardType.STUDY_RECRUIT, "today", "content"));
        Post alreadyClosedPost = postRepository.save(Post.create(member, BoardType.PROJECT_RECRUIT, "closed", "content"));

        recruitmentDetailRepository.save(RecruitmentDetail.create(
                expiredPost,
                4,
                0,
                "오픈채팅",
                LocalDate.of(2026, 3, 10),
                RecruitStatus.OPEN
        ));
        recruitmentDetailRepository.save(RecruitmentDetail.create(
                todayPost,
                6,
                0,
                "이메일",
                LocalDate.of(2026, 3, 11),
                RecruitStatus.OPEN
        ));
        recruitmentDetailRepository.save(RecruitmentDetail.create(
                alreadyClosedPost,
                3,
                1,
                "오픈채팅",
                LocalDate.of(2026, 3, 1),
                RecruitStatus.CLOSED
        ));

        int firstClosedCount = recruitmentService.closeExpiredOpenRecruitments(LocalDate.of(2026, 3, 11));
        int secondClosedCount = recruitmentService.closeExpiredOpenRecruitments(LocalDate.of(2026, 3, 11));

        assertEquals(1, firstClosedCount);
        assertEquals(0, secondClosedCount);
        assertEquals(
                RecruitStatus.CLOSED,
                recruitmentDetailRepository.findByPostId(expiredPost.getId()).orElseThrow().getRecruitStatus()
        );
        assertEquals(
                RecruitStatus.OPEN,
                recruitmentDetailRepository.findByPostId(todayPost.getId()).orElseThrow().getRecruitStatus()
        );
        assertEquals(
                RecruitStatus.CLOSED,
                recruitmentDetailRepository.findByPostId(alreadyClosedPost.getId()).orElseThrow().getRecruitStatus()
        );
    }
}
