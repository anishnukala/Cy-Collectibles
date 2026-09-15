package cycollectibles.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {
    void deleteByTransactionId(Integer transactionId);
    boolean existsByTransactionId(Integer transactionId);
    List<Transaction> findByBuyer_Id(Integer buyerId);
    List<Transaction> findByPosting_PostingId(Integer postingId);
    List<Transaction> findByPosting_Seller_Id(Integer sellerId);

}
