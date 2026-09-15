package cycollectibles.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

/**
 *
 * @author Vivek Bengre
 *
 */

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    public static Integer ACTIVE= 1;
    public static Integer BANNED=0;
    public static Integer DELETED = -1;


    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userType;
    /*
     * Email annotation is part of the hibernate validator package that helps with validation of email
     * input. In which case, it currently is matching to the regexp that expresses any characters are allowed followed by an '@' except for '|' and '
     * as they are potential SQL injection risk. Flag here is used to discern that input is not case-sensitive.
     */
    @Column(unique = true,nullable = false)
    private String emailId;

    private Integer flagCount =0;
    private Boolean banned =false;

    private Integer userStatus = User.ACTIVE;



    public User(String userName, String userType,String emailId,int flagCount,boolean banned) {
        this.username=userName;
        this.userType=userType;
        this.emailId=emailId;
        this.flagCount=flagCount;
        this.banned=banned;
    }

    public User() {
    }

    // =============================== Getters and Setters for each field ================================== //

}

