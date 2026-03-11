package back.sw.domain.like.controller;

import back.sw.domain.auth.service.AuthService;
import back.sw.domain.like.dto.response.LikeToggleResponse;
import back.sw.domain.like.service.LikeService;
import back.sw.global.response.RsData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class LikeController {
    private final LikeService likeService;
    private final AuthService authService;

    @PostMapping
    public RsData<LikeToggleResponse> toggle(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int postId
    ) {
        int memberId = authService.getMemberIdFromAuthorizationHeader(authorization);
        LikeToggleResponse data = likeService.toggle(memberId, postId);

        String message = data.liked() ? "좋아요를 등록했습니다." : "좋아요를 취소했습니다.";
        return new RsData<>("200-1", message, data);
    }
}
