<template>
  <span>
    <Button v-for="n in btnCounts" :type="btnTypes[n-1]" :size="btnSize" v-show="btnShows[n-1]"
            @click="handleClick(n-1, btnBindDatas[n-1])" style="margin-right: 5px;">{{btnTexts[n-1]}}</Button>
  </span>
</template>

<script>
  export default {
    name: "MultiClickButton",
    props:{
      btnCounts:{
        type: Number,
        default : 1,
      },
      btnSize:{
        type: String,
        default: 'small',
      },
      btnTypes:{
        type: Array,
        default: ['primary'],
      },
      btnShows:{
        type: Array,
        default: [true],
      },
      btnTexts:{
        type: Array,
        default: ['primaryText'],
      },
      btnBindDatas: {
        type: Array,
        default: ['bindData'],
      },
    },
    methods:{
      handleClick:function (n, bindData) {
        this.$emit('handleClick', n, bindData);
      }
    }
  }
</script>

<style scoped>

</style>
