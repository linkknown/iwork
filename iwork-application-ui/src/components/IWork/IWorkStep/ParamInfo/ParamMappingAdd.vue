<template>
  <ISimpleBtnTriggerModal ref="triggerModal" btn-text="新增" btn-size="small" modal-title="新增" :modal-width="700" @btnClick="showDialog">
    <Input type="textarea" :rows="5" style="margin-bottom: 20px;" v-model.trim="paramMappingName"
           placeholder="请输入新增的字段名称"></Input>
    <div>
      <div style="margin-top: 15px;margin-bottom: 10px;">推荐字段：</div>
      <CheckboxGroup v-model="choosed_recommend_fields">
        <Checkbox v-for="recommend_field in recommend_fields" :label="recommend_field">
          <span>{{recommend_field}}</span>
        </Checkbox>
      </CheckboxGroup>
    </div>

    <div v-if="record_param_fields && record_param_fields.length > 0">
      <div style="margin-top: 15px;margin-bottom: 10px;">自动检测字段：</div>
      <CheckboxGroup v-model="choosed_record_param_fields">
        <Checkbox v-for="record_param_field in record_param_fields" :label="record_param_field">
          <span>{{record_param_field}}</span>
        </Checkbox>
      </CheckboxGroup>
    </div>

    <Row style="text-align: right;">
      <Button type="success" @click="handleSubmit()" style="margin-top: 6px">提交</Button>
    </Row>
  </ISimpleBtnTriggerModal>
</template>

<script>
  import ISimpleBtnTriggerModal from "../../../Common/modal/ISimpleBtnTriggerModal"
  import {LoadRecordParamData} from "../../../../api"
  import {checkNotEmpty} from "../../../../tools";

  export default {
    name: "ParamMappingAdd",
    components:{ISimpleBtnTriggerModal},
    props:{
      workId: {
        type: [Number, String],
        default: -1,
      },
    },
    data(){
      return {
        paramMappingName:"",
        recommend_fields: ["status", "result", "errorMsg", "current_page", "offset", "page_size", "paginator", "id"],
        choosed_recommend_fields:[],
        record_param_fields:[],
        choosed_record_param_fields:[],
      }
    },
    methods:{
      handleSubmit:function () {
        let choosed_fields = Array.from(new Set([].concat(this.choosed_recommend_fields).concat(this.choosed_record_param_fields)));
        choosed_fields.forEach(choosed_field => {
          this.$emit("handleSubmit", choosed_field);
        });
        this.paramMappingName.split(",").forEach(_paramMappingName => {
          if(_paramMappingName.trim() !== ""){
            this.$emit("handleSubmit", _paramMappingName.trim());
          }
        });
        this.$refs.triggerModal.hideModal();
      },
      loadRecordParamData:async function () {
        if (this.workId < 0 && checkNotEmpty(this.$route.query.work_id)) {
          this.workId = parseInt(this.$route.query.work_id);
        }
        const result = await LoadRecordParamData({work_id: this.workId});
        if (result.status === "SUCCESS") {
          this.record_param_fields = result.record_param_fields;
        }
      },
      showDialog: function () {
        this.loadRecordParamData();
      }
    },
  }
</script>

<style scoped>

</style>
