package back.sw.domain.post.service;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.dto.request.PostCreateRequest;
import back.sw.domain.post.dto.response.PostCreateResponse;
import back.sw.domain.post.dto.response.PostDetailResponse;
import back.sw.domain.post.dto.response.PostSummaryResponse;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.global.exception.ServiceException;
import back.sw.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostCreateResponse create(int memberId, PostCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        Post post = Post.create(member, request.boardType(), request.title(), request.content());
        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    public List<PostSummaryResponse> getList(BoardType boardType, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return postRepository.findByBoardTypeAndDeletedFalse(boardType, pageable)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional
    public PostDetailResponse getDetail(int postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        post.increaseViewCount();

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
}
