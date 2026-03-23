package back.sw.domain.admin.service;

import back.sw.domain.admin.dto.response.AdminCommentReportItemResponse;
import back.sw.domain.admin.dto.response.AdminCommentReportListResponse;
import back.sw.domain.admin.dto.response.AdminPostReportItemResponse;
import back.sw.domain.admin.dto.response.AdminPostReportListResponse;
import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.entity.MemberRole;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.event.PostDeletedEvent;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.report.repository.CommentReportRepository;
import back.sw.domain.report.repository.PostReportRepository;
import back.sw.global.exception.ServiceException;
import back.sw.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private static final Sort REPORT_SORT = Sort.by(
            Sort.Order.desc("createDate"),
            Sort.Order.desc("id")
    );

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AdminPostReportListResponse getPostReports(int memberId, int page, int size) {
        assertAdmin(memberId);

        Pageable pageable = PageUtils.createPageable(page, size, REPORT_SORT);
        Page<AdminPostReportItemResponse> reportPage = postReportRepository.findAll(pageable)
                .map(report -> new AdminPostReportItemResponse(
                        report.getId(),
                        report.postId(),
                        report.reporterId(),
                        report.getReason(),
                        report.getDescription(),
                        report.isPostDeleted(),
                        report.getCreateDate()
                ));

        return new AdminPostReportListResponse(
                reportPage.getContent(),
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalPages(),
                reportPage.getTotalElements(),
                reportPage.hasNext(),
                reportPage.hasPrevious()
        );
    }

    public AdminCommentReportListResponse getCommentReports(int memberId, int page, int size) {
        assertAdmin(memberId);

        Pageable pageable = PageUtils.createPageable(page, size, REPORT_SORT);
        Page<AdminCommentReportItemResponse> reportPage = commentReportRepository.findAll(pageable)
                .map(report -> new AdminCommentReportItemResponse(
                        report.getId(),
                        report.commentId(),
                        report.postId(),
                        report.reporterId(),
                        report.getReason(),
                        report.getDescription(),
                        report.isCommentDeleted(),
                        report.getCreateDate()
                ));

        return new AdminCommentReportListResponse(
                reportPage.getContent(),
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalPages(),
                reportPage.getTotalElements(),
                reportPage.hasNext(),
                reportPage.hasPrevious()
        );
    }

    @Transactional
    public void deletePost(int memberId, int postId) {
        assertAdmin(memberId);
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        post.softDelete();
        applicationEventPublisher.publishEvent(new PostDeletedEvent(postId));
    }

    @Transactional
    public void deleteComment(int memberId, int commentId) {
        assertAdmin(memberId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404-1", "댓글을 찾을 수 없습니다."));

        if (comment.isDeleted()) {
            throw new ServiceException("400-1", "이미 삭제된 댓글입니다.");
        }

        comment.softDelete();
        comment.decreasePostCommentCount();
    }

    private void assertAdmin(int memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        if (member.getRole() != MemberRole.ADMIN) {
            throw new ServiceException("403-1", "관리자 권한이 필요합니다.");
        }
    }
}
