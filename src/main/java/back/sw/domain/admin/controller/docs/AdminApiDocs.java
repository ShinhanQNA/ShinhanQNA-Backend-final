package back.sw.domain.admin.controller.docs;

import back.sw.domain.admin.dto.response.AdminCommentReportListResponse;
import back.sw.domain.admin.dto.response.AdminPostReportListResponse;
import back.sw.global.response.RsData;
import back.sw.global.security.AuthenticatedMember;
import back.sw.global.swagger.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin", description = "관리자 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
public interface AdminApiDocs {
    @Operation(summary = "관리자 게시글 신고 목록 조회", description = "관리자 권한으로 게시글 신고 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "페이지 파라미터 오류",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<AdminPostReportListResponse> getPostReports(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기(기본 20, 최대 100)", example = "20") int size
    );

    @Operation(summary = "관리자 댓글 신고 목록 조회", description = "관리자 권한으로 댓글 신고 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "페이지 파라미터 오류",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<AdminCommentReportListResponse> getCommentReports(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기(기본 20, 최대 100)", example = "20") int size
    );

    @Operation(summary = "관리자 게시글 삭제", description = "관리자 권한으로 게시글을 소프트 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 또는 게시글 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<Void> deletePost(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId
    );

    @Operation(summary = "관리자 댓글 삭제", description = "관리자 권한으로 댓글을 소프트 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 삭제된 댓글",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 또는 댓글 없음",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<Void> deleteComment(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "댓글 ID", example = "15") int commentId
    );
}
