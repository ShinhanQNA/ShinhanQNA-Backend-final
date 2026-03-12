package back.sw.domain.admin.controller;

import back.sw.domain.admin.dto.response.AdminCommentReportListResponse;
import back.sw.domain.admin.dto.response.AdminPostReportListResponse;
import back.sw.domain.admin.service.AdminService;
import back.sw.domain.auth.service.AuthService;
import back.sw.global.response.RsData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class AdminController {
    private final AdminService adminService;
    private final AuthService authService;

    @GetMapping("/reports/posts")
    public RsData<AdminPostReportListResponse> getPostReports(
            @RequestHeader("Authorization") String authorization
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        AdminPostReportListResponse data = new AdminPostReportListResponse(adminService.getPostReports(memberId));

        return new RsData<>("200-1", "게시글 신고 목록을 조회했습니다.", data);
    }

    @GetMapping("/reports/comments")
    public RsData<AdminCommentReportListResponse> getCommentReports(
            @RequestHeader("Authorization") String authorization
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        AdminCommentReportListResponse data = new AdminCommentReportListResponse(adminService.getCommentReports(memberId));

        return new RsData<>("200-1", "댓글 신고 목록을 조회했습니다.", data);
    }

    @DeleteMapping("/posts/{postId}")
    public RsData<Void> deletePost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int postId
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        adminService.deletePost(memberId, postId);

        return new RsData<>("200-1", "게시글을 삭제했습니다.", null);
    }

    @DeleteMapping("/comments/{commentId}")
    public RsData<Void> deleteComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int commentId
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        adminService.deleteComment(memberId, commentId);

        return new RsData<>("200-1", "댓글을 삭제했습니다.", null);
    }
}
