package cycollectibles.Cart;

import cycollectibles.Postings.Posting;
import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem {//hiiiiiiiiiiii
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "posting_id", nullable = false)
    private Posting posting;

    public CartItem(){

    }
    public CartItem(User user, Posting posting){
        this.user = user;
        this.posting=posting;
    }
}
