<template>
  <!-- 按钮触发模态框 -->
  <!-- ref 的作用是为了在其它地方方便的获取到当前子组件 -->
  <ISimpleBtnTriggerModal ref="triggerModal" btn-text="新增 Appid" btn-size="small" modal-title="新增/编辑 Appid"
                          modal-top="50px" :modal-width="600">
    <!-- 表单信息 -->
    <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="140">
      <FormItem label="app_name" prop="app_name">
        <Input v-model.trim="formValidate.app_name" placeholder="请输入 app_name"></Input>
      </FormItem>
      <FormItem label="app_desc" prop="app_desc">
        <Input type="textarea" :rows="3" v-model.trim="formValidate.app_desc" placeholder="请输入 app_desc"></Input>
      </FormItem>
      <FormItem>
        <Button type="success" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交</Button>
        <Button type="warning" @click="handleReset('formValidate')" style="margin-right: 6px">重置</Button>
      </FormItem>
    </Form>
  </ISimpleBtnTriggerModal>
</template>

<script>
  import ISimpleBtnTriggerModal from "../../Common/modal/ISimpleBtnTriggerModal"
  import {EditAppid} from "../../../api"

  export default {
    name: "AppidEdit",
    components: {ISimpleBtnTriggerModal},
    data() {
      return {
        formValidate: {
          id: 0,
          app_name: '',
          app_desc: '',
        },
        ruleValidate: {
          app_name: [
            {required: true, message: 'app_name 不能为空!', trigger: 'blur'}
          ],
          app_desc: [
            {required: true, message: 'app_desc 不能为空!', trigger: 'blur'}
          ],
        }
      }
    },
    methods: {
      handleSubmit(name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            const result = await EditAppid({
              id: this.formValidate.id,
              app_name: this.formValidate.app_name,
              app_desc: this.formValidate.app_desc
            });
            if (result.status == "SUCCESS") {
              this.$Message.success('提交成功!');
              // 调用子组件隐藏 modal (this.refs.xxx.子组件定义的方法())
              this.$refs.triggerModal.hideModal();
              // 通知父组件添加成功
              this.$emit('handleSuccess');
            } else {
              this.$Message.error('提交失败!');
            }
          }
        })
      },
      handleReset(name) {
        this.$refs[name].resetFields();
      },
      initData(appId) {
        this.formValidate = appId;
        this.$refs.triggerModal.triggerClick();
      }
    }
  }
</script>

<style scoped>

</style>
