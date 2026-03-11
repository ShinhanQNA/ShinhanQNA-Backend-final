package back.sw.domain.comment.repository;

import back.sw.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostIdOrderByCreateDateDescIdDesc(int postId);

    Optional<Comment> findByIdAndPostId(int commentId, int postId);
}
