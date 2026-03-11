package back.sw.domain.member.entity;

import back.sw.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String studentNumber;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(length = 1000)
    private String refreshToken;

    public Member(String email, String studentNumber, String password, String nickname, MemberRole role) {
        this.email = email;
        this.studentNumber = studentNumber;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member join(String email, String studentNumber, String encodedPassword, String nickname) {
        return new Member(email, studentNumber, encodedPassword, nickname, MemberRole.STUDENT);
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }
}
