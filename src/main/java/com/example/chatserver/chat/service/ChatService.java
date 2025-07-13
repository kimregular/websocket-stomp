package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomResponse;
import com.example.chatserver.chat.dto.MyChatListResponse;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberResponse;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConcurrentMap<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;
    private final ReadStatusRepository readStatusRepository;


    @Transactional
    public void save(Long roomId, ChatMessageDto chatMessageDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));

        // 보낸사람 조회
        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 사용자별 읽음 여부 저장
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c : participants) {
            List<MyChatListResponse> myChatRooms = getMyChatRoomsByMember(c.getMember());
            sendUnreadCountUpdate(c.getMember().getId(), myChatRooms);
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().getEmail().equals(sender.getEmail()))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    @Transactional
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

    @Transactional
    public void addParticipantToRoom(Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));

        if(chatRoom.getIsGroupChat().equals("N")) throw new IllegalArgumentException("그룹 채팅이 아닙니다.");

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

    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 내가 해당 채팅방의 참여자가 아닐경우 에러
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));
        // member 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        if(!chatParticipantRepository.existsByChatRoomAndMember(chatRoom, member))
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");

        return chatMessageRepository
                .findByChatRoomOrderByCreatedAt(chatRoom)
                .stream()
                .map(c -> new ChatMessageDto(chatRoom.getId(), c.getContent(), c.getMember().getEmail()))
                .toList();
    }

    public boolean isRoomParticipants(String email, Long roomId) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));
        return chatParticipantRepository.existsByChatRoomAndMember(chatRoom, member);
    }

    @Transactional
    public void messageRead(Long roomId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));

        readStatusRepository.findByChatRoomAndMember(chatRoom, member).forEach(r -> r.updateReadStatus(true));
    }

    public List<MyChatListResponse> getMyChatRooms() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        return chatParticipantRepository.findAllByMember(member).stream().map(r -> {
            Long unReadCount = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(r.getChatRoom(), member);
            return MyChatListResponse.builder()
                    .roomId(r.getChatRoom().getId())
                    .roomName(r.getChatRoom().getName())
                    .isGroupChat(r.getChatRoom().getIsGroupChat())
                    .unReadCount(unReadCount)
                    .build();
        }).toList();
    }

    @Transactional
    public void leaveGroupChatRoom(Long roomId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("Room Not Found"));

        if(chatRoom.getIsGroupChat().equals("N")) throw new IllegalArgumentException("단체 채팅방이 아닙니다.");

        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(chatParticipant);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        log.info("otherMemberId {}", otherMemberId);
        Member me = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        Member other = memberRepository.findById(otherMemberId).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        log.info("me {}", me);
        log.info("other {}", other);
        // 나와 상대방이 1:1 채팅에 이미 참석하고 있다면 해당 roomId return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(me.getId(), other.getId());
        log.info("chatRoom {}", chatRoom);
        if(chatRoom.isPresent()) return chatRoom.get().getId();
        // 만약 1:1 채팅방이 없다면 새로운 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(me.getName() + "-" + other.getName())
                .build();
        chatRoomRepository.save(newRoom);
        // 두 사람 모두 참여자로 추가

        ChatParticipant meParticipant = ChatParticipant.builder()
                .chatRoom(newRoom)
                .member(me)
                .build();
        ChatParticipant otherParticipant = ChatParticipant.builder()
                .chatRoom(newRoom)
                .member(other)
                .build();

        chatParticipantRepository.save(meParticipant);
        chatParticipantRepository.save(otherParticipant);
        return newRoom.getId();
    }

    public SseEmitter subscribeUnreadCount() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("Member Not Found"));
        SseEmitter emitter = new SseEmitter(0L); // 무제한 유지
        emitterMap.put(member.getId(), emitter); // memberId 기준으로 관리

        emitter.onCompletion(() -> emitterMap.remove(member.getId()));
        emitter.onTimeout(() -> emitterMap.remove(member.getId()));

        return emitter;
    }

    private void sendUnreadCountUpdate(Long memberId, List<MyChatListResponse> updatedList) {
        SseEmitter emitter = emitterMap.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread")
                        .data(updatedList));
            } catch (IOException e) {
                emitterMap.remove(memberId);
            }
        }
    }

    private List<MyChatListResponse> getMyChatRoomsByMember(Member member) {
        return chatParticipantRepository.findAllByMember(member).stream().map(r -> {
            Long unReadCount = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(r.getChatRoom(), member);
            return MyChatListResponse.builder()
                    .roomId(r.getChatRoom().getId())
                    .roomName(r.getChatRoom().getName())
                    .isGroupChat(r.getChatRoom().getIsGroupChat())
                    .unReadCount(unReadCount)
                    .build();
        }).toList();
    }
}
