package back.sw.domain.member.controller.docs;

import back.sw.domain.member.dto.request.MemberJoinRequest;
import back.sw.domain.member.dto.request.NicknameUpdateRequest;
import back.sw.domain.member.dto.response.MemberJoinResponse;
import back.sw.domain.member.dto.response.NicknameResponse;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import back.sw.global.swagger.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Member", description = "회원 API")
public interface MemberApiDocs {
    @Operation(summary = "회원가입", description = "이메일/학번/비밀번호/닉네임으로 회원가입합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MemberJoinRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "email": "user@example.com",
                                      "studentNumber": "20241234",
                                      "password": "password1234",
                                      "nickname": "코넥트유저"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "201-1",
                                              "msg": "회원가입이 완료되었습니다.",
                                              "data": {
                                                "memberId": 1,
                                                "email": "user@example.com",
                                                "nickname": "코넥트유저"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 또는 중복 데이터(이메일/학번/닉네임)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    ResponseEntity<RsData<MemberJoinResponse>> join(MemberJoinRequest request);

    @Operation(summary = "내 닉네임 변경", description = "로그인한 회원의 닉네임을 변경합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NicknameUpdateRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "nickname": "새닉네임"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 변경 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "200-1",
                                              "msg": "닉네임이 수정되었습니다.",
                                              "data": {
                                                "nickname": "새닉네임"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패 또는 중복 닉네임",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(토큰 누락/만료/형식 오류)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<NicknameResponse> changeNickname(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            NicknameUpdateRequest request
    );
}
