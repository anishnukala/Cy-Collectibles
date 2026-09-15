package cycollectibles.Comments;

import cycollectibles.Postings.Posting;
import cycollectibles.Postings.PostingRepository;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import cycollectibles.helper.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostingRepository postingRepository;

    @GetMapping("/posting/{postingId}")
    ResponseEntity<?> getPostingComments(@PathVariable Integer postingId){
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);
        if(postingWrapper.isEmpty()) {
            return ResponseEntity.status(404).body(Helper.errorMap("message", "Posting not found"));
        }
        List<Comment> comments =commentRepository.findByPosting_PostingIdOrderByCreatedAt(postingId);
        ArrayList<CommentResponse> commentResponses = new ArrayList<>();
        for(Comment comment:comments){
            commentResponses.add(new CommentResponse(comment.getCommentId(),
                    comment.getSender().getUsername(),
                    comment.getContent(),
                    comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
                    comment.getCreatedAt()
            ));
        }
        return ResponseEntity.ok().body(commentResponses);
    }

    @PostMapping("/posting/{postingId}")
    ResponseEntity<?> addComment(@PathVariable Integer postingId, @RequestBody CommentRequest req){
        Comment comment = new Comment(null,null,null,null);
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);
        if(postingWrapper.isEmpty()){
            return ResponseEntity.status(404).body(Helper.errorMap("message", "Posting not found"));
        }
        Posting posting = postingWrapper.get();
        Optional<User> userWrapper = userRepository.findById(req.getSenderId());
        if(userWrapper.isEmpty()){
            return ResponseEntity.status(404).body(Helper.errorMap("message", "User not found"));
        }
        User sender = userWrapper.get();
        Comment parentComment =null;
        if(req.getParentCommentId()!=null){
            Optional<Comment> parentCommentWrapper = commentRepository.findById(req.getParentCommentId());
            if(parentCommentWrapper.isEmpty()){
                return ResponseEntity.status(404).body(Helper.errorMap("message", "Parent comment not found"));
            }
            parentComment = parentCommentWrapper.get();
        }
        comment = new Comment(req.getContent(),sender,posting,parentComment);
        commentRepository.save(comment);
        return ResponseEntity.status(201).build();
    }
}
