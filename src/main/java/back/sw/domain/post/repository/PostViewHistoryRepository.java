package back.sw.domain.post.repository;

import back.sw.domain.post.entity.PostViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostViewHistoryRepository extends JpaRepository<PostViewHistory, Integer> {
    Optional<PostViewHistory> findByPostIdAndViewerKey(int postId, String viewerKey);
}
