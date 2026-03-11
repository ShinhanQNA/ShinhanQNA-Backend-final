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
import back.sw.global.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostImageStorageService postImageStorageService;

    @InjectMocks
    private PostService postService;

    @Test
    void createSuccessWithoutImages() {
        Member member = Member.join("user1@univ.ac.kr", "20250001", "encoded", "nick1");
        ReflectionTestUtils.setField(member, "id", 1);

        PostCreateRequest request = new PostCreateRequest(BoardType.FREE, "제목", "내용");

        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100);
            return saved;
        });

        PostCreateResponse response = postService.create(1, request, List.of());

        assertEquals(100, response.postId());
    }

    @Test
    void createWithImagesSavesPostImageMetadata() {
        Member member = Member.join("user1@univ.ac.kr", "20250001", "encoded", "nick1");
        ReflectionTestUtils.setField(member, "id", 1);

        PostCreateRequest request = new PostCreateRequest(BoardType.FREE, "제목", "내용");
        List<MockMultipartFile> images = List.of(
                createImage("a.png", "image-a"),
                createImage("b.png", "image-b")
        );

        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100);
            return saved;
        });
        when(postImageStorageService.store(images))
                .thenReturn(List.of("/uploads/a.png", "/uploads/b.png"));

        PostCreateResponse response = postService.create(1, request, images);

        assertEquals(100, response.postId());
        ArgumentCaptor<List<PostImage>> captor = ArgumentCaptor.forClass(List.class);
        verify(postImageRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals("/uploads/a.png", captor.getValue().get(0).getImageUrl());
        assertEquals("/uploads/b.png", captor.getValue().get(1).getImageUrl());
    }

    @Test
    void createFailsWhenImageCountExceedsLimit() {
        PostCreateRequest request = new PostCreateRequest(BoardType.FREE, "제목", "내용");
        List<MockMultipartFile> images = List.of(
                createImage("1.png", "1"),
                createImage("2.png", "2"),
                createImage("3.png", "3"),
                createImage("4.png", "4"),
                createImage("5.png", "5"),
                createImage("6.png", "6")
        );

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> postService.create(1, request, images)
        );

        assertEquals("400-1", exception.getRsData().resultCode());
        assertEquals("이미지는 최대 5개까지 업로드할 수 있습니다.", exception.getRsData().msg());
    }

    @Test
    void getListReturnsLatestOrderAndAnonymousName() {
        Member member = Member.join("user2@univ.ac.kr", "20250002", "encoded", "nick2");
        ReflectionTestUtils.setField(member, "id", 2);

        Post latest = Post.create(member, BoardType.FREE, "최신 글", "new");
        Post old = Post.create(member, BoardType.FREE, "오래된 글", "old");
        ReflectionTestUtils.setField(latest, "id", 2);
        ReflectionTestUtils.setField(old, "id", 1);
        ReflectionTestUtils.setField(latest, "createDate", LocalDateTime.now());
        ReflectionTestUtils.setField(old, "createDate", LocalDateTime.now().minusMinutes(1));

        when(postRepository.findByBoardTypeAndDeletedFalse(eq(BoardType.FREE), any()))
                .thenReturn(new PageImpl<>(List.of(latest, old), PageRequest.of(0, 20), 2));

        PostPageResponse response = postService.getList(BoardType.FREE, 0, 20);

        List<PostSummaryResponse> items = response.items();
        assertEquals(2, items.size());
        assertEquals("최신 글", items.get(0).title());
        assertEquals("익명", items.get(0).authorName());
        assertEquals("오래된 글", items.get(1).title());
        assertEquals(2, response.totalElements());
    }

    @Test
    void getDetailReturnsPostData() {
        Member member = Member.join("user3@univ.ac.kr", "20250003", "encoded", "nick3");
        ReflectionTestUtils.setField(member, "id", 3);

        Post post = Post.create(member, BoardType.QNA, "질문", "질문 내용");
        ReflectionTestUtils.setField(post, "id", 10);

        when(postRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(post));
        when(postImageRepository.findByPostIdOrderBySortOrderAsc(10))
                .thenReturn(List.of(
                        PostImage.create(post, "/uploads/1.png", 0),
                        PostImage.create(post, "/uploads/2.png", 1)
                ));

        PostDetailResponse response = postService.getDetail(10);

        assertEquals("질문", response.title());
        assertEquals("익명", response.authorName());
        assertEquals(2, response.imageUrls().size());
        assertEquals("/uploads/1.png", response.imageUrls().get(0));
        assertEquals("/uploads/2.png", response.imageUrls().get(1));
    }

    @Test
    void deleteFailsWhenNotAuthor() {
        Member writer = Member.join("user4@univ.ac.kr", "20250004", "encoded", "nick4");
        ReflectionTestUtils.setField(writer, "id", 4);

        Post post = Post.create(writer, BoardType.FREE, "삭제 테스트", "content");
        ReflectionTestUtils.setField(post, "id", 20);

        when(postRepository.findByIdAndDeletedFalse(20)).thenReturn(Optional.of(post));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> postService.delete(999, 20)
        );

        assertEquals("403-1", exception.getRsData().resultCode());
        assertFalse(post.isDeleted());
    }

    @Test
    void deleteSuccess() {
        Member writer = Member.join("user5@univ.ac.kr", "20250005", "encoded", "nick5");
        ReflectionTestUtils.setField(writer, "id", 5);

        Post post = Post.create(writer, BoardType.FREE, "삭제 성공", "content");
        ReflectionTestUtils.setField(post, "id", 21);

        when(postRepository.findByIdAndDeletedFalse(21)).thenReturn(Optional.of(post));

        postService.delete(5, 21);

        assertTrue(post.isDeleted());
    }

    private MockMultipartFile createImage(String fileName, String content) {
        return new MockMultipartFile(
                "images",
                fileName,
                "image/png",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
