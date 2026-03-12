package back.sw.domain.comment.service;

import back.sw.domain.comment.dto.request.CommentCreateRequest;
import back.sw.domain.comment.dto.request.CommentUpdateRequest;
import back.sw.domain.comment.dto.response.CommentCreateResponse;
import back.sw.domain.comment.dto.response.CommentListResponse;
import back.sw.domain.comment.entity.Comment;
import back.sw.domain.comment.entity.CommentAnonymousProfile;
import back.sw.domain.comment.repository.CommentAnonymousProfileRepository;
import back.sw.domain.comment.repository.CommentRepository;
import back.sw.domain.member.entity.Member;
import back.sw.domain.member.repository.MemberRepository;
import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import back.sw.domain.post.repository.PostRepository;
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentAnonymousProfileRepository commentAnonymousProfileRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createAssignsAnonymousNumberAndIncrementsPostCommentCount() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commenter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(commenter));
        when(commentAnonymousProfileRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.empty());
        when(commentAnonymousProfileRepository.findTopByPostIdOrderByAnonymousNoDesc(10)).thenReturn(Optional.empty());
        when(commentAnonymousProfileRepository.save(any(CommentAnonymousProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100);
            return saved;
        });

        CommentCreateResponse response = commentService.create(2, 10, new CommentCreateRequest("첫 댓글"));

        assertEquals(100, response.commentId());
        assertEquals(1, post.getCommentCount());
    }

    @Test
    void createReusesExistingAnonymousNumberForSameMember() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commenter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");
        CommentAnonymousProfile existingProfile = CommentAnonymousProfile.create(post, commenter, 3);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(commenter));
        when(commentAnonymousProfileRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.of(existingProfile));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.create(2, 10, new CommentCreateRequest("재댓글"));

        verify(commentAnonymousProfileRepository, never()).save(any(CommentAnonymousProfile.class));
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertEquals(3, commentCaptor.getValue().anonymousNo());
    }

    @Test
    void createReplyWithParentCommentSuccess() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member replier = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        CommentAnonymousProfile writerProfile = CommentAnonymousProfile.create(post, postWriter, 1);
        Comment parentComment = Comment.create(post, postWriter, writerProfile, "부모 댓글");
        ReflectionTestUtils.setField(parentComment, "id", 50);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(replier));
        when(commentRepository.findByIdAndPostId(50, 10)).thenReturn(Optional.of(parentComment));
        when(commentAnonymousProfileRepository.findByPostIdAndMemberId(10, 2)).thenReturn(Optional.empty());
        when(commentAnonymousProfileRepository.findTopByPostIdOrderByAnonymousNoDesc(10))
                .thenReturn(Optional.of(writerProfile));
        when(commentAnonymousProfileRepository.save(any(CommentAnonymousProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 101);
            return saved;
        });

        CommentCreateResponse response = commentService.create(2, 10, new CommentCreateRequest("대댓글", 50));

        assertEquals(101, response.commentId());
        assertEquals(1, post.getCommentCount());
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals(50, captor.getValue().parentId());
    }

    @Test
    void createReplyFailsWhenParentNotFound() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member replier = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(memberRepository.findById(2)).thenReturn(Optional.of(replier));
        when(commentRepository.findByIdAndPostId(999, 10)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.create(2, 10, new CommentCreateRequest("대댓글", 999))
        );

        assertEquals("404-1", exception.getRsData().resultCode());
        assertEquals("부모 댓글을 찾을 수 없습니다.", exception.getRsData().msg());
    }

    @Test
    void getListReturnsAnonymousLabelAndPostAuthorBadge() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member otherMember = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");
        CommentAnonymousProfile profile1 = CommentAnonymousProfile.create(post, postWriter, 1);
        CommentAnonymousProfile profile2 = CommentAnonymousProfile.create(post, otherMember, 2);

        Comment deletedComment = Comment.create(post, otherMember, profile2, "삭제 대상");
        deletedComment.softDelete();
        ReflectionTestUtils.setField(deletedComment, "id", 12);
        Comment normalComment = Comment.create(post, postWriter, profile1, "원글 작성자 댓글");
        ReflectionTestUtils.setField(normalComment, "id", 11);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreateDateDescIdDesc(10))
                .thenReturn(List.of(deletedComment, normalComment));

        CommentListResponse response = commentService.getList(10);

        assertEquals(2, response.items().size());
        assertEquals("삭제된 댓글입니다.", response.items().get(0).content());
        assertEquals("익명2", response.items().get(0).anonymousLabel());
        assertFalse(response.items().get(0).isPostAuthor());
        assertEquals("익명1", response.items().get(1).anonymousLabel());
        assertTrue(response.items().get(1).isPostAuthor());
    }

    @Test
    void deleteFailsWhenNotAuthor() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commentWriter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");
        post.increaseCommentCount();

        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, commentWriter, 1);
        Comment comment = Comment.create(post, commentWriter, profile, "댓글");
        ReflectionTestUtils.setField(comment, "id", 20);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostId(20, 10)).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.delete(999, 10, 20)
        );

        assertEquals("403-1", exception.getRsData().resultCode());
        assertFalse(comment.isDeleted());
        assertEquals(1, post.getCommentCount());
    }

    @Test
    void deleteSoftDeletesAndDecrementsPostCommentCount() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commentWriter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");
        post.increaseCommentCount();

        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, commentWriter, 1);
        Comment comment = Comment.create(post, commentWriter, profile, "댓글");
        ReflectionTestUtils.setField(comment, "id", 20);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostId(20, 10)).thenReturn(Optional.of(comment));

        commentService.delete(2, 10, 20);

        assertTrue(comment.isDeleted());
        assertEquals(0, post.getCommentCount());
    }

    @Test
    void updateFailsWhenNotAuthor() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commentWriter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, commentWriter, 1);
        Comment comment = Comment.create(post, commentWriter, profile, "수정 전");
        ReflectionTestUtils.setField(comment, "id", 30);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostId(30, 10)).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.update(999, 10, 30, new CommentUpdateRequest("수정 후"))
        );

        assertEquals("403-1", exception.getRsData().resultCode());
        assertEquals("수정 전", comment.getContent());
    }

    @Test
    void updateFailsWhenCommentAlreadyDeleted() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commentWriter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, commentWriter, 1);
        Comment comment = Comment.create(post, commentWriter, profile, "삭제된 댓글");
        comment.softDelete();
        ReflectionTestUtils.setField(comment, "id", 31);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostId(31, 10)).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.update(2, 10, 31, new CommentUpdateRequest("수정 시도"))
        );

        assertEquals("400-1", exception.getRsData().resultCode());
    }

    @Test
    void updateSuccess() {
        Member postWriter = createMember(1, "writer@univ.ac.kr", "20250001", "writer");
        Member commentWriter = createMember(2, "user@univ.ac.kr", "20250002", "user");
        Post post = createPost(10, postWriter, "제목", "내용");

        CommentAnonymousProfile profile = CommentAnonymousProfile.create(post, commentWriter, 1);
        Comment comment = Comment.create(post, commentWriter, profile, "수정 전");
        ReflectionTestUtils.setField(comment, "id", 32);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(commentRepository.findByIdAndPostId(32, 10)).thenReturn(Optional.of(comment));

        commentService.update(2, 10, 32, new CommentUpdateRequest("수정 후"));

        assertEquals("수정 후", comment.getContent());
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
