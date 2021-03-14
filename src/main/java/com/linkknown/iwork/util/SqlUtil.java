package com.linkknown.iwork.util;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class SqlUtil {

    public static List<String> getAllTableNames(String url, String userName, String password) {
        List<String> tableNames = new LinkedList<>();
        String sql = "show tables;";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DBUtil.getConnection(url, userName, password);

            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                tableNames.add(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(resultSet, statement, connection);
        }

        return tableNames;
    }

    public static List<String> getAllColumnNames(String url, String userName, String password, String tableName) {
        String sql = String.format("select * from %s where 1=0", tableName);
        return getMetaDatas(url, userName, password, sql);
    }

    public static List<String> getMetaDatas(String url, String userName, String password, String sql) {
        return executeQuery(url, userName, password, sql, resultSet -> {
            List<String> columnNames = new LinkedList<>();
            try {
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(resultSet.getMetaData().getColumnName(i));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return columnNames;
        });
    }

    public static <T> T executeQuery(Connection connection, String sql, List<Object> sqlBinding, Function<ResultSet, T> convertFunction) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            for (int index = 0; index < sqlBinding.size(); index++) {
                statement.setObject(index + 1, sqlBinding.get(index));
            }

            resultSet = statement.executeQuery();

            return convertFunction.apply(resultSet);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T executeQuery(String url, String userName, String password, String sql, Function<ResultSet, T> convertFunction) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DBUtil.getConnection(url, userName, password);

            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            return convertFunction.apply(resultSet);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(resultSet, statement, connection);
        }
        return null;
    }

    public static void execute(String url, String userName, String password, String sql, Object... params) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBUtil.getConnection(url, userName, password);

            statement = connection.prepareStatement(sql);
            for (int index=0; index< params.length; index++) {
                statement.setObject(index + 1, params[index]);
            }
            statement.execute();

        } catch (ClassNotFoundException | SQLException e) {
            throw e;
        } finally {
            DBUtil.close(statement, connection);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class SqlExecuteResult {
        private int lastInsertId;
        private int affected;
    }

    public static SqlExecuteResult execute(Connection connection, String sql, List<Object> sqlBinding) throws SQLException {
        int lastInsertId = -1;
        int affected = -1;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < sqlBinding.size(); index++) {
                statement.setObject(index + 1, sqlBinding.get(index));
            }
            statement.executeUpdate();
            //返回的结果集中包含主键,注意：主键还可以是UUID,复合主键等,所以这里不是直接返回一个整型
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()) {
                lastInsertId = rs.getInt(1);
            }
            affected = statement.getUpdateCount();
        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtil.close(statement);
        }
        return new SqlExecuteResult()
                .setLastInsertId(lastInsertId)
                .setAffected(affected);
    }

    public static void execute(Connection connection, String sql) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.execute();

        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtil.close(statement);
        }
    }

    public static void executeQuietly(String url, String userName, String password, String sql) {
        try {
            execute(url, userName, password, sql);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void executeQuietly(Connection connection, String sql) {
        try {
            execute(connection, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String parseSpecialCharsetAnd(String sqlStr) {
        sqlStr = RegExUtils.replaceAll(sqlStr, "__AND__(\\s|__AND__)+__AND__", "__AND__");   // 将多个 __AND__ 替换成一个
        // 根据分隔符进行分割得到数组,同时保留分隔符 sep
        List<String> parts = StringUtil.splitWithSepRetain(sqlStr, "__AND__");

        for (int index=0; index < parts.size(); index ++) {
            String part = parts.get(index);
            if (StringUtils.equals(part, "__AND__")) {
                if (concatableN(parts.get(index-1), parts.get(index+1))) {
                    parts.set(index, " and ");
                } else {
                    parts.set(index, " ");
                }
            }
        }

        return StringUtils.join(parts, "");
    }

    public static boolean concatableN(String part1, String part2) {
        return concatable(part1) && concatable(part2);
    }

    public static boolean concatable(String part) {
        part = StringUtils.lowerCase(StringUtils.trim(part));
        // .匹配任意一个字符 ，*匹配零个或多个 ，优先匹配更多(贪婪)
        boolean orderMatch = part.matches("^(order)\\s+.*");
        boolean whereMatch = part.matches("^(where)\\s+.*");
        boolean whereMatch2 = part.matches(".*\\s+(where)$");

        if (orderMatch || whereMatch || whereMatch2) {
            return false;
        }
        if (StringUtils.equals(part, ";") || StringUtils.equals(part, "")) {
            return false;
        }
        return true;
    }

    @Data
    @Accessors(chain = true)
    public static class NamingSqlResult {
        private String namingSql;
        // 替换成 ? 后的 sql
        private String questionSql;
        private List<String> namings;
    }

    public static NamingSqlResult parseNamingSql(String sql) {
        List<String> namings = StringUtil.getSubStringWithRegexp(sql, ":[a-zA-Z0-9_]+");
        String questionSql = RegExUtils.replaceAll(sql,":[a-zA-Z0-9_]+", "?");
        return new NamingSqlResult()
                .setNamingSql(sql)
                .setQuestionSql(questionSql)
                .setNamings(namings);
    }
}
