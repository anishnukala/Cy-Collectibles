package cycollectibles.Transaction;

import cycollectibles.Cart.CartItemRepository;
import cycollectibles.Notifications.NotificationWebSocket;
import cycollectibles.Postings.Posting;
import cycollectibles.Postings.PostingRepository;
import cycollectibles.Postings.PostingResponse;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @PostMapping("/{postingId}/{buyerId}")
    ResponseEntity<?> purchasePosting(@PathVariable Integer postingId, @PathVariable Integer buyerId){
        Transaction tact = new Transaction();
        Optional<Posting> postingWrapper= postingRepository.findById(postingId);
        Optional<User> buyerWrapper = userRepository.findById(buyerId);
        if(buyerWrapper.isEmpty()||postingWrapper.isEmpty()) return ResponseEntity.notFound().build();

        Posting posting = postingWrapper.get();
        User buyer = buyerWrapper.get();

        if(!posting.getStatus().equals(Posting.ACTIVE)) return ResponseEntity.status(409).build();

        if(!buyer.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();
        cartItemRepository.deleteByPosting_PostingId(postingId);

        posting.setStatus(Posting.COMPLETED);
        postingRepository.save(posting);

        tact = new Transaction(buyer,posting,new Date());
        transactionRepository.save(tact);
        // notify seller
        try {
            NotificationWebSocket.sendNotification(posting.getSeller().getId(), "PURCHASE", "Your item " +
                    posting.getTitle() + " has been sold to " + buyer.getUsername());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();

    }

    @GetMapping("/buyer/{buyerId}")
    ResponseEntity<?> getBuyerTransactions(@PathVariable Integer buyerId){
        Optional<User> buyerWrapper = userRepository.findById(buyerId);
        if(buyerWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User buyer = buyerWrapper.get();
        if(!buyer.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();

        List<Transaction> transactions = transactionRepository.findByBuyer_Id(buyerId);
        List<TransactionResponse> response = new ArrayList<>();
        for(Transaction t : transactions){
            Posting post = t.getPosting();
            PostingResponse postRes = new PostingResponse(post.getPostingId(),
                    post.getSeller().getId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getPrice(),
                    post.getGenre(),
                    post.getDatePosted(),
                    post.getImageUrl());
            response.add(new TransactionResponse(t.getTransactionId(),t.getBuyer().getId(), t.getDateSold(), postRes));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/{sellerId}")
    ResponseEntity<?> getSellerTransactions(@PathVariable Integer sellerId){
        Optional<User> sellerWrapper = userRepository.findById(sellerId);
        if(sellerWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User seller = sellerWrapper.get();
        if(!seller.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();

        List<Transaction> transactions = transactionRepository.findByPosting_Seller_Id(sellerId);
        List<TransactionResponse> response = new ArrayList<>();
        for(Transaction t : transactions){
            Posting post = t.getPosting();
            PostingResponse postRes = new PostingResponse(post.getPostingId(),
                    post.getSeller().getId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getPrice(),
                    post.getGenre(),
                    post.getDatePosted(),
                    post.getImageUrl());
            response.add(new TransactionResponse(t.getTransactionId(), t.getBuyer().getId(),t.getDateSold(), postRes));
        }
        return ResponseEntity.ok(response);
    }
}
