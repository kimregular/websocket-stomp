<template>
  <v-app>
    <HeaderComponent/>
    <v-main>
      <router-view/>
    </v-main>
  </v-app>
</template>

<script>

import {defineComponent} from "vue";
import HeaderComponent from "@/components/HeaderComponent.vue";

export default defineComponent({
  components: {HeaderComponent}
})
</script>

<style>

</style>
