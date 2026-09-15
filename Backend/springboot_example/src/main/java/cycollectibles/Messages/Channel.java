package cycollectibles.Messages;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer channelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    public Channel(){}

    public Channel(String name,ChannelType type){
        this.name=name;
        this.type=type;
        this.createdAt=new Date();
    }

    public enum ChannelType {
        DIRECT,
        GROUP
    }
}
