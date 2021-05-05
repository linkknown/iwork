import Vue from 'vue'
import Router from 'vue-router'

// es6 import 异步语法,使用异步组件加载机制减少耗时操作
const WorkList = () => import("@/components/IWork/IWork/WorkList");
const WorkStepList = () => import("@/components/IWork/IWorkStep/WorkStepList");
const RunLogList = () => import("@/components/IWork/ILog/RunLogList");
const OperateLogList = () => import("@/components/IWork/ILog/OperateLogList");
const RunlogDetail = () => import("@/components/IWork/ILog/RunlogDetail");
const IWorkLayout = () => import("@/components/ILayout/IWorkLayout");
const ILayout = () => import("@/components/ILayout/ILayout");
const AppidList = () => import("@/components/IWork/Appid/AppidList");
const ResourceList = () => import("@/components/IWork/IResource/ResourceList");
const MigrateList = () => import("@/components/IWork/IMigrate/MigrateList");
const EditMigrate = () => import("@/components/IWork/IMigrate/EditMigrate");
const QuickSql = () => import("@/components/IWork/IAssistant/QuickSql");
const GlobalVarList = () => import("@/components/IWork/IGlobalVar/GlobalVarList");
const DashBoard = () => import("@/components/IWork/IDashBoard/DashBoard");
const IModuleList = () => import("@/components/IWork/IModule/IModuleList");
const IFilterList = () => import("@/components/IWork/IFilter/IFilterList");
const Login = () => import("@/components/IWork/Security/Login");

Vue.use(Router);

const IWorkRouter = [
  {
    path: '/',
    redirect: '/iwork/workList'
  },
  {
    path: '/iwork',
    component: IWorkLayout,
    children: [
      {path: 'moduleList',component: IModuleList},
      {path: 'appidList', component: AppidList},
      {path: 'resourceList',component: ResourceList},
      {path: 'workList',component: WorkList},
      {path: 'filterList',component: IFilterList},
      {path: 'workstepList',component: WorkStepList},
      {path: 'runLogList',component: RunLogList},
      {path: 'runLogDetail', component: RunlogDetail},
      {path: 'migrateList',component: MigrateList},
      {path: 'editMigrate',component: EditMigrate},
      {path: 'quickSql',component: QuickSql},
      {path: 'globalVarList',component: GlobalVarList},
      {path: 'operateLogList',component: OperateLogList},
      {path: 'dashboard',component: DashBoard},
    ]
  },
  {
    path: '/security',
    component: ILayout,
    children: [
      {path: 'login', component: Login},
    ]
  }
];


export default new Router({
  // # 主要用来区分前后台应用, history 模式需要使用 nginx 代理
  // History 模式,去除vue项目中的 #
  // mode: 'history',
  routes: IWorkRouter,
  // 页面跳转时,让页面滚动在顶部
  scrollBehavior(to,from,savedPosition){
    if(savedPosition){
      return savedPosition;
    }else{
      return {x:0,y:0}
    }
  },
})
