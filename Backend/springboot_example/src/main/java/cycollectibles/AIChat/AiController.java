package cycollectibles.AIChat;

import cycollectibles.Favourites.Favourite;
import cycollectibles.Favourites.FavouriteRepository;
import cycollectibles.Transaction.Transaction;
import cycollectibles.Transaction.TransactionRepository;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import cycollectibles.helper.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiMessageRepository aiMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @GetMapping("/chat/{userId}")
    ResponseEntity<?> getChatHistory(@PathVariable Integer userId){
        Optional<User> userWrapper = userRepository.findById(userId);

        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user  = userWrapper.get();

        List<AiMessage> history = aiMessageRepository.findByUser_IdOrderByCreatedAt(userId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/chat/{userId}")
    ResponseEntity<?> sendMessage(@PathVariable Integer userId, @RequestBody Map<String, String> body){
        Optional<User> userWrapper = userRepository.findById(userId);
        if(userWrapper.isEmpty()) return ResponseEntity.notFound().build();
        User user = userWrapper.get();

        String userMessage = body.get("message");
        if(userMessage == null || userMessage.isBlank())
            return ResponseEntity.badRequest().body(Helper.errorMap("message", "Message cannot be empty"));

        // save user message to DB
        aiMessageRepository.save(new AiMessage(user, "user", userMessage));

        // fetch conversation history
        List<AiMessage> history = aiMessageRepository.findByUser_IdOrderByCreatedAt(userId);

        // build contents array for Gemini
        List<Map<String, Object>> contents = new ArrayList<>();

        List<Favourite> favourites = favouriteRepository.findByUser_Id(userId);
        List<Transaction> transactionHist;
        if(user.getUserType().equals("buyer")){
            transactionHist = transactionRepository.findByBuyer_Id(userId);
        }
        else if(user.getUserType().equals("seller")){
            transactionHist = transactionRepository.findByPosting_Seller_Id(userId);
        }
        else{
            transactionHist=new ArrayList<>();
        }
        // for favourites
        String favouriteCategories = favourites.stream()
                .map(Favourite::getCategory)
                .collect(Collectors.joining(", "));

// for transactions
        String transactionSummary = transactionHist.stream()
                .map(t -> t.getPosting().getTitle())
                .collect(Collectors.joining(", "));

        // system prompt first
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", "You are CyBot, a helpful assistant for CyCollectibles — a marketplace for buying and selling collectibles including Comics, Cards, Electronics, Figures, Antiques, Furniture, Video Games, and more. " +
                        "Here is how the platform works: " +
                        "Sellers can create listings with a title, description, price, genre, and image. Buyers can browse listings, filter by genre, price, and title, add items to their cart, and purchase them. " +
                        "Users can report listings they find suspicious, and admins will review and verify or ban them. " +
                        "Users can message each other through direct messages or group chats. " +
                        "Users can leave comments on listings. " +
                        "Buyers can save their favourite categories and get notified when new listings are posted in those categories. " +
                        "Users get notifications for purchases, messages, flags, bans, and listing updates. " +
                        "Admins can ban users, ban listings, flag users, and verify reported listings. " +
                        "Here is some context about the user you are talking to: " +
                        "Username: " + user.getUsername() + ". " +
                        "User type: " + user.getUserType() + ". " +
                        "Favourite categories: " + (favouriteCategories.isEmpty() ? "none set" : favouriteCategories) + ". " +
                        "Transaction history: " + (transactionSummary.isEmpty() ? "no transactions yet" : transactionSummary) + ". " +
                        "Use this context to give personalized recommendations and advice. " +
                        "You can help with: finding collectibles, pricing advice, selling tips, platform features, and anything related to collectibles as a hobby or investment. " +
                        "If the user asks about something completely unrelated to collectibles or the platform, politely redirect them by saying: " +
                        "'I'm CyBot, I'm here to help with all things collectibles and CyCollectibles! Is there anything I can help you with on the platform?'"))
        ));
        //add info about , user, traction history, prefrences
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text", "Understood, I will only help with CyCollectibles related questions."))
        ));

        // add conversation history
        for(AiMessage msg : history){
            contents.add(Map.of(
                    "role", msg.getRole(),
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        // call Gemini
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + geminiApiKey;
            Map<String, Object> requestBody = Map.of("contents", contents);
            Map response = restTemplate.postForObject(url, requestBody, Map.class);

            // extract response text
            List candidates = (List) response.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            String aiResponse = (String) part.get("text");

            // save AI response to DB
            aiMessageRepository.save(new AiMessage(user, "model", aiResponse));

            return ResponseEntity.ok(Map.of("response", aiResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Helper.errorMap("message", "AI service error"));
        }
    }
}
