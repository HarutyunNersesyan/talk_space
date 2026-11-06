package com.talk_space.modules.chat.api;
import com.talk_space.modules.chat.api.dto.ChatMessageDto;
import com.talk_space.modules.chat.api.dto.UserChatDto;

import com.talk_space.modules.chat.internal.persistence.ChatRepository;

import com.talk_space.modules.network.api.NetworkAccess;
import com.talk_space.modules.chat.internal.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service @RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final NetworkAccess access;

    @Transactional
    public ChatMessageDto send(String sender, String receiver, String content) {
        return send(sender, receiver, content, null);
    }
    @Transactional
    public ChatMessageDto send(String sender, String receiver, String content, String clientMessageId) {
        if (receiver==null || content==null || content.isBlank() || content.length()>4000)
            throw new ResponseStatusException(BAD_REQUEST,"Message must contain 1–4000 characters");
        access.lockPair(sender,receiver);
        access.requireChat(sender,receiver);
        if (clientMessageId != null) {
            try { java.util.UUID.fromString(clientMessageId); }
            catch (IllegalArgumentException ex) { throw new ResponseStatusException(BAD_REQUEST,"Invalid message id"); }
            var existing = chatRepository.findBySenderUserNameAndClientMessageId(sender,clientMessageId);
            if (existing.isPresent()) {
                ChatMessage saved=existing.get();
                if (!saved.getReceiver().getUserName().equals(receiver) || !saved.getContent().equals(content.trim()))
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Message id already used");
                return convertToDto(saved);
            }
        }
        ChatMessage message=new ChatMessage();
        message.setClientMessageId(clientMessageId);
        message.setSender(access.member(sender)); message.setReceiver(access.member(receiver));
        message.setContent(content.trim()); message.setTimestamp(LocalDateTime.now()); message.setRead(false);
        return convertToDto(chatRepository.saveAndFlush(message));
    }
    @Transactional(readOnly=true)
    public void requireChat(String sender,String receiver) { access.requireChat(sender,receiver); }

    @Transactional(readOnly=true)
    public List<ChatMessageDto> getChatHistory(String user1,String user2) {
        access.requireRestActor(user1); access.requireChat(user1,user2);
        return chatRepository.findChatHistory(user1,user2).stream().filter(m -> visible(m, user1)).map(this::convertToDto).toList();
    }
    @Transactional
    public void markMessagesAsRead(String sender,String receiver) {
        access.requireRestActor(receiver); access.requireChat(sender,receiver);
        chatRepository.markMessagesAsRead(sender,receiver);
    }
    @Transactional(readOnly=true)
    public long getUnreadCount(String sender,String receiver) {
        access.requireRestActor(receiver); access.requireChat(sender,receiver);
        return chatRepository.countUnreadMessages(sender,receiver);
    }
    @Transactional(readOnly=true)
    public List<UserChatDto> getUserChats(String userName) {
        access.requireRestActor(userName);
        Map<String,UserChatDto> chats=new LinkedHashMap<>();
        for (var partner:access.partners(userName)) {
            UserChatDto dto=new UserChatDto(); dto.setPartnerUsername(partner.getUserName());
            dto.setPartnerName(partner.getFirstName()+" "+partner.getLastName());
            dto.setLastMessage(""); dto.setLastMessageTime(null); dto.setUnreadCount(0L);
            if (partner.getImage()!=null) dto.setPartnerImage(partner.getImage().getFilePath());
            chats.put(partner.getUserName(),dto);
        }
        // Existing history alone never grants permission to appear in the chat list.
        for (var message:chatRepository.findAllUserMessages(userName)) {
            if (!visible(message, userName)) continue;
            String other=message.getSender().getUserName().equals(userName)?message.getReceiver().getUserName():message.getSender().getUserName();
            var dto=chats.get(other);
            if (dto!=null && (dto.getLastMessageTime()==null || message.getTimestamp().isAfter(dto.getLastMessageTime()))) {
                dto.setLastMessage(message.getContent()); dto.setLastMessageTime(message.getTimestamp());
                dto.setUnreadCount(chatRepository.countUnreadMessages(other,userName));
            }
        }
        return chats.values().stream().sorted(Comparator.comparing(UserChatDto::getLastMessageTime,
            Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(UserChatDto::getPartnerUsername)).toList();
    }
    private boolean visible(ChatMessage message, String user) {
        return message.getSender().getUserName().equals(user) ? !message.isDeletedForSender() : !message.isDeletedForReceiver();
    }
    private ChatMessage ownedMessage(Long id, String actor) {
        access.requireRestActor(actor);
        ChatMessage message = chatRepository.findForUpdate(id).orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        String sender = message.getSender().getUserName(), receiver = message.getReceiver().getUserName();
        if (!actor.equals(sender) && !actor.equals(receiver)) throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        access.requireChat(sender, receiver);
        if (!visible(message, actor)) throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        return message;
    }
    @Transactional
    public ChatMessageDto edit(Long id, String actor, String content) {
        ChatMessage message = ownedMessage(id, actor);
        if (!message.getSender().getUserName().equals(actor)) throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        if (content == null || content.isBlank() || content.length() > 4000) throw new ResponseStatusException(BAD_REQUEST, "Message must contain 1–4000 characters");
        if (!message.getContent().equals(content.trim())) { message.setContent(content.trim()); message.setEdited(true); }
        return convertToDto(message);
    }
    @Transactional
    public String delete(Long id, String actor, boolean everyone) {
        ChatMessage message = ownedMessage(id, actor);
        boolean sender = message.getSender().getUserName().equals(actor);
        if (everyone && !sender) throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        if (sender || everyone) message.setDeletedForSender(true);
        if (!sender || everyone) message.setDeletedForReceiver(true);
        if (message.isDeletedForSender() && message.isDeletedForReceiver()) message.setContent("");
        return sender ? message.getReceiver().getUserName() : message.getSender().getUserName();
    }
    public ChatMessageDto convertToDto(ChatMessage message) {
        ChatMessageDto dto=new ChatMessageDto(); dto.setId(message.getId());
        dto.setSender(message.getSender().getUserName());
        dto.setSenderDisplayName(message.getSender().getFirstName()+" "+message.getSender().getLastName());
        dto.setReceiver(message.getReceiver().getUserName());
        dto.setReceiverDisplayName(message.getReceiver().getFirstName()+" "+message.getReceiver().getLastName());
        dto.setContent(message.getContent()); dto.setTimestamp(message.getTimestamp()); dto.setRead(message.isRead()); dto.setEdited(message.isEdited());
        if (message.getSender().getImage()!=null) dto.setSenderImage(message.getSender().getImage().getFilePath());
        if (message.getReceiver().getImage()!=null) dto.setReceiverImage(message.getReceiver().getImage().getFilePath());
        return dto;
    }
}
