package cycollectibles.Comments;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentResponse {
    private Integer commentId;
    private String senderUsername;
    private String content;
    private Integer parentCommentId;
    private Date createdAt;

    public CommentResponse(Integer commentId, String senderUsername, String content, Integer parentCommentId, Date createdAt){
        this.commentId = commentId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
    }
}
