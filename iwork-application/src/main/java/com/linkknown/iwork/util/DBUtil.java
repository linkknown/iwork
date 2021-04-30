package com.linkknown.iwork.util;

import com.linkknown.iwork.common.exception.IWorkException;
import lombok.Data;

import java.sql.*;

public class DBUtil {

    public static Connection getConnection(String url, String userName, String password) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url, userName, password); //获取连接
    }

    @Data
    public static class PingResult {
        private boolean isValid;
        private String errorMsg;
    }

    public static boolean pingQueitly (String url, String userName, String password) {
        try {
            return ping(url, userName, password);
        } catch (IWorkException|SQLException|ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean ping (String url, String userName, String password) throws IWorkException, SQLException, ClassNotFoundException {
        Connection connection = getConnection(url, userName, password);
        boolean valid = connection.isValid(10);
        if (!valid) {
            throw new IWorkException(String.format("DB 实例连接失败：url is %s, userName is %s, password is", url, userName, password));
        }
        return true;
    }


    public static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
        close(rs, stmt, conn);
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
