<template>
  <ISimpleConfirmModal ref="globalVarEditModal" modal-title="新增/编辑 GlobalVar" :modal-width="600" :footer-hide="true">
    <!-- 表单信息 -->
    <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="150">
      <FormItem label="变量名称" prop="name">
        <Input v-model.trim="formValidate.name" placeholder="请输入全局变量名称"></Input>
      </FormItem>
      <FormItem label="环境" prop="env_name">
        <Select v-model.trim="formValidate.env_name" placeholder="环境" style="width:100%">
          <Option v-for="item in EnvNameList" :value="item" :key="item">{{ item }}</Option>
        </Select>
      </FormItem>
      <FormItem label="base64加密" prop="encrypt_flag">
        <Select v-model.trim="formValidate.encrypt_flag" placeholder="是否加密" style="width:100%">
          <Option :value="1" key="1">true</Option>
          <Option :value="0" key="2">false</Option>
        </Select>
      </FormItem>
      <FormItem label="变量值" prop="value">
        <Input type="textarea" :rows="3" v-model.trim="formValidate.value" placeholder="请输入变量值"></Input>
      </FormItem>
      <FormItem>
        <Button type="success" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交</Button>
        <Button type="warning" @click="handleReset('formValidate')" style="margin-right: 6px">重置</Button>
      </FormItem>
    </Form>
  </ISimpleConfirmModal>
</template>

<script>
  import ISimpleConfirmModal from "../../Common/modal/ISimpleConfirmModal"
  import {EditGlobalVar} from "../../../api"

  export default {
    name: "GlobalVarEdit",
    components: {ISimpleConfirmModal},
    props:{
      EnvNameList:{
        type:Array,
        required:true,
      }
    },
    data(){
      return {
        formValidate: {
          id: 0,
          name: '',
          env_name: '',
          value: '',
          encrypt_flag: 0,
        },
        ruleValidate: {
          name: [
            {required: true, message: 'name 不能为空!', trigger: 'blur'}
          ],
          env_name: [
            {required: true, message: 'env_name 不能为空!', trigger: 'blur'}
          ],
          value: [
            {required: true, message: 'value 不能为空!', trigger: 'blur'}
          ],
        },
      }
    },
    methods:{
      showModal: function (flag) {
        if (flag) {
          this.$refs.globalVarEditModal.showModal();
        } else {
          this.$refs.globalVarEditModal.hideModal();
        }
      },
      handleSubmit (name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            const result = await EditGlobalVar(this.formValidate);
            if (result.status === "SUCCESS") {
              this.handleSubmitSuccess("提交成功!");
              this.showModal(false);
              this.$emit("handleSubmit");
            } else {
              this.$refs.GlobalVarEdit.handleSubmitError("提交失败!");
            }
          }
        })
      },
      handleSubmitSuccess (msg){
        this.handleReset('formValidate');
        this.$Message.success(msg);
      },
      handleSubmitError (msg){
        this.$Message.error(msg);
      },
      initFormData(globalVar) {
        this.handleReset('formValidate');
        this.showModal(true);
        if (globalVar) {
          globalVar.encrypt_flag = globalVar.encrypt_flag ? 1 : 0;
          this.formValidate = globalVar;
        }
      },
      handleReset (name) {
        this.$refs[name].resetFields();
      },
    }
  }
</script>

<style scoped>

</style>
