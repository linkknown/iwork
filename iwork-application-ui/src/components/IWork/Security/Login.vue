<template>
  <div style="margin-top: 120px;margin-left: auto;margin-right: auto;width: 500px;">
    <input class="focus" name="username" v-model.trim="username" placeholder="账号..." type="text"
           style="width: 100%;height: 40px;margin: 15px 0 0 15px;padding-left: 10px" required @keyup.enter="login"/>
    <input class="focus" name="passwd" v-model.trim="passwd" placeholder="密码..." type="password"
           style="width: 100%;height: 40px;margin: 15px 0 0 15px;padding-left: 10px" autocomplete="new-password"
           required @keyup.enter="login"/>
    <input class="login_button" id="submit" type="submit" value="登    录"
           style="width: 100%;height: 40px;margin: 15px 0 0 15px;padding-left: 10px" @click="login">
  </div>
</template>

<script>
  import {Login} from "../../../api"
  import {checkEmpty} from "../../../tools";

  export default {
    name: "Login",
    data() {
      return {
        username: '',
        passwd: '',
      }
    },
    methods: {
      login: async function () {
        const result = await Login({username: this.username, passwd: this.passwd});
        if (result.status === "SUCCESS") {
          localStorage.setItem("iwork_userName", this.username);
          localStorage.setItem("iwork_passwd", this.passwd);
          localStorage.setItem("iwork_loginExpiredSecond", new Date().getTime() + result.loginExpiredSecond * 1000);
          localStorage.setItem("iwork_tokenString", result.tokenString);
          this.$router.push({path: '/iwork/appidList'});
        } else {
          this.$Message.error("登录失败！请检查用户名和密码！");
        }
      },
    },
    mounted() {
      if (!checkEmpty(localStorage.getItem("iwork_userName")) && !checkEmpty(localStorage.getItem("iwork_passwd"))) {
        // 自动填充密码
        this.username = localStorage.getItem("iwork_userName");
        this.passwd = localStorage.getItem("iwork_passwd");
      }
    }
  }
</script>

<style scoped>
  .focus:focus {
    background-color: #ffffff;
    border-color: #2c5bff;
  }
</style>
