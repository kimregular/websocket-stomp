package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomResponse;
import com.example.chatserver.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        chatService.addParticipantToGroupChat(roomId);
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
}
