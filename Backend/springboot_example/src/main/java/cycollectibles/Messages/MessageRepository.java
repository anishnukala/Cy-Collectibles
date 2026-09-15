package cycollectibles.Messages;

import cycollectibles.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChannel_ChannelId(Integer channelId);
    List<Message> findByChannel_ChannelIdOrderBySentAt(Integer channelId);
    Optional<Message> findTopByChannel_ChannelIdOrderBySentAtDesc(Integer channelId);
}