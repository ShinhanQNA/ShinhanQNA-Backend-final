package back.sw.domain.like.service;

import back.sw.domain.like.dto.response.LikeToggleResponse;
import back.sw.domain.like.entity.PostLike;
import back.sw.domain.like.repository.PostLikeRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LikeToggleResponse toggle(int memberId, int postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "회원을 찾을 수 없습니다."));

        Optional<PostLike> existing = postLikeRepository.findByPostIdAndMemberId(postId, memberId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            postRepository.decrementLikeCount(postId);
            return new LikeToggleResponse(false, getCurrentLikeCount(postId));
        }

        try {
            postLikeRepository.save(PostLike.create(post, member));
            postRepository.incrementLikeCount(postId);
            return new LikeToggleResponse(true, getCurrentLikeCount(postId));
        } catch (DataIntegrityViolationException ex) {
            throw new ServiceException("409-1", "좋아요 처리 중 충돌이 발생했습니다. 다시 시도해 주세요.");
        }
    }

    private int getCurrentLikeCount(int postId) {
        return postRepository.findLikeCountByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다."));
    }
}
