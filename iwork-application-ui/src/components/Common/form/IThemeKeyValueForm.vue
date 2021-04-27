<template>
  <!-- 表单信息 -->
  <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="150">
    <FormItem :label="formThemeLabel" prop="formtheme">
      <Select v-model.trim="formValidate.formtheme" :placeholder="formThemePlaceholder">
        <Option v-for="formtheme in formthemes" :value="formtheme">{{formtheme}}</Option>
      </Select>
    </FormItem>
    <FormItem :label="formKeyLabel" prop="formkey">
      <Input v-model.trim="formValidate.formkey" :placeholder="formKeyPlaceholder"></Input>
    </FormItem>
    <FormItem :label="formValueLabel" prop="formvalue">
      <Input v-model.trim="formValidate.formvalue" type="textarea" :rows="4" :placeholder="formValuePlaceholder"></Input>
    </FormItem>
    <FormItem style="text-align: center;">
      <Button type="success" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交</Button>
      <Button type="warning" @click="handleReset('formValidate')" style="margin-right: 6px">重置</Button>
    </FormItem>
  </Form>
</template>

<script>
  export default {
    name: "IThemeKeyValueForm",
    props:{
      formthemes:{
        type:Array,
        default: function () {
          return ['test1','test2'];
        },
      },
      formThemeLabel:{
        type:String,
        default:'formThemeLabel',
      },
      formKeyLabel:{
        type:String,
        default:'formKeyLabel',
      },
      formValueLabel:{
        type:String,
        default:'formValueLabel',
      },
      formThemePlaceholder:{
        type:String,
        default:'请输入 formThemePlaceholder',
      },
      formKeyPlaceholder:{
        type:String,
        default:'请输入 formKey',
      },
      formValuePlaceholder:{
        type:String,
        default:'请输入 formValue',
      },
      formkeyValidator:{
        type: Function,
      },
      formvalueValidator:{
        type: Function,
      }
    },
    data(){
      return {
        formValidate: {
          formmode: null,    // 用户回显使用
          formtheme:'',
          formkey: '',
          formvalue: '',
        },
        ruleValidate: {
          formtheme:[
            { required: true, message: 'Please select the formtheme', trigger: 'change' }
          ],
          formkey: [
            { validator: this.formkeyValidator, trigger: 'blur' },
            { required: true, message: 'formkey 不能为空!', trigger: 'blur' }
          ],
          formvalue: [
            { validator: this.formvalueValidator, trigger: 'blur' },
            { required: true, message: 'formvalue 不能为空!', trigger: 'blur' }
          ],
        },
      }
    },
    methods:{
      handleSubmit (name) {
        this.$refs[name].validate((valid) => {
          if (valid) {
            this.$emit("handleSubmit", this.formValidate.formmode, this.formValidate.formtheme, this.formValidate.formkey, this.formValidate.formvalue);
          }
        })
      },
      handleSubmitSuccess (msg){
        this.$Message.success(msg);
        this.handleReset('formValidate');
      },
      handleSubmitError (msg){
        this.$Message.error(msg);
      },
      initFormData (formmode, formtheme, formkey, formvalue){
        this.formValidate.formmode = formmode;
        this.formValidate.formtheme = formtheme;
        this.formValidate.formkey = formkey;
        this.formValidate.formvalue = formvalue;
      },
      handleReset (name) {
        this.$refs[name].resetFields();
      },
    }
  }
</script>

<style scoped>

</style>
