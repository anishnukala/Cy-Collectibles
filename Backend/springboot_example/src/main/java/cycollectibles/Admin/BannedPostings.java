package cycollectibles.Admin;

import cycollectibles.Postings.Posting;
import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "banned_postings")
public class BannedPostings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "admin_id",nullable = false)
    private User admin;

    @ManyToOne
    @JoinColumn(name = "posting_id",nullable = false)
    private Posting posting;

    private Date date;

    public BannedPostings() {}

    public BannedPostings(User admin, Posting posting, Date date) {
        this.admin = admin;
        this.posting = posting;
        this.date = date;
    }
}
