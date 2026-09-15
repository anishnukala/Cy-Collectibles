package cycollectibles.Postings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import cycollectibles.Users.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Date;

/**
 *
 * @author Vivek Bengre
 *
 */

@Entity
@Getter
@Setter
@Table(name = "postings")
public class Posting {
    public static Integer ACTIVE= 1;
    public static Integer BANNED=0;
    public static Integer COMPLETED = -1;
    public static Integer REPORTED = 2;
    public static Integer DELETED=3;

    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postingId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;
    /*
     * Email annotation is part of the hibernate validator package that helps with validation of email
     * input. In which case, it currently is matching to the regexp that expresses any characters are allowed followed by an '@' except for '|' and '
     * as they are potential SQL injection risk. Flag here is used to discern that input is not case-sensitive.
     */
    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private Integer status =ACTIVE;

    @Column(nullable = false)
    private Date datePosted;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id",nullable=false)
    private User seller;



    public Posting(String title, String description, int price, String genre, User seller) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.genre = genre;
        this.seller = seller;
        this.datePosted = new Date();
        this.status = ACTIVE;
    }

    public Posting() {
    }

    // =============================== Getters and Setters for each field ================================== //

}

