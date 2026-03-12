package back.sw.domain.member.controller;

import back.sw.domain.member.controller.docs.MemberApiDocs;
import back.sw.domain.member.dto.request.MemberJoinRequest;
import back.sw.domain.member.dto.request.NicknameUpdateRequest;
import back.sw.domain.member.dto.response.MemberJoinResponse;
import back.sw.domain.member.dto.response.NicknameResponse;
import back.sw.domain.member.service.MemberService;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberApiDocs {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<RsData<MemberJoinResponse>> join(@Valid @RequestBody MemberJoinRequest request) {
        MemberJoinResponse data = memberService.join(request);

        return ResponseEntity.status(201)
                .body(new RsData<>("201-1", "회원가입이 완료되었습니다.", data));
    }

    @PatchMapping("/me/nickname")
    public RsData<NicknameResponse> changeNickname(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        int memberId = authenticatedMember.memberId();
        NicknameResponse data = memberService.changeNickname(memberId, request.nickname());

        return new RsData<>("200-1", "닉네임이 수정되었습니다.", data);
    }
}
