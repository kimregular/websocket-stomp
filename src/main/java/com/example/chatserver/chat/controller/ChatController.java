package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomResponse;
import com.example.chatserver.chat.dto.MyChatListResponse;
import com.example.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    // 그룹 채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String roomName) {
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }

    // 그룹 채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupRooms() {
        List<ChatRoomResponse> chatRooms = chatService.getGroupChatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    // 그룹 채팅 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupRoom(@PathVariable Long roomId) {
        chatService.addParticipantToRoom(roomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 그룹 채팅 구성원 확인
    @GetMapping("/room/group/{roomId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getGroupChatMembers(roomId));
    }

    // 이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getGroupHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chats = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(chats);
    }

    // 메시지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId) {
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    // 내 채팅방 목록 조회
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyRooms() {
        List<MyChatListResponse> myChatList = chatService.getMyChatRooms();
        return ResponseEntity.ok(myChatList);
    }

    // 그룹채팅 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupRoom(@PathVariable Long roomId) {
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.ok().build();
    }

    // 개인 채팅방 개설 또는 기존 roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId) {
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
        log.info("roomId {}", roomId);
        return ResponseEntity.ok(roomId);
    }

    // SSE 읽지 않은 메시지 갱신
    @GetMapping("/sse/unread")
    public SseEmitter subscribeUnreadCount() {
        return chatService.subscribeUnreadCount();
    }
}
