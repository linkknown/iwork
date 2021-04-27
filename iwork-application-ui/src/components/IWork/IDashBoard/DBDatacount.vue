<template>
  <div v-if="tableName">
    <span style="color: red;">{{tableName}}</span>
    <div v-for="(monitor, index) in monitors">{{monitor.table_name}} - {{monitor.data_count}} -
      {{monitor.last_updated_time}}
    </div>
  </div>
</template>

<script>
  import {LoadDBMonitorData} from "../../../api"

  export default {
    name: "DBDatacount",
    props: {
      tableName: {
        type: String,
        default: '',
      },
      app_id: {
        type: Number,
        default: 0,
      },
      resource_name: {
        type: String,
        default: '',
      }
    },
    data() {
      return {
        monitors: [],
      }
    },
    methods: {
      loadDBMonitorData: async function () {
        // _app_id 防止和全局拦截器中的 app_id 冲突
        const result = await LoadDBMonitorData({
          '_app_id': this.app_id,
          'resource_name': this.resource_name,
          'table_name': this.tableName
        });
        if (result.status === "SUCCESS") {
          this.monitors = result.monitors;
        }
      },
    },
    mounted() {
      this.loadDBMonitorData();
    }
  }
</script>

<style scoped>

</style>
