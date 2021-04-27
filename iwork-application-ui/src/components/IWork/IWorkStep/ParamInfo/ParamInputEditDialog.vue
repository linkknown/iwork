<template>
  <Modal
    v-model="showFormModal"
    width="1200"
    title="查看/编辑 workstep 参数"
    :footer-hide="true"
    :transfer="false"
    :mask-closable="false"
    :styles="{top: '20px'}"
    :scrollable="true">
    <Row>
      <Col span="6">
        <ParamInputEditDataSource ref="paramInputEditDataSource" :pre-pos-tree-node-arr="prePosTreeNodeArr"/>
      </Col>
      <Col span="3" style="text-align: center;">
        <i-circle :percent="editProgress" :size="85">
          编辑进度
          <span class="demo-Circle-inner" style="font-size:24px">{{editProgress}}%</span>
        </i-circle>

        <Button @click="appendData('parent')" style="margin-top: 10px;"><Icon type="ios-arrow-forward"></Icon>选择父节点</Button>
        <Button @click="appendData('children')" style="margin-top: 10px;"><Icon type="ios-arrow-forward"></Icon>选择子节点</Button>

        <Dropdown trigger="hover" style="margin-top: 200px;">
          <Button>
            输入列表&nbsp;&nbsp;
            <Icon type="ios-arrow-forward"></Icon>
          </Button>
          <DropdownMenu slot="list">
            <Scroll height="300">
              <DropdownItem v-for="(item,index) in paramInputSchemaItems">
                <Button :type="item.ParamValue ? 'default' : 'text'" long size="small" @click="handleReload(index, false)">
                  {{ item.ParamName }}&nbsp;&nbsp;&nbsp;<Badge status="processing" v-if="paramIndex == index"/>
                </Button>
              </DropdownItem>
            </Scroll>
          </DropdownMenu>
        </Dropdown>
      </Col>
      <Col span="15">
        <div style="margin-bottom: 5px;">
          <span style="color: #657180;font-size: 14px;">
            (参数索引-{{paramIndex}}<span v-if="inputLabel.indexOf('?')>0">可选参数</span>) - {{inputLabel}}
          </span>
          <Checkbox v-model="pureText">纯文本值</Checkbox>
          <Button type="info" size="small" @click="parseToMultiValue()">多值预览</Button>
          <router-link :to="{ path: '/iwork/quickSql' }" tag="a" target="_blank">
            <Button type="warning" size="small">辅助工具</Button>
          </router-link>
        </div>
        <div v-if="showMultiVals" style="margin-top: 20px;">
          <Scroll height="350">
            <table style="width: 100%;">
              <tr v-for="(val,index) in multiVals">
                <td style="width: 10%;">
                  <p>参数 {{index}} </p>
                  <p v-if="ParamNamings && index < ParamNamings.length" style="color:green;">{{ParamNamings[index]}}</p>
                </td>
                <td><Input type="textarea" :value="val" :readonly="true"/></td>
              </tr>
            </table>
          </Scroll>
        </div>
        <div v-else>
          <Input v-model="inputTextData" type="textarea" :rows="12" placeholder="Enter something..."
                 @drop.native="handleInputDrop" @dragover.native="handleDragover"/>
          <p style="margin-top: 5px;">变量占位符</p>
          <Scroll height="100">
            <Tag color="default" v-for="(variable,index) in variables" style="margin-right: 10px;"
                 @drop.native="handlePlaceholderDrop($event, index)" @dragover.native="handleDragover">{{variable}}
                 <Icon type="ios-close" style="margin-left: 10px;" @click="deleteVariable(index)"/>
                 <Icon type="md-create" style="margin-left: 10px;" @click="editVariable(index)"/>
                 <span>
                   <input type="text" v-show="variablesShowEdit[index] == true"
                      :id="'variablesShowEdit_' + index"></input>
                 </span>
            </Tag>
          </Scroll>

          <Row style="text-align: right;margin-top: 10px;">
            <Button type="success" size="small" @click="handleSubmit">提交</Button>
            <Button type="info" size="small" @click="closeModal">Close</Button>
          </Row>
        </div>
      </Col>
    </Row>
  </Modal>
</template>

<script>
  import {LoadPreNodeOutput, ParseToMultiValue} from "../../../../api"
  import ISimpleBtnTriggerModal from "../../../Common/modal/ISimpleBtnTriggerModal"
  import {checkEmpty, getMatchArrForString, strSplit} from "../../../../tools"
  import ParamInputEditDataSource from "./ParamInputEditDataSource"

  export default {
    name: "ParamInputEditDialog",
    components:{ISimpleBtnTriggerModal,ParamInputEditDataSource},
    props:{
      paramInputSchemaItems:{
        type: Array,
        default: () => [],
      },
    },
    data(){
      return {
        showFormModal:false,
        inputLabel:'',
        oldPureText:false,
        pureText:false,
        oldInputTextData:'',
        inputTextData:'',
        ParamNamings: [],
        showMultiVals:false,  // 默认非多值视图
        multiVals:[],         // 存储多值列表
        paramIndex:1,
        prePosTreeNodeArr:[],
        variables:[],
        variablesShowEdit:[],
        variableConcats:"",
        editProgress: 0,
      }
    },
    methods:{
      handlePlaceholderDrop:function(event, index){
        // 取消冒泡
        event.stopPropagation();
        event.preventDefault();
        var transferText = event.dataTransfer.getData("Text");
        this.variables[index] =  transferText.substr(0, transferText.lastIndexOf(";\n"));     // 将值替换进 variables
        this.handleRefillInputTextData();
      },
      handleRefillInputTextData:function(){
        var _inputTextData = this.variableConcats;
        for(var i=0; i<this.variables.length; i++){
          _inputTextData = _inputTextData.replace("__sep__", this.variables[i]);
        }
        this.inputTextData = _inputTextData;
      },
      handleInputDrop:function(){
        const event = window.event||arguments[0];
        // 取消冒泡
        event.stopPropagation();
        event.preventDefault();
        var transferText = event.dataTransfer.getData("Text");
        // 将数据添加到右侧
        this.inputTextData = this.inputTextData + transferText;
      },
      handleDragover:function(){
        const event = window.event||arguments[0];
        event.preventDefault();
      },
      parseToMultiValue: async function(){
        const result = await ParseToMultiValue(this.pureText, this.inputTextData);
        if(result.status == "SUCCESS"){
          this.showMultiVals = !this.showMultiVals;
          this.multiVals = result.multiVals;
        }else{
          this.$Message.error('提交失败!' + result.errorMsg);
        }
      },
      handleReload: function(paramIndex, refreshOutput){
        var _this = this;
        if(this.checkDrity()){
          this.$Modal.confirm({
            title:"确认",
            content:"是否需要保存上一步操作?",
            onOk: function () {
              _this.handleSubmit(function () {
                _this.$emit("handleReload", paramIndex, refreshOutput);
              });
            },
          });
        }else{
          this.$emit("handleReload", paramIndex, refreshOutput);
        }
      },
      refreshEditProgress:function(){
        var count = 0;
        for(var i=0; i<this.paramInputSchemaItems.length; i++){
          if(!checkEmpty(this.paramInputSchemaItems[i].ParamValue) || this.paramInputSchemaItems[i].ParamName.indexOf("?") > 0){
            count ++;
          }
        }
        this.editProgress = Math.floor(count / this.paramInputSchemaItems.length * 100);
      },
      refreshParamInput: function(index, item, refreshOutput){
        this.showFormModal = true;
        this.item = item;
        this.paramIndex = index;
        this.inputLabel = item.ParamName;
        this.pureText = item.PureText;
        // 文本输入框设置历史值
        this.inputTextData = item.ParamValue;
        this.ParamNamings = strSplit(item.ParamNamings, ",");
        this.showMultiVals = false;
        this.clearDirty();
        if (refreshOutput == true){
          this.refreshPreNodeOutput();
        }
        this.refreshEditProgress();
      },
      closeModal: function(){
        this.showFormModal = false;
      },
      clearDirty: function (){
        this.oldInputTextData = this.inputTextData;
        this.oldPureText = this.pureText;
      },
      checkDrity: function(){
        return this.oldInputTextData != this.inputTextData || this.oldPureText != this.pureText;
      },
      handleSubmit:function (callback) {
        this.$emit("handleSubmit", this.inputLabel, this.inputTextData, this.pureText);
        this.clearDirty();
        if(callback != null && callback != undefined && typeof callback === "function"){
          callback();     // 提交成功后的回调函数
        }
      },
      refreshPreNodeOutput:async function () {
        const result = await LoadPreNodeOutput(this.$store.state.current_work_id, this.$store.state.current_work_step_id);
        if(result.status == "SUCCESS"){
          this.prePosTreeNodeArr = result.prePosTreeNodeArr;
        }
      },
      appendData:function (chooseType) {
        let datas = this.$refs.paramInputEditDataSource.getChooseDatas(chooseType);
        if(Array.isArray(datas)){
          for(var i=0; i<datas.length; i++){
            this.inputTextData = this.inputTextData + datas[i];
          }
        }else{
          this.inputTextData = this.inputTextData + datas;
        }
      },
      deleteVariable:function (index) {
        this.variables[index] = "";
        this.handleRefillInputTextData();
      },
      editVariable:function (index) {
        if(this.variablesShowEdit[index] == false){
          this.$set(this.variablesShowEdit, index, true);  // 刷新界面
        }else{
          this.$set(this.variablesShowEdit, index, false);
          let _variablesShowEdit = document.getElementById("variablesShowEdit_" + index).value;
          this.$set(this.variables, index,
            checkEmpty(_variablesShowEdit) == true ? this.variables[index] : _variablesShowEdit);
          this.handleRefillInputTextData();
        }
      }
    },
    watch: {
      inputTextData(val) {
        // 所有占位符变量
        this.variables = getMatchArrForString(this.inputTextData, /\$[a-zA-Z0-9_]+(\.[a-zA-Z0-9_]+)*/g);
        this.variableConcats = this.inputTextData;
        if(this.variables != null){
          for(var i=0; i<this.variables.length; i++){
            this.variableConcats = this.variableConcats.replace(this.variables[i], "__sep__");
          }
          for (var i=0; i<this.variables.length; i++){
            this.variablesShowEdit[i] = false;
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
