<script setup>

import {ref} from "vue";
import axios from 'axios';
import router from "@/router";
import {jwtDecode} from "jwt-decode";

const memberLogin = async () => {
  const data = {
    email : email.value,
    password : password.value,
  };

  const response = await axios.post(`${process.env.VUE_APP_API_BASE_URL}/member/doLogin`, data);

  const token = response.data.token;
  if (token) {
    const role = jwtDecode(token).role;
    const email = jwtDecode(token).sub;
    localStorage.setItem("token", token);
    localStorage.setItem("role", role);
    localStorage.setItem("email", email);
    window.location.href = "/";
  } else {
    await router.push("/login");
  }
}

const email = ref();
const password = ref();
</script>

<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" sm="4" md="6">
        <v-card>
          <v-card-title class="text-h5 text-center">로그인</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="memberLogin">
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
              <v-btn type="submit" color="primary" block>로그인</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>

</style>