<template>
  <div>
    <Select v-model="current_filter_id" style="width:400px" @on-change="chooseFilter">
      <Option v-for="filterWork in filterWorks" :value="filterWork.id" :key="filterWork.work_name">
        {{filterWork.work_name}}
      </Option>
    </Select>

    <div v-for="module in modules" style="list-style: none;margin-top: 20px;">
      <Row style="border-bottom: 1px solid rgba(0,34,232,0.16);">
        <Col span="4">
          <Button type="success" size="small" @click="handleCheckAll(module.module_name)">全选</Button>
          <Tag><span>{{module.module_name}}</span></Tag>
        </Col>
        <Col span="20">
          <CheckboxGroup v-model="choosedWorkNames">
            <Checkbox :label="moduleWork.work_name" v-for="moduleWork in filterWorksWithModule(module.module_name)"></Checkbox>
          </CheckboxGroup>
        </Col>
      </Row>
    </div>

    <Input v-model.trim="complexWorkName" type="textarea" :rows="4" placeholder="复杂过滤器配置"></Input>

    <Button type="success" size="small" @click="saveFilters" style="margin-top: 20px;">保存</Button>
  </div>
</template>

<script>
  import {GetAllFiltersAndWorks} from "../../../api"
  import {SaveFilters} from "../../../api"
  import {checkEmpty, oneOf} from "../../../tools"

  export default {
    name: "IFilterList",
    data(){
      return {
        filterWorks:[],
        filters:[],
        modules:[],
        works: [],
        moduleWorks:[],
        current_filter_id:-1,
        choosedWorkNames:[],
        complexWorkName:'',
      }
    },
    methods:{
      filterWorksWithModule:function(moduleName){
        return this.works.filter(work => work.module_name == moduleName);
      },
      refreshFilterList:async function () {
        const result = await GetAllFiltersAndWorks();
        if(result.status == "SUCCESS"){
          this.filterWorks = result.filterWorks;
          this.modules = result.modules;
          this.works = result.works;
          this.filters = result.filters;
        }
      },
      handleCheckAll:function (module_name) {
        let work_names = this.filterWorksWithModule(module_name).map(work => work.work_name);
        var allInflag = true;
        for(var i=0; i<work_names.length; i++){
          let work_name = work_names[i];
          if(!oneOf(work_name, this.choosedWorkNames)){
            allInflag = false;
            break;
          }
        }
        if(allInflag){
          this.choosedWorkNames = this.choosedWorkNames.filter(checkWork => !oneOf(checkWork, work_names));
        }else{
          let addWorks = work_names.filter(work_name => !oneOf(work_name, this.choosedWorkNames));
          for(var i=0; i<addWorks.length; i++){
            let work_name = addWorks[i];
            this.choosedWorkNames.push(work_name);
          }
        }
      },
      saveFilters:async function () {
        if(this.current_filter_id < 0){
          this.$Message.error('请选择 filter!');
        }else{
          const result = await SaveFilters(this.current_filter_id, this.choosedWorkNames.join(","), this.complexWorkName);
          if(result.status == "SUCCESS"){
            this.$Message.success("保存成功！");
            this.refreshFilterList();
          }else{
            this.$Message.error(result.errorMsg);
          }
        }
      },
      chooseFilter:function () {
        // 切换了有效的过滤器
        if (this.current_filter_id > 0){
          var current_filters = this.filters.filter(filter => filter.filter_work_id == this.current_filter_id);
          var _choosedWorkNames = current_filters.filter(filter => !checkEmpty(filter.work_name)).map(filter => filter.work_name);
          this.choosedWorkNames = _choosedWorkNames != null && _choosedWorkNames.length > 0 ? _choosedWorkNames[0].split(",") : [];
          var complexWorkNames = current_filters.filter(filter => !checkEmpty(filter.complex_work_name)).map(filter => filter.complex_work_name);
          this.complexWorkName = complexWorkNames != null && complexWorkNames.length > 0 ? complexWorkNames[0] : "";
        }

      }
    },
    mounted(){
      this.refreshFilterList();
    },
  }
</script>

<style scoped>

</style>
