<template>
  <span>
    <span v-if="showArrow">
      <Icon type="md-arrow-round-up" size="15" @click="handleClick('up')" style="margin-right: 5px;"/>
      <Icon type="md-arrow-round-down" size="15" @click="handleClick('down')" style="margin-right: 5px;"/>
      <Icon type="md-arrow-round-back" size="15" @click="handleClick('back')" style="margin-right: 5px;"/>
      <Icon type="md-arrow-round-forward" size="15" @click="handleClick('forward')" style="margin-right: 5px;"/>
    </span>
    <Button v-if="showEdit" type="error" size="small" @click="handleClick('edit')" style="margin-right: 5px;">编辑</Button>
    <Button v-if="showParam" type="info" size="small" @click="handleClick('param')" style="margin-right: 5px;">参数</Button>
    <Button v-if="showDelete" type="warning" size="small" @click="handleClick('delete')" style="margin-right: 5px;">删除</Button>
    <Button v-if="showCopy" type="success" size="small" @click="handleClick('copy')" style="margin-right: 5px;">复制</Button>
    <Button v-if="showRefer" type="warning" size="small" @click="handleClick('refer')" style="margin-right: 5px;">父流程</Button>
  </span>
</template>

<script>
  export default {
    name: "WorkStepEditBtns",
    props: {
      showArrow: {
        type: Boolean,
        default: true
      },
      showEdit: {
        type: Boolean,
        default: true
      },
      showParam: {
        type: Boolean,
        default: true
      },
      showDelete: {
        type: Boolean,
        default: true
      },
      showCopy: {
        type: Boolean,
        default: true
      },
      showRefer: {
        type: Boolean,
        default: true
      },
    },
    methods:{
      handleClick:function (clickType) {
        this.$emit("handleClick", clickType);
      }
    }
  }
</script>

<style scoped>

</style>
