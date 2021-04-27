<template>
  <Modal
    v-model="showFormModal"
    :width="800"
    title="新增/编辑 Work"
    :footer-hide="true"
    :transfer="false"
    :mask-closable="false"
    :styles="{top: '50px'}">
    <div>
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="100">
        <FormItem label="模块名称" prop="module_name">
          <div style="position: relative;">
            <Input v-model.trim="formValidate.module_name" readonly="readonly" placeholder="请选择模块"/>

            <div style="position: absolute; right: 10px;top: 0;">
              <Poptip v-model="visible" placement="left-start" width="620">
                <a href="javascript:;">选择模块</a>
                <div slot="content">
                  <span v-for="module in modules" style="margin: 5px;float: left;">
                    <Tag><a @click="closePoptip(module.module_name)">{{module.module_name}}</a></Tag>
                  </span>
                </div>
              </Poptip>
            </div>
          </div>
        </FormItem>
        <FormItem label="流程名称" prop="work_name">
          <Input v-model.trim="formValidate.work_name" placeholder="请输入流程名称"></Input>
        </FormItem>
        <FormItem label="流程描述" prop="work_desc">
          <Input v-model.trim="formValidate.work_desc" type="textarea" :rows="4" placeholder="请输入流程描述"></Input>
        </FormItem>
        <Row>
          <Col span="12">
            <FormItem label="流程类型" prop="work_type">
              <Select :transfer="true" v-model="formValidate.work_type">
                <Option value="filter" key="filter">filter</Option>
                <Option value="work" key="work">work</Option>
                <Option value="quartz" key="quartz">quartz</Option>
              </Select>
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="缓存结果" prop="cache_result">
              <Select :transfer="true" v-model="formValidate.cache_result">
                <Option value="true" key="true">true</Option>
                <Option value="false" key="false">false</Option>
              </Select>
            </FormItem>
          </Col>
        </Row>
        <FormItem v-if="formValidate.work_type === 'quartz'" label="cron表达式" prop="work_cron">
          <Input v-model.trim="formValidate.work_cron" placeholder="请输入cron表达式"></Input>

          <div style="margin-top: 10px;">
            <h3>cron举例说明</h3>
            <p>每隔5秒执行一次：<span style="color: red;">*/5 * * * * ?</span></p>
            <p>每隔1分钟执行一次：<span style="color: red;">0 */1 * * * ?</span></p>
            <p>每天23点执行一次：<span style="color: red;">0 0 23 * * ?</span></p>
            <p>每天凌晨1点执行一次：<span style="color: red;">0 0 1 * * ?</span></p>
            <p>每月1号凌晨1点执行一次：<span style="color: red;">0 0 1 1 * ?</span></p>
            <p>在26分、29分、33分执行一次：<span style="color: red;">0 26,29,33 * * * ?</span></p>
            <p>每天的0点、13点、18点、21点都执行一次：<span style="color: red;">0 0 0,13,18,21 * * ?</span></p>
          </div>
        </FormItem>
        <FormItem>
          <Button type="success" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交</Button>
          <Button type="warning" @click="handleReset('formValidate')" style="margin-right: 6px">重置</Button>
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>

<script>
  import {GetAllModules, EditWork} from "../../../api"
  import {validateCommonPatternForString} from "../../../tools/index"


  const workNameValidator = (rule, value, callback) => {
    if (value === '') {
      callback(new Error('字段值不能为空!'));
    } else if (!validateCommonPatternForString(value)) {
      callback(new Error('存在非法字符，只能包含字母，数字，下划线!'));
    } else {
      callback();
    }
  }

  export default {
    name: "WorkEdit",
    data () {
      return {
        modules: [],
        visible:false,
        showFormModal: false,
        formValidate: {
          id: -1,
          work_name: '',
          work_desc: '',
          work_type: '',
          module_name: '',
          cache_result: '',
          work_cron: '',
        },
        ruleValidate: {
          work_name: [
            { validator: workNameValidator, trigger: 'blur' },
            {required: true, message: 'work_name 不能为空!', trigger: 'blur'}
          ],
          work_desc: [
            {required: true, message: 'app_desc 不能为空!', trigger: 'blur'}
          ],
          work_type: [
            {required: true, message: 'work_type 不能为空!', trigger: 'blur,change'}
          ],
          module_name: [
            {required: true, message: 'module_name 不能为空!', trigger: 'blur,change'}
          ],
          cache_result: [
            {required: true, message: 'cache_result 不能为空!', trigger: 'blur,change'}
          ],
          work_cron: [
            {required: true, message: 'work_cron 不能为空!', trigger: 'blur,change'}
          ],
        }
      }
    },
    methods: {
      handleSubmit (name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            const result = await EditWork(this.formValidate);
            if (result.status === "SUCCESS") {
              this.showFormModal = false;
              this.$Message.success('提交成功!');
              this.$emit("handleSubmitSuccess");
            }else{
              this.$Message.error('提交失败!');
            }
          } else {
            this.$Message.error('校验失败!');
          }
        })
      },
      handleReset (name) {
        this.formValidate.id = -1;
        this.$refs[name].resetFields();
      },
      closePoptip (module_name) {
        this.formValidate.module_name=module_name;
        this.visible = false;
      },
      refreshAllModules:async function(){
        const result = await GetAllModules();
        if (result.status === "SUCCESS") {
          this.modules = result.moudles;
        }
      },
      initData (work) {
        if (work) {
          this.formValidate.id = work.id;
          this.formValidate = work;
          this.formValidate.cache_result = work.cacheResult === true ? "true" : "false";
        } else {
          this.$refs['formValidate'].resetFields();
        }
        this.showFormModal = true;
      }
    },
    mounted () {
      this.refreshAllModules();
    }
  }
</script>

<style scoped>

</style>
