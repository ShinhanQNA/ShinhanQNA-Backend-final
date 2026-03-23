package back.sw.domain.post.service;

import back.sw.domain.post.entity.PostImage;
import back.sw.domain.post.event.PostDeletedEvent;
import back.sw.domain.post.repository.PostImageRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring IoC가 관리하는 불변 참조 주입 패턴으로 방어적 복사가 불필요합니다."
)
public class PostImageCleanupEventListener {
    private final PostImageRepository postImageRepository;
    private final PostImageStorageService postImageStorageService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostDeletedEvent event) {
        List<String> imageUrls = postImageRepository.findByPostIdOrderBySortOrderAsc(event.postId())
                .stream()
                .map(PostImage::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            return;
        }

        postImageStorageService.deleteAll(imageUrls);
    }
}
