package back.sw.domain.comment.controller.docs;

import back.sw.domain.comment.dto.request.CommentCreateRequest;
import back.sw.domain.comment.dto.request.CommentUpdateRequest;
import back.sw.domain.comment.dto.response.CommentCreateResponse;
import back.sw.domain.comment.dto.response.CommentListResponse;
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

@Tag(name = "Comment", description = "댓글 API")
public interface CommentApiDocs {
    @Operation(summary = "댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다. parentId가 있으면 대댓글입니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CommentCreateRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "댓글 작성",
                                    value = """
                                            {
                                              "content": "댓글 내용"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "대댓글 작성",
                                    value = """
                                            {
                                              "content": "대댓글 내용",
                                              "parentId": 11
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글/부모댓글/회원 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "댓글 익명 번호 생성 충돌",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            )
    })
    ResponseEntity<RsData<CommentCreateResponse>> create(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId,
            CommentCreateRequest request
    );

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 또는 삭제됨",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<CommentListResponse> getList(@Parameter(description = "게시글 ID", example = "101") int postId);

    @Operation(summary = "댓글 수정", description = "작성자 본인만 댓글을 수정할 수 있습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CommentUpdateRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "content": "수정된 댓글 내용"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패 또는 이미 삭제된 댓글",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글/댓글 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<Void> update(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId,
            @Parameter(description = "댓글 ID", example = "15") int commentId,
            CommentUpdateRequest request
    );

    @Operation(summary = "댓글 삭제", description = "작성자 본인만 댓글을 소프트 삭제할 수 있습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 삭제된 댓글",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글/댓글 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<Void> delete(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId,
            @Parameter(description = "댓글 ID", example = "15") int commentId
    );
}
