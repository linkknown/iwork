<template>
 <ISimpleBtnTriggerModal ref="triggerModal" btn-text="项目校验" btn-size="small" modal-title="查看校验结果" :modal-width="1000" modal-top="50px">
  <Button type="success" size="small" @click="validateWork">校验</Button>
  <Button type="success" size="small" @click="refreshValidateResult">刷新校验结果</Button>

  <div style="margin: 20px;min-height: 300px;">
    <p style="color: green;">last tracking_id: {{tracking_id}}</p>
    <Scroll height="350">
      <Table border :columns="columns1" :data="validateDetails" size="small"></Table>
    </Scroll>
  </div>
</ISimpleBtnTriggerModal>
</template>

<script>
  import ISimpleBtnTriggerModal from "../../Common/modal/ISimpleBtnTriggerModal"
  import {ValidateWork} from "../../../api/index"
  import {LoadValidateResult} from "../../../api/index"
  import {checkEmpty} from "../../../tools/index"

  export default {
    name: "WorkValidate",
    components:{ISimpleBtnTriggerModal},
    props:{
      work_id:{
        type: Number,
        default: -1,
      }
    },
    data(){
      return {
        showDetailDialog:false,
        validating:false,
        validateDetails:[],
        tracking_id:'',
        columns1: [
          {
            title: 'work_name',
            key: 'work_name',
          },
          {
            title: 'work_step_name',
            key: 'work_step_name',
          },
          {
            title: 'detail',
            key: 'detail',
            width: 500,
            render: (h,params)=>{
              return h('span',{
                style: {
                  color: checkEmpty(this.validateDetails[params.index]['work_step_name']) ? 'green': 'red',
                  overflow: 'hidden',         // 内容超出不换行
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                },
                attrs:{
                  title: this.validateDetails[params.index]['detail'],
                },
              },this.validateDetails[params.index]['detail']
              )
            }
          },
          {
            title: '操作',
            key: 'operate',
            render: (h, params) => {
              return h('div', [
                h('Button', {
                  props: {
                    type: 'success',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px',
                  },
                  on: {
                    click: () => {
                      this.$Modal.confirm({
                        title:"确认",
                        width: 1000,
                        content:params.row.detail,
                      });
                    }
                  }
                }, '详情'),
              ]);
            }
          }
        ],
      }
    },
    methods:{
      validateWork:async function () {
        if(this.validating == true){
          this.$Message.error("校验中,请稍后！");
        }else{
          this.validating = true;
          const result = await ValidateWork(this.work_id);
          if(result.status == "SUCCESS"){
            this.refreshValidateResult();
          }
          this.validating = false;
        }
      },
      refreshValidateResult: async function () {
        const result = await LoadValidateResult(this.work_id);
        if(result.status == "SUCCESS"){
          this.validateDetails = result.details;
          this.tracking_id = result.details[0].tracking_id;
        }else{
          this.$Message.error(result.errorMsg);
        }
      }
    }
  }
</script>

<style scoped>

</style>
