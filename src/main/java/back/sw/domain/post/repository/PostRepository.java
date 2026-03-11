package back.sw.domain.post.repository;

import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Page<Post> findByBoardTypeAndDeletedFalse(BoardType boardType, Pageable pageable);

    Optional<Post> findByIdAndDeletedFalse(int id);
}
