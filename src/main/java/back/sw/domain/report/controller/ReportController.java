package back.sw.domain.report.controller;

import back.sw.domain.report.dto.request.ReportCreateRequest;
import back.sw.domain.report.dto.response.ReportCreateResponse;
import back.sw.domain.report.service.ReportService;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<RsData<ReportCreateResponse>> createPostReport(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int postId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        int memberId = authenticatedMember.memberId();
        ReportCreateResponse data = reportService.createPostReport(memberId, postId, request);

        return ResponseEntity.status(201)
                .body(new RsData<>("201-1", "게시글 신고가 접수되었습니다.", data));
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<RsData<ReportCreateResponse>> createCommentReport(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int commentId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        int memberId = authenticatedMember.memberId();
        ReportCreateResponse data = reportService.createCommentReport(memberId, commentId, request);

        return ResponseEntity.status(201)
                .body(new RsData<>("201-1", "댓글 신고가 접수되었습니다.", data));
    }
}
