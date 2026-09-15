package cycollectibles.Messages;

import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ChannelController {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ChannelMembershipRepository channelMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/channel")
    ResponseEntity<?> createChannel(@Valid @RequestBody ChannelRequestDTO crd, BindingResult result){
        if(result.hasErrors()) return ResponseEntity.badRequest().build();

        if(crd.getType() == Channel.ChannelType.DIRECT ){

            if(crd.getMemberIds().size() != 2) return ResponseEntity.badRequest().build();

            List<User> members = verifyMembers(crd.getMemberIds());

            if(members == null) return ResponseEntity.notFound().build();

            List<Integer> memberIds = new ArrayList<>();
            members.forEach(member->memberIds.add(member.getId()));
            if(!checkDMChannelExists(memberIds)) {

                Channel channel = channelRepository.save(new Channel(null, Channel.ChannelType.DIRECT));

                saveMembers(channel, members);
            }
        }
        else if (crd.getType() == Channel.ChannelType.GROUP){
            if(crd.getMemberIds().isEmpty() || crd.getName()==null||crd.getName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }


            List<User> members = verifyMembers(crd.getMemberIds());
            if(members == null) return ResponseEntity.notFound().build();

            Channel channel = channelRepository.save(new Channel(crd.getName(), Channel.ChannelType.GROUP));
            saveMembers(channel,members);
        }
        else{
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).build();

    }

    @GetMapping("/channel/{channelId}")
    ResponseEntity<?> getChannelInfo(@PathVariable Integer channelId){
        Optional<Channel> channelWrapper = channelRepository.findById(channelId);
        if(channelWrapper.isEmpty()) return ResponseEntity.notFound().build();
        Channel channel = channelWrapper.get();
        List<MemberResponse> memberIds = new ArrayList<>();
        getMembers(channelId).forEach(member->memberIds.add(new MemberResponse(member.getId(),member.getUsername())));
        ChannelResponse cr =new ChannelResponse(channelId,channel.getName(),channel.getType(),
                channel.getCreatedAt(),memberIds);
        return ResponseEntity.ok(cr);
    }

    @GetMapping("/channel/user/{userId}")
    ResponseEntity<?> userChannelList(@PathVariable Integer userId){
        if(!userRepository.existsById(userId)) return ResponseEntity.notFound().build();

        List<ChannelMembership> memberships = channelMembershipRepository.findByMember_Id(userId);
        List<UserChannelResponse> response = new ArrayList<>();
        Message dummy = new Message();

        for(ChannelMembership cm : memberships){
            Channel channel = cm.getChannel();
            Optional<Message> lastMessageWrapper = messageRepository.findTopByChannel_ChannelIdOrderBySentAtDesc(channel.getChannelId());
            String lastMessage = lastMessageWrapper.orElse(dummy).getContent();
            Date lastMessageDate = lastMessageWrapper.orElse(dummy).getSentAt();
            response.add(new UserChannelResponse(
                    channel.getChannelId(),
                    channel.getName(),
                    channel.getType(),
                    cm.getUnread(),
                    lastMessage,
                    lastMessageDate
            ));
        }
        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/channel/")
//    ResponseEntity<?> leaveChannel()


    private void saveMembers(Channel channel,List<User> members){
        for(User member : members){
            channelMembershipRepository.save(new ChannelMembership(channel, member));
        }
    }

    private List<User> verifyMembers(List<Integer> memberIds){
        List<User> members = new ArrayList<>();
        for(Integer id : memberIds){
            Optional<User> memberWrapper = userRepository.findById(id);
            if(memberWrapper.isEmpty()) return null;
            members.add(memberWrapper.get());
        }
        return members;
    }

    private List<User>getMembers(Integer channelId){
        List<ChannelMembership> cms =channelMembershipRepository.findByChannel_ChannelId(channelId);
        List<User> members = new ArrayList<>();
        for(ChannelMembership cm: cms){
            members.add(cm.getMember());
        }
        return members;
    }

    Boolean checkDMChannelExists(List<Integer> reqMemberIds){
        List<Channel> dms =channelRepository.findByType(Channel.ChannelType.DIRECT);

        for(Channel ch: dms){
            List<Integer> memberIds = new ArrayList<>();
            getMembers(ch.getChannelId()).forEach(member->memberIds.add(member.getId()));
            if(new HashSet<>(reqMemberIds).equals(new HashSet<>(memberIds))) return true;

        }
        return false;
    }

}