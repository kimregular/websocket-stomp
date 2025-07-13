package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.service.ChatService;
import com.example.chatserver.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;

    // 방법1 MessageMapping과 sendTo 한번에 처리
//    @MessageMapping("/{roomId}") // 클라이언트에서 특정 publish/roomId 형태로 메시지를 발행시 messageMapping 수신
//    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발생하여 구독중인 클라이언트에게 메시지 전송
//    public String sendMessage(@DestinationVariable String roomId,  String message) {
//        log.info("message {}", message);
//        return message;
//    }

    // 방법2 messageMapping 어노테이션만 활용
    @MessageMapping("/{roomId}")
    @SendTo("/topic/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) throws JsonProcessingException {
        chatService.save(roomId, chatMessageDto);
        chatMessageDto.setRoomId(roomId);
//        messagingTemplate.convertAndSend("/topic/" + roomId, chatMessageDto);
        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(chatMessageDto);
        redisPubSubService.publish("chat", message);
    }
}
