package com.dict.util;

import com.dict.bean.ParserPage;
import com.dict.crawl.BaseExtractor;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

/**
 * Created by baidu on 16/5/7.
 */
@CommonsLog
public class CleanUtil {

    static Configuration conf = new Configuration();
    static JdbcTemplate jdbcTemplate = null;
    static  ParserPage p = new ParserPage();

    public static String cleanContent(String content){
        if(content == null || content.length() < 1){
            log.error("content null while word count");
            return  "";
        }
        Element contentE = Jsoup.parse(content);

        //去除html标签
        String text = contentE.text();

        text = text.replaceAll("[ \\(\\)【】]", " ");
        text = text.replaceAll("  ", " ");

        //去除中文
        text = text.replaceAll("[\u4E00-\u9FA5]", "");

        text = text.trim();

        return text;

    }

    public static void main(String[] args){

        try {

            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    conf.get(Configuration.MYSQL_URL),
                    conf.get(Configuration.MYSQL_USER), conf.get(Configuration.MYSQL_PASSWORD), 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }

        List<Map<String, Object>> urls = jdbcTemplate.queryForList("SELECT id, content, avgWordLength FROM parser_page where cet4pts is null ORDER BY id desc");
        for(int i = 0; i < urls.size(); i++){
            String id = String.valueOf(urls.get(i).get("id"));
            String content = (String)urls.get(i).get("content");
//            Double avfW = (Double)urls.get(i).get("avgWordLength");
            content = cleanContent(content);
            p = new ParserPage();
            p.setContent(content);

            BaseExtractor.extracteAvgLength(p);
            int updates = jdbcTemplate.update("update parser_page set content = ?, wordCount = ?, uniqueWordCount = ?" +
                            ", avgWordLength = ?, avgSentLength = ?, highschoolpts = ?, cet4pts = ?, cet6pts = ?, kaoypts = ?, " +
                    "toflepts = ?, ieltspts = ?, grepts =?  where id = ?",
                     p.getContent(), p.getWordCount()
                    , p.getUniqueWordCount(), p.getAvgWordLength(), p.getAvgSentLength(),
                    p.getHighschoolpts(), p.getCet4pts(), p.getCet6pts(), p.getKaoypts(), p.getToflepts(), p.getIeltspts()
                    , p.getGrepts(),
                    id);

            if (updates == 1) {
                System.out.println("更新成功");
            } else {
                System.out.println("更新失败");
            }

        }



    }

}
