<template>
  <span>
    <div style="margin-bottom: 10px;">
      <Button type="primary" size="small" @click="loadLevel('ERROR')">Error</Button>
      <Button type="success" size="small" @click="loadLevel('')">All</Button>
    </div>

    <Table :columns="columns1" :data="runLogRecords" size="small"></Table>
    <Page :total="total" :page-size="offset" show-total show-sizer :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </span>
</template>

<script>
  import {FilterPageLogRecord} from "../../../api/index"
  import {formatDate} from "../../../tools/index"

  export default {
    name: "RunLogList",
    props:{
      workId:{
        type: Number,
        default: -1,
      }
    },
    data(){
      return {
        // 当前页
        current_page:1,
        // 总数
        total:0,
        // 每页记录数
        offset:10,
        runLogRecords: [],
        logLevel:'',
        columns1: [
          {
            title: 'tracking_id',
            key: 'tracking_id',
            width: 300,
          },
          {
            title: 'work_name',
            key: 'work_name',
          },
          {
            title: 'log_level',
            key: 'log_level',
            width: 100,
          },
          {
            title: 'last_updated_time',
            key: 'last_updated_time',
            render: (h,params)=>{
              return h('div',
                formatDate(new Date(params.row.last_updated_time),'yyyy-MM-dd hh:mm')
              )
            }
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
                      this.$router.push({ path: '/iwork/runLogDetail', query: { tracking_id: this.runLogRecords[params.index]['tracking_id'] }});
                    }
                  }
                }, '查看'),
              ]);
            }
          }
        ],
      }
    },
    methods:{
      loadLevel:function(logLevel){
        this.logLevel = logLevel;
        this.refreshRunlogRecordList();
      },
      refreshRunlogRecordList: async function () {
        const result = await FilterPageLogRecord(this.getWorkId(),this.logLevel,this.offset,this.current_page);
        if(result.status=="SUCCESS"){
          this.runLogRecords = result.runLogRecords;
          this.total = result.paginator.totalcount;
        }
      },
      handleChange(page){
        this.current_page = page;
        this.refreshRunlogRecordList();
      },
      handlePageSizeChange(pageSize){
        this.offset = pageSize;
        this.refreshRunlogRecordList();
      },
      getWorkId:function () {
        return this.workId > 0 ? this.workId : this.$route.query.work_id;
      },
    },
    mounted: function () {
      this.refreshRunlogRecordList();
    },
    watch:{
      // 监听路由是否变化
      '$route' (to, from) {
        this.refreshRunlogRecordList();
      }
    },
  }
</script>

<style scoped>

</style>
