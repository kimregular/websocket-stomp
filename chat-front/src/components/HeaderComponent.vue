<script setup>

import {onBeforeMount, ref} from "vue";

let isLogin = ref(false);

const doLogout = () => {
  localStorage.removeItem("token");
  window.location.reload();
}

onBeforeMount(() => {
  const token = localStorage.getItem("token");
  if (token) {
    isLogin = true;
  }
})
</script>

<template>
  <v-app-bar app dark>
    <v-container >
      <v-row align="center">
        <v-col class="d-flex justify-start">
          <v-btn :to="{path:'/member/list'}">회원목록</v-btn>
          <v-btn :to="{path:'/groupchatting/list'}">채팅방 목록</v-btn>
        </v-col>
        <v-col class="text-center">
          <v-btn :to="{path:'/'}">chat 서비스</v-btn>
        </v-col>
        <v-col class="d-flex justify-end">
          <v-btn v-if="isLogin" :to="{path:'/my/chat/page'}">MyChatPage</v-btn>
          <v-btn v-if="!isLogin" :to="{path:'/member/create'}">회원가입</v-btn>
          <v-btn v-if="!isLogin" :to="{path:'/login'}">로그인</v-btn>
          <v-btn v-if="isLogin" @click="doLogout">로그아웃</v-btn>
        </v-col>
      </v-row>
    </v-container>
  </v-app-bar>
</template>

<style scoped>

</style>