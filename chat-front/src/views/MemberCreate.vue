<script setup>

import {ref} from "vue";
import axios from 'axios';
import router from "@/router";

const memberCreate = async () => {
  const data = {
    name : name.value,
    email : email.value,
    password : password.value,
  };
  await axios.post(`${process.env.VUE_APP_API_BASE_URL}/member/save`, data);
  await router.push({path: "/"});
}
const name = ref();
const email = ref();
const password = ref();
</script>

<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" sm="4" md="6">
        <v-card>
          <v-card-title class="text-h5 text-center">회원가입</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="memberCreate">
              <v-text-field
                  label="이름"
                  v-model=name
                  required
              >
              </v-text-field>
              <v-text-field
                  label="이메일"
                  v-model=email
                  type="email"
                  required
              >
              </v-text-field>
              <v-text-field
                  label="password"
                  v-model=password
                  type="password"
                  required
              >
              </v-text-field>
              <v-btn type="submit" color="primary" block>등록</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>

</style>