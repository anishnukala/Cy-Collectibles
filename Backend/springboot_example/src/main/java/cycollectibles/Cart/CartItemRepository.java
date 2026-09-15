package cycollectibles.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUser_Id(Integer userId);
    boolean existsByUser_IdAndPosting_PostingId(Integer userId, Integer postingId);
    @Transactional
    void deleteByUser_IdAndPosting_PostingId(Integer userId, Integer postingId);
    @Transactional
    void deleteByUser_Id(Integer userId);
    List<CartItem> findByPosting_PostingId(Integer postingId);

    @Transactional
    void deleteByPosting_PostingId(Integer postingId);
}