package cycollectibles.Ratings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingReq {

    @NotNull
    private Integer customerId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
