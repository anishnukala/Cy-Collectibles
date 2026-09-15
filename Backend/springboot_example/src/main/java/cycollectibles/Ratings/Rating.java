package cycollectibles.Ratings;

import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ratingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    private Integer rating;

    public Rating(Integer ratingId,User customer,User seller,Integer rating){
        this.ratingId=ratingId;
        this.customer=customer;
        this.seller=seller;
        this.rating=rating;
    }

    public Rating(){

    }
}
