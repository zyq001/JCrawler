package com.youdao.dict.util;


import com.youdao.dict.bean.ParserPage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuhl on 15-8-5.
 */
public class DBClient {

    public static long insert(ParserPage p) {

        String sql = "insert into parser_page (title, type, label, level, style, host, url, content, version, mainimage) values (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = DBConnUtil.getConn();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getType());
            ps.setString(3, p.getLabel());
            ps.setString(4, p.getLevel());
            ps.setString(5, p.getStyle());
            ps.setString(6, p.getHost());
            ps.setString(7, p.getUrl());
            ps.setString(8, p.getContent());
            ps.setString(9, p.getVersion());
            ps.setString(10, p.getMainimage());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnUtil.closeAll(rs, ps, conn);
        }
        return 0;
    }

    public static List<ParserPage> getList() {
        List<ParserPage> list = new ArrayList<ParserPage>();
        String sql = "select * from parser_page where host like '%cnn.com%'";
        Connection conn = DBConnUtil.getConn();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ParserPage p = new ParserPage();
                p.setId(rs.getLong("id"));
                p.setTitle(rs.getString("title"));
                p.setType(rs.getString("type"));
                p.setLabel(rs.getString("label"));
                p.setLevel(rs.getString("level"));
                p.setStyle(rs.getString("style"));
                p.setHost(rs.getString("host"));
                p.setUrl(rs.getString("url"));
                p.setContent(rs.getString("content"));
                p.setTime(rs.getString("time"));
                p.setVersion(rs.getString("version"));
                p.setMainimage(rs.getString("mainimage"));
                p.setEndtime(rs.getInt("endtime"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, String> getChannelMapping() {
        Map<String, String> result = new HashMap<String, String>();
        String sql = "select type,channel from type_channel";
        Connection conn = DBConnUtil.getConn();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("type"), rs.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean updateTime(ParserPage p) {
        boolean flag = true;
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "update parser_page set time=? where id=?";
        conn = DBConnUtil.getConn();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getTime());
            ps.setLong(2, p.getId());
            int i = ps.executeUpdate();
            if (i == 0) {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnUtil.closeAll(null, ps, conn);
        }
        return flag;
    }

    public static boolean updateType(String oldType, String newType) {
        boolean flag = true;
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "update parser_page set type=? where type=?";
        conn = DBConnUtil.getConn();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, oldType);
            ps.setString(1, newType);
            int i = ps.executeUpdate();
            if (i == 0) {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnUtil.closeAll(null, ps, conn);
        }
        return flag;
    }

    public static boolean update(ParserPage p) {
        boolean flag = true;
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "update parser_page set content=? where id=?";
        conn = DBConnUtil.getConn();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getContent());
            ps.setLong(2, p.getId());
            int i = ps.executeUpdate();
            if (i == 0) {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnUtil.closeAll(null, ps, conn);
        }
        return flag;
    }

    public static boolean remove(ParserPage p) {
        boolean flag = true;
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "delete from parser_page where id=?";
        conn = DBConnUtil.getConn();
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, p.getId());
            int i = ps.executeUpdate();
            if (i == 0) {
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnUtil.closeAll(null, ps, conn);
        }

        return flag;
    }
}
