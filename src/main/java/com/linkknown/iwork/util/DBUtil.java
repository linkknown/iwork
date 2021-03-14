package com.linkknown.iwork.util;

import lombok.Data;

import java.io.Closeable;
import java.io.IOException;
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

    public static PingResult ping(String url, String userName, String password) {
        PingResult pingResult = new PingResult();

        try {
            Connection connection = getConnection(url, userName, password);
            pingResult.setValid(connection.isValid(10));

        } catch (Exception e) {
            e.printStackTrace();
            pingResult.setErrorMsg(e.getMessage() + ":" + e.getCause().getMessage());
        }
        return pingResult;
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
