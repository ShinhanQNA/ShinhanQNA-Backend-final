package back.sw.domain.admin.service;

import back.sw.domain.admin.dto.response.AdminCommentReportItemResponse;
import back.sw.domain.admin.dto.response.AdminCommentReportListResponse;
import back.sw.domain.admin.dto.response.AdminPostReportItemResponse;
import back.sw.domain.admin.dto.response.AdminPostReportListResponse;
import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.entity.CommentAnonymousProfile;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.entity.MemberRole;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostReportRepository postReportRepository;

    @Mock
    private CommentReportRepository commentReportRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getPostReportsFailsWhenNotAdmin() {
        Member student = createMember(1, "student@univ.ac.kr", MemberRole.STUDENT);
        when(memberRepository.findById(1)).thenReturn(Optional.of(student));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> adminService.getPostReports(1, 0, 20)
        );

        assertEquals("403-1", exception.getRsData().resultCode());
    }

    @Test
    void getPostReportsReturnsMappedItems() {
        Member admin = createMember(1, "admin@univ.ac.kr", MemberRole.ADMIN);
        Member writer = createMember(2, "writer@univ.ac.kr", MemberRole.STUDENT);
        Member reporter = createMember(3, "reporter@univ.ac.kr", MemberRole.STUDENT);
        Post post = createPost(10, writer);
        PostReport report = PostReport.create(post, reporter, ReportReason.SPAM, "도배");
        ReflectionTestUtils.setField(report, "id", 100);

        when(memberRepository.findById(1)).thenReturn(Optional.of(admin));
        when(postReportRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(report), PageRequest.of(0, 20), 1)
        );

        AdminPostReportListResponse response = adminService.getPostReports(1, 0, 20);
        List<AdminPostReportItemResponse> items = response.items();

        assertEquals(1, items.size());
        assertEquals(100, items.get(0).reportId());
        assertEquals(10, items.get(0).postId());
        assertEquals("SPAM", items.get(0).reason().name());
        assertEquals(1, response.totalElements());
    }

    @Test
    void getCommentReportsReturnsMappedItems() {
        Member admin = createMember(1, "admin2@univ.ac.kr", MemberRole.ADMIN);
        Member writer = createMember(2, "writer2@univ.ac.kr", MemberRole.STUDENT);
        Member commenter = createMember(3, "commenter@univ.ac.kr", MemberRole.STUDENT);
        Member reporter = createMember(4, "reporter2@univ.ac.kr", MemberRole.STUDENT);
        Post post = createPost(11, writer);
        Comment comment = createComment(20, post, commenter);
        CommentReport report = CommentReport.create(comment, reporter, ReportReason.ABUSE, "욕설");
        ReflectionTestUtils.setField(report, "id", 101);

        when(memberRepository.findById(1)).thenReturn(Optional.of(admin));
        when(commentReportRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(report), PageRequest.of(0, 20), 1)
        );

        AdminCommentReportListResponse response = adminService.getCommentReports(1, 0, 20);
        List<AdminCommentReportItemResponse> items = response.items();

        assertEquals(1, items.size());
        assertEquals(101, items.get(0).reportId());
        assertEquals(20, items.get(0).commentId());
        assertEquals(11, items.get(0).postId());
        assertEquals(1, response.totalElements());
    }

    @Test
    void deletePostAsAdminSoftDeletesPost() {
        Member admin = createMember(1, "admin3@univ.ac.kr", MemberRole.ADMIN);
        Member writer = createMember(2, "writer3@univ.ac.kr", MemberRole.STUDENT);
        Post post = createPost(30, writer);

        when(memberRepository.findById(1)).thenReturn(Optional.of(admin));
        when(postRepository.findByIdAndDeletedFalse(30)).thenReturn(Optional.of(post));

        adminService.deletePost(1, 30);

        assertEquals(true, post.isDeleted());
    }

    @Test
    void deleteCommentAsAdminSoftDeletesCommentAndDecreasesCount() {
        Member admin = createMember(1, "admin4@univ.ac.kr", MemberRole.ADMIN);
        Member writer = createMember(2, "writer4@univ.ac.kr", MemberRole.STUDENT);
        Member commenter = createMember(3, "commenter4@univ.ac.kr", MemberRole.STUDENT);
        Post post = createPost(31, writer);
        post.increaseCommentCount();
        Comment comment = createComment(21, post, commenter);

        when(memberRepository.findById(1)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(21)).thenReturn(Optional.of(comment));

        adminService.deleteComment(1, 21);

        assertEquals(true, comment.isDeleted());
        assertEquals(0, post.getCommentCount());
        verify(commentRepository).findById(21);
    }

    private Member createMember(int id, String email, MemberRole role) {
        Member member = new Member(email, "2025" + id, "encoded", "nick" + id, role);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Post createPost(int id, Member writer) {
        Post post = Post.create(writer, BoardType.FREE, "게시글", "내용");
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
