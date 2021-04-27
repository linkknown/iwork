<template>
  <div>

    <ISimpleLeftRightRow style="margin-bottom: 10px;margin-right: 10px;">
      <!-- left 插槽部分 -->
      <span slot="left">
        <Button type="success" size="small" @click="addGlobalVar">新增</Button>
        <Button type="warning" size="small" @click="viewPattern = !viewPattern">大纲</Button>
        <span style="margin-left: 200px;color: #eb7d37;font-size: 14px;">
          当前正在使用的环境:<b style="margin-left: 10px;color: #cc0000">{{onuse}}</b>
        </span>
        <GlobalVarEdit ref="GlobalVarEdit" :env-name-list="EnvNameList" @handleSubmit="refreshGlobalVarList"/>
      </span>
      <!-- right 插槽部分 -->
      <ISimpleSearch slot="right" @handleSimpleSearch="handleSearch"/>
    </ISimpleLeftRightRow>

    <table v-if="viewPattern" cellspacing="0">
      <tr>
        <th>变量名称</th>
        <th v-for="(env_name, index) in env_names">{{env_name}}</th>
      </tr>
      <tr v-for="(name, index) in names">
        <td>{{name}}</td>
        <td v-for="(env_name, index2) in env_names">
          <span v-html="renderShowText(name, env_name)"></span>
        </td>
      </tr>
    </table>

    <Table v-else border :columns="columns1" :data="globalVars" size="small"></Table>
  </div>
</template>

<script>
  import {DeleteGlobalVarById, GlobalVarList, queryEvnNameList} from "../../../api"
  import ISimpleLeftRightRow from "../../Common/layout/ISimpleLeftRightRow"
  import ISimpleSearch from "../../Common/search/ISimpleSearch"
  import IKeyValueForm from "../../Common/form/IKeyValueForm"
  import GlobalVarEdit from "./GlobalVarEdit";

  export default {
    name: "GlobalVarList",
    components: {GlobalVarEdit, ISimpleLeftRightRow, ISimpleSearch, IKeyValueForm},
    data(){
      return {
        search:"",
        globalVars: [],
        EnvNameList:[],
        onuse:'',
        columns1: [
          {
            title: 'name',
            key: 'name',
            width: 200,
          },
          {
            title:'env_name',
            key:'env_name',
            width:100,
          },
          {
            title: 'value',
            key: 'value',
            width:450,
          },
          {
            title: '操作',
            key: 'operate',
            render: (h, params) => {
              return h('div', [
                h('Button', {
                  props: {
                    type: 'success',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.$refs.GlobalVarEdit.initFormData(this.globalVars[params.index]);
                    }
                  }
                }, '编辑'),
                h('Button', {
                  props: {
                    type: 'warning',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      let gv = this.globalVars[params.index];
                      gv.id = 0;
                      this.$refs.GlobalVarEdit.initFormData(gv);
                    }
                  }
                }, '拷贝'),
                h('Button', {
                  props: {
                    type: 'error',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.deleteGlobalVar(this.globalVars[params.index].id);
                    }
                  }
                }, '删除'),
              ]);
            }
          }
        ],
        names: [],
        env_names: [],
        viewPattern: false,
      }
    },
    methods:{
      queryEvnNameList:async function(){
        const result = await queryEvnNameList();
        if(result.status == "SUCCESS"){
          this.EnvNameList = result.EnvNameList;
        }else{
          this.$Message.error("查询环境EnvNameList失败！");
        }
      },
      addGlobalVar(){
        this.$refs.GlobalVarEdit.initFormData();
      },
      handleSearch(data){
        this.search = data;
        this.refreshGlobalVarList();
      },
      refreshGlobalVarList:async function () {
        const result = await GlobalVarList(this.search);
        if(result.status=="SUCCESS"){
          this.onuse = result.onuse;
          this.globalVars = result.globalVars;
          this.renderShow();
        }
      },
      deleteGlobalVar:async function (id){
        const result = await DeleteGlobalVarById(id);
        if(result.status == "SUCCESS"){
          this.refreshGlobalVarList();
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      renderShow: function () {
        let names = Array.from(new Set(this.globalVars.map(gv => gv.name)));
        let env_names = Array.from(new Set(this.globalVars.map(gv => gv.env_name)));
        this.names = names;
        this.env_names = env_names;
      },
      renderShowText: function (name, env_name) {
        return this.globalVars.filter(gv => gv.name === name && gv.env_name === env_name).length > 0 ? "Y" : "N";
      }
    },
    mounted: function () {
      this.refreshGlobalVarList();
      this.queryEvnNameList();
    },
  }
</script>

<style scoped>
  td {
    padding: 10px 20px;
  }
</style>
