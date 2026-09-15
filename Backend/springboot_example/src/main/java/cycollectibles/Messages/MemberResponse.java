package cycollectibles.Messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse {
    private Integer id;
    private String username;

    public MemberResponse(Integer id, String username){
        this.id = id;
        this.username = username;
    }
}
