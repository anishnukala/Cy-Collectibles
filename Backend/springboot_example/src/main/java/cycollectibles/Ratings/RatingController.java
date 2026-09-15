package cycollectibles.Ratings;

import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import cycollectibles.helper.Helper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class RatingController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RatingRepository ratingRepository;

    @PostMapping("/rate/{sellerId}")
    ResponseEntity<?> rateSeller(@PathVariable Integer sellerId, @Valid @RequestBody RatingReq ratingReq, BindingResult result){
        if(result.hasErrors()){
            return Helper.validationErrors(result);
        }

        Optional<User> customerWrapper = userRepository.findById(ratingReq.getCustomerId());
        Optional<User> sellerWrapper = userRepository.findById(sellerId);
        if(customerWrapper.isEmpty()||sellerWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Rating rating=new Rating();
        rating.setCustomer(customerWrapper.get());
        rating.setSeller(sellerWrapper.get());
        rating.setRating(ratingReq.getRating());

        ratingRepository.save(rating);
        return ResponseEntity.status(201).build();
    }
}
