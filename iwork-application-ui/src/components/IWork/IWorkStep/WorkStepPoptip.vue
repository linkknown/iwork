<template>
  <div v-if="workstep" style="width: 500px;padding: 10px;">
    <Scroll>
      <div style="text-align: right;">
        <Button size="small" type="success" v-if="workstep.work_step_type == 'work_sub'"
                @click="gotoWorkDetail(workstep.work_id, workstep.work_sub_id)">进入</Button>
      </div>

      <Row>
        <Col span="8">输入参数</Col>
        <Col span="8">输出参数</Col>
        <Col span="8">引用关系</Col>
        <Col span="8" v-if="ParamInputSchemaItems">
          <ul>
            <li v-for="item in ParamInputSchemaItems">
              <Tag @click.native="showNotice(item.ParamName, item.ParamValue)">{{item.ParamName}}</Tag>
            </li>
            <li><a @click="showAllNoticeWithId(workstep.work_step_id)">全部参数</a></li>
          </ul>
        </Col>
        <Col span="8" v-if="ParamInputSchemaItems">
          <ul>
            <li v-for="item in ParamOutputSchemaItems">
              <Tag>{{item.ParamName}}</Tag>
            </li>
          </ul>
        </Col>
        <Col span="8" v-if="usedMap">
        <span v-for="(usedWorkStepIds, currentWorkStepId) in usedMap">
          <span v-if="currentWorkStepId == workstep.work_step_id">
            <li style="list-style: none;" v-if="usedWorkStepIds" v-for="usedWorkStepId in usedWorkStepIds">
              <Tag @click.native="showAllNoticeWithId(usedWorkStepId)">{{usedWorkStepId | renderWorkStepName }}</Tag>
            </li>
          </span>
        </span>
        </Col>
      </Row>
    </Scroll>
  </div>
</template>

<script>
  import {checkEmpty} from "../../../tools"
  import {GetRelativeWork} from "../../../api"

  var global_this;
  export default {
    name: "WorkStepPoptip",
    props:{
      worksteps:{
        type: Array,
        default: [],
      },
      workstep:{
        type: Object,
        default: null,
      },
      usedMap:{
        type:Object,
        default: null,
      }
    },
    methods:{
      gotoWorkDetail:async function(work_id, work_sub_id){
        const result = await GetRelativeWork(work_id);
        const work_subs = result.subworks.filter(subWork => subWork.id === work_sub_id);
        if(work_subs.length > 0){
          this.$router.push({ path: '/iwork/workstepList',
            query: { work_id: work_subs[0].id, work_name: work_subs[0].work_name, parent_href: location.href }});
        }
      },
      showNotice:function (paramName, paramValue) {
        this.$Notice.success({
          title: "参数名称：" + paramName,
          desc: "参数值：" + paramValue,
        });
      },
      showAllNoticeWithId:function(workStepId){
        let workstep = this.worksteps.filter(workstep => workstep.work_step_id == workStepId)[0];
        let paramInputSchemaItems =
            checkEmpty(workstep.work_step_input) ? null : JSON.parse(workstep.work_step_input).ParamInputSchemaItems;
        if(paramInputSchemaItems != null){
          var paramValueAll = "";
          for(var index in paramInputSchemaItems){
            const item = paramInputSchemaItems[index];
            paramValueAll += "<span style='color:green;'>" + item.ParamName + "</span>" + ":" + "<span style='color:blue;'>" + item.ParamValue + "</span><br/><br/>";
          }
          this.$Notice.success({
            title: "全部参数",
            duration: 0,  // 0 则不自动关闭
            render: h => {
              return h('div', {
                style:'word-break: break-all;',
                domProps: {
                  innerHTML: paramValueAll,
                },
              });
            }
          });
        }
      },
    },
    computed:{
      ParamInputSchemaItems () {
        return checkEmpty(this.workstep.work_step_input) ? null : JSON.parse(this.workstep.work_step_input).ParamInputSchemaItems;
      },
      ParamOutputSchemaItems () {
        return checkEmpty(this.workstep.work_step_output) ? null : JSON.parse(this.workstep.work_step_output).ParamOutputSchemaItems;
      }
    },
    beforeCreate: function () {
      global_this = this;         // 在 beforeCreate中将vue实例赋值给全局变量 global_this,然后filters中即可通过 global_this 获取data中数据
    },
    filters:{
      renderWorkStepName:function (workStepId) {
        return global_this.worksteps.filter(workStep => workStep.work_step_id == workStepId)[0].work_step_name;
      }
    }
  }
</script>

<style scoped>
  ul li{
    list-style: none;
  }
</style>
