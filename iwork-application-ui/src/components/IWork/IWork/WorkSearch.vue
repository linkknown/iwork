<template>
  <div>
    <Input v-model.trim="search" placeholder="搜索..." @on-keydown="handleSearch"/>
    <div v-for="(work, index) in works" @click="handleClick(work.work_name)" style="cursor: pointer;">{{work.work_name}}&nbsp;&nbsp;
      <span style="color: #b6b6b6;font-size: 12px;">{{work.work_desc}}</span>
    </div>
    <Page :total="total" :page-size="offset" size="small" show-total show-sizer :styles="{'text-align': 'center','margin-top': '10px'}"
          @on-change="handleChange" @on-page-size-change="handlePageSizeChange"/>
  </div>
</template>

<script>
  import {FilterPageWorks} from "../../../api";

  export default {
    name: "WorkSearch",
    data (){
      return {
        search_work_type:"all",
        search_module:"all",
        // 当前页
        current_page:1,
        // 总数
        total:0,
        // 每页记录数
        offset:10,
        // 搜索条件
        search:"",
        works: [],
      }
    },
    methods :{
      handleClick: function (work_name){
        this.$emit("handleClick", work_name);
      },
      refreshWorkList:async function () {
        const result = await FilterPageWorks(this.offset,this.current_page,this.search,this.search_work_type,this.search_module);
        if (result.status === "SUCCESS") {
          this.works = result.works;
          this.total = result.paginator.totalcount;
        }
      },
      handleChange(page){
        this.current_page = page;
        this.refreshWorkList();
      },
      handlePageSizeChange(pageSize){
        this.offset = pageSize;
        this.refreshWorkList();
      },
      handleSearch(data){
        this.offset = 10;
        this.current_page = 1;
        this.refreshWorkList();
      },
    },
    mounted (){
      this.refreshWorkList();
    }
  }
</script>

<style scoped>

</style>
