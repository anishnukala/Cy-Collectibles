package cycollectibles.Messages;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MessageResponse {
    private Integer msgId;
    private String sender;
    private Date dateSent;
    private String content;
    private Integer parentMessageId;

    public MessageResponse(Integer msgId,String sender,Date dateSent,String content,Integer parentMessageId){
        this.msgId=msgId;
        this.sender=sender;
        this.dateSent=dateSent;
        this.content=content;
        this.parentMessageId=parentMessageId;
    }
}
