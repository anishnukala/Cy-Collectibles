package cycollectibles.Messages;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ChannelRepository extends JpaRepository<Channel, Integer> {
    List<Channel> findByType(Channel.ChannelType channelType);
}
