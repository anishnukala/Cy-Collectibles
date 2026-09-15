package cycollectibles.Favourites;

import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/favourites")
public class FavouriteController {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}")
    ResponseEntity<?> getUserFavourites(@PathVariable Integer userId){
        if(!userRepository.existsById(userId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(favouriteRepository.findByUser_Id(userId));
    }

    @PostMapping("/{userId}/{category}")
    ResponseEntity<?> addFavourite(@PathVariable Integer userId, @PathVariable String category){
        Optional<User> userWrapper = userRepository.findById(userId);
        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        if(favouriteRepository.existsByUser_IdAndCategory(userId, category)) return ResponseEntity.status(409).build();
        Favourite newFavourite = new Favourite(userWrapper.get(),category);
        favouriteRepository.save(newFavourite);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{category}")
    ResponseEntity<?> removeFavourite(@PathVariable Integer userId, @PathVariable String category){
        if(!userRepository.existsById(userId)) return ResponseEntity.notFound().build();
        if(!favouriteRepository.existsByUser_IdAndCategory(userId, category)) return ResponseEntity.notFound().build();
        favouriteRepository.deleteByUser_IdAndCategory(userId,category);
        return ResponseEntity.ok().build();
    }
}
