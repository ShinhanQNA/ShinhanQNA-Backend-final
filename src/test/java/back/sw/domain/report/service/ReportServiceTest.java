package back.sw.domain.report.service;

import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.entity.CommentAnonymousProfile;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.report.dto.request.ReportCreateRequest;
import back.sw.domain.report.dto.response.ReportCreateResponse;
import back.sw.domain.report.entity.CommentReport;
import back.sw.domain.report.entity.PostReport;
import back.sw.domain.report.entity.ReportReason;
import back.sw.domain.report.repository.CommentReportRepository;
import back.sw.domain.report.repository.PostReportRepository;
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock
    private PostReportRepository postReportRepository;

    @Mock
    private CommentReportRepository commentReportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void createPostReportSuccessWithOptionalDescription() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20254001", "writer");
        Member reporter = createMember(2, "reporter@univ.ac.kr", "20254002", "reporter");
        Post post = createPost(10, writer);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.SPAM, null);

        when(memberRepository.findById(2)).thenReturn(Optional.of(reporter));
        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(postReportRepository.existsByPostIdAndMemberId(10, 2)).thenReturn(false);
        when(postReportRepository.save(any(PostReport.class))).thenAnswer(invocation -> {
            PostReport saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100);
            return saved;
        });

        ReportCreateResponse response = reportService.createPostReport(2, 10, request);

        assertEquals(100, response.reportId());
    }

    @Test
    void createPostReportFailsWhenDuplicate() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20254001", "writer");
        Member reporter = createMember(2, "reporter@univ.ac.kr", "20254002", "reporter");
        Post post = createPost(10, writer);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.ABUSE, "중복 신고");

        when(memberRepository.findById(2)).thenReturn(Optional.of(reporter));
        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(postReportRepository.existsByPostIdAndMemberId(10, 2)).thenReturn(true);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reportService.createPostReport(2, 10, request)
        );

        assertEquals("409-1", exception.getRsData().resultCode());
    }

    @Test
    void createCommentReportSuccess() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20254001", "writer");
        Member commenter = createMember(2, "commenter@univ.ac.kr", "20254002", "commenter");
        Member reporter = createMember(3, "reporter@univ.ac.kr", "20254003", "reporter");
        Post post = createPost(10, writer);
        Comment comment = createComment(20, post, commenter);
        ReportCreateRequest request = new ReportCreateRequest(ReportReason.ADVERTISEMENT, "광고 댓글");

        when(memberRepository.findById(3)).thenReturn(Optional.of(reporter));
        when(commentRepository.findById(20)).thenReturn(Optional.of(comment));
        when(commentReportRepository.existsByCommentIdAndMemberId(20, 3)).thenReturn(false);
        when(commentReportRepository.save(any(CommentReport.class))).thenAnswer(invocation -> {
            CommentReport saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 101);
            return saved;
        });

        ReportCreateResponse response = reportService.createCommentReport(3, 20, request);

        assertEquals(101, response.reportId());
    }

    @Test
    void createCommentReportFailsWhenCommentDeleted() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20254001", "writer");
        Member commenter = createMember(2, "commenter@univ.ac.kr", "20254002", "commenter");
        Member reporter = createMember(3, "reporter@univ.ac.kr", "20254003", "reporter");
        Post post = createPost(10, writer);
        Comment comment = createComment(20, post, commenter);
        comment.softDelete();

        when(memberRepository.findById(3)).thenReturn(Optional.of(reporter));
        when(commentRepository.findById(20)).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reportService.createCommentReport(3, 20, new ReportCreateRequest(ReportReason.ETC, null))
        );

        assertEquals("404-1", exception.getRsData().resultCode());
    }

    private Member createMember(int id, String email, String studentNumber, String nickname) {
        Member member = Member.join(email, studentNumber, "encoded-password", nickname);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Post createPost(int id, Member member) {
        Post post = Post.create(member, BoardType.FREE, "게시글", "내용");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment createComment(int id, Post post, Member member) {
        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, member, 1);
        Comment comment = Comment.create(post, member, profile, "댓글");
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
