package back.sw.domain.post.repository;

import back.sw.domain.post.entity.BoardType;
import back.sw.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Page<Post> findByBoardTypeAndDeletedFalse(BoardType boardType, Pageable pageable);

    Optional<Post> findByIdAndDeletedFalse(int id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :postId and p.deleted = false")
    int incrementLikeCount(@Param("postId") int postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Post p
            set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end
            where p.id = :postId and p.deleted = false
            """)
    int decrementLikeCount(@Param("postId") int postId);

    @Query("select p.likeCount from Post p where p.id = :postId and p.deleted = false")
    Optional<Integer> findLikeCountByIdAndDeletedFalse(@Param("postId") int postId);
}
