package cycollectibles.Messages;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ChannelResponse {
    private Integer channelId;
    private String name;
    private Channel.ChannelType type;
    private Date createdAt;
    private List<MemberResponse> members;

    public ChannelResponse(Integer channelId, String name, Channel.ChannelType type, Date createdAt, List<MemberResponse> members){
        this.channelId = channelId;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.members = members;
    }

}