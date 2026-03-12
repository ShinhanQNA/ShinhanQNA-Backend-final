package back.sw.domain.comment.service;

import back.sw.domain.comment.dto.request.CommentCreateRequest;
import back.sw.domain.comment.dto.request.CommentUpdateRequest;
import back.sw.domain.comment.dto.response.CommentCreateResponse;
import back.sw.domain.comment.dto.response.CommentItemResponse;
import back.sw.domain.comment.dto.response.CommentListResponse;
import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.entity.CommentAnonymousProfile;
import back.sw.domain.comment.repository.CommentAnonymousProfileRepository;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private static final int ANON_PROFILE_CREATE_RETRY_COUNT = 3;

    private final CommentRepository commentRepository;
    private final CommentAnonymousProfileRepository commentAnonymousProfileRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentCreateResponse create(int memberId, int postId, CommentCreateRequest request) {
        Post post = getPost(postId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        CommentAnonymousProfile profile = getOrCreateAnonymousProfile(post, member);
        Comment comment = Comment.create(post, member, profile, request.content());
        commentRepository.save(comment);
        post.increaseCommentCount();

        return new CommentCreateResponse(comment.getId());
    }

    public CommentListResponse getList(int postId) {
        getPost(postId);
        List<CommentItemResponse> items = commentRepository.findByPostIdOrderByCreateDateDescIdDesc(postId)
                .stream()
                .map(this::toCommentItemResponse)
                .toList();

        return new CommentListResponse(items);
    }

    @Transactional
    public void update(int memberId, int postId, int commentId, CommentUpdateRequest request) {
        getPost(postId);
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ServiceException("404-1", "댓글을 찾을 수 없습니다."));

        if (!comment.isWrittenBy(memberId)) {
            throw new ServiceException("403-1", "수정 권한이 없습니다.");
        }

        if (comment.isDeleted()) {
            throw new ServiceException("400-1", "이미 삭제된 댓글입니다.");
        }

        comment.update(request.content());
    }

    @Transactional
    public void delete(int memberId, int postId, int commentId) {
        Post post = getPost(postId);
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ServiceException("404-1", "댓글을 찾을 수 없습니다."));

        if (!comment.isWrittenBy(memberId)) {
            throw new ServiceException("403-1", "삭제 권한이 없습니다.");
        }

        if (comment.isDeleted()) {
            throw new ServiceException("400-1", "이미 삭제된 댓글입니다.");
        }

        comment.softDelete();
        post.decreaseCommentCount();
    }

    private Post getPost(int postId) {
        return postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));
    }

    private CommentAnonymousProfile getOrCreateAnonymousProfile(Post post, Member member) {
        Optional<CommentAnonymousProfile> existing = commentAnonymousProfileRepository.findByPostIdAndMemberId(
                post.getId(),
                member.getId()
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        for (int attempt = 0; attempt < ANON_PROFILE_CREATE_RETRY_COUNT; attempt++) {
            int nextAnonymousNo = commentAnonymousProfileRepository.findTopByPostIdOrderByAnonymousNoDesc(post.getId())
                    .map(profile -> profile.getAnonymousNo() + 1)
                    .orElse(1);

            try {
                CommentAnonymousProfile newProfile = CommentAnonymousProfile.create(post, member, nextAnonymousNo);
                return commentAnonymousProfileRepository.save(newProfile);
            } catch (DataIntegrityViolationException ignored) {
                Optional<CommentAnonymousProfile> retryExisting =
                        commentAnonymousProfileRepository.findByPostIdAndMemberId(post.getId(), member.getId());
                if (retryExisting.isPresent()) {
                    return retryExisting.get();
                }
            }
        }

        throw new ServiceException("409-1", "댓글 익명 번호 생성에 실패했습니다. 다시 시도해 주세요.");
    }

    private CommentItemResponse toCommentItemResponse(Comment comment) {
        return new CommentItemResponse(
                comment.getId(),
                comment.displayContent(),
                "익명" + comment.anonymousNo(),
                comment.isPostAuthor(),
                comment.isDeleted(),
                comment.getCreateDate(),
                comment.getModifyDate()
        );
    }
}
