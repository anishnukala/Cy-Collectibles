package cycollectibles.Notifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipient_IdAndStatus(Integer userId, Integer status);
    List<Notification> findByRecipient_Id(Integer userId);
}
