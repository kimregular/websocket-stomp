<script setup>

import {onBeforeMount, ref} from "vue";
import axios from "axios";
import router from "@/router";

const newRoomTitle = ref("");
const chatRoomList = ref()
const showCreateRoomModal = ref(false)
const joinChatRoom = async (roomId) => {
  await axios.post(`${process.env.VUE_APP_API_BASE_URL}/chat/room/group/${roomId}/join`)
  await router.push(`/chatpage/${roomId}`)
}
const createChatRoom = async () => {
  console.log("newRoomTitle", newRoomTitle.value)
  await axios.post(`${process.env.VUE_APP_API_BASE_URL}/chat/room/group/create?roomName=${newRoomTitle.value}`, null);
  showCreateRoomModal.value = false;
  await loadChatRoom();
};
const loadChatRoom = async () => {
  return axios.get(`${process.env.VUE_APP_API_BASE_URL}/chat/room/group/list`);
}
onBeforeMount(async () => {
  const response = await loadChatRoom();
  chatRoomList.value = response.data;
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-card>
          <v-card-title class="text-center text-h5">
            채팅방 목록
            <div class="d-flex justify-end">
              <v-btn color="secondary" @click="showCreateRoomModal = true">채팅방 생성</v-btn>
            </div>
          </v-card-title>
          <v-card-text>
            <v-table>
              <thead>
                <tr>
                  <th>방 번호</th>
                  <th>방 제목</th>
                  <th>채팅</th>
                </tr>
              </thead>
              <tbody>
              <tr v-for="chat in chatRoomList" :key="chat.roomId">
                <th>{{chat.roomId}}</th>
                <th>{{chat.roomName}}</th>

                <td>
                  <v-btn color="primary" @click="joinChatRoom(chat.roomId)">참여하기</v-btn>
                </td>
              </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    <v-dialog v-model="showCreateRoomModal" max-width="500px">
      <v-card>
        <v-card-title class="text-h6">
          채팅방 생성
        </v-card-title>
        <v-card-text>
          <v-text-field label="방제목" v-model="newRoomTitle"/>
        </v-card-text>
        <v-card-actions>
          <v-btn color="grey" @click="showCreateRoomModal = false">취소</v-btn>
          <v-btn color="primary" @click="createChatRoom">생성</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<style scoped>

</style>