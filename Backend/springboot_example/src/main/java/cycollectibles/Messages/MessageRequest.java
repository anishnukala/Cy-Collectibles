package cycollectibles.Messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String content;
    private Integer parentMessageId;

    public MessageRequest(){}

    public MessageRequest(String content, Integer parentMessageId){
        this.content = content;
        this.parentMessageId = parentMessageId;
    }
}
