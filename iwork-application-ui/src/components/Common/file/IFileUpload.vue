<template>
<span>
  <Button v-if="showButton" :size="size" type="success" @click="fileUploadModal = true">{{ uploadLabel }}</Button>
  <Modal
    v-model="fileUploadModal"
    width="500"
    :title="uploadLabel"
    :mask-closable="false">
    <div>
      <Upload
        ref="upload"
        multiple
        :on-success="uploadComplete"
        :action="action"
        :format="format"
        :on-format-error="onFormatError"
        :multiple="multiple">
        <Button icon="ios-cloud-upload-outline">{{ uploadLabel }}</Button>
      </Upload>
    </div>
  </Modal>
</span>
</template>

<script>
  export default {
    name: "IFileUpload",
    props: {
      showButton: {
        type: Boolean,
        default: true
      },
      uploadLabel: {
        type: String,
        default: '文件上传'
      },
      action: {
        type: String,
        default: ''
      },
      size: {
        type: String,
        default: 'default'
      },
      format: {
        type: Array,
        default() {
          return [];
        }
      },
      multiple: {
        type: Boolean,
        default: false,
      },
      autoHideModal: {
        type: Boolean,
        deafult: true,
      },
      extraData: {
        type: [Object,Number],
        default: function () {
          return {}
        },
      }
    },
    data() {
      return {
        // 文件上传 modal
        fileUploadModal: false,
      }
    },
    methods: {
      onFormatError: function () {
        this.$Message.error("文件格式不合法!");
      },
      uploadComplete(result, file) {
        if (result.status == "SUCCESS") {
          result.extraData = this.extraData;  // 返回 extraData
          // 父子组件通信
          this.$emit('uploadComplete', result);
          this.$Notice.success({
            title: '文件上传成功',
            desc: '文件 ' + file.name + ' 上传成功!'
          });
          if (this.autoHideModal) {
            this.hideModal();
          }
        } else {
          this.$Notice.error({
            title: '文件上传失败',
            desc: '文件 ' + file.name + ' 上传失败!'
          });
        }
      },
      showModal() {
        this.fileUploadModal = true;
      },
      hideModal() {
        this.fileUploadModal = false;
      }
    }
  }
</script>

<style scoped>

</style>
