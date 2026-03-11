package back.sw.domain.member.dto.response;

public record MemberJoinResponse(
        int memberId,
        String email,
        String nickname
) {
}
