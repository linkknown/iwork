<template>
  <Modal
    v-model="showChooserModal"
    width="950"
    :title="chooserTitle"
    :footer-hide="true"
    :transfer="false"
    :mask-closable="false"
    :styles="{top: '20px'}">
    <div style="min-height: 380px;">
      <slot/>
    </div>
  </Modal>
</template>

<script>
  export default {
    name: "IBaseChooser",
    props:{
      chooserTitle:{
        type:String,
        default:"chooserTitle",
      }
    },
    data(){
      return {
        showChooserModal:false,
      }
    },
    methods:{
      showModal:function () {
        this.showChooserModal = true;
      },
      hideModal:function () {
        this.showChooserModal = false;
      }
    }
  }
</script>

<style scoped>

</style>
