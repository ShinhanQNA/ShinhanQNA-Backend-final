package back.sw.domain.report.service;

import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.report.dto.request.ReportCreateRequest;
import back.sw.domain.report.dto.response.ReportCreateResponse;
import back.sw.domain.report.entity.CommentReport;
import back.sw.domain.report.entity.PostReport;
import back.sw.domain.report.repository.CommentReportRepository;
import back.sw.domain.report.repository.PostReportRepository;
import back.sw.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReportCreateResponse createPostReport(int memberId, int postId, ReportCreateRequest request) {
        Member member = getMember(memberId);
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        if (postReportRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new ServiceException("409-1", "이미 신고한 게시글입니다.");
        }

        PostReport report = PostReport.create(post, member, request.reason(), request.description());
        postReportRepository.save(report);

        return new ReportCreateResponse(report.getId());
    }

    @Transactional
    public ReportCreateResponse createCommentReport(int memberId, int commentId, ReportCreateRequest request) {
        Member member = getMember(memberId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404-1", "댓글을 찾을 수 없습니다."));

        if (comment.isDeleted()) {
            throw new ServiceException("404-1", "댓글을 찾을 수 없습니다.");
        }

        if (commentReportRepository.existsByCommentIdAndMemberId(commentId, memberId)) {
            throw new ServiceException("409-1", "이미 신고한 댓글입니다.");
        }

        CommentReport report = CommentReport.create(comment, member, request.reason(), request.description());
        commentReportRepository.save(report);

        return new ReportCreateResponse(report.getId());
    }

    private Member getMember(int memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));
    }
}
