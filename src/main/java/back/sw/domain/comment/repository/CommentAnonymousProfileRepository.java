package back.sw.domain.comment.repository;

import back.sw.domain.comment.entity.CommentAnonymousProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentAnonymousProfileRepository extends JpaRepository<CommentAnonymousProfile, Integer> {
    Optional<CommentAnonymousProfile> findByPostIdAndMemberId(int postId, int memberId);

    Optional<CommentAnonymousProfile> findTopByPostIdOrderByAnonymousNoDesc(int postId);
}
