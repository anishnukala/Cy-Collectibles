package cycollectibles.Admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BannedUsersRepository extends JpaRepository<BannedUsers, Integer> {
}
