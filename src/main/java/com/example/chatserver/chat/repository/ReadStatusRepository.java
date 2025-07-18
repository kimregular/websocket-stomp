package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.member.domain.Member;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadStatusRepository extends CrudRepository<ReadStatus, Long> {

    List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, Member member);
}
