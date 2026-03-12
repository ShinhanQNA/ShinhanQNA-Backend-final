package back.sw.domain.post.controller.docs;

import back.sw.domain.post.dto.request.PostCreateRequest;
import back.sw.domain.post.dto.request.PostUpdateRequest;
import back.sw.domain.post.dto.response.PostCreateResponse;
import back.sw.domain.post.dto.response.PostDetailResponse;
import back.sw.domain.post.dto.response.PostPageResponse;
import back.sw.domain.post.entity.BoardType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Post", description = "게시글 API")
public interface PostApiDocs {
    @Operation(
            summary = "게시글 작성",
            description = "게시글을 작성합니다. multipart/form-data 요청이며 `post` 파트(JSON)와 `images` 파트(파일 배열, 선택)를 받습니다."
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 작성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "resultCode": "201-1",
                                              "msg": "게시글이 작성되었습니다.",
                                              "data": {
                                                "postId": 101
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패(필수 필드 누락, 이미지 5개 초과, 모집정보 정책 위반)",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "작성자 회원 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    ResponseEntity<RsData<PostCreateResponse>> create(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(
                    description = "게시글 JSON 파트. 모집 게시판(PROJECT_RECRUIT/STUDY_RECRUIT)일 때 recruitment 필수",
                    required = true
            ) PostCreateRequest request,
            @Parameter(
                    description = "이미지 파일 파트(선택, 최대 5개). 동일 key(images)로 여러 파일 전달",
                    required = false
            ) List<MultipartFile> images
    );

    @Operation(summary = "게시글 목록 조회", description = "게시판 타입 기준으로 최신순 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 게시판 타입 또는 페이지 파라미터 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<PostPageResponse> getList(
            @Parameter(
                    description = "게시판 타입",
                    required = true,
                    schema = @Schema(allowableValues = {"FREE", "QNA", "PROJECT_RECRUIT", "STUDY_RECRUIT"})
            ) BoardType boardType,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기(기본 20, 최대 100)", example = "20") int size
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보와 이미지 URL, 모집 정보(해당 시)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 또는 삭제됨",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<PostDetailResponse> getDetail(@Parameter(description = "게시글 ID", example = "101") int postId);

    @Operation(summary = "게시글 수정", description = "작성자 본인만 게시글 제목/내용을 수정할 수 있습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PostUpdateRequest.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "title": "수정된 제목",
                                      "content": "수정된 본문"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 또는 삭제됨",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<Void> update(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId,
            PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제", description = "작성자 본인만 게시글을 소프트 삭제할 수 있습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음 또는 삭제됨",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RsData.class)
                    )
            )
    })
    RsData<Void> delete(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId
    );
}
