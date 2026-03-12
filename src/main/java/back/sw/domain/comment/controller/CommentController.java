package back.sw.domain.comment.controller;

import back.sw.domain.comment.controller.docs.CommentApiDocs;
import back.sw.domain.comment.dto.request.CommentCreateRequest;
import back.sw.domain.comment.dto.request.CommentUpdateRequest;
import back.sw.domain.comment.dto.response.CommentCreateResponse;
import back.sw.domain.comment.dto.response.CommentListResponse;
import back.sw.domain.comment.service.CommentService;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class CommentController implements CommentApiDocs {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<RsData<CommentCreateResponse>> create(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        int memberId = authenticatedMember.memberId();
        CommentCreateResponse data = commentService.create(memberId, postId, request);

        return ResponseEntity.status(201)
                .body(new RsData<>("201-1", "댓글이 작성되었습니다.", data));
    }

    @GetMapping
    public RsData<CommentListResponse> getList(@PathVariable int postId) {
        CommentListResponse data = commentService.getList(postId);

        return new RsData<>("200-1", "댓글 목록을 조회했습니다.", data);
    }

    @PatchMapping("/{commentId}")
    public RsData<Void> update(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int postId,
            @PathVariable int commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        int memberId = authenticatedMember.memberId();
        commentService.update(memberId, postId, commentId, request);

        return new RsData<>("200-1", "댓글이 수정되었습니다.", null);
    }

    @DeleteMapping("/{commentId}")
    public RsData<Void> delete(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable int postId,
            @PathVariable int commentId
    ) {
        int memberId = authenticatedMember.memberId();
        commentService.delete(memberId, postId, commentId);

        return new RsData<>("200-1", "댓글이 삭제되었습니다.", null);
    }
}
