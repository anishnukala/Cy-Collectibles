package cycollectibles.Notifications;

import cycollectibles.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}")
    ResponseEntity<?> getUserNotifications(@PathVariable Integer userId){
        // get all notifications for user
        if(!userRepository.existsById(userId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(notificationRepository.findByRecipient_Id(userId));
    }

    @GetMapping("/notif/{notificationId}")
    ResponseEntity<?> getNotif(@PathVariable Integer notificationId){
        Optional<Notification> notificationWrapper = notificationRepository.findById(notificationId);
        if(notificationWrapper.isEmpty()) return ResponseEntity.notFound().build();
        Notification notification = notificationWrapper.get();
        markAsRead(notification);
        notification = notificationRepository.save(notification);
        return  ResponseEntity.ok(notification);
    }

    private void markAsRead(Notification notification){
        notification.setStatus(Notification.READ);
    }

}
