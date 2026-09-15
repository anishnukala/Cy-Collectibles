package cycollectibles.Comments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Integer senderId;
    private String content;
    private Integer parentCommentId;
}
