package com.youdao.dict.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by liuhl on 15-8-6.
 */
public class DBConnUtil {

    private static String jdbcDriver = "com.mysql.jdbc.Driver";      //定义连接信息
    private static String jdbcUrl = "jdbc:mysql://bizdb1-reader1/eadb1";
    private static String jdbcUser = "eadonline4nb";
    private static String jdbcPasswd = "new1ife4Th1sAugust";

    static {
        InputStream is = null;
        try {
            is = DBConnUtil.class.getClassLoader().getResourceAsStream("conf/database.properties");   //加载database.properties文件
            Properties p = new Properties();
            p.load(is);
            jdbcDriver = p.getProperty("jdbcDriver");    //赋值
            jdbcUrl = p.getProperty("jdbcUrl");
            jdbcUser = p.getProperty("jdbcUser");
            jdbcPasswd = p.getProperty("jdbcPasswd");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();   //  关闭is
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Connection getConn() {   // 建立连接方法
        Connection conn = null;
        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPasswd);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void closeAll(ResultSet rs, Statement st, Connection conn) {  //关闭连接（用于增删改）
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeAll(ResultSet rs, PreparedStatement ps, Connection conn) {  // 关闭连接（用于查）
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}

