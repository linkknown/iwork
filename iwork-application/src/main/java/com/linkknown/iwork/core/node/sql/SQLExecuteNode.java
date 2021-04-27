package com.linkknown.iwork.core.node.sql;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.util.DBUtil;
import com.linkknown.iwork.util.DatatypeUtil;
import com.linkknown.iwork.util.SqlUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

@AutoRegistry
public class SQLExecuteNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.FOREACH_PREFIX + "batch_data?", "仅供批量插入数据时使用"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "sql", "执行sql语句"));
        paramMetaList.add(new ParamMeta(Constants.MULTI_PREFIX + "sql_binding?", "sql绑定数据,个数必须和当前执行sql语句中的占位符参数个数相同"/*, "repeatable__" + Constants.FOREACH_PREFIX + "batch_data?"*/));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "panic_no_affected?", "执行 sql 影响条数为 0 时,抛出的异常信息,为空时不抛出异常!"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "db_conn", "数据库连接信息,需要使用 $RESOURCE 全局参数"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "affected", Constants.NUMBER_PREFIX + "lastInsertId"));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        // 需要存储的中间数据
        Map<String, Object> paramMap = new HashMap<>();

        // 获取待执行 sql
        String sql = (String)this.getTmpDataMap().get(Constants.STRING_PREFIX + "sql");
        // 三种 sql
        SqlUtil.NamingSqlResult namingSqlResult = SqlUtil.parseNamingSql(sql);
        sql = namingSqlResult.getQuestionSql();
        List<String> namings = namingSqlResult.getNamings();

        // insert 语句且有批量操作时整改 sql 语句
        sql = this.modifySqlInsertWithBatch(this.getTmpDataMap(), sql);
        sql = SqlUtil.parseSpecialCharsetAnd(sql);
        // sql_binding 参数获取
        List<Object> sql_binding = getSqlBinding(this.getTmpDataMap(), namings);

        // 数据源
        Resource resource = (Resource) this.getTmpDataMap().get(Constants.STRING_PREFIX+"db_conn");

        Object txManger = this.getDataStore().getTxManger();

        Connection conn = null;
        try {
            String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resource.getResourceLink(), "|||");
            String resourceUrl = resourceLinkArr[0];
            String resourceUserName = resourceLinkArr[1];
            String resourcePasswd = resourceLinkArr[2];

            conn = DBUtil.getConnection(resourceUrl, resourceUserName, resourcePasswd);
        } catch (ClassNotFoundException|SQLException e) {
           throw IWorkException.wrapException("执行 sql 失败!", this.getWorkStep().getWorkStepName(), e);
        }
        int lastInsertId = -1;
        int affected = -1;
        try {
            SqlUtil.SqlExecuteResult result = SqlUtil.execute(conn, sql, sql_binding);
            lastInsertId = result.getLastInsertId();
            affected = result.getAffected();
        } catch (SQLException e) {
            throw IWorkException.wrapException("执行 sql 失败!", this.getWorkStep().getWorkStepName(), e);
        }

        // 将数据数据存储到数据中心
        // 存储 affected
        paramMap.put(Constants.NUMBER_PREFIX + "affected", affected);
        paramMap.put(Constants.NUMBER_PREFIX + "lastInsertId", lastInsertId);
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
        this.checkPanicNoAffectedMsg(affected);
    }

    // 当影响条数为 0 时,是否报出异常信息
    private void checkPanicNoAffectedMsg(int affected) throws IWorkException {
        Object panicNoAffected = this.getTmpDataMap().get(Constants.STRING_PREFIX+"panic_no_affected?");
        if (panicNoAffected != null) {
            String panicNoAffectedMsg = (String) panicNoAffected;
            if (affected == 0 && StringUtils.isNotEmpty(panicNoAffectedMsg)) {
//                panic(&interfaces.InsensitiveError{Error: errors.New(panicNoAffectedMsg)})
                throw new IWorkException(panicNoAffectedMsg);
            }
        }
    }

    private String modifySqlInsertWithBatch(Map<String,Object> tmpDataMap, String sql) {
        Object batch_data = this.getTmpDataMap().get(Constants.FOREACH_PREFIX + "batch_data?");
        if (batch_data != null) {
            int start = StringUtils.indexOf(sql, "BATCH[" + "BATCH[".length());
            int end = StringUtils.indexOf(sql, "]");
            String values = StringUtils.substring(sql, start, end);
            String replaceValues = StringUtils.repeat(values + ",", DatatypeUtil.objectToInt(batch_data));
            replaceValues = StringUtils.removeEnd(replaceValues, ",");

            sql = StringUtils.replace(sql, String.format("BATCH[%s]", values), replaceValues, -1);
        }
        return sql;
    }

    // 从 tmpDataMap 获取 sqlBinding 数据
    private List<Object> getSqlBinding (Map<String, Object> tmpDataMap, List<String> namings) {
        List<Object> result = new ArrayList<>();

        Object sqlBinding = tmpDataMap.get(Constants.MULTI_PREFIX + "sql_binding?");
        if (sqlBinding == null) {
            return result;
        }

        if (sqlBinding instanceof List) {
            // 支持单层切片和双层切片
            ((List<?>) sqlBinding).forEach((Consumer<Object>) sqlBinding1 -> {
                if (sqlBinding1 instanceof List) {
                    result.addAll((List<?>)sqlBinding1);
                } else {
                    result.add(sqlBinding1);
                }
            });
        } else if (sqlBinding instanceof Map) {
            namings.forEach(name -> ((Map<?,?>) sqlBinding).entrySet().forEach((Consumer<Map.Entry<?, ?>>) entry -> {
                if (StringUtils.equals(name, ":" + entry.getKey())) {
                    result.add(entry.getValue());
                }
            }));
        } else {
            result.add(sqlBinding);
        }
        return result;
    }
}
