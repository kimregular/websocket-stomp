<script setup>

import {onBeforeMount, ref} from "vue";
import axios from "axios";
import router from "@/router";
const chatList = ref([]);

// 채팅방 입장
const enterChatRoom = async (roomId) => {
  await router.push(`/chatpage/${roomId}`);
};

// 채팅방 나가기
const leaveChatRoom = async (roomId) => {
  await axios.delete(`${process.env.VUE_APP_API_BASE_URL}/chat/room/group/${roomId}/leave`);
  chatList.value = chatList.value.filter(chat => chat.roomId !== roomId);
};

// SSE 구독 및 이벤트 수신

// 초기 데이터 + SSE 연결
onBeforeMount(async () => {
  const response = await axios.get(`${process.env.VUE_APP_API_BASE_URL}/chat/my/rooms`);
  chatList.value = response.data;
});

// 페이지 떠날 때 SSE 연결 종료
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-card>
          <v-card-title class="text-center text-h5">
            내 채팅 목록
          </v-card-title>
          <v-card-text>
            <v-table>
              <thead>
              <tr>
                <th>채팅방 이름</th>
                <th>읽지 않은 메시지</th>
                <th>액션</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="chat in chatList" :key="chat.roomId">
                <th>{{ chat.roomName }}</th>
                <th>{{ chat.unReadCount }}</th>
                <td>
                  <v-btn color="primary" @click="enterChatRoom(chat.roomId)">입장</v-btn>
                  <v-btn style="margin-left: 5px" color="secondary" :disabled="chat.isGroupChat === 'N'"
                         @click="leaveChatRoom(chat.roomId)">나가기
                  </v-btn>
                </td>
              </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>

</style>