<script setup>
import {onBeforeMount, ref, nextTick, onBeforeUnmount} from "vue";
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import {onBeforeRouteLeave} from "vue-router";
import {useRoute} from 'vue-router';

const route = useRoute();
const roomId = ref(null);

const messages = ref([]);
const newMessage = ref("");
const chatBoxRef = ref(null);

const stompClient = ref(null);
const token = ref(null);
const senderEmail = ref("");

// WebSocket 연결
const connectWebsocket = () => {
  if(stompClient.value && stompClient.value.connect) return;

  const sockJs = new SockJS(`${process.env.VUE_APP_API_BASE_URL}/connect`);
  token.value = localStorage.getItem("token");
  stompClient.value = Stomp.over(sockJs);

  stompClient.value.connect({
        Authorization: `Bearer ${token.value}`,
      },
      () => {
        stompClient.value.subscribe(`/topic/${roomId.value}`, (message) => {
          const parsedMessage = JSON.parse(message.body);
          messages.value.push(parsedMessage);
          scrollToBottom();
        });
      }
  );
};

// 메시지 전송
const sendMessage = () => {
  if (!newMessage.value.trim()) {
    newMessage.value = "";
    return;
  }

  const message = {
    senderEmail: senderEmail.value,
    message: newMessage.value
  }
  stompClient.value.send(`/publish/${roomId.value}`, JSON.stringify(message));
  newMessage.value = "";
};

// 스크롤을 최하단으로 이동
const scrollToBottom = () => {
  nextTick(() => {
    if (chatBoxRef.value) {
      chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
  })
}

const disconnectWebSocket = () => {
  if(stompClient.value && stompClient.value.connect) {
    stompClient.value.unsubscribe(`/topic/${roomId.value}`);
    stompClient.value.disconnect();
  }
}

// 컴포넌트 마운트 시 WebSocket 연결
onBeforeMount(() => {
  roomId.value = route.params.roomId;
  senderEmail.value = localStorage.getItem("email");
  connectWebsocket()
});

onBeforeRouteLeave((from, to, next) => {
  disconnectWebSocket()
  next()
})

// 연결 종료 시 클린업
onBeforeUnmount( () => {
  disconnectWebSocket()
});
</script>

<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title class="text-center text-h5">채팅</v-card-title>
          <v-card-text>
            <div class="chat-box" ref="chatBoxRef">
              <div
                  v-for="(msg, index) in messages"
                  :key="index"
                  :class="['chat-message', msg.senderEmail === senderEmail ? 'sent' : 'received']"
              >
                <strong>{{msg.senderEmail}}:</strong> {{msg.message}}
              </div>
            </div>
            <v-text-field
                v-model="newMessage"
                label="메시지 입력"
                @keyup.enter="sendMessage"
            />
            <v-btn color="primary" block @click="sendMessage">전송</v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.chat-box {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  margin-bottom: 10px;
  padding: 10px;
}

.chat-message {
  margin-bottom: 10px;
}

.sent {
  text-align: right;
}

.received {
  text-align: left;
}
</style>
