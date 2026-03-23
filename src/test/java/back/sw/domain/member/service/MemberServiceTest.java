package back.sw.domain.member.service;

import back.sw.domain.member.dto.request.MemberJoinRequest;
import back.sw.domain.member.dto.response.MemberJoinResponse;
import back.sw.domain.member.dto.response.NicknameResponse;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    void joinSuccess() {
        MemberJoinRequest request = new MemberJoinRequest(
                "user1@univ.ac.kr",
                "20250001",
                "password1234",
                "dongbin1"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(memberRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberJoinResponse response = memberService.join(request);

        assertEquals("user1@univ.ac.kr", response.email());
        assertEquals("dongbin1", response.nickname());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertEquals("encoded-password", captor.getValue().getPassword());
    }

    @Test
    void joinFailsWhenNicknameDuplicated() {
        MemberJoinRequest request = new MemberJoinRequest(
                "user2@univ.ac.kr",
                "20250002",
                "password1234",
                "dupNick"
        );

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(memberRepository.existsByStudentNumber(request.studentNumber())).thenReturn(false);
        when(memberRepository.existsByNickname(request.nickname())).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class, () -> memberService.join(request));

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    @Test
    void joinFailsWhenPasswordTooShort() {
        MemberJoinRequest request = new MemberJoinRequest(
                "user3@univ.ac.kr",
                "20250003",
                "short",
                "nick3"
        );

        ServiceException exception = assertThrows(ServiceException.class, () -> memberService.join(request));

        assertEquals("400-1", exception.getRsData().resultCode());
        assertEquals("비밀번호는 8자 이상이어야 합니다.", exception.getRsData().msg());
    }

    @Test
    void changeNicknameSuccess() {
        Member member = Member.join("user4@univ.ac.kr", "20250004", "encoded", "originNick");

        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("changedNick")).thenReturn(false);

        NicknameResponse response = memberService.changeNickname(1, "changedNick");

        assertEquals("changedNick", response.nickname());
    }

    @Test
    void changeNicknameFailsWhenDuplicated() {
        Member member = Member.join("user5@univ.ac.kr", "20250005", "encoded", "originNick");

        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("occupiedNick")).thenReturn(true);

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> memberService.changeNickname(1, "occupiedNick")
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }
}
