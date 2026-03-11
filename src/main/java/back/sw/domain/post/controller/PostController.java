package back.sw.domain.post.controller;

import back.sw.domain.auth.service.AuthService;
import back.sw.domain.post.dto.request.PostCreateRequest;
import back.sw.domain.post.dto.response.PostCreateResponse;
import back.sw.domain.post.dto.response.PostDetailResponse;
import back.sw.domain.post.dto.response.PostPageResponse;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.service.PostService;
import back.sw.global.response.RsData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class PostController {
    private final PostService postService;
    private final AuthService authService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RsData<PostCreateResponse>> create(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestPart("post") PostCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        PostCreateResponse data = postService.create(memberId, request, images);

        return ResponseEntity.status(201)
                .body(new RsData<>("201-1", "게시글이 작성되었습니다.", data));
    }

    @GetMapping
    public RsData<PostPageResponse> getList(
            @RequestParam BoardType boardType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PostPageResponse data = postService.getList(boardType, page, size);

        return new RsData<>("200-1", "게시글 목록을 조회했습니다.", data);
    }

    @GetMapping("/{postId}")
    public RsData<PostDetailResponse> getDetail(@PathVariable int postId) {
        PostDetailResponse data = postService.getDetail(postId);

        return new RsData<>("200-1", "게시글을 조회했습니다.", data);
    }

    @DeleteMapping("/{postId}")
    public RsData<Void> delete(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int postId
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        postService.delete(memberId, postId);

        return new RsData<>("200-1", "게시글이 삭제되었습니다.", null);
    }
}
