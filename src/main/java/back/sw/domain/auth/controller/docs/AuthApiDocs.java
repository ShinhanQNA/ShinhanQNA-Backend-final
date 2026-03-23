package back.sw.domain.auth.controller.docs;

import back.sw.domain.auth.dto.request.LoginRequest;
import back.sw.domain.auth.dto.request.LogoutRequest;
import back.sw.domain.auth.dto.request.RefreshTokenRequest;
import back.sw.domain.auth.dto.response.AccessTokenResponse;
import back.sw.domain.auth.dto.response.TokenResponse;
import back.sw.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Auth", description = "인증/토큰 API")
public interface AuthApiDocs {
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 Access/Refresh Token을 발급합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "로그인 요청",
                            value = """
                                    {
                                      "email": "user@example.com",
                                      "password": "password1234"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "200-1",
                                              "msg": "로그인에 성공했습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOi...",
                                                "refreshToken": "eyJhbGciOi..."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 형식/값 검증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "400-1",
                                              "msg": "이메일은 필수입니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "401-1",
                                              "msg": "이메일 또는 비밀번호가 올바르지 않습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    RsData<TokenResponse> login(LoginRequest request);

    @Operation(summary = "Access Token 재발급", description = "유효한 Refresh Token으로 Access Token을 재발급합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(
                            name = "재발급 요청",
                            value = """
                                    {
                                      "refreshToken": "eyJhbGciOi..."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "200-1",
                                              "msg": "Access Token을 재발급했습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOi..."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 형식/값 검증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh Token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "401-1",
                                              "msg": "유효하지 않은 토큰입니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            )
    })
    RsData<AccessTokenResponse> refresh(RefreshTokenRequest request);

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하여 로그아웃 처리합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LogoutRequest.class),
                    examples = @ExampleObject(
                            name = "로그아웃 요청",
                            value = """
                                    {
                                      "refreshToken": "eyJhbGciOi..."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "200-1",
                                              "msg": "로그아웃되었습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 형식/값 검증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh Token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<Void> logout(LogoutRequest request);
}
