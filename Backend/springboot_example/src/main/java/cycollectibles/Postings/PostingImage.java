//package cycollectibles.Postings;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import cycollectibles.Postings.Posting;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//public class PostingImage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    private String imageUrl;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "posting_id", nullable = false)
//    private Posting posting;
//
//}
