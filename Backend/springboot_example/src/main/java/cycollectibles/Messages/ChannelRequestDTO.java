package cycollectibles.Messages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChannelRequestDTO {
    @NotNull
    private Channel.ChannelType type;

    private String name;

    @NotEmpty
    private List<Integer> memberIds;

}
