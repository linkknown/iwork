<template>
  <div>
    切换数据源：
    <Select v-model="choose_resource_link" style="width:400px" @on-change="chooseResource">
      <Option v-for="resource in resources" :value="resource.resource_link" :key="resource.resource_link">
        {{resource.resource_name}} ~ {{resource.resource_link}}
      </Option>
    </Select>
    <QuickResource v-if="currentResource" :resource="currentResource"/>
  </div>
</template>

<script>
  import {GetAllResource} from "../../../api"
  import QuickResource from "./QuickResource"

  export default {
    name: "QuickSql",
    components:{QuickResource},
    data(){
      return {
        resources:[],
        currentResource:null,
        choose_resource_link:'',
      }
    },
    methods:{
      refreshAllResource:async function () {
        const result = await GetAllResource("db");
        if (result.status === "SUCCESS") {
          this.resources = result.resources;
        }
      },
      chooseResource:function () {
        this.currentResource = this.resources.filter(resource => resource.resource_link === this.choose_resource_link)[0];
      }
    },
    mounted:function () {
      this.refreshAllResource();
    }
  }
</script>

<style scoped>

</style>
