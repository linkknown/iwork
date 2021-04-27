<template>
  <Modal
    v-model="showFormModal"
    width="850"
    title="查看/编辑 workstep"
    :footer-hide="true"
    :mask-closable="false"
    :styles="{top: '20px'}"
    :scrollable="true">
    <div>
      <Row>
        <Col span="8">
          <Scroll height="450">
            <p v-for="workstep in worksteps">
              <Tag><span @click="reloadWorkStepBaseInfo(workstep.work_step_id, workstep.work_step_name)">{{workstep.work_step_name}}</span></Tag>
            </p>
          </Scroll>
        </Col>
        <Col span="16">
          <Spin size="large" fix v-if="spinShow"></Spin>
          <!-- 表单信息 -->
          <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="140">
            <FormItem label="work_id" prop="work_id">
              <Input v-model.trim="formValidate.work_id" readonly placeholder="请输入 work_id"></Input>
            </FormItem>
            <FormItem label="work_step_id" prop="work_step_id">
              <Input v-model.trim="formValidate.work_step_id" readonly placeholder="请输入 work_step_id"></Input>
            </FormItem>
            <FormItem label="work_step_name" prop="work_step_name">
              <Input v-model.trim="formValidate.work_step_name" placeholder="请输入 work_step_name"></Input>
            </FormItem>
            <FormItem label="work_step_type" prop="work_step_type">
              <Row>
                <Col span="18">
                  <Input v-model.trim="formValidate.work_step_type" placeholder="请输入 work_step_type"></Input>
                </Col>
                <Col span="6">
                  <Poptip v-model="visible" placement="left" width="420">
                    <Button style="margin-left: 5px;">选择步骤类型</Button>
                    <div slot="content">
                      <span v-for="default_work_step_type in nodeMetas" style="margin: 5px;float: left;">
                        <Tag><span @click="closePoptip(default_work_step_type.name)">{{default_work_step_type.name}}</span></Tag>
                      </span>
                    </div>
                  </Poptip>
                </Col>
              </Row>
            </FormItem>
            <FormItem label="is_defer" prop="is_defer">
              <Select v-model="formValidate.is_defer">
                <Option value="true">true</Option>
                <Option value="false">false</Option>
              </Select>
            </FormItem>
            <FormItem label="work_step_desc" prop="work_step_desc">
              <Input v-model.trim="formValidate.work_step_desc" type="textarea" :rows="4" placeholder="请输入 work_step_desc"></Input>
            </FormItem>
            <FormItem>
              <Button type="success" size="small" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交
              </Button>
              <Button type="warning" size="small" @click="showFormModal = false" style="margin-right: 6px">Close</Button>
            </FormItem>
          </Form>
        </Col>
      </Row>
    </div>
  </Modal>
</template>

<script>
  import {EditWorkStepBaseInfo, LoadWorkStepInfo} from "../../../../api"
  import {validateCommonPatternForString} from "../../../../tools/index"
  import {oneOf} from "../../../../tools"

  export default {
    name: "BaseInfo",
    props:{
      worksteps: {
        type: Array,
        default: () => [],
      },
    },
    data(){
      const _validateWorkStepName = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('字段值不能为空!'));
        } else if (!validateCommonPatternForString(value)) {
          callback(new Error('存在非法字符，只能包含字母，数字，下划线!'));
        } else if (value.length > 30) {
          callback(new Error('长度不能超过30个字符!'));
        } else {
          callback();
        }
      };
      return {
        spinShow:false,           // 加载中
        showFormModal:false,
        visible:false,
        nodeMetas: [],
        formValidate: {
          work_id: -1,
          work_step_id: -1,
          work_step_name: '',
          work_step_type: '',
          work_step_desc: '',
          is_defer: 'false',
        },
        ruleValidate: {
          work_step_name: [
            { validator: _validateWorkStepName, trigger: 'blur' },
          ],
          work_step_type: [
            { required: true, message: 'work_step_type 不能为空!', trigger: 'blur' }
          ],
        },
      }
    },
    methods:{
      closePoptip (type) {
        this.formValidate.work_step_type=type;
        this.visible = false;
      },
      loadWorkStepInfo:async function(){
        this.spinShow = true;
        const result = await LoadWorkStepInfo(this.formValidate.work_id,this.formValidate.work_step_id);
        this.spinShow = false;
        if(result.status == "SUCCESS"){
          this.formValidate.work_step_name = result.step.work_step_name;
          this.formValidate.work_step_type = result.step.work_step_type;
          this.formValidate.work_step_desc = result.step.work_step_desc;
          this.formValidate.is_defer = result.step.is_defer;
        }
      },
      reloadWorkStepBaseInfo:function(work_step_id, work_step_name){
        if(!oneOf(work_step_name, ['start','end'])){  // start 和 end 禁止修改
          this.$emit("reloadWorkStepBaseInfo", work_step_id);
        }
      },
      showWorkStepBaseInfo:function (work_id, work_step_id) {
        // 重置表单,清除缓存
        this.$refs["formValidate"].resetFields();
        this.formValidate.work_id = work_id;
        this.formValidate.work_step_id = work_step_id;
        this.loadWorkStepInfo();
        this.showFormModal = true;

        // 从 store 中获取 nodeMetas
        this.nodeMetas = this.$store.state.nodeMetas;
      },
      handleSubmit (name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            const result = await EditWorkStepBaseInfo(this.formValidate.work_id, this.formValidate.work_step_id,
              this.formValidate.work_step_name,this.formValidate.work_step_desc, this.formValidate.work_step_type, this.formValidate.is_defer);
            if(result.status == "SUCCESS"){
              this.$Message.success('提交成功!');
              // 通知父组件添加成功
              this.$emit('handleSuccess');
              // 重置表单,清除缓存
              this.$refs[name].resetFields();
            }else{
              this.$Message.error('提交失败!参数不合法或者步骤名称已存在!');
            }
          }
        })
      },
    },
  }
</script>

<style scoped>

</style>
