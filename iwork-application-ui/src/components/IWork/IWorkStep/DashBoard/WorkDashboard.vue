<template>
  <Modal v-model="showModal" fullscreen title="运行报表">
    <div>AAAAAAAAAAAA</div>
  </Modal>
</template>

<script>
  export default {
    name: "WorkDashboard",
    data(){
      return {
        showModal:true,
      }
    },
    methods:{
      showWorkDashboard:function (flag) {
        this.showModal = flag;
      }
    }
  }
</script>

<style scoped>

</style>
