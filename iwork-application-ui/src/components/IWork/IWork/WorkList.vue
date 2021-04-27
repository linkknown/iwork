<template>
  <div style="margin: 10px;">
    <Row type="flex" justify="center">
      <Col span="3"><WorkValidate/></Col>
    </Row>
    <ISimpleLeftRightRow style="margin: 10px 10px 10px 0;">
      <!-- left 插槽部分 -->
      <span slot="left">
        <Button type="success" size="small" @click="handleWorkEdit">新增</Button>
        <Button type="warning" size="small" @click="$router.push({ path:'/iwork/filterList'})">过滤器配置</Button>

        <WorkEdit ref="workEdit" @handleSubmitSuccess="refreshWorkList()"></WorkEdit>

      </span>
      <Row slot="right">
        <Col span="4">
          <Poptip trigger="hover" title="根据步骤类型搜索" content="content" :width="500" :word-wrap="true">
            <Button size="small">步骤类型搜索</Button>
            <div slot="content">
              <Tag v-for="(default_work_step_type,index) in nodeMetas">
                <span @click="chooseWorkStepType(default_work_step_type.name)">{{default_work_step_type.name}}</span>
              </Tag>
            </div>
          </Poptip>

        </Col>
        <Col span="20"><ISimpleSearch ref="search" @handleSimpleSearch="handleSearch"/></Col>
      </Row>

    </ISimpleLeftRightRow>

    <Row>
      所有类型：
      <Tag :color="search_work_type === 'all' ? 'success' : 'default'">
        <span @click="filterWorkTypes('all')">all</span>
      </Tag>
      <Tag :color="search_work_type === 'work' ? 'success' : 'default'">
        <span @click="filterWorkTypes('work')">work</span>
      </Tag>
      <Tag :color="search_work_type === 'filter' ? 'success' : 'default'">
        <span @click="filterWorkTypes('filter')">filter</span>
      </Tag>
      <Tag :color="search_work_type === 'quartz' ? 'success' : 'default'">
        <span @click="filterWorkTypes('quartz')">quartz</span>
      </Tag>
    </Row>
    <Row>
      所有模块：
      <span>
        <Tag :color="search_module === 'all' ? 'success' : 'default'">
          <span @click="filterModuleWork('all')">all</span>
        </Tag>
        <span v-for="module in modules">
          <Tag :color="search_module === module.module_name ? 'success' : 'default'">
            <span @click="filterModuleWork(module.module_name)">{{module.module_name}}</span>
          </Tag>
        </span>
      </span>
    </Row>
    <br>
    <Table border :columns="columns1" :data="works" size="small"></Table>
    <Page :total="total" :page-size="offset" show-total show-sizer :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </div>
</template>

<script>
  import {DeleteOrCopyWorkById, FilterPageWorks, GetAllModules, GetMetaInfo, RunWork} from "../../../api"

  const ISimpleLeftRightRow = () => import("@/components/Common/layout/ISimpleLeftRightRow");
  const ISimpleSearch = () => import("@/components/Common/search/ISimpleSearch");
  const WorkValidate = () => import("@/components/IWork/IValidate/WorkValidate");
  const WorkEdit = () => import("@/components/IWork/IWork/WorkEdit");

  export default {
    name: "WorkList",
    components: {ISimpleLeftRightRow, ISimpleSearch, WorkValidate, WorkEdit},
    data(){
      return {
        nodeMetas: [],
        search_work_type:"all",
        search_module:"all",
        // 当前页
        current_page:1,
        // 总数
        total:0,
        // 每页记录数
        offset:10,
        // 搜索条件
        search:"",
        works: [],
        runLogRecordCount:{},
        modules: [],
        columns1: [
          {
            title: '流程名称',
            key: 'work_name',
            width: 250,
            render: (h, params) => {
              return h('div', [
                h('span', {
                  style:{
                    marginRight: '10px',
                    color: 'green',
                    background: '#e4e4e4',
                    display: 'inline-block',
                    textAlign: 'center',
                    fontWeight: 'bold',
                    width: '40px',
                  }
                }, this.works[params.index].work_type),
                h('span', this.works[params.index].work_name),
              ]);
            }
          },
          {
            title: '模块名称',
            key: 'module_name',
            align: 'center',
            width: 120,
          },
          {
            title: '失败次数/总次数',
            key: 'error/total',
            align: 'center',
            width: 130,
            render: (h, params) => {
              return h('div', [
                h('span', {
                  style:{
                    color: 'red',
                  }
                }, this.getErrorOrTotalCount(this.works[params.index].id, 'error')),
                h('span', this.getErrorOrTotalCount(this.works[params.index].id, 'total')),
              ]);
            }
          },
          {
            title: '流程描述',
            key: 'work_desc',
            width: 400,
          },
          {
            title: '定时任务表达式',
            key: 'work_cron',
            width: 150,
          },
          {
            title: '操作',
            key: 'operate',
            width: 450,
            fixed: 'right',
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
                      this.$refs.workEdit.initData(this.works[params.index]);
                    }
                  }
                }, '编辑'),
                h('Button', {
                  props: {
                    type: 'info',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.deleteOrCopyWorkById('copy', this.works[params.index]['id']);
                    }
                  }
                }, '复制'),
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
                      var _this = this;
                      _this.$Modal.confirm({
                        title: '删除',
                        content: '确认删除该条数据吗？请谨慎操作！',
                        onOk: () => {
                          _this.deleteOrCopyWorkById('delete', _this.works[params.index]['id']);
                        },
                        onCancel: () => {
                          _this.$Message.info('取消操作');
                        }
                      });
                    }
                  }
                }, '删除'),
                h('Button', {
                  props: {
                    type: 'info',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.download(this.works[params.index]['id']);
                    }
                  }
                }, '下载'),
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
                      this.editWorkSteps(this.works[params.index]['id'], this.works[params.index]['work_name']);
                    }
                  }
                }, '编辑步骤'),
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
                      this.runWork(this.works[params.index]['id']);
                    }
                  }
                }, '运行流程'),
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
                      this.$router.push({ path: '/iwork/runLogList', query: { work_id: this.works[params.index]['id'] }});
                    }
                  }
                }, '运行日志'),
              ]);
            }
          },
        ],
      }
    },
    methods:{
      handleWorkEdit: function () {
        this.$refs.workEdit.initData(null);
      },
      chooseWorkStepType:function(work_type){
        this.$refs.search.initSearchText(work_type);
        this.search = work_type;
        this.refreshWorkList();
      },
      filterWorkTypes:function(work_type){
        this.search_work_type = work_type;
        this.refreshWorkList();
      },
      filterModuleWork:function(module_name){
        this.search_module = module_name;
        this.refreshWorkList();
      },
      refreshWorkList:async function () {
        const result = await FilterPageWorks(this.offset,this.current_page,this.search,this.search_work_type,this.search_module);
        if (result.status === "SUCCESS") {
          this.works = result.works;
          this.total = result.paginator.totalcount;
          this.runLogRecordCount = result.runLogRecordCount;
        }
      },
      handleChange(page){
        this.current_page = page;
        this.refreshWorkList();
      },
      handlePageSizeChange(pageSize){
        this.offset = pageSize;
        this.refreshWorkList();
      },
      handleSearch(data){
        this.offset = 10;
        this.current_page = 1;
        this.search = data;
        this.refreshWorkList();
      },
      download:function(id){
        window.location.href = "/api/iwork/download/" + id;
      },
      deleteOrCopyWorkById:async function(operate, id){
        const result = await DeleteOrCopyWorkById(operate, id);
        if (result.status === "SUCCESS") {
          this.refreshWorkList();
        }
      },
      runWork:async function (work_id) {
        const result = await RunWork(work_id);
        if (result.status === "SUCCESS") {
          this.$Message.success("运行任务已触发!");
        }
      },
      editWorkSteps:function (id, work_name) {
        this.$router.push({ path: '/iwork/workstepList', query: { work_id: id, work_name: work_name }});
      },
      getErrorOrTotalCount:function (workId, flag) {
        // 此处统一转换成字符串进行比较, 而不是 int 和 字符串比较
        var key = Object.keys(this.runLogRecordCount).filter(_key => _key  + "" === workId + "")[0];
        if (key && this.runLogRecordCount[key]) {
          return flag === "error" ? this.runLogRecordCount[key].errorCount : "/" + this.runLogRecordCount[key].allCount;
        }
      },
      refreshAllModules:async function(){
        const result = await GetAllModules();
        if (result.status === "SUCCESS") {
          this.modules = result.moudles;
        }
      },
      refreshNodeMetas:async function () {
        const result = await GetMetaInfo("nodeMetas");
        if (result.status === "SUCCESS") {
          this.nodeMetas = result.nodeMetas;
        }
      }
    },
    mounted: function () {
      if(this.$route.query.work_name != null){
        this.search = this.$route.query.work_name;
      }
      this.refreshWorkList();
      this.refreshAllModules();
      this.refreshNodeMetas();
    },
  }
</script>

<style scoped>

</style>
