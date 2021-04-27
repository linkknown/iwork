// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'
// 引用全局静态数据
import global_ from './components/GlobalData' //引用文件
// 使用 iview
import iView from 'iview'
import 'iview/dist/styles/iview.css'
// 使用 vue-markdown
import mavonEditor from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
import {checkEmpty} from "./tools";

router.beforeEach((to, from, next) => {
  // 登录参数拦截
  var expiredTime = localStorage.getItem("iwork_loginExpiredSecond");
  if (checkEmpty(localStorage.getItem("iwork_userName")) || checkEmpty(localStorage.getItem("iwork_passwd"))
    || !(expiredTime != null && new Date().getTime() < expiredTime)) {
    if (to.path.indexOf("/security/login") === -1) {
      next("/security/login");
      return;
    }
  }
  if (to.path.indexOf("/iwork/appidList") >= 0 || to.path.indexOf("/security/login") >= 0) {
    next();
    return;
  }
  let appId = localStorage.getItem("iwork_appId");
  if (appId == null || appId === undefined) {
    alert("请先选择 AppID");
    next("/iwork/appidList");
  } else {
    next();
  }
})

Vue.prototype.GLOBAL = global_                    //挂载到Vue实例上面,通过 this.GLOBAL.xxx 访问全局变量
Vue.use(iView);

Vue.use(mavonEditor)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>',
  store, // 使用上vuex
});
