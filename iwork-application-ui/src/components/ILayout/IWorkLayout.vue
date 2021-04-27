<template>
  <div class="layout">
    <Layout>
      <Header>
        <Menu mode="horizontal" theme="dark" active-name="1">
          <div class="layout-iwork">
            <Row>
              <Col>
                <div>
                   <img src="../../assets/images/linkknown.jpg" class="linkknownlogo" @click="tolinkknown" title="API Market 低代码开发平台"/>
                </div>
              </Col>
              <Col>
                <div style="margin-left: 10px;">IWork {{version}}</div>
              </Col>
            </Row>
          </div>
          <div class="layout-nav">
            <MenuItem name="1">
              <Icon type="ios-navigate"></Icon>
              使用说明
            </MenuItem>
            <MenuItem name="2">
              <Icon type="ios-keypad"></Icon>
              用户协议
            </MenuItem>
            <MenuItem name="3">
              <Icon type="ios-analytics"></Icon>
              License管理
            </MenuItem>
            <MenuItem name="4">
              <Icon type="ios-paper"></Icon>
              <span @click="saveProject">保存项目</span>
            </MenuItem>
            <MenuItem name="4">
              <Icon type="ios-paper"></Icon>
              <span @click="importProject">导入项目</span>
            </MenuItem>
            <MenuItem name="5">
              <Icon type="ios-paper"></Icon>
              <span @click="toggleAppid">切换 AppID <span v-if="appId">{{appId.app_name}}</span></span>
            </MenuItem>
          </div>
        </Menu>
      </Header>
      <Layout>
        <Sider hide-trigger :style="{background: '#fff'}">
          <Menu active-name="3-2" theme="light" width="auto" :open-names="['3']" accordion>
            <Submenu name="1">
              <template slot="title">
                <Icon type="ios-keypad"></Icon>
                AppID管理
              </template>
              <MenuItem name="1-1">
                <router-link to="/iwork/appidList">AppID管理</router-link>
              </MenuItem>
            </Submenu>
            <Submenu name="2">
              <template slot="title">
                <Icon type="ios-keypad"></Icon>
                资源管理
              </template>
              <MenuItem name="2-1"><router-link to="/iwork/resourceList">连接资源管理</router-link></MenuItem>
              <MenuItem name="2-2"><router-link to="/iwork/migrateList">数据库迁移管理</router-link></MenuItem>
              <MenuItem name="2-3">
                <router-link to="/iwork/globalVarList">全局变量管理</router-link>
              </MenuItem>
            </Submenu>
            <Submenu name="3">
              <template slot="title">
                <Icon type="ios-navigate"></Icon>
                流程管理
              </template>
              <MenuItem name="3-1">
                <router-link to="/iwork/moduleList">模块管理</router-link>
              </MenuItem>
              <MenuItem name="3-2">
                <router-link to="/iwork/workList">流程列表</router-link>
              </MenuItem>
              <MenuItem name="3-3">
                <router-link to="/iwork/filterList">过滤器管理</router-link>
              </MenuItem>
            </Submenu>
            <Submenu name="4">
              <template slot="title">
                <Icon type="ios-barcode"></Icon>
                日志管理
              </template>
              <MenuItem name="4-1">
                <router-link to="/iwork/runLogList">日志列表</router-link>
              </MenuItem>
              <MenuItem name="4-2">
                <router-link to="/iwork/dashboard">统计仪表盘</router-link>
              </MenuItem>
            </Submenu>
            <Submenu name="5">
              <template slot="title">
                <Icon type="ios-barcode"></Icon>
                帮助助手
              </template>
              <MenuItem name="5-1">
                <router-link to="/iwork/quickSql">快捷sql</router-link>
              </MenuItem>
            </Submenu>
          </Menu>
        </Sider>
        <Layout :style="{padding: '24px'}">
          <Content :style="{padding: '24px', minHeight: '90vh', background: '#fff'}">
            <router-view/>
          </Content>
        </Layout>
      </Layout>
    </Layout>
  </div>
</template>

<script>
  import {ImportProject, SaveProject} from "../../api"

  export default {
    name: "IWorkLayout",
    data(){
      return {
        appId: null,
        version: '1.0.4',
      }
    },
    methods:{
      tolinkknown: function (){
        // 打开新窗口跳转
        window.open("http://www.linkknown.com/resource/resourceList", '_blank');
      },
      saveProject:async function () {
        const result = await SaveProject();
        if(result.status === "SUCCESS"){
          this.$Message.success("保存成功!");
        }
      },
      importProject:async function () {
        const result = await ImportProject();
        if(result.status === "SUCCESS"){
          this.$Message.success("导入成功!");
        }
      },
      toggleAppid: function () {
        this.$router.push({path: "/iwork/appidList"});
      }
    },
    mounted() {
      let appId = localStorage.getItem("iwork_appId");
      if (this.appId === null && appId != null && appId !== undefined) {
        this.appId = JSON.parse(appId);
        this.$Message.success("已选择 AppID " + this.appId.app_name);
      }
    }
  }
</script>

<style scoped>
  .layout{
    border: 1px solid #d7dde4;
    background: #f5f7f9;
    position: relative;
    border-radius: 4px;
    overflow: hidden;
  }

  .layout-iwork {
    width: 100px;
    height: 30px;
    color: white;
    cursor: pointer;
    font-size: 14px;
    line-height: 65px;
    float: left;
  }
  .layout-nav{
    width: 920px;
    float: right;
  }

  .linkknownlogo {
    position: absolute;
    width: 40px;
    height: 40px;
    left: -40px;
    top: 10px;
  }
</style>
