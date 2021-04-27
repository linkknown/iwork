<template>
  <Modal
    v-model="showFormModal"
    width="1200"
    title="查看/编辑 workstep 参数"
    :footer-hide="true"
    :mask-closable="false"
    :styles="{top: '20px'}"
    :scrollable="true">
    <Row>
      <Col span="6">
        <Scroll height="450">
          <p v-for="workstep in worksteps">
            <Tag><span @click="reloadWorkStepParamInfo(workstep.work_step_id, workstep.work_step_name)">{{workstep.work_step_name}}</span></Tag>
          </p>
        </Scroll>
      </Col>
      <Col span="18">
        <Spin size="large" fix v-if="spinShow"></Spin>
        <Scroll height="450" style="position: relative;">
          <!-- 表单信息 -->
          <Row style="margin-bottom: 20px;text-align: center;color: green;">
            <Col span="18">
              <h2>步骤名称:{{workStepParamInfo.work_step_name}},步骤类型:{{workStepParamInfo.work_step_type}}</h2>
            </Col>
          </Row>
          <Row style="margin-right: 5px;margin-left:20px;" :gutter="16">
            <Col span="18">
              <Tabs type="card" name="tab_paramInfo" :animated="false" value="edit" style="overflow: visible;">
                <TabPane label="ParamMapping" name="ParamMapping" v-if="showParamMapping" tab="tab_paramInfo" :index="1">
                  <ParamMapping :paramMappings="paramMappings" :work-step-type="workStepParamInfo.work_step_type" :work-id="workStepParamInfo.work_id"/>
                </TabPane>
                <TabPane label="edit" name="edit" v-if="showEdit" tab="tab_paramInfo" :index="2">
                  <ParamInputEdit ref="paramInputEdit" :work-id="workStepParamInfo.work_id" :work-step-id="workStepParamInfo.work_step_id"
                                  :paramInputSchemaItems="paramInputSchema.ParamInputSchemaItems"/>
                </TabPane>
                <Button size="small" slot="extra">input</Button>
              </Tabs>
              <Row style="padding-left: 130px;margin-top: 20px;">
                <Button type="success" size="small" @click="handleSubmit">提交</Button>
                <Button type="info" size="small" @click="closeModal">Close</Button>
                <span class="moreToolBox" style="margin-left: 50px;">
                  <Icon type="ios-hand-outline" size="20"/>
                  <span class="moreTool"><Button type="success" size="small" @click="handleCopyAttrs" title="多个属性用,分割">复制所有属性名</Button></span>
                </span>
              </Row>
            </Col>
            <Col span="6">
              <Tabs type="card" :animated="false">
                <TabPane label="Tree">
                  <PreParamOutputTree v-if="paramOutputSchemaTreeNode" :paramOutputSchemaTreeNode="paramOutputSchemaTreeNode"/>
                </TabPane>
                <Button size="small" slot="extra">output</Button>
              </Tabs>
            </Col>
          </Row>
        </Scroll>
      </Col>
    </Row>
  </Modal>
</template>

<script>
  import ParamInputEdit from "./ParamInputEdit"
  import PreParamOutputTree from "./PreParamOutputTree"
  import ISimpleConfirmModal from "../../../Common/modal/ISimpleConfirmModal"
  import ParamMapping from "./ParamMapping"
  import {EditWorkStepParamInfo, LoadWorkStepInfo} from "../../../../api"
  import {oneOf, copyText} from "../../../../tools/index"

  export default {
    name: "ParamInfo",
    components:{ParamInputEdit,PreParamOutputTree,ParamMapping,ISimpleConfirmModal},
    props: {
      workId: {
        type: [Number, String],
        default: -1
      },
      worksteps: {
        type: Array,
        default: () => [],
      },
    },
    data(){
      return {
        spinShow:false,           // 加载中
        showFormModal:false,
        // 输入参数
        paramInputSchema:"",
        // 输出参数
        paramOutputSchema:"",
        paramOutputSchemaTreeNode:null,
        // 显示效果
        showParamMapping:false,
        showEdit:false,
        // 参数映射
        paramMappings:[],
        workStepParamInfo: {
          work_id: this.workId,
          work_step_id: 0,
          work_step_name: '',
          work_step_type: '',
        },
      }
    },
    methods:{
      reloadWorkStepParamInfo:function(work_step_id, work_step_name){
        this.$emit("reloadWorkStepParamInfo", work_step_id);
      },
      // 复制当前节点的所有属性名,多个属性用逗号分割
      handleCopyAttrs: function () {
        var _this = this;
        let paramNames = this.paramInputSchema.ParamInputSchemaItems.map(item => item.ParamName);
        let copyStr = paramNames.join(",");
        copyText(copyStr, function (){
          _this.$Message.success('复制成功');
        });
      },
      handleSubmit:async function() {
        const paramInputSchemaStr = JSON.stringify(this.paramInputSchema);
        const paramMappingsStr = JSON.stringify(this.paramMappings);
        const result = await EditWorkStepParamInfo(this.workStepParamInfo.work_id, this.workStepParamInfo.work_step_id, paramInputSchemaStr, paramMappingsStr);
        if(result.status == "SUCCESS"){
          this.$Message.success('提交成功!');
          // 通知父组件添加成功
          this.$emit('handleSuccess');
          if (this.showFormModal){    // 未被关闭则刷新加载 param 信息
            // 直接刷新不关闭
            this.showWorkStepParamInfo(this.workStepParamInfo.work_id, this.workStepParamInfo.work_step_id);
          }
        }else{
          this.$Message.error('提交失败!' + result.errorMsg);
        }
      },
      loadWorkStepInfo:async function(){
        this.spinShow = true;
        const result = await LoadWorkStepInfo(this.workStepParamInfo.work_id,this.workStepParamInfo.work_step_id);
        this.spinShow = false;
        if(result.status == "SUCCESS"){
          this.workStepParamInfo.work_id = result.step.work_id;
          this.workStepParamInfo.work_step_id = result.step.work_step_id;
          this.workStepParamInfo.work_step_name = result.step.work_step_name;
          this.workStepParamInfo.work_step_type = result.step.work_step_type;

          if(oneOf(result.step.work_step_type, ["work_start","work_end","mapper","entity_parser","goto_condition"])){
            this.showParamMapping = true;
          }else{
            this.showParamMapping = false;
          }
          this.showEdit = true; // tab 都是用 v-if 是为了解决排序错乱问题

          // 入参渲染
          this.paramInputSchema = result.paramInputSchema;
          // 出参渲染
          this.paramOutputSchema = result.paramOutputSchema;
          this.paramOutputSchemaTreeNode = result.paramOutputSchemaTreeNode;
          // 参数映射渲染
          this.paramMappings = result.paramMappings != null ? result.paramMappings : [];
          // 提交 action
          this.$store.dispatch('commitSetCurrent',{"current_work_id":result.step.work_id, "current_work_step_id":result.step.work_step_id});
          // 异步请求加载完成之后才显示模态对话框
          this.showFormModal = true;
          if(this.$refs.paramInputEdit){
            // 刷新校验结果
            this.$refs.paramInputEdit.refreshWorkValidateDetail();
          }
        }else{
          // 加载失败
          this.$Message.error('加载失败!');
        }
      },
      closeModal: function(){
        this.showFormModal = false;
      },
      showWorkStepParamInfo:function (work_id, work_step_id) {
        this.workStepParamInfo.work_id = work_id;
        this.workStepParamInfo.work_step_id = work_step_id;
        this.loadWorkStepInfo();
      },
    },
  }
</script>

<style scoped>
  .moreTool {
    display: none;
  }
  .moreToolBox:hover > .moreTool {
    display: inline-block;
  }
</style>
