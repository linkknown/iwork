<template>
  <div>
    <div style="margin-bottom: 5px;">
      <AppidEdit ref="appid_edit" @handleSuccess="refreshAppIdList"/>
    </div>

    <Table border :columns="columns1" :data="appids" size="small"></Table>
    <Page :total="total" :page-size="offset" show-total show-sizer
          :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </div>
</template>

<script>
  import {formatDate} from "../../../tools/index"
  import {DeleteAppid, QueryPageAppIdList} from "../../../api"
  import AppidEdit from "./AppidEdit"

  export default {
    name: "AppidList",
    components: {AppidEdit},
    data() {
      return {
        // 当前页
        current_page: 1,
        // 总数
        total: 0,
        // 每页记录数
        offset: 10,
        appids: [],
        columns1: [
          {
            title: 'AppID 名称',
            key: 'app_name',
            width: 230,
          },
          {
            title: '描述',
            key: 'app_desc',
          },
          {
            title: '修改人',
            key: 'last_updated_by',
            width: 150,
          },
          {
            title: '修改时间',
            key: 'last_updated_time',
            width: 150,
            render: (h, params) => {
              return h('div',
                formatDate(new Date(params.row.last_updated_time), 'yyyy-MM-dd hh:mm')
              )
            }
          },
          {
            title: '操作',
            key: 'operate',
            width: 280,
            fixed: 'right',
            render: (h, params) => {
              return h('div', [
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
                      this.$refs.appid_edit.initData(this.appids[params.index]);
                    }
                  }
                }, '编辑'),
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
                      this.deleteAppid(this.appids[params.index]['id']);
                    }
                  }
                }, '删除'),
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
                      this.chooseAppid(this.appids[params.index]);
                    }
                  }
                }, '选择'),
              ]);
            }
          }
        ],
      }
    },
    methods: {
      handleChange(page) {
        this.current_page = page;
        this.refreshAppIdList();
      },
      handlePageSizeChange(pageSize) {
        this.offset = pageSize;
        this.refreshAppIdList();
      },
      refreshAppIdList: async function () {
        const result = await QueryPageAppIdList(this.offset, this.current_page, this.search);
        if (result.status == "SUCCESS") {
          this.appids = result.appids;
        }
      },
      deleteAppid: async function (id) {
        const result = await DeleteAppid({id: id});
        if (result.status == "SUCCESS") {
          this.refreshAppIdList();
        }
      },
      chooseAppid: function (appId) {
        localStorage.setItem("iwork_appId", JSON.stringify(appId));
        this.$router.go(0);     // 强制刷新页面
      }
    },
    mounted() {
      this.refreshAppIdList();
    }
  }
</script>

<style scoped>

</style>
