<template>
  <Drawer title="全部组件" placement="left" :closable="true" :mask="false" scrollable v-model="showComponentDrawer">
    <Tabs value="tabPane1">
      <TabPane label="组件" name="tabPane1">
        <div class="searchTypeBox">
          <span :class="{searchTypeChoosed:!showAllNodeGroup,searchType:showAllNodeGroup}" @click="showAllNodeGroup = false">分类</span>
          <span :class="{searchTypeChoosed:showAllNodeGroup,searchType:!showAllNodeGroup}" @click="showAllNodeGroup = true">全部</span>
        </div>

        <div v-if="showAllNodeGroup">
          <span v-for="default_work_step_type in nodeMetas"
                draggable="true" @dragstart="dragstart($event, 'work_type__' + default_work_step_type.name)">
           <Tag v-if="showComponent(default_work_step_type.name)" title="描述信息待完善">{{default_work_step_type.name}}</Tag>
          </span>
        </div>
        <Collapse v-else>
          <Panel v-for="nodeGroup in nodeGroups" :name="nodeGroup">
            {{nodeGroup}}&nbsp;&nbsp;组
            <div slot="content">
              <div v-if="default_work_step_type.group === nodeGroup" v-for="default_work_step_type in nodeMetas"
                   draggable="true" @dragstart="dragstart($event, 'work_type__' + default_work_step_type.name)">
                <Tag v-if="showComponent(default_work_step_type.name)" title="描述信息待完善">{{default_work_step_type.name}}</Tag>
              </div>
            </div>
          </Panel>
        </Collapse>
      </TabPane>

      <TabPane label="流程" name="tabPane2">
         <div class="searchTypeBox">
           <span :class="{searchTypeChoosed:!showAllModuleName,searchType:showAllModuleName}"  @click="showAllModuleName = false">分类</span>
           <span :class="{searchTypeChoosed:showAllModuleName,searchType:!showAllModuleName}"  @click="showAllModuleName = true">全部</span>
         </div>

        <div v-if="showAllModuleName">
          <div v-for="work in works"
               draggable="true" @dragstart="dragstart($event, 'work_name__' + work.work_name)">
            <Tag><span @click="$router.push({path:'/iwork/workList',query:{work_name:work.work_name}})">{{ work.work_name }}</span></Tag>
          </div>
        </div>

        <Collapse v-else>
          <Panel v-for="moduleName in moduleNames" :name="moduleName">
            {{moduleName}}&nbsp;&nbsp;模块
            <div slot="content">
              <div v-if="work.module_name === moduleName" v-for="work in works"
                   draggable="true" @dragstart="dragstart($event, 'work_name__' + work.work_name)">
                <Tag><span @click="$router.push({path:'/iwork/workList',query:{work_name:work.work_name}})">{{ work.work_name }}</span></Tag>
              </div>
            </div>
          </Panel>
        </Collapse>
      </TabPane>
    </Tabs>
  </Drawer>
</template>

<script>
  import {GetAllFiltersAndWorks, GetMetaInfo} from "../../../api"
  import {oneOf} from "../../../tools"

  export default {
    name: "WorkStepComponent",
    data(){
      return {
        showComponentDrawer:false,
        nodeMetas: [],
        works:[],
        moduleNames:[],
        showAllModuleName: false,
        nodeGroups: [],       // 组件（节点）所属组
        showAllNodeGroup: false,
      }
    },
    methods:{
      showComponent:function(name){
        return !oneOf(name, ['work_start',"work_end"]);   // 开始和结束节点不能添加和拖拽
      },
      dragstart:function(event, transferData){
        event.dataTransfer.setData("Text", transferData);
      },
      toggleShow:function () {
        this.showComponentDrawer = !this.showComponentDrawer;
      },
      refreshAllWorks:async function () {
        const result = await GetAllFiltersAndWorks();
        if(result.status === "SUCCESS"){
          this.works = result.works;
          this.moduleNames = this.getUniqueArr(this.works.map(work => work.module_name));
        }
      },
      getUniqueArr: function(arr){
        var x = new Set(arr);
        return [...x];
      },
      refreshNodeMetas:async function () {
        const result = await GetMetaInfo("nodeMetas");
        if(result.status === "SUCCESS"){
          this.nodeMetas = result.nodeMetas;
          this.nodeGroups = this.getUniqueArr(result.nodeMetas.map(nodeMeta => nodeMeta.group));
        }
      }
    },
    mounted(){
      // 加载所有的流程
      this.refreshAllWorks();
      // 加载所有的组件（节点）
      this.refreshNodeMetas();
    }
  }
</script>

<style scoped>
  .searchTypeBox{
    text-align: right;
    padding: 0 10px 10px 10px;
  }
  .searchType {
    cursor: pointer;
    background-color: rgba(0,0,0,.7);
    color: white;
    border-radius: 5px;
    padding: 3px 10px;
  }
  .searchTypeChoosed {
    cursor: pointer;
    background-color: rgba(232, 0, 0, 0.7);
    color: white;
    border-radius: 5px;
    padding: 3px 10px;
  }
</style>
