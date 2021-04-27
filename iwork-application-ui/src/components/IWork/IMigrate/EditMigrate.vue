<template>
  <!-- 表单信息 -->
  <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="150">
    <span style="color:red;">{{migrate_name_error}}</span>
    <FormItem label="migrate_name" prop="migrate_name">
      <Input v-model="formValidate.migrate_name" placeholder="Enter your migrate_name"></Input>
    </FormItem>
    <FormItem label="migrate_sql" prop="migrate_sql">
      <Input v-model="formValidate.migrate_sql" type="textarea" :rows="20" placeholder="Enter your migrate_sql"></Input>
    </FormItem>
    <FormItem>
      <Button type="success" @click="handleSubmit('formValidate')" style="margin-right: 6px">提交</Button>
    </FormItem>
  </Form>
</template>

<script>
  import {EditSqlMigrate, GetSqlMigrateInfo} from "../../../api"

  export default {
    name: "EditMigrate",
    data(){
      return {
        id: -1,
        migrate_name_error:'',
        formValidate: {
          migrate_name: '',
          migrate_sql: '',
        },
        ruleValidate: {
          migrate_name: [
            {required: true, message: 'The migrate_name cannot be empty', trigger: 'blur'}
          ],
          migrate_sql: [
            {required: true, message: 'The migrate_sql cannot be empty', trigger: 'blur'},
          ],
        }
      }
    },
    methods:{
      handleSubmit:function (name) {
        this.$refs[name].validate(async (valid) => {
          if (valid) {
            const result = await EditSqlMigrate(this.id, this.formValidate.migrate_name, this.formValidate.migrate_sql);
            if(result.status == "SUCCESS"){
              this.$Message.success("编辑成功！");
              this.$router.push({ path: '/iwork/migrateList'});
            }else{
              this.migrate_name_error = result.errorMsg;
            }
          }
        })
      },
      refreshSqlMigrateInfo: async function (id) {
        const result = await GetSqlMigrateInfo(id);
        if(result.status=="SUCCESS"){
          this.id = result.migrate.id;
          this.$set(this.formValidate, "migrate_name", result.migrate.migrate_name);
          this.$set(this.formValidate, "migrate_sql", result.migrate.migrate_sql);
        }
      }
    },
    mounted(){
      if(this.$route.query.id != undefined && this.$route.query.id != null){
        this.refreshSqlMigrateInfo(this.$route.query.id);
      }
    }
  }
</script>

<style scoped>

</style>
