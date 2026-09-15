package cycollectibles.Postings;

import cycollectibles.Cart.CartItem;
import cycollectibles.Cart.CartItemRepository;
import cycollectibles.Comments.Comment;
import cycollectibles.Comments.CommentRepository;
import cycollectibles.Favourites.Favourite;
import cycollectibles.Favourites.FavouriteRepository;
import cycollectibles.Notifications.NotificationWebSocket;
import cycollectibles.Users.UserRepository;
import jakarta.validation.Valid;
import cycollectibles.Users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.nio.file.*;
import java.io.IOException;
import cycollectibles.helper.Helper;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PostingController {// triggerrrrr
    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @PostMapping(value = "/postings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> createPosting(
            @RequestPart("data") @Valid PostingRequest postingReq,
            BindingResult result,
            @RequestPart(required = false, value = "image") MultipartFile image
    ) throws IOException {

        if (result.hasErrors()) {
            return Helper.validationErrors(result);
        }

        Optional<User> sellerWrapper =
                userRepository.findById(postingReq.getSellerId());

        if (sellerWrapper.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Posting posting = new Posting();
        posting.setDatePosted(new Date());
        posting.setTitle(postingReq.getTitle());
        posting.setDescription(postingReq.getDescription());
        posting.setGenre(postingReq.getGenre());
        posting.setPrice(postingReq.getPrice());
        posting.setSeller(sellerWrapper.get());

        if(image==null || image.isEmpty()){
            return ResponseEntity.badRequest().body(Helper.errorMap("image","Please insert a picture"));
        }
        if(!image.getContentType().startsWith("image/")){
            return ResponseEntity.badRequest().body(Helper.errorMap("image","Only image files allowed"));
        }


        String fileName = System.currentTimeMillis() +
                "_" + image.getOriginalFilename();

        Path uploadPath = Paths.get("/backend/uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(image.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        posting.setImageUrl("/backend/uploads/" + fileName);


        posting=postingRepository.save(posting);
        notifyFavouriteUsers(posting.getGenre(), posting.getTitle());

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/postings")
    ResponseEntity<?> getPostings(@RequestParam(required = false) String genre ,
                                  @RequestParam(required = false) String titlePartial,
                                  @RequestParam(required = false) Integer minPrice ,
                                  @RequestParam(required = false) Integer maxPrice ,
                                  Sort sort){

        Specification<Posting> spec = (root, query, cb) ->
                cb.equal(root.get("status"), Posting.ACTIVE);

        if (genre != null) {
            spec = spec.and(hasGenre(genre));
        }

        if (minPrice != null) {
            spec = spec.and(hasMinPrice(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(hasMaxPrice(maxPrice));
        }

        if(titlePartial!=null && !titlePartial.isBlank()){
            spec=spec.and(containsTitle(titlePartial));
        }
        List<Posting> activePostings = postingRepository.findAll(spec,sort);
        ArrayList <PostingResponse>  response = new ArrayList<>();
        for(Posting posting : activePostings){


            PostingResponse postres = new PostingResponse(
                    posting.getPostingId(),
                    posting.getSeller().getId(),
                    posting.getTitle(),
                    posting.getDescription(),
                    posting.getPrice(),
                    posting.getGenre(),
                    posting.getDatePosted(),
                    posting.getImageUrl()
            );

            response.add(postres);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping(path="/postings/count")
    ResponseEntity<?> getActivePostings(){
        Integer count = postingRepository.countByStatus(Posting.ACTIVE);
        return ResponseEntity.ok(Map.of("numpostings",count));
    }

    @GetMapping("/postings/seller/{sellerId}")
    ResponseEntity<?> getSellerActiveListings(@PathVariable Integer sellerId){
        if(!userRepository.existsById(sellerId)){
            return ResponseEntity.notFound().build();
        }
        List<Posting> activeListing = postingRepository.findBySeller_IdAndStatus(sellerId,Posting.ACTIVE);
        ArrayList<PostingResponse> result = new ArrayList<>();
        for(Posting post : activeListing){
            PostingResponse postRes = new PostingResponse(post.getPostingId(),
                    sellerId,
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

    @PatchMapping(path="/postings/report/{postingId}")
    ResponseEntity<?> reportPost(@PathVariable Integer postingId){
        Optional<Posting> postWrapper = postingRepository.findById(postingId);
        if(postWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Posting post = postWrapper.get();
        if(!post.getStatus().equals(Posting.ACTIVE)) return ResponseEntity.status(409).build();
        post.setStatus(Posting.REPORTED);
        postingRepository.save(post);
        try {
            NotificationWebSocket.sendNotification(post.getSeller().getId(), "REPORT", "Your item " +
                    post.getTitle() + " has been reported.");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(path="postings/{postingId}")
    ResponseEntity<?> getAPost(@PathVariable Integer postingId){
        Optional<Posting> postWrapper = postingRepository.findById(postingId);
        if(postWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Posting post = postWrapper.get();
        PostingResponse postRes = new PostingResponse(post.getPostingId(),
                post.getSeller().getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPrice(),
                post.getGenre(),
                post.getDatePosted(),
                post.getImageUrl());
        return ResponseEntity.ok(postRes);
    }

    @DeleteMapping("/postings/{postingId}")
    ResponseEntity<?> deletePosting(@PathVariable Integer postingId){
        if(!postingRepository.existsByPostingIdAndStatus(postingId,Posting.ACTIVE)){
            return ResponseEntity.notFound().build();
        }
        cartItemRepository.deleteByPosting_PostingId(postingId);
        commentRepository.nullifyParentComments(postingId);
        commentRepository.deleteByPosting_PostingId(postingId);
        postingRepository.deleteByPostingId(postingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/postings/genre")
    ResponseEntity<?> getGenres(){
        ArrayList<String> genres = new ArrayList<>();
        genres.add("Comics");
        genres.add("Cards");
        genres.add("Electronics");
        genres.add("Figures");
        genres.add("Antique");
        genres.add("Furniture");
        genres.add("Video Games");
        genres.add("Other");
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/postings/ai/{postingId}")
    ResponseEntity<?> aiInfoPosting(@PathVariable Integer postingId){
        Optional<Posting> postingWrapper = postingRepository.findById(postingId);
        if(postingWrapper.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Posting posting = postingWrapper.get();
        List<Comment> comments = commentRepository.findByPosting_PostingId(postingId);
        String commentsSummary = comments.stream()
                .map(c -> c.getSender().getUsername() + ": " + c.getContent())
                .collect(Collectors.joining(". "));

        String prompt = "Summarize the following collectible listing and its comments for a potential buyer. " +
                "Include key details about the item, any price negotiations mentioned, seller responses, and overall sentiment from the comments. " +
                "Listing title: " + posting.getTitle() + ". " +
                "Description: " + posting.getDescription() + ". " +
                "Price: $" + posting.getPrice() + ". " +
                "Genre: " + posting.getGenre() + ". " +
                "Comments: " + (commentsSummary.isEmpty() ? "No comments yet." : commentsSummary);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + geminiApiKey;
            Map<String, Object> requestBody = Map.of("contents", List.of(
                    Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
            ));
            Map response = restTemplate.postForObject(url, requestBody, Map.class);
            List candidates = (List) response.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            String aiResponse = (String) part.get("text");
            return ResponseEntity.ok(Map.of("summary", aiResponse));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("AI ERROR: " + e.getMessage());
            System.out.println("AI ERROR CLASS: " + e.getClass().getName());
            return ResponseEntity.status(500).body(Helper.errorMap("message", "AI service error"));
        }


    }


    public static Specification<Posting> hasGenre(String genre) {
        return (root, query, cb) ->
                cb.equal(root.get("genre"), genre);
    }

    public static Specification<Posting> hasMinPrice(Integer price) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), price);
    }
    public static Specification<Posting> hasMaxPrice(Integer price) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), price);
    }

    public static Specification<Posting> containsTitle(String titlePartial) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%"+titlePartial.trim().toLowerCase()+"%");

    }

    private void notifyFavouriteUsers(String category, String postingTitle){
        List<Favourite> favourites = favouriteRepository.findByCategory(category);
        for(Favourite f : favourites){
            try {
                NotificationWebSocket.sendNotification(f.getUser().getId(), "NEW_POSTING",
                        "New posting in your favourite category " + category + ": " + postingTitle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
