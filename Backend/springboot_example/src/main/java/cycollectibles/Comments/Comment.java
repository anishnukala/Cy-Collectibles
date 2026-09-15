package cycollectibles.Comments;

import cycollectibles.Postings.Posting;
import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id", nullable = true)
    private Comment parentComment;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    public Comment() {}

    public Comment(String content, User sender, Posting posting, Comment parentComment) {
        this.content = content;
        this.sender = sender;
        this.posting = posting;
        this.parentComment = parentComment;
        this.createdAt = new Date();
    }
}