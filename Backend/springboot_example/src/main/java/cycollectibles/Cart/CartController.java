package cycollectibles.Cart;

import cycollectibles.Postings.Posting;
import cycollectibles.Postings.PostingRepository;
import cycollectibles.Postings.PostingResponse;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostingRepository postingRepository;

    @GetMapping("/{userId}")
    ResponseEntity<?> getCart(@PathVariable Integer userId){
        Optional<User> userWrapper = userRepository.findById(userId);

        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user = userWrapper.get();
        if(!user.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();
        List<CartItem> cart = cartItemRepository.findByUser_Id(userId);
        List<PostingResponse> cartPostingResponses = new ArrayList<>();
        for(CartItem cartItem: cart){
            Posting post = cartItem.getPosting();
            PostingResponse postRes = new PostingResponse(post.getPostingId(),
                    post.getSeller().getId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getPrice(),
                    post.getGenre(),
                    post.getDatePosted(),
                    post.getImageUrl());
            cartPostingResponses.add(postRes);
        }
        return ResponseEntity.ok(cartPostingResponses);
    }

    @PostMapping("/{userId}/{postingId}")
    ResponseEntity<?> addToCart(@PathVariable Integer userId, @PathVariable Integer postingId){
        Optional<User> userWrapper = userRepository.findById(userId);
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);
        if(userWrapper.isEmpty()||postingWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user = userWrapper.get();
        Posting post = postingWrapper.get();
        if(!user.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();
        if(!post.getStatus().equals(Posting.ACTIVE)) return ResponseEntity.status(409).build();
        if(cartItemRepository.existsByUser_IdAndPosting_PostingId(userId, postingId)) return ResponseEntity.status(409).build();
        CartItem cartItem= new CartItem(user,post);
        cartItemRepository.save(cartItem);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{userId}/{postingId}")
    ResponseEntity<?> removeFromCart(@PathVariable Integer userId, @PathVariable Integer postingId){
        Optional<User> userWrapper = userRepository.findById(userId);

        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user = userWrapper.get();
        if(!user.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();

        cartItemRepository.deleteByUser_IdAndPosting_PostingId(userId,postingId);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping("/{userId}")
    ResponseEntity<?> clearCart(@PathVariable Integer userId){
        Optional<User> userWrapper = userRepository.findById(userId);

        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user = userWrapper.get();
        if(!user.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).build();
        cartItemRepository.deleteByUser_Id(userId);
        return ResponseEntity.ok().build();
    }
}
