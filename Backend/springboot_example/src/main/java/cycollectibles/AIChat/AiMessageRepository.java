package cycollectibles.AIChat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Integer> {
    List<AiMessage> findByUser_IdOrderByCreatedAt(Integer userId);
}
