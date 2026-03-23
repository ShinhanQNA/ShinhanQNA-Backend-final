package back.sw.domain.admin.controller;

import back.sw.domain.admin.controller.docs.AdminApiDocs;
import back.sw.domain.admin.dto.response.AdminCommentReportListResponse;
import back.sw.domain.admin.dto.response.AdminPostReportListResponse;
import back.sw.domain.admin.service.AdminService;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class AdminController implements AdminApiDocs {
    private final AdminService adminService;

    @GetMapping("/reports/posts")
    public RsData<AdminPostReportListResponse> getPostReports(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int memberId = authenticatedMember.memberId();
        AdminPostReportListResponse data = adminService.getPostReports(memberId, page, size);

        return new RsData<>("200-1", "게시글 신고 목록을 조회했습니다.", data);
    }

    @GetMapping("/reports/comments")
    public RsData<AdminCommentReportListResponse> getCommentReports(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int memberId = authenticatedMember.memberId();
        AdminCommentReportListResponse data = adminService.getCommentReports(memberId, page, size);

        return new RsData<>("200-1", "댓글 신고 목록을 조회했습니다.", data);
    }

    @DeleteMapping("/posts/{postId}")
    public RsData<Void> deletePost(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int postId
    ) {
        int memberId = authenticatedMember.memberId();
        adminService.deletePost(memberId, postId);

        return new RsData<>("200-1", "게시글을 삭제했습니다.", null);
    }

    @DeleteMapping("/comments/{commentId}")
    public RsData<Void> deleteComment(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int commentId
    ) {
        int memberId = authenticatedMember.memberId();
        adminService.deleteComment(memberId, commentId);

        return new RsData<>("200-1", "댓글을 삭제했습니다.", null);
    }
}
