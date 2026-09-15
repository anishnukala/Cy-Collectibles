package cycollectibles.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Vivek Bengre
 *
 */

public interface UserRepository extends JpaRepository<User, Integer> {

    @Transactional
    void deleteById(int id);

    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    boolean existsByEmailId(String emailId);
    List<User> findByBanned(Boolean banned);
    Integer countByBanned(Boolean banned);
    List<User> findByUserStatus(Integer userStatus);
    Integer countByUserStatus(Integer userStatus);
    Integer countByUserStatusAndUserType(Integer userStatus,String usertype);




}
