<script setup>
import {onBeforeMount, ref, nextTick, onBeforeUnmount} from "vue";

const ws = ref(null);
const messages = ref([]);
const newMessage = ref("");
const chatBoxRef = ref(null);

// WebSocket 연결
const connectWebsocket = () => {
  if (ws.value) return; // 중복 연결 방지

  ws.value = new WebSocket("ws://localhost:8080/connect");

  ws.value.onopen = () => {
    console.log("connected");
  };

  ws.value.onmessage = (e) => {
    messages.value.push(e.data);
    scrollToBottom();
  };

  ws.value.onerror = (e) => {
    console.error("WebSocket error:", e);
  };

  ws.value.onclose = () => {
    console.log("disconnected");
    ws.value = null;
  };
};

// 메시지 전송
const sendMessage = () => {
  if (!newMessage.value.trim()) {
    newMessage.value = "";
    return;
  }

  ws.value?.send(newMessage.value);
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

// 컴포넌트 마운트 시 WebSocket 연결
onBeforeMount(connectWebsocket);

// 연결 종료 시 클린업
onBeforeUnmount(() => {
  ws.value?.close();
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
              >
                {{ msg }}
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
</style>
