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
import back.sw.domain.post.entity.PostImage;
import back.sw.domain.post.repository.PostImageRepository;
import back.sw.domain.post.repository.PostRepository;
import back.sw.domain.recruitment.dto.request.RecruitmentCreateRequest;
import back.sw.domain.recruitment.dto.response.RecruitmentDetailResponse;
import back.sw.domain.recruitment.service.RecruitmentService;
import back.sw.global.exception.ServiceException;
import back.sw.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private static final int MAX_IMAGE_COUNT = 5;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final PostImageStorageService postImageStorageService;
    private final RecruitmentService recruitmentService;

    @Transactional
    public PostCreateResponse create(int memberId, PostCreateRequest request, List<? extends MultipartFile> images) {
        List<? extends MultipartFile> safeImages = normalizeImages(images);
        validateImageCount(safeImages);
        validateRecruitmentPolicy(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        Post post = Post.create(member, request.boardType(), request.title(), request.content());
        postRepository.save(post);
        savePostImages(post, safeImages);
        saveRecruitmentDetail(post, request.recruitment());

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
    public PostDetailResponse getDetail(int postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

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
        List<String> imageUrls = postImageRepository.findByPostIdOrderBySortOrderAsc(post.getId())
                .stream()
                .map(PostImage::getImageUrl)
                .toList();
        RecruitmentDetailResponse recruitment = recruitmentService.getDetailResponseByPostId(post.getId())
                .orElse(null);

        return new PostDetailResponse(
                post.getId(),
                post.getBoardType(),
                post.getTitle(),
                post.getContent(),
                post.getLikeCount(),
                post.getCommentCount(),
                imageUrls,
                recruitment,
                "익명",
                post.getCreateDate(),
                post.getModifyDate()
        );
    }

    private List<? extends MultipartFile> normalizeImages(List<? extends MultipartFile> images) {
        return images == null ? List.of() : images;
    }

    private void validateImageCount(List<? extends MultipartFile> images) {
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new ServiceException("400-1", "이미지는 최대 5개까지 업로드할 수 있습니다.");
        }
    }

    private void savePostImages(Post post, List<? extends MultipartFile> images) {
        if (images.isEmpty()) {
            return;
        }

        List<String> imageUrls = postImageStorageService.store(images);
        List<PostImage> postImages = IntStream.range(0, imageUrls.size())
                .mapToObj(i -> PostImage.create(post, imageUrls.get(i), i))
                .toList();

        postImageRepository.saveAll(postImages);
    }

    private void validateRecruitmentPolicy(PostCreateRequest request) {
        boolean isRecruitBoard = request.boardType().isRecruitBoard();
        boolean hasRecruitment = request.recruitment() != null;

        if (isRecruitBoard && !hasRecruitment) {
            throw new ServiceException("400-1", "모집 게시판 글에는 모집 정보가 필요합니다.");
        }

        if (!isRecruitBoard && hasRecruitment) {
            throw new ServiceException("400-1", "모집 정보는 모집 게시판에서만 입력할 수 있습니다.");
        }
    }

    private void saveRecruitmentDetail(Post post, RecruitmentCreateRequest recruitment) {
        if (recruitment == null) {
            return;
        }

        recruitmentService.createForPost(post, recruitment);
    }
}
