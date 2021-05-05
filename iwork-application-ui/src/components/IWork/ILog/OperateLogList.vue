<template>
  <div>
    <Table :columns="columns1" :data="operateLogs" size="small"></Table>
    <Page :total="total" :page-size="offset" show-total show-sizer :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </div>
</template>

<script>
  import {FilterPageOperateLog} from "../../../api"
  import {formatDate} from "../../../tools/index"

  export default {
    name: "OperateLogList",
    data () {
      return {
        // 当前页
        current_page:1,
        // 总数
        total:0,
        // 每页记录数
        offset:10,
        operateLogs: [],
        columns1: [
          {
            title: '登陆用户',
            key: 'user_name',
            width: 200,
          },
          {
            title: 'ip地址',
            key: 'ip_addr',
            width: 200,
          },
          {
            title: '操作日志',
            key: 'log',
          },
          {
            title: '操作时间',
            key: 'created_time',
            width: 200,
            render: (h,params)=>{
              return h('div',
                formatDate(new Date(params.row.created_time),'yyyy-MM-dd hh:mm')
              )
            }
          },
        ],
      }
    },
    methods: {
      handleChange(page){
        this.current_page = page;
        this.refreshOperateLogList();
      },
      handlePageSizeChange(pageSize){
        this.offset = pageSize;
        this.refreshOperateLogList();
      },
      refreshOperateLogList: async function () {
        const result = await FilterPageOperateLog({offset: this.offset, current_page: this.current_page});
        if(result.status === "SUCCESS"){
          this.operateLogs = result.operateLogs;
          this.total = result.paginator.totalcount;
        }
      }
    },
    mounted () {
      this.refreshOperateLogList();
    }
  }
</script>

<style scoped>

</style>
