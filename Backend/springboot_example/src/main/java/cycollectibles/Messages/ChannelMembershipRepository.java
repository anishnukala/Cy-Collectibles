package cycollectibles.Messages;

import cycollectibles.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ChannelMembershipRepository extends JpaRepository<ChannelMembership, Integer> {
    List<ChannelMembership> findByChannel_ChannelIdAndMember_Id(Integer channelId, Integer senderId);

    List<ChannelMembership> findByChannel_ChannelId(Integer channelId);

    List<ChannelMembership> findByChannel_Type(Channel.ChannelType type);

    List <ChannelMembership> findByMember_Id(Integer userId);
}