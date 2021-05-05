<template>
  <div>
    <div style="text-align: right;margin-bottom: 10px;">
      <span style="margin-right:400px;" v-if="runLogRecord">
        流程名称: {{ runLogRecord.work_name }}
        <Button type="success" size="small" style="margin-left: 5px;" @click="viewWorkSteps(runLogRecord.work_id,runLogRecord.work_name)">查看流程详情</Button>
      </span>
      <Button type="error" size="small" @click="highlightError = !highlightError">高亮显示错误</Button>
    </div>

    <Table border :columns="columns1" :data="runLogDetails" :row-class-name="rowClassName" size="small"></Table>
  </div>
</template>

<script>
  import {GetLastRunlogDetail} from "../../../api"

  export default {
    name: "RunlogDetail",
    data(){
      return {
        highlightError:false,
        runLogDetails:[],
        runLogRecord: null,
        columns1: [
          {
            title: '步骤名称',
            key: 'work_step_name',
            width: 250,
          },
          {
            title: '日志编号',
            key: 'log_order',
            width: 100,
            align: 'center',
          },
          {
            title: 'log_level',
            key: 'log_level',
            width:120,
            align: 'center',
            filters: [
              {
                label: 'ERROR',
                value: 'ERROR',
              },
              {
                label: 'ALL',
                value: 'ALL',
              }
            ],
            filterMultiple: false,
            filterMethod (value, row) {
              if (value == "ERROR") {
                return row.log_level == "ERROR";
              }
              return true;
            }
          },
          {
            title: 'detail',
            key: 'detail',
            render: (h,params)=>{
              return h('div',{
                domProps:{
                  innerHTML:params.row.detail,
                }
              })
            }
          },
        ],
      }
    },
    methods:{
      viewWorkSteps:function (id, work_name) {
        this.$router.push({ path: '/iwork/workstepList', query: { work_id: id, work_name: work_name }});
      },
      refreshRunlogDetail: async function () {
        const result = await GetLastRunlogDetail(this.$route.query.tracking_id);
        if(result.status=="SUCCESS"){
          this.runLogRecord = result.runLogRecord;
          this.runLogDetails = result.runLogDetails;
        }
      },
      rowClassName (row, index) {
        if (row.log_level === "ERROR" && this.highlightError) {
          return 'demo-table-error-row';
        }
        return '';
      }
    },
    created () {
      var _this = this;
      // 当需要在 innerHTML 中绑定事件，如： <a onclick='window.showRunLogDetail'>
      // 则可以转换给 window 对象动态绑定事件
      window.showRunLogDetail = function (trackingId) {
        // 新窗口打开
        let routeData = _this.$router.resolve({ path: '/iwork/runLogDetail', query: { tracking_id: trackingId }});
        window.open(routeData.href, '_blank');
      }
    },
    mounted(){
      this.refreshRunlogDetail();
    }
  }
</script>

<style>
  /*
    vue中慎用style的scoped属性
    scoped肯定是解决了样式私有化的问题,但同时也引入了新的问题,scoped设计的初衷就是让样式变得不可修改
  */
  .ivu-table .demo-table-error-row td{
    background-color: pink;
  }
</style>
