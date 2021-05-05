<template>
  <div style="margin: 10px;">
    <WorkStepComponent ref="workStepComponent"/>
    <WorkDashboard ref="workDashboard" v-show="showWorkDashboard"/>

    <h3 v-if="$route.query.work_name" style="text-align:center;">
      <span class="workNameBox">
        流程名称：{{$route.query.work_name}} &nbsp;&nbsp;
        <span class="workHttpService" v-if="work" @click="handleCopy(workHttpServiceUri)">{{workHttpServiceUri}}</span>
      </span>
    </h3>
    <p v-if="work" style="text-align: center;background: #fff5dd;padding: 10px;margin: 10px;">
      所属模块：{{work.module_name}}&nbsp;&nbsp;&nbsp;&nbsp;
      流程描述：{{work.work_desc}}&nbsp;&nbsp;&nbsp;&nbsp;
      流程注释比率：<span style="color: green;font-weight: bold;">{{commentRate}}</span></p>

    <Button class="isoft_mr10" type="success" size="small" @click="$router.push({ path:'/iwork/workList'})">返回列表</Button>
    <Button class="isoft_mr10" type="warning" size="small" @click="$router.push({ path:'/iwork/filterList'})">过滤器配置</Button>

    <div style="margin-top: 10px;margin-bottom: 10px;">
      <Button class="isoft_mr10" type="error" size="small" @click="$refs.workStepComponent.toggleShow()">显示组件</Button>
      <Button class="isoft_mr10" type="warning" size="small" @click="showRefactorModal">重构流程</Button>
      <Button class="isoft_mr10" type="info" size="small" @click="batchChangeIndent('left', null)">向左缩进</Button>
      <Button class="isoft_mr10" type="error" size="small" @click="batchChangeIndent('right', null)">向右缩进</Button>
      <Button class="isoft_mr10" type="warning" size="small" @click="runWork">运行流程</Button>
      <Button class="isoft_mr10" type="info" size="small" @click="showRunLogList">运行日志</Button>
      <Button class="isoft_mr10" type="error" size="small" @click="showWorkDashboardFunc">运行报表</Button>
      <WorkValidate class="isoft_mr10" :work_id="_work_id"/>

      <ISimpleConfirmModal ref="refactor_modal" modal-title="重构为子流程" :modal-width="500" @handleSubmit="refactor">
        <Input v-model.trim="refactor_worksub_name" placeholder="请输入重构的子流程名称"></Input>
      </ISimpleConfirmModal>
    </div>
    <BaseInfo ref="workStepBaseInfo" @reloadWorkStepBaseInfo="showWorkStepBaseInfo" @handleSuccess="refreshWorkStepList" :worksteps="worksteps"/>
    <ParamInfo ref="workStepParamInfo" @reloadWorkStepParamInfo="showWorkStepParamInfo"
               @handleSuccess="refreshWorkStepList" :worksteps="worksteps" :work-id="$route.query.work_id"/>
    <Row style="margin-bottom: 5px;">
      <Col span="24">
        <span @click="showRunLogList">
          <Button type="default" size="small">失败次数/总次数 &nbsp;&nbsp;<span style="color: red;">{{errorCount}}</span> {{allCount}}</Button>
        </span>
        <Drawer title="运行日志" width="900" :closable="false" v-model="showRunLogDrawer">
          <RunLogList ref="runLogList" :work-id="_work_id"/>
        </Drawer>
      </Col>
    </Row>

    <Table :loading="loading" :height="500" border :columns="columns1" ref="selection" :data="worksteps"
           size="small"></Table>
  </div>
</template>

<script>
  import {
    AddWorkStep,
    BatchChangeIndent,
    ChangeWorkStepOrder,
    CopyWorkStepByWorkStepId,
    DeleteWorkStepByWorkStepId,
    EditWorkStepBaseInfo,
    GetMetaInfo,
    LoadValidateResult,
    QueryWorkDetail,
    RefactorWorkStepInfo,
    RunWork,
    WorkStepList
  } from "../../../api"
  import ParamInfo from "./ParamInfo/ParamInfo"
  import ISimpleLeftRightRow from "../../Common/layout/ISimpleLeftRightRow"
  import BaseInfo from "./BaseInfo/BaseInfo"
  import {
    checkEmpty,
    checkFastClick,
    checkNotEmpty,
    copyText,
    getRepeatStr,
    oneOf,
    percentNum,
    startsWith
  } from "../../../tools"
  import WorkValidate from "../IValidate/WorkValidate"
  import ISimpleConfirmModal from "../../Common/modal/ISimpleConfirmModal"
  import WorkStepEditBtns from "./WorkStepEditBtns"
  import WorkStepComponent from "./WorkStepComponent"
  import WorkDashboard from "./DashBoard/WorkDashboard"
  import WorkStepPoptip from "./WorkStepPoptip"
  import RunLogList from "../ILog/RunLogList"

  export default {
    name: "WorkStepList",
    components:{ParamInfo,ISimpleLeftRightRow,BaseInfo,WorkValidate,ISimpleConfirmModal,
      WorkStepEditBtns,WorkStepComponent,WorkDashboard,WorkStepPoptip,RunLogList},
    data(){
      return {
        validateDetails:[],
        // 默认不显示组件
        refactor_worksub_name:'',
        nodeMetas: [],
        worksteps: [],
        // 当前流程注释率
        commentRate: '',
        loading:false,
        showWorkDashboard:false,
        usedMap: null,
        runLogRecordCount:{},
        // 显示运行日志的抽屉
        showRunLogDrawer:false,
        work: null,
      }
    },
    computed:{
      workHttpServiceUri:function () {
        let appId = localStorage.getItem("iwork_appId");
        if (appId != null && appId !== undefined) {
          return "/api/iwork/httpservice/" + JSON.parse(appId).app_name + "/" + this.work.work_name;
        }
        return "";
      },
      errorCount:function () {
        return this.getErrorOrTotalCount(this.$route.query.work_id, 'error');
      },
      allCount:function () {
        return this.getErrorOrTotalCount(this.$route.query.work_id, 'total');
      },
      _work_id:function () {
        return parseInt(this.$route.query.work_id);
      },
      columns1(){
        var _this = this;
        let columns = [
          {
            type: 'selection',
            width: 60,
            align: 'center',
          },
          {
            title: '步骤编号',
            key: 'work_step_id',
            width: 100,
            render: (h,params)=>{
              let validateErrors = _this.validateDetails.filter(validateDetail => _this.$route.query.work_id == validateDetail.work_id
                        && _this.worksteps[params.index]['work_step_id'] == validateDetail.work_step_id);
              return h('div', {}, [
                h('span', {}, this.worksteps[params.index]['work_step_id']),
                h('a', {    // 延迟执行函数显示效果
                  style: {
                    marginLeft: '10px',
                    color: 'blue',
                    fontStyle: 'italic',
                    display: oneOf(this.worksteps[params.index]['is_defer'], ["true"])  ? undefined : 'none',
                  },
                  on:{
                    click:function () {
                      _this.$Modal.confirm({
                        title:"确认",
                        width: 400,
                        content:'defer 延迟执行',
                      });
                    }
                  }
                }, 'D'),
                h('a', {    // 校验结果
                  style: {
                    marginLeft: '10px',
                    color: 'red',
                    fontStyle: 'italic',
                    display: validateErrors.length > 0 ? undefined : 'none',
                  },
                  on:{
                    click:function () {
                      var errorMsg = "";
                      for(var i=0; i<validateErrors.length; i++){
                        errorMsg += validateErrors[i].detail + "<br/>";
                      }
                      _this.$Modal.confirm({
                        title:"校验错误",
                        width: 600,
                        render: h => {
                          return h('div', {
                            style:'word-break: break-all;',
                            domProps: {
                              innerHTML: errorMsg,
                            },
                          });
                        }
                      });
                    }
                  }
                }, 'E'),
              ]);
            },
          },
          {
            title: '操作',
            key: 'work_step_operate',
            width: 380,
            render: (h,params)=>{
              return h('div', {
                  on:{
                    drop: () => {
                      const event = window.event||arguments[0];
                      // 取消冒泡
                      event.stopPropagation();
                      event.preventDefault();
                      var work_step_meta = event.dataTransfer.getData("Text");
                      this.addWorkStep(params.row.work_step_id, work_step_meta);
                    },
                    dragover: () => this.allowDrop(),
                  }
                }, [
                  h(WorkStepEditBtns,{
                    props:{
                      showArrow: !oneOf(this.worksteps[params.index]['work_step_type'], ["work_start", "work_end"]),
                      showEdit: !oneOf(this.worksteps[params.index]['work_step_type'], ["work_start", "work_end"]),
                      showParam: true,
                      showDelete: !oneOf(this.worksteps[params.index]['work_step_type'], ["work_start", "work_end"]),
                      showCopy: !oneOf(this.worksteps[params.index]['work_step_type'], ["work_start", "work_end"]),
                      showRefer: oneOf(this.worksteps[params.index]['work_step_type'], ["work_start"]),
                    },
                    on: {
                      handleClick:function (clickType) {
                        if (checkFastClick()){
                          _this.$Message.error("点击过快,请稍后重试!");
                          return;
                        }
                        switch (clickType) {
                          case "up":
                            _this.changeWorkStepOrder(params.row.work_step_id, "up");
                            break;
                          case "down":
                            _this.changeWorkStepOrder(params.row.work_step_id, "down");
                            break;
                          case "back":
                            _this.batchChangeIndent('left', [params.row.work_step_id]);
                            break;
                          case "forward":
                            _this.batchChangeIndent('right', [params.row.work_step_id]);
                            break;
                          case "edit":
                            _this.showWorkStepBaseInfo(params.row.work_step_id);
                            break;
                          case "param":
                            if (params.row.work_step_type){
                              _this.$refs.workStepParamInfo.showWorkStepParamInfo(_this.$route.query.work_id, params.row.work_step_id);
                            }
                            break;
                          case "delete":
                            _this.$Modal.confirm({
                              title: '删除',
                              content: '确认删除该条数据吗？请谨慎操作！',
                              onOk: () => {
                                _this.deleteWorkStepByWorkStepId(_this.$route.query.work_id, params.row.work_step_id);
                              },
                              onCancel: () => {
                                _this.$Message.info('取消操作');
                              }
                            });
                            break;
                          case "copy":
                            _this.copyWorkStepByWorkStepId(_this.$route.query.work_id, params.row.work_step_id);
                            break;
                          case "refer":
                            if(!checkEmpty(_this.$route.query.parent_href)){
                              window.location.href = _this.$route.query.parent_href;
                            }
                            break;
                        }
                      }
                    }
                  }),
                ]
              )
            }
          }
        ];
        // push 其余列
        [].push.apply(columns,[
          {
            title: '步骤名称',
            key: 'work_step_name',
            width: 600,
            render: (h, params) => {
              var _this = this; // vue 实例
              // 可编辑模式
              if(params.row.$isEdit && !oneOf(params.row.work_step_name, ["start", "end"])){
                return h('input', {
                  style:{
                    width: "300px",
                  },
                  domProps: {
                    value: params.row.work_step_name,
                  },
                  on: {
                    input: function (event) {
                      params.row.work_step_name = event.target.value
                    },
                    blur: async function (event) {
                      if(startsWith(params.row.work_step_name, params.row.work_step_type + "_")){
                        // 发生过修改
                        const result = await EditWorkStepBaseInfo(params.row.work_id, params.row.work_step_id,
                          params.row.work_step_name,params.row.work_step_desc, params.row.work_step_type, params.row.is_defer);
                        if(result.status == "SUCCESS"){
                          _this.$Message.success('修改成功!');
                        }else{
                          _this.$Message.error('修改失败！');
                        }
                      }else{
                        _this.$Message.error('名称不合法！必须以 ' + params.row.work_step_type + '_ 开头');
                      }
                      // 刷新组件
                      _this.refreshWorkStepList();
                    }
                  }
                });
              }else{
                const blankStr = '\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0\xa0';
                // 显示名称
                let work_step_name_str = getRepeatStr(blankStr, params.row.work_step_indent) + this.worksteps[params.index]['work_step_name'];
                // 显示的额外信息
                let extraStr = getRepeatStr(blankStr, 1) + _this.getExtraStr(this.worksteps[params.index]);

                // 非可编辑模式
                return h('div', {
                  style:{
                    overflow: 'hidden',         // 内容超出不换行
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  },
                },[
                  h('Poptip', {
                      props: {
                        trigger: 'hover',
                        title: '步骤名称:' + this.worksteps[params.index]['work_step_name'] + ' --- 步骤类型:' + this.worksteps[params.index]['work_step_type'],
                        placement: params.index <= 6 ? "left-start" : "left-end",
                      },
                      style: {
                        marginRight: '10px',
                      },
                  },
                  [h(WorkStepPoptip,{
                    slot:'content',
                    props:{
                      worksteps: this.worksteps,
                      workstep: this.worksteps[params.index],
                      usedMap: this.usedMap,
                    },
                  }),
                  h('Icon', {
                    props: {
                      type: this.renderWorkStepTypeIcon(this.worksteps[params.index]['work_step_type']),
                      size: 25,
                    },
                    style: {
                      marginRight: '10px',
                    },
                  })]),
                  h('span', {
                    style: {
                      // work_step_name 根据缩进级别进行缩进,不同级别使用不同颜色
                      color: ['green','blue','grey','red'][params.row.work_step_indent],
                    },
                    on: {
                      // click 变成可编辑模式
                      click:function (event) {
                        var workstep = _this.worksteps[params.index];
                        workstep.$isEdit = true;
                        _this.$set(_this.worksteps[params.index], workstep);  // 刷新界面
                      }
                    }
                  }, work_step_name_str),
                  h('span',{
                    style:{
                      color: 'red',
                    },
                    attrs:{
                      title: extraStr,
                    }
                  }, extraStr),
                ]);
              }
            }
          },
          {
            title: '步骤描述',
            key: 'work_step_desc',
            width: 400,
          },
        ]);
        return columns;
      },
    },
    methods:{
      showWorkDashboardFunc:function(){
        this.showWorkDashboard = true;
        this.$refs.workDashboard.showWorkDashboard(true);
      },
      refreshWorkValidateDetail: async function(){
        const result = await LoadValidateResult(this.$route.query.work_id);
        if(result.status === "SUCCESS"){
          this.validateDetails = result.details;
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      refreshWorkStepList:async function () {
        this.loading = true;
        const result = await WorkStepList(this.$route.query.work_id);
        if(result.status==="SUCCESS"){
          this.usedMap = result.usedMap;
          this.worksteps = result.worksteps;
          this.commentRate = percentNum(this.worksteps.filter(step => checkNotEmpty(step.work_step_desc)).length, this.worksteps.length);
          this.runLogRecordCount = result.runLogRecordCount;
          this.refreshWorkValidateDetail();
        }
        this.loading = false;
      },
      copyWorkStepByWorkStepId: async function(work_id, work_step_id){
        const result = await CopyWorkStepByWorkStepId(work_id, work_step_id);
        if(result.status==="SUCCESS"){
          this.refreshWorkStepList();
        }
      },
      deleteWorkStepByWorkStepId:async function(work_id, work_step_id){
        const result = await DeleteWorkStepByWorkStepId(work_id, work_step_id);
        if(result.status==="SUCCESS"){
          this.refreshWorkStepList();
        }
      },
      changeWorkStepOrder:async function(work_step_id, type){
        const result = await ChangeWorkStepOrder(this.$route.query.work_id, work_step_id, type);
        if(result.status === "SUCCESS"){
          this.refreshWorkStepList();
          this.$Message.success('换位成功!');
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      addWorkStep:async function (work_step_id, work_step_meta) {
        const result = await AddWorkStep(this.$route.query.work_id, work_step_id, work_step_meta);
        if(result.status === "SUCCESS"){
          this.refreshWorkStepList();
          this.$Message.success('添加成功!');
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      renderWorkStepTypeIcon:function (workStepType) {
        for(var i=0; i<this.nodeMetas.length; i++){
          let default_work_step_type = this.nodeMetas[i];
          if(default_work_step_type.name === workStepType){
            return default_work_step_type.icon;
          }
        }
      },
      showRefactorModal:function (){
        this.$refs.refactor_modal.showModal();
      },
      getSelectionArr:function(){
        let selectionArr = [];
        let selections = this.$refs.selection.getSelection();
        for(var i=0; i<selections.length; i++){
          selectionArr.push(selections[i].work_step_id);
        }
        return selectionArr;
      },
      refactor: async function () {
        let selections = this.getSelectionArr();
        const result = await RefactorWorkStepInfo(this.$route.query.work_id, this.refactor_worksub_name, JSON.stringify(selections));
        if(result.status === "SUCCESS"){
          this.refreshWorkStepList();
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      batchChangeIndent:async function (mod, selections) {
        if (selections == null){
          selections = this.getSelectionArr();
        }
        const result = await BatchChangeIndent(this.$route.query.work_id, mod, JSON.stringify(selections));
        if(result.status === "SUCCESS"){
          this.refreshWorkStepList();
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      showRunLogList: function(){
        this.showRunLogDrawer = true;
        this.$refs.runLogList.refreshRunlogRecordList();
      },
      runWork: async function(){
        const result = await RunWork(this.$route.query.work_id);
        if(result.status === "SUCCESS"){
          this.$Message.success("运行任务已触发!");
        }
      },
      allowDrop:function(){
        const event = window.event||arguments[0];
        event.preventDefault();
      },
      showWorkStepParamInfo:function(work_step_id){
        this.$refs.workStepParamInfo.showWorkStepParamInfo(this.$route.query.work_id, work_step_id);
      },
      showWorkStepBaseInfo:function (work_step_id) {
        this.$refs.workStepBaseInfo.showWorkStepBaseInfo(this.$route.query.work_id, work_step_id);
      },
      getExtraStr:function (workstep) { // 显示额外信息
        if (workstep.work_step_input !== "") {
          var paramInputSchema = JSON.parse(workstep.work_step_input);
          if (paramInputSchema.ParamInputSchemaItems != null) {
            for (var i = 0; i < paramInputSchema.ParamInputSchemaItems.length; i++) {
              var item = paramInputSchema.ParamInputSchemaItems[i];
              if (item.ParamName === "bool_expression") {
                return item.ParamValue;
              }
            }
          }
        }
        return "";
      },
      refreshWorkDetail:async function(){
        const result = await QueryWorkDetail(this.$route.query.work_id);
        if (result.status === "SUCCESS") {
          this.work = result.work;
        }
      },
      refreshNodeMetas:async function () {
        const result = await GetMetaInfo("nodeMetas");
        if (result.status === "SUCCESS") {
          this.nodeMetas = result.nodeMetas;
          // 提交 action
          this.$store.dispatch('commitSetNodeMetas',{"nodeMetas":result.nodeMetas});
        }
      },
      getErrorOrTotalCount:function (workId, flag) {
        // 此处统一转换成字符串进行比较, 而不是 int 和 字符串比较
        var key = Object.keys(this.runLogRecordCount).filter(_key => _key + "" === workId + "")[0];
        if (key && this.runLogRecordCount[key]) {
          return flag === "error" ? this.runLogRecordCount[key].errorCount : "/" + this.runLogRecordCount[key].allCount;
        }
      },
      handleCopy: function () {
        var _this = this;
        copyText(this.workHttpServiceUri, function () {
          _this.$Message.success("复制成功！");
        });
      }
    },
    mounted: function () {
      this.refreshWorkDetail();
      this.refreshWorkStepList();
      this.refreshNodeMetas();
    },
    watch:{
      // 监听路由是否变化
      '$route' (to, from) {
        this.refreshWorkStepList();
      }
    },
  }
</script>

<style scoped>
  .workNameBox {
    position: relative;
  }
  .workHttpService {
    display: none;
    position: absolute;
    color: #fff;
    background-color: rgba(0,0,0,.7);
    font-size: 12px;
    height: 22px;
    line-height: 22px;
    border-radius: 5px;
    padding: 0 15px;
    cursor: pointer;
  }
  .workNameBox:hover .workHttpService {
    display: inline-block;
  }
  .isoft_mr10 {
    margin-right: 10px;
  }
</style>
