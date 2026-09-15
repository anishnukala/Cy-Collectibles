package cycollectibles.Admin;

import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "banned_users")
public class BannedUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User bannedUser;

    private Date date;

    public BannedUsers() {}

    public BannedUsers(User admin, User bannedUser, Date date) {
        this.admin = admin;
        this.bannedUser = bannedUser;
        this.date = date;
    }
}
