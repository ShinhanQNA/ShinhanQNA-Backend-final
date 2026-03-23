package back.sw.domain.like.controller.docs;

import back.sw.domain.like.dto.response.LikeToggleResponse;
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

@Tag(name = "Like", description = "좋아요 API")
public interface LikeApiDocs {
    @Operation(summary = "좋아요 토글", description = "게시글 좋아요를 등록/취소 토글합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공"),
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
                    description = "동시성 충돌로 인한 좋아요 처리 실패",
                    content = @Content(schema = @Schema(implementation = RsData.class))
            )
    })
    RsData<LikeToggleResponse> toggle(
            @Parameter(hidden = true) AuthenticatedMember authenticatedMember,
            @Parameter(description = "게시글 ID", example = "101") int postId
    );
}
