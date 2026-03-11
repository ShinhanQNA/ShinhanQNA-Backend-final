package back.sw.domain.member.service;

import back.sw.domain.member.dto.request.MemberJoinRequest;
import back.sw.domain.member.dto.response.MemberJoinResponse;
import back.sw.domain.member.dto.response.NicknameResponse;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberJoinResponse join(MemberJoinRequest request) {
        validateJoinRequest(request);

        Member member = Member.join(
                request.email(),
                request.studentNumber(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );

        memberRepository.save(member);

        return new MemberJoinResponse(member.getId(), member.getEmail(), member.getNickname());
    }

    public Member getById(int id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));
    }

    @Transactional
    public NicknameResponse changeNickname(int memberId, String nickname) {
        Member member = getById(memberId);

        if (!member.getNickname().equals(nickname) && memberRepository.existsByNickname(nickname)) {
            throw new ServiceException("400-1", "이미 사용 중인 닉네임입니다.");
        }

        member.changeNickname(nickname);
        return new NicknameResponse(member.getNickname());
    }

    private void validateJoinRequest(MemberJoinRequest request) {
        if (request.password() == null || request.password().length() < 8) {
            throw new ServiceException("400-1", "비밀번호는 8자 이상이어야 합니다.");
        }

        if (memberRepository.existsByEmail(request.email())) {
            throw new ServiceException("400-1", "이미 사용 중인 이메일입니다.");
        }

        if (memberRepository.existsByStudentNumber(request.studentNumber())) {
            throw new ServiceException("400-1", "이미 사용 중인 학번입니다.");
        }

        if (memberRepository.existsByNickname(request.nickname())) {
            throw new ServiceException("400-1", "이미 사용 중인 닉네임입니다.");
        }
    }
}
