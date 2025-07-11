package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageRequest;
import com.example.chatserver.chat.dto.ChatRoomResponse;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberResponse;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;
    private final ReadStatusRepository readStatusRepository;


    @Transactional
    public void save(Long roomId, ChatMessageRequest chatMessageRequest) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));

        // 보낸사람 조회
        Member sender = memberRepository.findByEmail(chatMessageRequest.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageRequest.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 사용자별 읽음 여부 저장
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c : participants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().getEmail().equals(sender.getEmail()))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String roomName) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        
        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
        
        // 개설자를 채팅방 멤버로 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomResponse> getGroupChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        return chatRooms.stream().map(r -> new ChatRoomResponse(r.getId(), r.getName())).toList();
    }

    public void addParticipantToGroupChat(Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));
        // member 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        // 이미 참여했는지 검증
        if(chatParticipantRepository.existsByChatRoomAndMember(chatRoom, member)) return;
        // chatParticipant 객체 생성 후 저장
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<MemberResponse> getGroupChatMembers(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));
        return chatParticipantRepository.findByChatRoom(room).stream().map(p -> new MemberResponse(p.getMember().getId(), p.getMember().getEmail(), p.getMember().getName())).toList();
    }
}
