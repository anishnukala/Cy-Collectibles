package cycollectibles.Transaction;

import cycollectibles.Postings.PostingResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TransactionResponse {
    private Integer transactionId;
    private Date dateSold;
    private PostingResponse posting;
    private Integer buyerId;

    public TransactionResponse(Integer transactionId,Integer buyerId, Date dateSold, PostingResponse posting) {
        this.transactionId = transactionId;
        this.dateSold = dateSold;
        this.posting = posting;
        this.buyerId=buyerId;
    }
    public TransactionResponse(){

    }
}
