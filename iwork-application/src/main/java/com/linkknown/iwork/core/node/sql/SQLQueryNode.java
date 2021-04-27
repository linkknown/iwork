package com.linkknown.iwork.core.node.sql;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.adapter.PageAdapter;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.ParamHelper;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.util.DBUtil;
import com.linkknown.iwork.util.DatatypeUtil;
import com.linkknown.iwork.util.PageUtil;
import com.linkknown.iwork.util.SqlUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@AutoRegistry
public class SQLQueryNode extends BaseNode {
    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "sql", "查询sql语句,带分页条件的sql"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "columnNames?", "查询结果集列名列表,以逗号分隔,动态sql需要提供"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "metadata_sql?", "查询 metadata 所需 sql"));
        paramMetaList.add(new ParamMeta(Constants.MULTI_PREFIX + "sql_binding?", "sql绑定数据,个数和sql中的?数量相同"));
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "current_page?", "当前页数"));
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "page_size?", "每页数据量"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "panic_no_datacounts?", "查询数据量为 0 时,抛出的异常信息,为空时不抛出异常!"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "db_conn", "数据库连接信息,需要使用 $RESOURCE 全局参数"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamInputSchema getDynamicParamInputSchema() {
        return new Param.ParamInputSchema();
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "datacounts", ""));
        paramMetaList.add(new ParamMeta("rows", ""));
        paramMetaList.add(new ParamMeta("firstrow", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDynamicParamOutputSchema() throws IWorkException {
        // 输出 metadata
        Param.ParamOutputSchema paramOutputSchema = this.getMetaDataQuietlyForQuery();

        // 输出分页信息
        String current_page = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.NUMBER_PREFIX + "current_page?", this.getWorkStep());
        String page_size = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.NUMBER_PREFIX + "page_size?", this.getWorkStep());

        if (StringUtils.isNoneEmpty(current_page) && StringUtils.isNoneEmpty(page_size)) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem()
                    .setParamName(Constants.COMPLEX_PREFIX + "paginator");
            paramOutputSchema.getParamOutputSchemaItems().add(item);

            for (String paginatorField : PageAdapter.getPaginatorFields()) {
                item = new Param.ParamOutputSchemaItem()
                        .setParentPath(Constants.COMPLEX_PREFIX + "paginator")
                        .setParamName(paginatorField);
                paramOutputSchema.getParamOutputSchemaItems().add(item);
            }
        }

        return paramOutputSchema;
    }

    private Param.ParamOutputSchema getMetaDataQuietlyForQuery() throws IWorkException {
        List<String> columnNames = null;
        List<String> namings = null;
        String columnNamesStr = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "columnNames?", this.getWorkStep());
        if (StringUtils.isNotBlank(columnNamesStr)) {
            columnNames = Arrays.asList(columnNamesStr.split(","));
        } else {
            String metadataSql = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "metadata_sql?", this.getWorkStep());
            SqlUtil.NamingSqlResult namingSqlResult = SqlUtil.parseNamingSql(metadataSql);

            Resource resource = this.validateAndGetResourceForDBConn();
            String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resource.getResourceLink(), "|||");
            String resourceUrl = resourceLinkArr[0];
            String resourceUserName = resourceLinkArr[1];
            String resourcePasswd = resourceLinkArr[2];

            columnNames = SqlUtil.getMetaDatas(resourceUrl, resourceUserName, resourcePasswd, namingSqlResult.getQuestionSql());
        }
        return renderMetaDataToOutputSchema(columnNames);
    }

    private Resource validateAndGetResourceForDBConn() throws IWorkException {
        Resource resource = (Resource) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "db_conn", this.getWorkStep());
        if (resource == null) {
            throw new IWorkException("Invalid param for db_conn! Can't resolve it!");
        }
//        _, err := iworkpool.GetDBConn("mysql", dataSourceName) // 全局 db 不能 Close
//        if err != nil {
//            panic(errors.Wrapf(err, "Can't get DB connection for %s!", dataSourceName))
//        }
        return resource;
    }

    private Param.ParamOutputSchema renderMetaDataToOutputSchema(List<String> columnNames) {
        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        for (String columnName : columnNames) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem()
                    .setParentPath("row")
                    .setParamName(columnName);
            paramOutputSchema.getParamOutputSchemaItems().add(item);

            item = new Param.ParamOutputSchemaItem()
                    .setParentPath("firstrow")
                    .setParamName(columnName);
            paramOutputSchema.getParamOutputSchemaItems().add(item);
        }
        return paramOutputSchema;
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

        sql = SqlUtil.parseSpecialCharsetAnd(sql);

        String total_sql = String.format("select count(*) as count from (%s) ttt", sql);
        String limit_sql = String.format("%s limit ?,?", sql);
        // 数据源
        Resource resource = (Resource) this.getTmpDataMap().get(Constants.STRING_PREFIX+"db_conn");
        // sql_binding 参数获取
        List<Object> sql_binding = getSqlBinding(this.getTmpDataMap(), namings);

        int totalcount = 0;                  // 分页查询时总数据量
        int datacounts = 0;                    // 实际查询时的总数据量
        List<Map<String, Object>> rowDatas = new ArrayList<>(); // 查询出来的数据

        // 判断是简单查询还是分页查询
        Object current_page = this.getTmpDataMap().get(Constants.NUMBER_PREFIX+"current_page?");
        Object page_size = this.getTmpDataMap().get(Constants.NUMBER_PREFIX+"page_size?");

        Object txManger = this.getDataStore().getTxManger();

        Connection conn = null;
        try {
            String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resource.getResourceLink(), "|||");
            String resourceUrl = resourceLinkArr[0];
            String resourceUserName = resourceLinkArr[1];
            String resourcePasswd = resourceLinkArr[2];

            conn = DBUtil.getConnection(resourceUrl, resourceUserName, resourcePasswd);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        txm.FirstBegin(dataSourceName)


        boolean isPageFlag = false;
        if (current_page != null && page_size != null) {
            int _current_page = PageUtil.getSafePageNo(DatatypeUtil.objectToInt(current_page));
            int _page_size = PageUtil.getSafePageSize(DatatypeUtil.objectToInt(page_size));
            if (_current_page > 0 && _page_size > 0) { // 正数才表示分页
                ResultSet resultSet = SqlUtil.executeQuery(conn, total_sql, sql_binding, resultSet1 -> resultSet1);
                try {
                    if (resultSet.next()) {
                        totalcount = resultSet.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                sql_binding.add( (_current_page-1)*_page_size);
                sql_binding.add(_page_size);
                resultSet = SqlUtil.executeQuery(conn, limit_sql, sql_binding, resultSet1 -> resultSet1);
                try {
                    resultSet.last();
                    datacounts=resultSet.getRow();  // getRow() 返回数据库当前行的行号
                    resultSet.beforeFirst();

                    rowDatas = parseRows(resultSet);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // 存储分页信息
                Map<String, Object> paginator = PageUtil.paginator(_current_page, _page_size, totalcount);
                paramMap.put(Constants.COMPLEX_PREFIX+"paginator", paginator);
                paginator.entrySet().stream().forEach(entry -> paramMap.put(Constants.COMPLEX_PREFIX+"paginator."+ entry.getKey(), entry.getValue()));
                isPageFlag = true;
            }
        }
        if (!isPageFlag) {
            try {
                ResultSet resultSet = SqlUtil.executeQuery(conn, sql, sql_binding, resultSet1 -> resultSet1);
                resultSet.last();
                datacounts=resultSet.getRow();  // getRow() 返回数据库当前行的行号
                resultSet.beforeFirst();

                rowDatas = parseRows(resultSet);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        // 将数据数据存储到数据中心
        // 存储 datacounts
        paramMap.put(Constants.NUMBER_PREFIX+"datacounts", datacounts);
        // 数组对象整体存储在 rows 里面
        paramMap.put("rows", rowDatas);
        if (rowDatas.size() > 0) {
            paramMap.put("firstrow", rowDatas.get(0));
        }
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
        this.checkPanicNoDataCount(datacounts);
    }

    private List<Map<String,Object>> parseRows(ResultSet resultSet) throws SQLException {
        // 列名、列值组成的 map,多行数据使用数组存储
        List<Map<String, Object>> rowDatas = new ArrayList<>();
        int columnCount = resultSet.getMetaData().getColumnCount();     // 获取列的数量
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String key = resultSet.getMetaData().getColumnLabel(i);
                Object value = resultSet.getObject(i);

                map.put(key, value);
            }
            rowDatas.add(map);
        }
        return rowDatas;
    }

    // 当影响条数为 0 时,是否报出异常信息
    private void checkPanicNoDataCount(int datacounts) throws IWorkException {
        Object panicNoData = this.getTmpDataMap().get(Constants.STRING_PREFIX+"panic_no_datacounts?");
        if (panicNoData != null) {
            String panicNDataMsg = (String) panicNoData;
            if (datacounts == 0 && StringUtils.isNotEmpty(panicNDataMsg)) {
//                panic(&interfaces.InsensitiveError{Error: errors.New(panicNoAffectedMsg)})
                throw new IWorkException(panicNDataMsg);
            }
        }

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
