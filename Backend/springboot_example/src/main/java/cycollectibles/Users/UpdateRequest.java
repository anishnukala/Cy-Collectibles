package cycollectibles.Users;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Must enter your old password")
    private String oldPassword;

    @NotBlank(message = "Must enter a password")
    @Size(min = 7, message = "Password must be at least 7 characters long")
    private String password;

    @NotBlank(message="Must enter an email address")
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE,message = "Please enter a valid mail")
    private String emailId;
}
