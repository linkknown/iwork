<template>
  <div style="margin: 10px;">
    <div>
      <div style="border: 1px solid #dcdee2;padding: 10px;">
          <div  class="sqlPart sqlPart1" v-for="(element,index) in hotSqlElements" draggable="true"
                @dragstart="dragstart($event, element, -1, 'left')"
                @click="appendSqlElements.push(element)">
            {{element}}
          </div>
      </div>

      <ISimpleConfirmModal ref="comfirmModal" modal-title="sql 预览" :footer-hide="true" :modal-width="800">
        <div>
          <Input v-model="sqlAppendViewStr" type="textarea" :rows="20"/>

          <div style="text-align: right;">
            <Button style="margin-top: 10px;" type="warning" @click="sqlFormat">格式化</Button>
          </div>
        </div>
      </ISimpleConfirmModal>

      <div style="border: 1px solid #dcdee2;padding: 10px;margin-top: 10px;">
        <div style="text-align: right;margin-top:5px;margin-bottom: 5px;">
            <span @drop="deleteElement($event)" @dragover="allowDrop($event)">
              <Button type="primary" size="small" icon="md-close">拖拽至此处进行删除</Button>
            </span>
          <Button type="dashed" size="small" @click="renderSql">sql 预览</Button>
        </div>

        <div class="sqlAppendBox" @drop="drop($event, -1)" @dragover="allowDrop($event)">
          <div class="sqlPart sqlPart2" v-for="(element,index) in appendSqlElements"
                draggable="true" @dragstart="dragstart($event, element, index, 'right')"
                @drop="drop($event, index)" @dragover="allowDrop($event)">
            {{element}}
            <span class="sqlPartRemove" @click="appendSqlElements.splice(index, 1)">X</span>
          </div>
        </div>
      </div>
    </div>

    <Row  style="border: 1px solid #dcdee2;padding: 10px;margin-top: 10px;">
      <Col span="4">
        <span style="background-color: lightgrey;padding: 5px 10px;">表名：{{tableName}}</span>
        <div class="columnBox">
          <CheckboxGroup v-model="checkTableColumns">
            <div v-for="tableColumn in tableColumns">
              <Checkbox :label="tableColumn"></Checkbox>
            </div>
          </CheckboxGroup>
        </div>
        <p style="margin-top: 10px;">
          <Button type="dashed" size="small" @click="chooseAll">全选</Button>
          <Button type="dashed" size="small" @click="toggleAll">反选</Button>
          <Button type="dashed" size="small" @click="appendColumn">添加自定义片段</Button>
        </p>
      </Col>
      <Col span="20">
        <span style="background-color: lightgrey;padding: 5px 10px;">sql 片段</span>
        <div class="sqlPartBox">
          <div class="sqlPart sqlPart3" v-for="tableSql in tableSqls" draggable="true"
               @dragstart="dragstart($event, tableSql, -1, 'bottom')"
               @click="appendSqlElements.push(tableSql)">
            {{tableSql }}
          </div>
          <div class="sqlPart sqlPart3" v-for="(customSql,index) in customSqls" draggable="true"
               @dragstart="dragstart($event, customSql, -1, 'bottom')"
               @click="appendSqlElements.push(customSql)" title="自定义 sqlPart">
            {{customSql }}
            <span class="sqlPartRemove" @click="deleteCustom(index)">X</span>
          </div>
        </div>
      </Col>
    </Row>
  </div>
</template>

<script>
  import {oneOf, swapArray} from "../../../tools"
  import ISimpleConfirmModal from "../../Common/modal/ISimpleConfirmModal";
  import {FormatSql} from "../../../api"

  export default {
    name: "QuickTable",
    components: {ISimpleConfirmModal},
    props:{
      tableName:{
        type:String,
        default:'',
      },
      tableColumns:{
        type:Array,
        default:[],
      },
      tableSqls:{
        type:Array,
        default:[],
      }
    },
    data(){
      return {
        split1: 0.4,
        // 选中的列
        checkTableColumns:[],
        customSqls:[],
        // default line 默认线路
        appendSqlElements: [],
        hotSqlElements: ["select", "update", "insert", "delete", "insert into", "set", "values", "(", ")", "count(*) as count", "where", "from", "where 1=0"],
        // 预览 sql
        sqlAppendViewStr: '',
      }
    },
    methods:{
      // 格式化 sql 事件
      sqlFormat: async function () {
        const result = await FormatSql(this.sqlAppendViewStr);
        if (result.status == "SUCCESS") {
          this.sqlAppendViewStr = result.sql;
        }
      },
      chooseAll:function () {
        if(this.checkTableColumns.length > 0){
          this.checkTableColumns = [];
        }else{
          this.checkTableColumns = this.tableColumns;
        }
      },
      toggleAll:function () {
        this.checkTableColumns = this.tableColumns.filter(column => !oneOf(column, this.checkTableColumns));
      },
      appendColumn:function () {
        if(this.checkTableColumns.length > 0){
          this.customSqls.push(this.checkTableColumns.join(","));
          this.customSqls.push(this.checkTableColumns.map(column => column + "=?").join(" and "));
        }
      },
      deleteCustom:function (index) {
        this.customSqls.splice(index,1);
      },
      renderSql:function () {
        // 拼接后的 sql
        this.sqlAppendViewStr = this.appendSqlElements.join(" ");
        // 显示对话框
        this.$refs.comfirmModal.showModal();
      },
      deleteElement:function (event) {
        event.preventDefault();
        var dataStr = event.dataTransfer.getData("Text");
        var data = JSON.parse(dataStr);
        var sourceIndex = data.index;
        var transferData = data.transferData;
        var location = data.location;
        if(location == 'right'){
          this.appendSqlElements.splice(sourceIndex, 1);
        }
      },
      dragstart:function(event, transferData, index, location){
        event.dataTransfer.setData("Text", JSON.stringify({'transferData':transferData, 'index':index, 'location':location}));
      },
      allowDrop:function(event){
        event.preventDefault();
      },
      drop:function(event, index){
        // 取消冒泡
        event.stopPropagation();
        event.preventDefault();
        var dataStr = event.dataTransfer.getData("Text");
        var data = JSON.parse(dataStr);
        var sourceIndex = data.index;
        var transferData = data.transferData;
        var location = data.location;
        if(index > 0){  // 目标位置有元素
          if(sourceIndex >= 0){
            // 交换位置
            swapArray(this.appendSqlElements, sourceIndex, index);
          }else{
            // index 后面添加
            this.appendSqlElements.splice(index + 1, 0, data.transferData);
          }
        }else{         // 目标元素为空div,直接追加
          if(location != 'right'){
            this.appendSqlElements.push(transferData);
          }
        }
      }
    },
  }
</script>

<style scoped>
  .sqlAppendBox {
    height: 300px;
    overflow-y: scroll;
    padding-right: 10px;
  }
  .columnBox {
    height: 300px;
    overflow-y: auto;
    padding-right: 10px;
    margin-right: 10px;
    margin-top: 5px;
  }
  .sqlPart .sqlPartRemove {
    visibility: hidden;
  }
  .sqlPart:hover .sqlPartRemove {
    visibility: visible;
  }

  .sqlPartBox {
    height: 300px;
    overflow-y: scroll;
    padding-right: 10px;
    margin-top: 5px;
  }
  .sqlPart {
    word-wrap:break-word;
    word-break:break-all;
    display: inline-block;
    padding: 5px 10px;
    margin: 5px 5px 0 0;
    cursor: pointer;
    position: relative;
    font-weight: 500;
  }
  .sqlPart1 {
    background-color: rgba(0, 128, 0, 0.2);
  }
  .sqlPart2 {
    background-color: rgba(255, 140, 0, 0.2);
  }
  .sqlPart3 {
    background-color: rgba(255, 0, 0, 0.2);
  }

  .sqlPartRemove {
    position: absolute;
    right: -3px;
    top: -3px;
    text-align: center;
    line-height: 20px;
    color: white;
    background-color: #989898;
    width: 20px;
    height: 20px;
    border-radius: 50%;
  }

  /*定义滚动条高宽及背景,高宽分别对应横竖滚动条的尺寸*/
  ::-webkit-scrollbar {
    width:4px;
    height:4px;
    background-color: rgb(97, 182, 235);
  }
  /*定义滚动条轨道 内阴影+圆角*/
  ::-webkit-scrollbar-track {
    background: rgba(33, 49, 71, 0.3);
    border-radius:2px;
  }
  /*定义滑块 内阴影+圆角*/
  ::-webkit-scrollbar-thumb {
    background: rgb(97, 182, 235);
    border-radius:2px;
  }
  ::-webkit-scrollbar-thumb:hover{
    background: rgb(97, 182, 235);
  }
  ::-webkit-scrollbar-corner{
    background: rgb(97, 182, 235);
  }

</style>
