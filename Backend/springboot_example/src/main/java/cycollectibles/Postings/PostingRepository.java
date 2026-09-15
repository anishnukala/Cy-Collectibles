package cycollectibles.Postings;

import cycollectibles.Postings.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Vivek Bengre
 *
 */

public interface PostingRepository extends JpaRepository<Posting, Integer>, JpaSpecificationExecutor<Posting> {

    @Transactional
    void deleteByPostingId(Integer postingId);
    List<Posting> getPostingsByStatus(Integer status);
    boolean existsByPostingIdAndStatus(Integer postingId,Integer status);
    List<Posting> getPostingsByStatusOrderByDatePosted(Integer status);

    List<Posting> findBySeller_IdAndStatus(Integer id,Integer status);
    List<Posting> findBySeller_Id(Integer sellerId);
    Integer countByStatus(Integer status);
    List<Posting> findBySeller_IdAndStatusNot(Integer sellerId, Integer status);
}

