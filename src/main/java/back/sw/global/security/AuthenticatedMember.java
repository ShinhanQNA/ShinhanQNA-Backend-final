package back.sw.global.security;

import back.sw.domain.member.entity.MemberRole;

public record AuthenticatedMember(int memberId, MemberRole role) {
}
