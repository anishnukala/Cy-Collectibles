package cycollectibles.Notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {
    public static Integer UNREAD = 0;
    public static Integer READ = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User recipient;

    @Column(nullable = false)
    private String type; // "PURCHASE", "MESSAGE", "RATING", "NEW_POSTING", "ADMIN"

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Integer status = UNREAD;

    public Notification(User recipient, String type, String message){
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.createdAt = new Date();
        this.status = UNREAD;
    }

    public Notification(){}
}
