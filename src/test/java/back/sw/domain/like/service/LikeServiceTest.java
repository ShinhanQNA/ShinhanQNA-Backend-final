package back.sw.domain.like.service;

import back.sw.domain.like.dto.response.LikeToggleResponse;
import back.sw.domain.like.entity.PostLike;
import back.sw.domain.like.repository.PostLikeRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void toggleCreatesLikeWhenNotExists() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20252001", "writer");
        Member liker = createMember(2, "liker@univ.ac.kr", "20252002", "liker");
        Post post = createPost(10, writer, "좋아요", "본문");

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(liker));
        when(postLikeRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.empty());
        when(postLikeRepository.save(any(PostLike.class))).thenReturn(PostLike.create(post, liker));
        when(postRepository.findLikeCountByIdAndDeletedFalse(10)).thenReturn(Optional.of(1));

        LikeToggleResponse response = likeService.toggle(2, 10);

        verify(postRepository).incrementLikeCount(10);
        assertEquals(true, response.liked());
        assertEquals(1, response.likeCount());
    }

    @Test
    void toggleCancelsLikeWhenAlreadyExists() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20252001", "writer");
        Member liker = createMember(2, "liker@univ.ac.kr", "20252002", "liker");
        Post post = createPost(10, writer, "좋아요", "본문");
        PostLike existing = PostLike.create(post, liker);
        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(liker));
        when(postLikeRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.of(existing));
        when(postRepository.findLikeCountByIdAndDeletedFalse(10)).thenReturn(Optional.of(0));

        LikeToggleResponse response = likeService.toggle(2, 10);

        verify(postLikeRepository).delete(existing);
        verify(postRepository).decrementLikeCount(10);
        assertEquals(false, response.liked());
        assertEquals(0, response.likeCount());
    }

    @Test
    void toggleFailsWhenPostNotFound() {
        when(postRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> likeService.toggle(1, 999));

        assertEquals("404-1", exception.getRsData().resultCode());
        assertEquals("게시글을 찾을 수 없습니다.", exception.getRsData().msg());
    }

    @Test
    void toggleFailsWhenMemberNotFound() {
        Member writer = createMember(1, "writer@univ.ac.kr", "20252001", "writer");
        Post post = createPost(10, writer, "좋아요", "본문");

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> likeService.toggle(2, 10));

        assertEquals("404-1", exception.getRsData().resultCode());
        assertEquals("회원을 찾을 수 없습니다.", exception.getRsData().msg());
    }

    private Member createMember(int id, String email, String studentNumber, String nickname) {
        Member member = Member.join(email, studentNumber, "encoded-password", nickname);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Post createPost(int id, Member member, String title, String content) {
        Post post = Post.create(member, BoardType.FREE, title, content);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
