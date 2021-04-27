<template>
  <div>
    <a>数据库数据量监控</a>
    <a>iwork 框架健康检查</a>

    <Tag v-for="(meta, index) in monitorMeta1"><span
      @click="clickMeta(meta[0], meta[1])">{{meta[0]}} - {{meta[1]}}</span></Tag>

    <div v-for="(meta2, index) in filterMonitors">
      <DBDatacount :app_id="meta2[0]" :resource_name="meta2[1]" :table-name="meta2[2]"
                   :key="meta2[0] + meta2[1] + meta2[2]"/>
    </div>
  </div>
</template>

<script>
  import {GetDBMonitorMeta} from "../../../api"
  import DBDatacount from "./DBDatacount";

  export default {
    name: "DashBoard",
    components: {DBDatacount},
    data() {
      return {
        monitorMeta1: [],
        monitorMeta2: [],
        filterMonitors: [],
        app_id: null,
        resource_name: null,
      }
    },
    methods: {
      clickMeta: function (app_id, resource_name) {
        this.app_id = app_id;
        this.resource_name = resource_name;
        this.filterMonitors = this.monitorMeta2.filter(monitor => monitor[0] === app_id && monitor[1] === resource_name);
      },
      refreshDBMonitor: async function () {
        const result = await GetDBMonitorMeta();
        if (result.status === "SUCCESS") {
          this.monitorMeta1 = result.monitorMeta1;
          this.monitorMeta2 = result.monitorMeta2;
        }
      },
    },
    mounted() {
      this.refreshDBMonitor();
    }
  }
</script>

<style scoped>

</style>
