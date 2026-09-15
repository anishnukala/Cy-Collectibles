package cycollectibles.Messages;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserChannelResponse {
    private Integer channelId;
    private String name;
    private Channel.ChannelType type;
    private Boolean unread;
    private String lastMessage;
    private Date lastMessageDate;

    public UserChannelResponse(Integer channelId, String name, Channel.ChannelType type, Boolean unread, String lastMessage, Date lastMessageDate){
        this.channelId = channelId;
        this.name = name;
        this.type = type;
        this.unread = unread;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }
}
