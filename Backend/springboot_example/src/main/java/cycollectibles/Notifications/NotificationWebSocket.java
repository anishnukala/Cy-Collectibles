package cycollectibles.Notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@ServerEndpoint("/notifications/{userId}")
public class NotificationWebSocket {

    private static NotificationRepository notificationRepository;
    private static UserRepository userRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<Integer, Session> userSessions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(NotificationWebSocket.class);

    @Autowired
    public void setRepositories(NotificationRepository nRepo, UserRepository uRepo){
        notificationRepository = nRepo;
        userRepository = uRepo;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) throws Exception {
        // validate user
        Optional<User> userWrapper = userRepository.findById(userId);
        if(userWrapper.isEmpty()) throw new Exception("User not found");
        // register session
        userSessions.put(userId,session);
        // send unread notifications
    }

    @OnClose
    public void onClose(@PathParam("userId") Integer userId){
        // remove session from map
        userSessions.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("userId") Integer userId){
        logger.info("Error for user " + userId + ": " + throwable.getMessage());
        userSessions.remove(userId);
        // remove session from map
    }

    // static method called from other controllers
    public static void sendNotification(Integer recipientId, String type, String message) throws Exception {
        // save to DB
        Optional<User> userWrapper = userRepository.findById(recipientId);
        if(userWrapper.isEmpty()) throw new Exception("User not found");
        User user = userWrapper.get();
        Notification notification = new Notification(user,type,message);
        notification = notificationRepository.save(notification);
        Session session = userSessions.get(recipientId);
        System.out.println("Sending notification to " + recipientId + " session found: " + (session != null));
        if(session != null && session.isOpen()){
            sendToSession(session, notification);
        }
        // if user is connected, push over WebSocket
    }

    private static void sendToSession(Session session, Notification notification){
        try{
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(notification));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}