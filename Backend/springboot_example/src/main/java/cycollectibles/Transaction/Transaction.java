package cycollectibles.Transaction;

import cycollectibles.Postings.Posting;
import cycollectibles.Users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @OneToOne
    @JoinColumn(name = "posting_id")
    private Posting posting;

    @Column(nullable = false)
    private Date dateSold;

    public Transaction(User buyer,Posting posting,Date dateSold){
        this.buyer=buyer;
        this.posting=posting;
        this.dateSold=dateSold;
    }

    public Transaction(){

    }
}
