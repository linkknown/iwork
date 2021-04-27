<template>
  <div>
    <ISimpleLeftRightRow style="margin-bottom: 10px;margin-right: 10px;">
      <!-- left 插槽部分 -->
      <span slot="left">
        <Button type="success" @click="addModule">新增</Button>
        <ISimpleConfirmModal ref="moduleEditModal" modal-title="新增/编辑 module" :modal-width="600" :footer-hide="true">
          <IKeyValueForm ref="moduleEditForm" form-key-label="module_name" form-value-label="module_desc"
                         form-key-placeholder="请输入 module_name" form-value-placeholder="请输入 module_desc"
                         @handleSubmit="editModule" :formkey-validator="moduleNameValidator">
          </IKeyValueForm>
        </ISimpleConfirmModal>
      </span>

      <!-- right 插槽部分 -->
      <ISimpleSearch slot="right" @handleSimpleSearch="handleSearch"/>
    </ISimpleLeftRightRow>

    <Table border :columns="columns1" :data="modules" size="small"></Table>
    <Page :total="total" :page-size="offset" show-total show-sizer :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </div>
</template>

<script>
  import ISimpleLeftRightRow from "../../Common/layout/ISimpleLeftRightRow"
  import ISimpleConfirmModal from "../../Common/modal/ISimpleConfirmModal"
  import IKeyValueForm from "../../Common/form/IKeyValueForm"
  import ISimpleSearch from "../../Common/search/ISimpleSearch"
  import {ModuleList} from "../../../api"
  import {EditModule} from "../../../api"
  import {DeleteModuleById} from "../../../api"
  import {validateCommonPatternForString} from "../../../tools/index"

  export default {
    name: "IModuleList",
    components:{ISimpleLeftRightRow,ISimpleConfirmModal,IKeyValueForm,ISimpleSearch},
    data(){
      return {
        // 当前页
        current_page:1,
        // 总数
        total:0,
        // 每页记录数
        offset:10,
        // 搜索条件
        search:"",
        modules:[],
        columns1:[
          {
            title: 'module_name',
            key: 'module_name',
          },
          {
            title: 'module_desc',
            key: 'module_desc',
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
                      this.$refs.moduleEditModal.showModal();
                      this.$refs.moduleEditForm.initFormData(this.modules[params.index].id, this.modules[params.index].module_name, this.modules[params.index].module_desc);
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
                      this.deleteModuleById(this.modules[params.index]['id']);
                    }
                  }
                }, '删除'),
              ]);
            }
          },
        ],
      }
    },
    methods:{
      deleteModuleById:async function(id){
        const result = await DeleteModuleById(id);
        if(result.status=="SUCCESS"){
          this.refreshModuleList();
        }
      },
      refreshModuleList:async function () {
        const result = await ModuleList(this.offset,this.current_page,this.search);
        if(result.status=="SUCCESS"){
          this.modules = result.modules;
          this.total = result.paginator.totalcount;
        }
      },
      handleChange(page){
        this.current_page = page;
        this.refreshModuleList();
      },
      handlePageSizeChange(pageSize){
        this.offset = pageSize;
        this.refreshModuleList();
      },
      handleSearch(data){
        this.offset = 10;
        this.current_page = 1;
        this.search = data;
        this.refreshModuleList();
      },
      addModule:function(){
        this.$refs.moduleEditModal.showModal();
      },
      editModule:async function (module_id, module_name, module_desc) {
        const result = await EditModule(module_id, module_name, module_desc);
        if(result.status == "SUCCESS"){
          this.$refs.moduleEditForm.handleSubmitSuccess("提交成功!");
          this.$refs.moduleEditModal.hideModal();
          this.refreshModuleList();
        }else{
          this.$refs.workEditForm.handleSubmitError("提交失败!");
        }
      },
      moduleNameValidator (rule, value, callback){
        if (value === '') {
          callback(new Error('字段值不能为空!'));
        } else if (!validateCommonPatternForString(value)) {
          callback(new Error('存在非法字符，只能包含字母，数字，下划线!'));
        } else {
          callback();
        }
      },
    },
    mounted(){
      this.refreshModuleList();
    }
  }
</script>

<style scoped>

</style>
