<template>
  <div>
    <table>
      <div v-for="(item,index) in paramInputSchemaItems">
        <tr>
          <td>
            <!--white-space: nowrap; //不换行-->
            <!--text-overflow: ellipsis; //超出部分用....代替-->
            <!--overflow: hidden; //超出隐藏-->
            <!--鼠标移动过去的时候显示全部文字,也很简单,给title赋值就可以了-->
            <div style="width: 150px;color: green;text-align: right;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;"
              :title="item.ParamName">
              {{item.ParamName}}&nbsp;&nbsp;<span v-if="item.PureText">(P)</span>
            </div>
          </td>
          <td>
            <Icon type="ios-book-outline" size="18" style="margin-left: 3px;" @click="showParamDesc(item.ParamDesc)"/>
            <!-- transfer="true" 表示是否将弹层放置于 body 内,
              在 Tabs、带有 fixed 的 Table 列内使用时,建议添加此属性,它将不受父级样式影响,从而达到更好的效果-->
            <Select style="width: 350px;" v-if="item.ParamChoices" v-model="item.ParamValue" :transfer="true">
              <Option v-for="choice in item.ParamChoices" :value="choice" :key="choice">
                {{choice}}
              </Option>
            </Select>
            <Input style="width: 350px;" v-else size="small" v-model.trim="item.ParamValue" readonly type="text" placeholder="输入内容"/>
          </td>
          <td class="moreToolBox">
            <Button v-if="!item.ParamChoices" type="success" size="small" @click="handleReload(index, true)">查看/编辑</Button>
            <span style="position: relative;">
              <span class="moreTool">
                <Icon type="ios-copy" size="20" title="复制属性名" @click="handleQuickOperation(index, 0)"/>
                <Icon type="md-copy" size="20" title="复制属性值" @click="handleQuickOperation(index, 1)"/>
              </span>
            </span>
          </td>
        </tr>
        <div><span style="color: red;">{{getValidateErrors(item.ParamName)}}</span></div>
      </div>

      <ParamInputEditDialog ref="paramInputEditDialog" @handleSubmit="refreshParamInputSchemaItems"
                           :param-input-schema-items="paramInputSchemaItems" @handleReload="handleReload"/>
    </table>
  </div>
</template>

<script>
  import ParamInputEditDialog from "./ParamInputEditDialog"
  import {LoadValidateResult} from "../../../../api"
  import {copyText} from "../../../../tools";

  export default {
    name: "ParamInputEdit",
    components:{ParamInputEditDialog},
    props:{
      workId: {
        type: Number,
        default: -1
      },
      workStepId: {
        type: Number,
        default: -1
      },
      paramInputSchemaItems:{
        type: Array,
        default: () => [],
      },
    },
    data(){
      return {
        validateErrors:[],
      }
    },
    methods:{
      // 快捷操作
      handleQuickOperation: function (index, type) {
        var _this = this;
        if (type === 0) {    // 复制属性名
          copyText(this.paramInputSchemaItems[index].ParamName, function () {
            _this.$Message.success("复制成功!");
          });
        } else if (type === 1){    // 复制属性值
          copyText(this.paramInputSchemaItems[index].ParamValue, function () {
            _this.$Message.success("复制成功!");
          });
        }
      },
      // 根据 paramIndex 重新加载
      handleReload: function(paramIndex, refreshOutput){
        if(paramIndex >=0 && paramIndex <= this.paramInputSchemaItems.length -1){
          let item = this.paramInputSchemaItems[paramIndex];
          this.$refs["paramInputEditDialog"].refreshParamInput(paramIndex, item, refreshOutput);
        }
      },
      // 强制刷新组件
      refreshParamInputSchemaItems:function (label, text, pureText) {
        for(var i=0; i<this.paramInputSchemaItems.length; i++){
          var paramInputSchemaItem = this.paramInputSchemaItems[i];
          if(paramInputSchemaItem.ParamName === label){
            paramInputSchemaItem.ParamValue = text;
            paramInputSchemaItem.PureText = pureText;
            this.$set(this.paramInputSchemaItems, i, paramInputSchemaItem);
            this.$Message.success('临时参数保存成功!');
          }
        }
      },
      showParamDesc:function (paramDesc) {
        this.$Modal.info({
          title: "使用说明",
          content: paramDesc
        });
      },
      refreshWorkValidateDetail: async function(){
        const result = await LoadValidateResult(this.workId);
        if(result.status === "SUCCESS"){
          let validateDetails = result.details;
          this.validateErrors = validateDetails.filter(validateDetail => this.workId === validateDetail.work_id
            && this.workStepId === validateDetail.work_step_id);
        }else{
          this.$Message.error(result.errorMsg);
        }
      },
      getValidateErrors:function (paramName) {
        let _validateErrors = this.validateErrors.filter(validateDetail => validateDetail.param_name === paramName)
          .map(validateDetail => validateDetail.detail).join(",");
        return _validateErrors;
      }
    },
  }
</script>

<style scoped>
  .moreTool {
    position: absolute;
    top: -5px;
    right: -160px;
    width:150px;
    padding: 3px 10px;
    background-color: #eee;
    border-radius: 5px;
    z-index: 10;
    display: none;
    cursor: pointer;
  }
  .moreToolBox:hover .moreTool {
    display: inline-block;
    animation: moveLeft 0.2s linear infinite;
    animation-iteration-count: 1;
    animation-fill-mode: forwards;
  }
  @keyframes moveLeft {
    0% {
      right: -160px;
    }
    100% {
      right: -150px;
    }
  }
</style>
