package cycollectibles.Users;

import java.util.*;

import cycollectibles.Postings.Posting;
import cycollectibles.Postings.PostingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cycollectibles.helper.Helper;


/**
 *
 * @author Vivek Bengre
 *
 */

@RestController
public class UserController {


    @Autowired
    UserRepository userRepository;

    @Autowired
    PostingRepository postingRepository;


    @PostMapping(path = "/users/login")
    ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequest loginRequest, BindingResult result){

        if(result.hasErrors()){
            return Helper.validationErrors(result);
        }

        Optional<User> userWrapper = userRepository.findByUsername(loginRequest.getUsername());
        if(userWrapper.isEmpty() || !userWrapper.get().getPassword().equals(loginRequest.getPassword())){
            return ResponseEntity.status(401).body(Helper.errorMap("message","Incorrect username or password"));
        }
        if(!userWrapper.get().getUserStatus().equals(User.ACTIVE)) return ResponseEntity.status(403).body(Helper.errorMap("message","This user account is no longer active"));

        return ResponseEntity.ok(userWrapper.get());
    }
    @GetMapping(path="/users")
    ResponseEntity<List<User>> getAlUsers(){
        return ResponseEntity.ok(userRepository.findByUserStatus(User.ACTIVE));
    }

    @GetMapping(path="/users/count")
    ResponseEntity<?> getNumActiveUsers(){
        Integer count = userRepository.countByUserStatus(User.ACTIVE);
        return ResponseEntity.ok(Map.of("numUsers", count));
    }

    @GetMapping(path = "/users/{id}")
    ResponseEntity<User> getUserById( @PathVariable int id){
        Optional<User> userWrapper = userRepository.findById(id);
        if(!userWrapper.isPresent()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userWrapper.get());
    }

    @PostMapping(path = "/users")
    ResponseEntity<?> signUpUser(@Valid @RequestBody SignUpRequest req, BindingResult result){

        if (result.hasErrors()){
            return Helper.validationErrors(result);
        }
        ResponseEntity<HashMap<String,String>> identifyErrors = identityConflictError(null,req.getUsername(),
                null,req.getEmailId());
        if(identifyErrors!=null){
            return identifyErrors;
        }
        User fullUser = new User();
        fullUser.setUsername(req.getUsername());
        fullUser.setPassword(req.getPassword());
        fullUser.setUserType((req.getUserType()));
        fullUser.setEmailId(req.getEmailId());


        userRepository.save(fullUser);
        return ResponseEntity.status(201).build();
    }

    /* not safe to update */
//    @PutMapping("/Users/{id}")
//    User updateUser(@PathVariable int id, @RequestBody User request){
//        User User = UserRepository.findById(id);
//        if(User == null)
//            return null;
//        UserRepository.save(request);
//        return UserRepository.findById(id);
//    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> patchUser(@PathVariable int id, @Valid @RequestBody UpdateRequestPatch req,
            BindingResult result){

        Optional<User> userWrapper = userRepository.findById(id);

        if(userWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(result.hasErrors()){
            return Helper.validationErrors(result);
        }

        User userdata = userWrapper.get();

        String newUsername = req.getUsername()!=null ? req.getUsername() : userdata.getUsername();
        String newEmail = req.getEmailId()!=null ? req.getEmailId() : userdata.getEmailId();

        ResponseEntity<HashMap<String,String>> identifyError =
                identityConflictError(
                        userdata.getUsername(),
                        newUsername,
                        userdata.getEmailId(),
                        newEmail
                );

        if(identifyError != null){
            return identifyError;
        }


        if(req.getUsername()!=null && !req.getUsername().isBlank()){
            userdata.setUsername(req.getUsername());
        }

        if(req.getEmailId()!=null){
            userdata.setEmailId(req.getEmailId());
        }
        if(req.getOldPassword()!=null && req.getPassword()==null){
            return ResponseEntity.badRequest()
                    .body(Helper.errorMap("password","New password required"));
        }
        if(req.getPassword()!=null){
            if(req.getOldPassword()==null ||
                    !req.getOldPassword().equals(userdata.getPassword())){
                return ResponseEntity.badRequest()
                        .body(Helper.errorMap("password","Old password does not match"));
            }

            userdata.setPassword(req.getPassword());
        }

        userRepository.save(userdata);

        return ResponseEntity.ok(userdata);
    }

    @PutMapping("/users/{id}")
    ResponseEntity<?> updateUser(@PathVariable int id, @Valid @RequestBody UpdateRequest req,
                                                      BindingResult result){
        Optional<User> userWrapper = userRepository.findById(id);


        if(userWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        User userdata=userWrapper.get();

        if (result.hasErrors()){
            return Helper.validationErrors(result);
        }
        if(!req.getOldPassword().equals(userdata.getPassword())){
            return ResponseEntity.badRequest().body(Helper.errorMap("password","Old password doesnt match"));
        }
        ResponseEntity<HashMap<String,String>> identifyError = identityConflictError(userdata.getUsername(),req.getUsername(),
                userdata.getEmailId(),req.getEmailId());
        if(identifyError!=null){
            return identifyError;
        }
        userdata.setUsername(req.getUsername());
        userdata.setPassword(req.getPassword());
        userdata.setEmailId((req.getEmailId()));


        userRepository.save(userdata);
        return ResponseEntity.status(200).body(userdata);
    }

    @DeleteMapping(path = "/users/{id}")
    ResponseEntity<HashMap<String,String>> deleteUser(@PathVariable int id){
        Optional<User> userWrapper = userRepository.findById(id);
        if (userWrapper.isEmpty()) {
            return ResponseEntity.status(404).body(Helper.errorMap("message", "User not found"));
        }
        User user = userWrapper.get();
        if(user.getUserStatus().equals(User.DELETED)){
            return ResponseEntity.status(409).body(Helper.errorMap("message", "User already deleted"));
        }
        List<Posting> activePostings = postingRepository.findBySeller_IdAndStatus(id, Posting.ACTIVE);
        List<Posting> reportedPostings  = postingRepository.findBySeller_IdAndStatus(id, Posting.REPORTED);
        if(!activePostings.isEmpty() || !reportedPostings.isEmpty()){
            return ResponseEntity.status(409).body(Helper.errorMap("message", "Cannot delete account with active or reported listings"));
        }
        user.setUserStatus(User.DELETED);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping(path="/users/stats")
    ResponseEntity<?> getStats(){
        Integer buyers = userRepository.countByUserStatusAndUserType(User.ACTIVE,"buyer");
        Integer sellers = userRepository.countByUserStatusAndUserType(User.ACTIVE,"seller");
        return ResponseEntity.ok(Map.of("buyers",buyers,"sellers",sellers));
    }


    //HELPER METHODS

    ResponseEntity<HashMap<String,String>> identityConflictError(String prevUsername, String newUsername, String prevEmail, String newEmail){
        HashMap<String,String> conflicts = new HashMap<>();

        if((prevUsername==null && userRepository.existsByUsername(newUsername))||
                (prevUsername!=null && !prevUsername.equals(newUsername) &&
                        userRepository.existsByUsername(newUsername))){
            conflicts.put("username","Username is already taken");
        }
        if((prevEmail==null && userRepository.existsByEmailId(newEmail))||
                (prevEmail!=null && !prevEmail.equals(newEmail) &&
                        userRepository.existsByEmailId(newEmail))){
            conflicts.put("emailId","This email is already registered");
        }
        if(conflicts.isEmpty()) return null;
        return ResponseEntity.status(409).body(conflicts);
    }
}

