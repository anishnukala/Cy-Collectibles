package cycollectibles.AIChat;

import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "ai_messages")
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer aiMessageId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // "user" or "model"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    public AiMessage() {}

    public AiMessage(User user, String role, String content) {
        this.user = user;
        this.role = role;
        this.content = content;
        this.createdAt = new Date();
    }
}
