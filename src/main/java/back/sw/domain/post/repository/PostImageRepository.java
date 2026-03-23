package back.sw.domain.post.repository;

import back.sw.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Integer> {
    List<PostImage> findByPostIdOrderBySortOrderAsc(int postId);
}
