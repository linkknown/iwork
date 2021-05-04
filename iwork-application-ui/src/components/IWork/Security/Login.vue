<template>
  <div class="loginDiv">
    <div class="loginBox">
      <h3 class="title">低代码开发管理平台</h3>
      <input class="focus" name="userName" v-model.trim="userName" placeholder="请输入账号..." type="text" required @keyup.enter="login"/>
      <input class="focus" name="passwd" v-model.trim="passwd" placeholder="请输入密码..." type="password" autocomplete="new-password" required @keyup.enter="login"/>
      <div class="verifyCodeArea">
        <input class="focus verifyCode" name="verifyCode" v-model.trim="verifyCode" placeholder="验证码..." type="text" required @keyup.enter="login"/>
        <a @click="genVerifyCode">
          <img class="verifyCodeImg" title="点击刷新验证码" :src="verifyCodeUrl"/>
        </a>
      </div>
      <input class="loginButton" id="submit" type="submit" value="登    录" @click="login">
    </div>

    <div class="copyRight">Copyright © 2019-2021 LinkKnown All Rights Reserved.</div>
  </div>
</template>

<script>
  import {Login, GenVerifyCode} from "../../../api"
  import {checkEmpty} from "../../../tools";

  export default {
    name: "Login",
    data() {
      return {
        userName: '',
        passwd: '',
        verifyCode: '',
        verifyCodeUrl: '',
      }
    },
    methods: {
      genVerifyCode: async function () {
        const result = await GenVerifyCode ();
        if (result.status === "SUCCESS") {
          this.verifyCodeUrl = result.img;
          console.log(this.verifyCodeUrl);
        }
      },
      login: async function () {
        const result = await Login({userName: this.userName, passwd: this.passwd, verifyCode: this.verifyCode});
        if (result.status === "SUCCESS") {
          localStorage.setItem("iwork_userName", this.userName);
          localStorage.setItem("iwork_passwd", this.passwd);
          localStorage.setItem("iwork_loginExpiredSecond", new Date().getTime() + result.loginExpiredSecond * 1000);
          localStorage.setItem("iwork_tokenString", result.tokenString);
          this.$router.push({path: '/iwork/appidList'});
        } else {
          // 登陆失败重置验证码
          this.genVerifyCode();

          this.$Message.error("登录失败！" + result.errorMsg);
        }
      },
    },
    mounted() {
      if (!checkEmpty(localStorage.getItem("iwork_userName")) && !checkEmpty(localStorage.getItem("iwork_passwd"))) {
        // 自动填充密码
        this.userName = localStorage.getItem("iwork_userName");
        this.passwd = localStorage.getItem("iwork_passwd");
      }
      this.genVerifyCode();
    }
  }
</script>

<style scoped>
  .loginDiv {
    height: 100vh;
    background-image: url(../../../assets/images/login_bg.jpg);
    background-size: cover;
  }
  .loginBox {
    border-radius: 6px;
    background: #ffffff;
    width: 450px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
    padding: 25px;
  }
  .title {
    margin: 0px auto 30px auto;
    text-align: center;
    color: #707070;
    font-size: 18px;
  }
  input {
    border: 1px solid #d3d3d3;
  }
  .focus {
    display: block;
    width: 100%;
    padding-left: 15px;
    height: 40px;
    margin-top: 15px;
  }
  .focus:focus {
    background-color: #ffffff;
  }
  .verifyCodeArea {
    display: flex;
    align-items: flex-start;
  }
  .verifyCode {
    flex-grow: 1;
    margin-right: 10px;
  }
  .verifyCodeImg {
    margin-top: 15px;
  }
  .loginButton {
    height: 40px;
    margin-top: 15px;
    width: 100%;
    background: #4d89bd;
    color: #fff;
    outline: none;
    font-size: 16px;
  }
  .loginButton:hover {
    cursor: pointer;
    background-color: #2e72bd;
  }
  .copyRight {
    position: absolute;
    width: 100%;
    bottom: 50px;
    height: 25px;
    line-height: 25px;
    text-align: center;
    color: white;
    font-size: 14px;
  }
</style>
