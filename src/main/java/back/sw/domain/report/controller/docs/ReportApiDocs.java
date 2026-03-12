package back.sw.domain.report.controller.docs;

import back.sw.domain.report.dto.request.ReportCreateRequest;
import back.sw.domain.report.dto.response.ReportCreateResponse;
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

@Tag(name = "Report", description = "신고 API")
public interface ReportApiDocs {
    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다. 동일 회원의 동일 게시글 중복 신고는 불가합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ReportCreateRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "reason": "SPAM",
                                      "description": "광고성 도배 게시글입니다."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "신고 접수 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 또는 회원 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 신고한 게시글",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    ResponseEntity<RsData<ReportCreateResponse>> createPostReport(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId,
            ReportCreateRequest request
    );

    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다. 동일 회원의 동일 댓글 중복 신고는 불가합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ReportCreateRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "reason": "ABUSE",
                                      "description": "욕설이 포함되어 있습니다."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "신고 접수 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글이 존재하지 않거나 이미 삭제됨, 또는 회원 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 신고한 댓글",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    ResponseEntity<RsData<ReportCreateResponse>> createCommentReport(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "댓글 ID", example = "15") int commentId,
            ReportCreateRequest request
    );
}
