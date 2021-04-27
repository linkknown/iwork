<template>
  <!-- 按钮触发模态框 -->
  <!-- ref 的作用是为了在其它地方方便的获取到当前子组件 -->
  <ISimpleBtnTriggerModal ref="triggerModal" btn-text="新增资源信息" btn-size="small"
    modal-title="新增/编辑资源信息" modal-top="50px" :modal-width="800" @btnClick="handleBtnClick">
    <!-- 表单信息 -->
    <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="140">
      <FormItem label="resource_name" prop="resource_name">
        <Input v-model.trim="formValidate.resource_name" placeholder="请输入 resource_name"></Input>
      </FormItem>
      <FormItem label="resource_type" prop="resource_type">
        <Select v-model="formValidate.resource_type" placeholder="请选择 resource_type">
          <Option v-for="(supportResourceType, index) in supportResourceTypes" :value="supportResourceType.resource_type">
            {{supportResourceType.resource_type}}
          </Option>
        </Select>
      </FormItem>

      <div v-for="(supportResourceType, index) in supportResourceTypes" v-if="supportResourceType.resource_type === formValidate.resource_type"
        style="margin:5px 0 10px 30px;padding: 5px 10px;background-color: #f3f3f3;">
        <div>
          格式：{{supportResourceType.resource_format}}
        </div>
        <div style="word-break: break-all; overflow:auto;">
          示例：{{supportResourceType.resource_demo}}
        </div>
      </div>

      <FormItem
        v-for="(item, index) in formValidate.formDynamic.items"
        :key="index"
        :label="item.name"
        :prop="'formDynamic.items.' + index + '.value'"
        :rules="{required: true, message: item.name + '不能为空', trigger: 'blur'}">
        <div style="position: relative;">
          <Input type="textarea" :rows="3" v-model="item.value" placeholder="Enter something..."></Input>

          <div style="position: absolute; right: 10px;top: 40px;">
            <Poptip v-model="item.visible" placement="left-start" width="550" :word-wrap="true">
              <a href="javascript:;">(可)选择全局变量</a>
              <div slot="content">
                <span v-for="(globalVar,index) in globalVars" style="margin: 5px;float: left;">
                  <Tag><a @click="chooseGlobalVar(item, '$Global.' + globalVar)">$Global.{{globalVar}}</a></Tag>
                </span>
              </div>
            </Poptip>
          </div>
        </div>
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
  import {EditResource, GetAllGlobalVars, GetResourceById} from "../../../api"
  import WorkStepComponent from "../IWorkStep/WorkStepComponent";
  import {checkNotEmpty} from "../../../tools";

  export default {
    name: "ResourceEdit",
    components: {WorkStepComponent, ISimpleBtnTriggerModal},
    data(){
      return {
        supportResourceTypes: [
          {
            resource_type: 'db',
            resource_format: 'resource_url|||resource_username|||resource_passwd',
            resource_demo: 'jdbc:mysql://127.0.0.1:3306/isoft_iwork?serverTimezone=UTC&useUnicode=true&characterEncoding=gbk&autoReconnect=true&failOverReadOnly=false|||root|||123456',
          },
          {
            resource_type: 'redis',
            resource_format: 'redis_host|||redis_port',
            resource_demo: 'localhost|8888',
          },
          {
            resource_type: 'sftp',
            resource_format: 'resource_url|||resource_username|||resource_passwd',
            resource_demo: 'url2|username2|passwd2',
          },
          {
            resource_type: 'ssh',
            resource_format: 'url3|||username3|||passwd3',
            resource_demo: 'url3|username3|passwd3',
          }
        ],
        globalVars: [],
        formValidate: {
          resource_id: -1,
          resource_name: '',
          resource_type: '',
          resource_link: '',
          env_name: '',
          // resource_link 动态表单项
          formDynamic: {
            items: [
              // {
              //   name: '',
              //   value: '',
              //   index: 1,
              //   visible:false,
              // }
            ]
          },
        },
        ruleValidate: {
          resource_name: [
            { required: true, message: 'resource_name 不能为空!', trigger: 'blur' }
          ],
          resource_type: [
            { required: true, message: 'resource_type 不能为空!', trigger: 'change' }
          ],
          resource_link: [
            { required: true, message: 'resource_link 不能为空!', trigger: 'blur' }
          ],
        },
      }
    },
    methods:{
      handleBtnClick: function () {
        // 表单重置
        this.formValidate.resource_id = -1;
        this.$refs.formValidate.resetFields();
      },
      // 选择全局变量
      chooseGlobalVar: function (item, globalVar) {
        item.value = globalVar;
        item.visible = false;
      },
      initData:async function(resource_id){
        const result = await GetResourceById(resource_id);
        if(result.status == "SUCCESS"){
          this.$refs.triggerModal.triggerClick();

          this.formValidate.resource_id =result.resource.id;
          this.formValidate.resource_name =result.resource.resource_name;
          this.formValidate.resource_type =result.resource.resource_type;
          this.formValidate.resource_link =result.resource.resource_link;
          this.formValidate.env_name =result.resource.env_name;

          this.$nextTick(() => {
            // resource_link 分割成多部分
            let resource_link_parts = result.resource.resource_link.split("|||");
            for (var index=0; index < resource_link_parts.length; index++) {
              // 每一部分重新回显表单
              this.formValidate.formDynamic.items[index].value = resource_link_parts[index];
            }
          });
        }
      },
      handleSubmit (name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            this.formValidate.resource_link = this.formValidate.formDynamic.items.map(item => item.value).join("|||");
            const result = await EditResource(this.formValidate);
            if(result.status == "SUCCESS"){
              this.$Message.success('提交成功!');
              // 调用子组件隐藏 modal (this.refs.xxx.子组件定义的方法())
              this.$refs.triggerModal.hideModal();
              this.handleReset('formValidate');
              // 通知父组件添加成功
              this.$emit('handleSuccess');
            }else{
              this.$Message.error('提交失败!');
            }
          }
        })
      },
      handleReset (name) {
        this.$refs[name].resetFields();
      },
      refreshAllGlobalVars: async function () {
        const result = await GetAllGlobalVars();
        if (result.status == "SUCCESS") {
          this.globalVars = result.globalVars;
        }
      }
    },
    watch: {
      // 监听 resource_type 变化
      'formValidate.resource_type': function (newVal, oldVal) {
        // resource_format 按照 ||| 分割后的参数列表
        // 设置初始为空
        var old_resource_format_items = [];
        var new_resource_format_items = [];
        if (checkNotEmpty(oldVal)) {
          // 根据 newVal 和 oldVal 找到对应的 supportResourceType
          var oldSupportResourceType = this.supportResourceTypes.filter(supportResourceType => supportResourceType.resource_type == oldVal)[0];
          // 分割得到 resource_format 参数列表
          old_resource_format_items = oldSupportResourceType.resource_format.split("|||");

          var _this = this;
          // 切换前记录旧的值
          this.formValidate.formDynamic.items.forEach(item => {
            // key 为 resource_type::formValidate.formDynamic.items:: resource_name, value为具体的值
            localStorage.setItem(oldVal + "::formValidate.formDynamic.items::" + item.name, item.value);
            // key 为 formValidate.formDynamic.items:: resource_name, value为具体的值
            localStorage.setItem("formValidate.formDynamic.items::" + item.name, item.value);
          })
        }

        if (checkNotEmpty(newVal)) {
          var newSupportResourceType = this.supportResourceTypes.filter(supportResourceType => supportResourceType.resource_type == newVal)[0];
          new_resource_format_items = newSupportResourceType.resource_format.split("|||");
        }

        this.formValidate.formDynamic.items.splice(0, this.formValidate.formDynamic.items.length);   //清空数组
        for (var index=0; index<new_resource_format_items.length; index++) {
          // 切换 resource_type 支持回显
          let historyValue = localStorage.getItem(newVal + "::formValidate.formDynamic.items::" + new_resource_format_items[index]) ||
            localStorage.getItem("formValidate.formDynamic.items::" + new_resource_format_items[index]) || '';
          // 动态表单模板
          var formDynamicItemTmpl = {name: new_resource_format_items[index], value: historyValue, index: index, visible:false,};
          // 添加进行
          this.formValidate.formDynamic.items.push(formDynamicItemTmpl);
        }
      },
    },
    mounted() {
      this.refreshAllGlobalVars();
    }
  }
</script>

<style scoped>

</style>



