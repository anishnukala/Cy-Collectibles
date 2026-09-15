package cycollectibles.Comments;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPosting_PostingId(Integer postingId);
    List<Comment> findByParentComment_CommentId(Integer parentCommentId);
    List<Comment> findByPosting_PostingIdOrderByCreatedAt(Integer postingId);
    @Transactional
    void deleteByPosting_PostingId(Integer postingId);
    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.parentComment = null WHERE c.posting.postingId = :postingId")
    void nullifyParentComments(@Param("postingId") Integer postingId);
}
