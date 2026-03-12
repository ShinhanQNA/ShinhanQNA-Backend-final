package back.sw.domain.post.service;

import back.sw.domain.post.entity.PostImage;
import back.sw.domain.post.event.PostDeletedEvent;
import back.sw.domain.post.repository.PostImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostImageCleanupEventListenerTest {
    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostImageStorageService postImageStorageService;

    @InjectMocks
    private PostImageCleanupEventListener postImageCleanupEventListener;

    @Test
    void handleDeletesStoredImagesWhenPostHasImages() {
        PostImage first = mock(PostImage.class);
        PostImage second = mock(PostImage.class);

        when(first.getImageUrl()).thenReturn("/uploads/a.png");
        when(second.getImageUrl()).thenReturn("/uploads/b.png");
        when(postImageRepository.findByPostIdOrderBySortOrderAsc(10)).thenReturn(List.of(first, second));

        postImageCleanupEventListener.handle(new PostDeletedEvent(10));

        verify(postImageStorageService).deleteAll(List.of("/uploads/a.png", "/uploads/b.png"));
    }

    @Test
    void handleSkipsDeleteWhenPostHasNoImages() {
        when(postImageRepository.findByPostIdOrderBySortOrderAsc(11)).thenReturn(List.of());

        postImageCleanupEventListener.handle(new PostDeletedEvent(11));

        verify(postImageStorageService, never()).deleteAll(anyList());
    }
}
