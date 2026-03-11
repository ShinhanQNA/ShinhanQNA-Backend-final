package back.sw.domain.post.service;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.dto.request.PostCreateRequest;
import back.sw.domain.post.dto.response.PostCreateResponse;
import back.sw.domain.post.dto.response.PostDetailResponse;
import back.sw.domain.post.dto.response.PostPageResponse;
import back.sw.domain.post.dto.response.PostSummaryResponse;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.entity.PostViewHistory;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.post.repository.PostViewHistoryRepository;
import back.sw.global.exception.ServiceException;
import back.sw.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Duration VIEW_COUNT_COOL_DOWN = Duration.ofMinutes(30);

    private final PostRepository postRepository;
    private final PostViewHistoryRepository postViewHistoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostCreateResponse create(int memberId, PostCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        Post post = Post.create(member, request.boardType(), request.title(), request.content());
        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    public PostPageResponse getList(BoardType boardType, int page, int size) {
        Pageable pageable = PageUtils.createPageable(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createDate"),
                        Sort.Order.desc("id")
                )
        );

        Page<Post> postPage = postRepository.findByBoardTypeAndDeletedFalse(boardType, pageable);
        List<PostSummaryResponse> items = postPage.stream()
                .map(this::toSummaryResponse)
                .toList();

        return new PostPageResponse(
                items,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.hasNext(),
                postPage.hasPrevious()
        );
    }

    @Transactional
    public PostDetailResponse getDetail(int postId, Integer memberId, String clientIp) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        String viewerKey = createViewerKey(memberId, clientIp);
        if (shouldIncreaseViewCount(post.getId(), viewerKey)) {
            post.increaseViewCount();
        }

        return toDetailResponse(post);
    }

    @Transactional
    public void delete(int memberId, int postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        if (!post.isWrittenBy(memberId)) {
            throw new ServiceException("403-1", "삭제 권한이 없습니다.");
        }

        post.softDelete();
    }

    private PostSummaryResponse toSummaryResponse(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                post.getLikeCount(),
                post.getCommentCount(),
                "익명",
                post.getCreateDate()
        );
    }

    private PostDetailResponse toDetailResponse(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                "익명",
                post.getCreateDate(),
                post.getModifyDate()
        );
    }

    private String createViewerKey(Integer memberId, String clientIp) {
        if (memberId != null) {
            return "MEMBER:" + memberId;
        }

        if (clientIp == null || clientIp.isBlank()) {
            return "IP:UNKNOWN";
        }

        return "IP:" + clientIp.trim();
    }

    private boolean shouldIncreaseViewCount(int postId, String viewerKey) {
        LocalDateTime now = LocalDateTime.now(KST);

        return postViewHistoryRepository.findByPostIdAndViewerKey(postId, viewerKey)
                .map(history -> {
                    if (history.canIncreaseViewCount(now, VIEW_COUNT_COOL_DOWN)) {
                        history.updateLastViewedAt(now);
                        return true;
                    }

                    return false;
                })
                .orElseGet(() -> {
                    postViewHistoryRepository.save(PostViewHistory.firstView(postId, viewerKey, now));
                    return true;
                });
    }
}
