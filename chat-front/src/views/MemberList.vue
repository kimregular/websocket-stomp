<script setup>

import {onBeforeMount, ref} from "vue";
import axios from "axios";
import router from "@/router";

const memberList = ref()

const startChat = async (memberId) => {
  // 기존 채팅방이 있으면 return 받고 없으면 새로 생성
  const response = await axios.post(`${process.env.VUE_APP_API_BASE_URL}/chat/room/private/create?otherMemberId=${memberId}`);
  const roomId = response.data;
  router.push(`/chatpage/${roomId}`);
}
onBeforeMount(async () => {
  const response = await axios.get(`${process.env.VUE_APP_API_BASE_URL}/member/list`);
  memberList.value = response.data;
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-card>
          <v-card-title class="text-center text-h5">
            회원목록
          </v-card-title>
          <v-card-text>
            <v-table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Chat</th>
                </tr>
              </thead>
              <tbody>
              <tr v-for="member in memberList" :key="member.id">
                <th>{{member.id}}</th>
                <th>{{member.name}}</th>
                <th>{{member.email}}</th>
                <td>
                  <v-btn color="primary" @click="startChat(member.id)">채팅하기</v-btn>
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