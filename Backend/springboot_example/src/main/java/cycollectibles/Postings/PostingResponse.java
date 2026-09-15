package cycollectibles.Postings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class PostingResponse {
    private Integer postingId;
    private Integer sellerId;

    private String title;

    private String description;

    private Integer price;

    private String genre;

    private Date date;

    private String imageUrl;
}
