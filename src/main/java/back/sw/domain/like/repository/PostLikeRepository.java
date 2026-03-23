package back.sw.domain.like.repository;

import back.sw.domain.like.entity.PostLike;
import back.sw.domain.like.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    Optional<PostLike> findByPostIdAndMemberId(int postId, int memberId);
}
