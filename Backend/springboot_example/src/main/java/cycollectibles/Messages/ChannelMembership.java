package cycollectibles.Messages;


import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "channel_memberships")
public class ChannelMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer channelMembershipId;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private Boolean unread = false;

    @Column(nullable = false, updatable = false)
    private Date joinedAt;

    public ChannelMembership() {}

    public ChannelMembership(Channel channel, User member) {
        this.channel = channel;
        this.member = member;
        this.unread = false;
        this.joinedAt = new Date();
    }
}

