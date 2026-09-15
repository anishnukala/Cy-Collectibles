package cycollectibles.Admin;

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

import java.util.*;

@RestController
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private BannedPostingsRepository bannedPostingsRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @PatchMapping("/ban/user/{adminId}/{userId}")
    ResponseEntity<?> banUser(@PathVariable Integer adminId, @PathVariable Integer userId) {
        Optional<User> adminWrapper = userRepository.findById(adminId);
        Optional<User> userWrapper = userRepository.findById(userId);

        if (adminWrapper.isEmpty()) return ResponseEntity.notFound().build();
        if (userWrapper.isEmpty()) return ResponseEntity.notFound().build();

        User admin = adminWrapper.get();
        User bannedUser = userWrapper.get();

        ResponseEntity<?> adminErrors = validateUser(admin, true);
        ResponseEntity<?> userErrors = validateUser(bannedUser, false);

        if (adminErrors != null) return adminErrors;
        if (userErrors != null) return userErrors;
        return banUserAndPostings(bannedUser, admin);
    }

    @PatchMapping("/ban/posting/{adminId}/{postingId}")
    ResponseEntity<?> banPosting(@PathVariable Integer adminId, @PathVariable Integer postingId,
                                 @RequestBody(required = false) Map<String, String> body) {
        Optional<User> adminWrapper = userRepository.findById(adminId);
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);

        if (adminWrapper.isEmpty()) return ResponseEntity.notFound().build();
        if (postingWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User admin = adminWrapper.get();
        Posting bannedPosting = postingWrapper.get();

        ResponseEntity<?> adminErrors = validateUser(admin, true);
        ResponseEntity<?> postingErrors = validatePostingBanned((bannedPosting.getPostingId()));
        if (adminErrors != null) return adminErrors;
        if(postingErrors!=null) return postingErrors;
        cartItemRepository.deleteByPosting_PostingId(postingId);
        bannedPosting.setStatus(Posting.BANNED);
        postingRepository.save(bannedPosting);
        bannedPostingsRepository.save(new BannedPostings(admin, bannedPosting, new Date()));
        String reason = (body != null && body.containsKey("message")&& !body.get("message").isBlank()) ? body.get("message") : "No reason provided";
        try {
            NotificationWebSocket.sendNotification(bannedPosting.getSeller().getId(), "ADMIN",
                    "Your posting " + bannedPosting.getTitle() + " has been banned. Reason: " + reason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/flag/user/{adminId}/{userId}")
    ResponseEntity<?> flagUser(@PathVariable Integer adminId,@PathVariable Integer userId,
                               @RequestBody(required = false) Map<String, String> body) {
        Optional<User> adminWrapper = userRepository.findById(adminId);
        Optional<User> userWrapper = userRepository.findById(userId);

        if (adminWrapper.isEmpty()) return ResponseEntity.notFound().build();
        if (userWrapper.isEmpty()) return ResponseEntity.notFound().build();

        User admin = adminWrapper.get();
        User flaggedUser = userWrapper.get();

        ResponseEntity<?> adminErrors = validateUser(admin, true);
        ResponseEntity<?> userErrors = validateUser(flaggedUser, false);

        if (adminErrors != null) return adminErrors;
        if (userErrors != null) return userErrors;

        flaggedUser.setFlagCount(flaggedUser.getFlagCount() + 1);

        if (flaggedUser.getFlagCount() >= 3) {
            return banUserAndPostings(flaggedUser, admin);
        }
        userRepository.save(flaggedUser);
        String reason = (body != null && body.containsKey("message")&& !body.get("message").isBlank()) ? body.get("message") : "No reason provided";
        try {
            NotificationWebSocket.sendNotification(flaggedUser.getId(), "ADMIN", "Your account has been flagged. Reason: "+reason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path="/verify/posting/{adminId}/{postingId}")
    ResponseEntity<?> verifyPosting(@PathVariable Integer adminId,@PathVariable Integer postingId){
        Optional<User> adminWrapper = userRepository.findById(adminId);
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);

        if (adminWrapper.isEmpty()) return ResponseEntity.notFound().build();
        if (postingWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User admin = adminWrapper.get();
        Posting reportedPosting = postingWrapper.get();

        ResponseEntity<?> adminErrors = validateUser(admin, true);
        if (adminErrors != null) return adminErrors;
        if(!reportedPosting.getStatus().equals(Posting.REPORTED)) return ResponseEntity.status(409).build();

        reportedPosting.setStatus(Posting.ACTIVE);
        postingRepository.save(reportedPosting);
        try {
            NotificationWebSocket.sendNotification(reportedPosting.getSeller().getId(), "ADMIN", "Your item " +
                    reportedPosting.getTitle() + " has been verified");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }
    @GetMapping("/admin/postings/reported")
    ResponseEntity<?> getReportedPostings(){
        List<Posting> reportedPostings = postingRepository.getPostingsByStatus(Posting.REPORTED);
        ArrayList<PostingResponse> result = new ArrayList<>();
        for(Posting post : reportedPostings){
            PostingResponse postRes = new PostingResponse(post.getPostingId(),
                    post.getSeller().getId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getPrice(),
                    post.getGenre(),
                    post.getDatePosted(),
                    post.getImageUrl());
            result.add(postRes);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/users/banned")
    ResponseEntity<?> getBannedUsers(){
        List<User> bannedUsers = userRepository.findByUserStatus(User.BANNED);
        return ResponseEntity.ok(bannedUsers);
    }

    ResponseEntity<?> validateUser(User user, Boolean admin) {
        if (!user.getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(409).build();
        if (admin && !user.getUserType().equals("admin")) return ResponseEntity.status(403).build();
        return null;
    }

    ResponseEntity<?> banUserAndPostings(User bannedUser, User admin) {
        List<Posting> bannedPostings = postingRepository.findBySeller_IdAndStatusNot(bannedUser.getId(), Posting.BANNED);
        List<Posting> modifiedPostings = new ArrayList<>();
        List<BannedPostings> bannedPostingInfo = new ArrayList<>();
        for (Posting p : bannedPostings) {
            cartItemRepository.deleteByPosting_PostingId(p.getPostingId());
            p.setStatus(Posting.BANNED);
            modifiedPostings.add(p);
            bannedPostingInfo.add(new BannedPostings(admin, p, new Date()));
        }

        postingRepository.saveAll(modifiedPostings);
        bannedPostingsRepository.saveAll(bannedPostingInfo);

        bannedUser.setUserStatus(User.BANNED);
        userRepository.save(bannedUser);
        bannedUsersRepository.save(new BannedUsers(admin, bannedUser, new Date()));
        try {
            NotificationWebSocket.sendNotification(bannedUser.getId(), "BAN", "Your account has been banned");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    ResponseEntity<?> validatePostingBanned(Integer postingId){
        if(bannedPostingsRepository.existsByPosting_PostingId(postingId)){
            return ResponseEntity.status(409).build();
        }
        return null;
    }
}