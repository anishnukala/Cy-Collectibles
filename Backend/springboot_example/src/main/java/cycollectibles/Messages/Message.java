package cycollectibles.Messages;

import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private Date sentAt;

    @ManyToOne
    @JoinColumn(name = "parent_message_id", nullable = true)
    private Message parentMessage;

    public Message() {}

    public Message(Channel channel, User sender, String content) {
        this.channel = channel;
        this.sender = sender;
        this.content = content;
        this.sentAt = new Date();
    }

    public Message(Channel channel, User sender, String content, Message parentMessage) {
        this.channel = channel;
        this.sender = sender;
        this.content = content;
        this.parentMessage = parentMessage;
        this.sentAt = new Date();
    }
}