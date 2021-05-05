/*
包含n个接口请求函数的模块
函数的返回值: promise对象
 */
import ajax from './ajax'

const BASE_URL = '/api'

export const DeleteAppid = (params) => ajax(BASE_URL + "/iwork/deleteAppid", params, 'POST');
export const EditAppid = (params) => ajax(BASE_URL + "/iwork/editAppid", params, 'POST');
export const QueryPageAppIdList = (offset, current_page, search) => ajax(BASE_URL + "/iwork/queryPageAppIdList", {
  offset,
  current_page,
  search
}, 'POST');

// resource 分页查询
export const ResourceList = (offset,current_page,search) => ajax(BASE_URL+"/iwork/filterPageResource", {offset,current_page,search},'POST');

// 添加 resource 记录
export const EditResource = (params) => ajax(BASE_URL+"/iwork/editResource", params,'POST');

// 删除 resource 记录
export const DeleteResource = (id) => ajax(BASE_URL+"/iwork/deleteResource", {id},'POST');

// 查询 resource 记录
export const GetResourceById = (id) => ajax(BASE_URL+"/iwork/getResourceById", {id},'POST');

// 验证 resource
export const ValidateResource = (id) => ajax(BASE_URL+"/iwork/validateResource", {id},'POST');

export const GetAllResource = (resource_type) => ajax(BASE_URL+"/iwork/getAllResource", {resource_type},'POST');

// work 分页查询
export const FilterPageWorks = (offset,current_page,search,search_work_type,search_module) =>
  ajax(BASE_URL+"/iwork/filterPageWorks", {offset,current_page,search,search_work_type,search_module},'POST');

// 编辑 work 记录
export const EditWork = (params) => ajax(BASE_URL+"/iwork/editWork", params,'POST');

// 删除 work 记录
export const DeleteOrCopyWorkById = (operate,id) => ajax(BASE_URL+"/iwork/deleteOrCopyWorkById", {operate,id},'POST');

// workstep 分页查询
export const WorkStepList = (work_id) => ajax(BASE_URL+"/iwork/workStepList", {work_id},'POST');

// 添加 workstep 记录
export const AddWorkStep = (work_id, work_step_id, work_step_meta) => ajax(BASE_URL+"/iwork/addWorkStep", {work_id, work_step_id, work_step_meta},'POST');

// 运行 work
export const RunWork = (work_id) => ajax(BASE_URL+"/iwork/runWork", {work_id},'POST');

// 编辑 workstep 基本信息
export const EditWorkStepBaseInfo = (work_id,work_step_id,work_step_name,work_step_desc,work_step_type, is_defer) => ajax(BASE_URL+"/iwork/editWorkStepBaseInfo", {work_id,work_step_id,work_step_name,work_step_desc,work_step_type, is_defer},'POST');

// 编辑 workstep 记录
export const EditWorkStepParamInfo = (work_id,work_step_id,paramInputSchemaStr, paramMappingsStr) => ajax(BASE_URL+"/iwork/editWorkStepParamInfo", {work_id,work_step_id,paramInputSchemaStr,paramMappingsStr},'POST');

// 加载 workstep 记录
export const LoadWorkStepInfo = (work_id,work_step_id) => ajax(BASE_URL+"/iwork/loadWorkStepInfo", {work_id,work_step_id},'POST');

// 加载前置节点输出参数,包括全局参数
export const LoadPreNodeOutput = (work_id,work_step_id) => ajax(BASE_URL+"/iwork/loadPreNodeOutput", {work_id,work_step_id},'POST');

// 删除 workstep 记录
export const DeleteWorkStepByWorkStepId = (work_id, work_step_id) => ajax(BASE_URL+"/iwork/deleteWorkStepByWorkStepId", {work_id, work_step_id},'POST');

// 复制 workstep 记录
export const CopyWorkStepByWorkStepId = (work_id, work_step_id) => ajax(BASE_URL+"/iwork/copyWorkStepByWorkStepId", {work_id, work_step_id},'POST');


// 交换 workstep 顺序
export const ChangeWorkStepOrder = (work_id,work_step_id,type) => ajax(BASE_URL+"/iwork/changeWorkStepOrder", {work_id,work_step_id,type},'POST');

// 获取最后一次运行日志详情
export const GetLastRunlogDetail = (tracking_id) => ajax(BASE_URL + "/iwork/getLastRunlogDetail", {tracking_id}, 'POST');

// 获取相关流程
export const GetRelativeWork = (work_id) => ajax(BASE_URL+"/iwork/getRelativeWork", {work_id},'POST');

// 校验整个工程
export const ValidateWork = (work_id) => ajax(BASE_URL+"/iwork/validateWork", {work_id},'POST');

// 显示校验结果
export const LoadValidateResult = (work_id) => ajax(BASE_URL+"/iwork/loadValidateResult", {work_id},'POST');

export const RefactorWorkStepInfo = (work_id, refactor_worksub_name,selections) => ajax(BASE_URL+"/iwork/refactorWorkStepInfo", {work_id, refactor_worksub_name,selections},'POST');

export const BatchChangeIndent = (work_id, mod,selections) => ajax(BASE_URL+"/iwork/batchChangeIndent", {work_id, mod,selections},'POST');

export const EditSqlMigrate = (id, migrate_name, migrate_sql) => ajax(BASE_URL+"/iwork/editSqlMigrate", {id, migrate_name, migrate_sql},'POST');

export const FilterPageSqlMigrate = (offset,current_page) => ajax(BASE_URL+"/iwork/filterPageSqlMigrate", {offset,current_page},'POST');

export const GetSqlMigrateInfo = (id) => ajax(BASE_URL+"/iwork/getSqlMigrateInfo", {id},'POST');

export const ToggleSqlMigrateEffective = (id) => ajax(BASE_URL+"/iwork/toggleSqlMigrateEffective", {id},'POST');

export const ExecuteMigrate = (resource_name, forceClean) => ajax(BASE_URL+"/iwork/executeMigrate", {resource_name, forceClean},'POST');
export const GetLastMigrateLogs = (trackingId) => ajax(BASE_URL+"/iwork/getLastMigrateLogs", {trackingId},'POST');

export const LoadQuickSqlMeta = (resource_id) => ajax(BASE_URL+"/iwork/loadQuickSqlMeta", {resource_id},'POST');
export const FormatSql = (sql) => ajax(BASE_URL+"/iwork/formatSql", {sql},'POST');

export const ParseToMultiValue = (pureText, value) => ajax(BASE_URL+"/iwork/parseToMultiValue", {pureText, value},'POST');

export const GlobalVarList = (search) => ajax(BASE_URL + "/iwork/globalVarList", {search}, 'POST');

export const EditGlobalVar = (params) => ajax(BASE_URL + "/iwork/editGlobalVar", params, 'POST');

export const queryEvnNameList = () => ajax(BASE_URL+"/iwork/queryEvnNameList", {},'POST');

export const DeleteGlobalVarById = (id) => ajax(BASE_URL+"/iwork/deleteGlobalVarById", {id},'POST');
export const GetAllGlobalVars = () => ajax(BASE_URL + "/iwork/getAllGlobalVars", {}, 'POST');

export const ModuleList = (offset,current_page, search) => ajax(BASE_URL+"/iwork/moduleList", {offset,current_page, search},'POST');

export const EditModule = (module_id, module_name, module_desc) => ajax(BASE_URL+"/iwork/editModule", {module_id, module_name, module_desc},'POST');

export const DeleteModuleById = (id) => ajax(BASE_URL+"/iwork/deleteModuleById", {id},'POST');

export const GetAllModules = () => ajax(BASE_URL+"/iwork/getAllModules", {},'POST');

export const GetAllFiltersAndWorks = () => ajax(BASE_URL+"/iwork/getAllFiltersAndWorks", {},'POST');

export const SaveFilters = (filter_id, workNames, complexWorkName) => ajax(BASE_URL+"/iwork/saveFilters", {filter_id, workNames, complexWorkName},'POST');

export const GetMetaInfo = (meta) => ajax(BASE_URL+"/iwork/getMetaInfo", {meta},'POST');

export const QueryWorkDetail = (work_id) => ajax(BASE_URL+"/iwork/queryWorkDetail", {work_id},'POST');

export const SaveProject = () => ajax(BASE_URL+"/iwork/saveProject", {},'POST');

export const ImportProject = () => ajax(BASE_URL+"/iwork/importProject", {},'POST');

export const GetDBMonitorMeta = (params) => ajax(BASE_URL + '/iwork/getDBMonitorMeta', params, 'POST');
export const LoadDBMonitorData = (params) => ajax(BASE_URL + '/iwork/loadDBMonitorData', params, 'POST');

export const LoadRecordParamData = (params) => ajax(BASE_URL + '/iwork/loadRecordParamData', params, 'POST');

// 分页查询运行记录
export const FilterPageLogRecord = (work_id,logLevel,offset,current_page) => ajax(BASE_URL+"/iwork/filterPageLogRecord", {work_id,logLevel,offset,current_page},'POST');
// 分页查询操作日志
export const FilterPageOperateLog = (params) => ajax(BASE_URL + '/iwork/filterPageOperateLog', params, 'POST');

// 登陆接口
export const Login = (params) => ajax(BASE_URL + '/iwork/login', params, 'POST');
// 获取验证码接口
export const GenVerifyCode = (params) => ajax(BASE_URL + '/iwork/genVerifyCode', params, 'POST');
