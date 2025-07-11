package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomOrderByCreatedAt(ChatRoom chatRoom);
}
