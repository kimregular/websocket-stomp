package com.example.chatserver.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebsocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOrigins("http://localhost:3000")
                // ws://가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 socketJs 라이브러리를 통한 요청을 허용하는 설정
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /publish/1 형태로 메시지 발행해야 함을 설정
        // /publish로 시작하는 url 패턴으로 메시지가 발행되면 @controller 객체의 @MessageMapping 메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");
        // /topic/1 형태로 메시지를 수신해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 웹소캣 요청시에는 http header 등 http 메시지를 넣어올 수 있고, 이를 interceptors를 통해 가로채 토큰등을 검증한다
        registration.interceptors(stompHandler);
    }
}
