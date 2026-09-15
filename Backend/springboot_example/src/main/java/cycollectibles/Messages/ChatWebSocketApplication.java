package cycollectibles.Messages;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cycollectibles.Notifications.NotificationWebSocket;
import cycollectibles.Users.User;
import cycollectibles.Users.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@ServerEndpoint(value="/chat/{channelId}/{userId}")
public class ChatWebSocketApplication {
    private static ChannelRepository channelRepository;
    private static ChannelMembershipRepository channelMembershipRepository;
    private static MessageRepository messageRepository;
    private  static UserRepository userRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setRepositories(ChannelRepository cRepo,ChannelMembershipRepository cMRepo,
                                MessageRepository mRepo,UserRepository uRepo){
        channelRepository=cRepo;
        channelMembershipRepository= cMRepo;
        messageRepository = mRepo;
        userRepository=uRepo;
    }

    private static Map<Integer, Set<Session>> channelSessions = new ConcurrentHashMap<>();
    private static Map<Integer, Set<Integer>> channelUsers = new ConcurrentHashMap<>();
    private static Map<Integer,Integer> userChannel = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ChatWebSocketApplication.class);
    User sender;
    Channel channel;

    @OnOpen
    public void onOpen(Session session, @PathParam("channelId") Integer channelId,
                       @PathParam("userId") Integer userId) throws Exception{
        List<ChannelMembership> cmWrapper = channelMembershipRepository.findByChannel_ChannelIdAndMember_Id(channelId, userId);
        if(cmWrapper.isEmpty()) {
            throw new Exception("User not a member of this channel");
        }
        ChannelMembership cmMain= cmWrapper.get(0);
        sender= cmMain.getMember();
        channel = cmMain.getChannel();


        channelSessions.computeIfAbsent(channelId, k-> new HashSet<>()).add(session);
        channelUsers.computeIfAbsent(channelId, k -> new HashSet<>()).add(userId);
        userChannel.put(userId, channelId);


        List<Message> rawHistory= getChatHistory(channelId);
        List<MessageResponse> messageResponses = new ArrayList<>();

        for(Message message: rawHistory){
            messageResponses.add(transformMessage(message));
        }

        for(MessageResponse mr: messageResponses){
            sendToUser(session,mr);
        }
        cmMain.setUnread(false);
        channelMembershipRepository.save(cmMain);


    }

    @OnMessage
    public void onMessage(String rawMessageRequest, Session session, @PathParam("channelId") Integer channelId, @PathParam("userId") Integer userId) throws JsonProcessingException {
        MessageRequest dto = objectMapper.readValue(rawMessageRequest, MessageRequest.class);
        Message parentMessage = null;
        if(dto.getParentMessageId() != null){
            parentMessage = messageRepository.findById(dto.getParentMessageId()).orElse(null);
        }
        List<ChannelMembership> usersInChannel = channelMembershipRepository.findByChannel_ChannelId(channelId);
        Set<Integer> activeUsersInChannel = channelUsers.get(channelId);

        String channelName = findChannelName(sender, channel);
        for(ChannelMembership cm: usersInChannel){
            if(!activeUsersInChannel.contains(cm.getMember().getId())) {
                cm.setUnread(true);
                try {
                    NotificationWebSocket.sendNotification(cm.getMember().getId(), "MESSAGE", channelName+" :You have a new message");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        channelMembershipRepository.saveAll(usersInChannel);
        Message msg = messageRepository.save(new Message(channel,sender,dto.getContent(), parentMessage));
        broadcastToChannel(channelId,transformMessage(msg));


    }

    @OnClose
    public void onClose(Session session, @PathParam("channelId") Integer channelId, @PathParam("userId") Integer userId){
        cleanUp(session,channelId,userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("channelId") Integer channelId,
                        @PathParam("userId") Integer userId){
        logger.info("Error: " + throwable.getMessage());
        try{
            session.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                    throwable.getMessage()
            ));
        } catch(IOException e){
            e.printStackTrace();
        }
        cleanUp(session,channelId,userId);

    }

    private List<Message> getChatHistory(Integer channelId){
        return messageRepository.findByChannel_ChannelIdOrderBySentAt(channelId);
    }

    private MessageResponse transformMessage(Message message){
        String name = message.getSender().getUsername();
        Date dateSent = message.getSentAt();
        String content = message.getContent();
        Message parentMessage = message.getParentMessage();
        return new MessageResponse(message.getMessageId(),name,dateSent,content,parentMessage != null ? parentMessage.getMessageId() : null);
    }

    private void broadcastToChannel(Integer channelId,MessageResponse mr){
        Set<Session> liveSessions = channelSessions.get(channelId);
        for(Session session:liveSessions){
            try{
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(mr));
            }
            catch (IOException e) {
                logger.info("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void sendToUser(Session session,MessageResponse mr){
        try{
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(mr));
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanUp(Session session, Integer channelId, Integer userId){
        if(channelSessions.containsKey(channelId)) channelSessions.get(channelId).remove(session);
        if(channelUsers.containsKey(channelId)) channelUsers.get(channelId).remove(userId);
        userChannel.remove(userId);
    }

    private static  String findChannelName(User user,Channel channel){
        if(channel.getType()== Channel.ChannelType.GROUP){
            return channel.getName();
        }
        List<ChannelMembership> channelMemberships = channelMembershipRepository.findByChannel_ChannelId(channel.getChannelId());
        for(ChannelMembership cm:channelMemberships){
            if(!cm.getMember().getId().equals(user.getId())) return cm.getMember().getUsername();
        }
        return null;
    }


}
