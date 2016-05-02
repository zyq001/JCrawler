package com.dict.crawl;

import com.dict.util.JDBCHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zangyq on 2015/12/7.
 */
public class DictBKWikiCrawler implements Job{

    public static JdbcTemplate jdbcTemplate = null;

    public static String js_baike_words = "http://xue.youdao.com/special/js_baike_words_v2";

    public static String urlTail = "eng";

    public static  String exmpDetailUrl = "http://dict.youdao.com/search?le=eng&q=bk:apple&keyfrom=dataserver&doctype=json&jsonversion=2";

    public static String dictWikiType = "Wiki";

    private  static DictBKWikiCrawler instance = null;

    public static DictBKWikiCrawler getInstance(){
        if(instance == null){
            instance = new DictBKWikiCrawler();
        }
        return  instance;
    }

    public static JdbcTemplate getJdbcTemplate(){
        if(jdbcTemplate == null ){
            try {
/*
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://localhost/readease?useUnicode=true&characterEncoding=utf8",
                    "root", "tiger", 5, 30);
*/
                jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                        "jdbc:mysql://pxc-mysql.inner.youdao.com/readease?useUnicode=true&characterEncoding=utf8",
                        "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
            } catch (Exception ex) {
                jdbcTemplate = null;
                System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");

            }
        }
        return jdbcTemplate;
    }

    public DictBKWikiCrawler(){
        try {
/*
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://localhost/readease?useUnicode=true&characterEncoding=utf8",
                    "root", "tiger", 5, 30);
*/
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://pxc-mysql.inner.youdao.com/readease?useUnicode=true&characterEncoding=utf8",
                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }
    }

    public static List<String> getUrls(){

        List<String> res = new ArrayList<String>();
        URL urls = null;
        try {
            urls = new URL(js_baike_words);
            HttpURLConnection urlConnection = (HttpURLConnection) urls.openConnection();
            InputStream is = urlConnection.getInputStream();
            Reader rd = new InputStreamReader(is, "utf-8");
            BufferedReader bf = new BufferedReader(rd);
            StringBuilder sb = new StringBuilder();
            String line = bf.readLine();
            if (line == null || line.equals("") || line.charAt(0) == '/') line = bf.readLine();
            sb.append('[');
            while (!line.contains("英文热点")) {
                line = bf.readLine();
            }
            line = bf.readLine();
            while ((line = bf.readLine()) != null) {
                if (line.contains("link")) {
                    int start = line.indexOf("http");
                    int end = line.indexOf(urlTail) + 3;
                    res.add(line.substring(start, end));
                    System.out.println(line);
                }
                if (line.contains("items") || line.contains("网络爆红") || line.contains("不可错过"))
                    break;
            }
            rd.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
//
//    public static String extracteContent(String text){
//        if(text == null || text.equals("")) return "";
//        String mainImage = null;
//        int width = 0;
//        Document doc = Jsoup.parse(text);
//        for(Element e: doc.select("img")){
//            String dataimg = e.attr("dataimg");
//            int wIndex = dataimg.indexOf("w=") + 2;
//            width = Integer.valueOf(dataimg.substring(wIndex));
//            e.attr(e.attr("dataimg"));
//            if(mainImage == null && width >= 130){
//                mainImage = dataimg;
////                            break;
//            }
//    }

    public static boolean fetch(String title){
        String url = exmpDetailUrl.replace("apple", title);
        if(url == null || url.equals("") || url.indexOf("http") < 0) return false;
        URL urls = null;
        try {
            urls = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection)urls.openConnection();
            InputStream is = urlConnection.getInputStream();
            Reader rd = new InputStreamReader(is, "utf-8");
            JsonObject json = new JsonParser().parse(rd).getAsJsonObject();

            String mainImage = null;
            int width = 0;

            StringBuilder sb = new StringBuilder();
            JsonObject wikipedia = json.get("wikipedia").getAsJsonObject();
            JsonArray ja = null;
            if(wikipedia != null )ja = wikipedia.get("sections").getAsJsonArray();
//            JsonArray ja = json.get("sections").getAsJsonArray();
            for(JsonElement je: ja){
                String text = je.getAsJsonObject().get("text").getAsString();
                Document doc = Jsoup.parse(text);
//                if(mainImage == null && text.contains("img")){



                doc.select("table").remove();
                    doc.select(".icon").remove();
                doc.select(".navbox").remove();
                doc.select(".mw-editsection").remove();
                doc.select(".mw-headline").select("span[id=See_also]").remove();
                doc.select(".mw-headline").select("span[id=External_links]").remove();
                doc.select(".reference").remove();
                for(Element e: doc.select("span")){
                    if(e.text().equals("Edit"))
                        e.remove();
                }

                    Elements hypLinks = doc.select("a");
                    for(Element a: hypLinks){
                        a.unwrap();
                    }

                    for(Element e: doc.select("img")){
                        String dataimg = e.attr("dataimg");
                        String src = e.attr("src");
                        if(src.startsWith("./")) {
                            e.remove();
                            continue;
                        }
                        width = Integer.valueOf(e.attr("width"));
//                        width = Integer.valueOf(dataimg.substring(wIndex));
//                        e.attr(e.attr("dataimg"));
                        if(mainImage == null && width >= 130){
                            mainImage = dataimg;
//                            break;
                        }
                    }
                String contentHtml = doc.html();
                contentHtml = contentHtml.replaceAll("&gt;", ">").replaceAll("&lt;", "<");//替换转义字符

                contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
                contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
                contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
                contentHtml = contentHtml.replaceAll("<(?!img|br|li|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
                contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n

//                }
                sb.append(contentHtml);

            }

            String content = sb.toString();
            content = BaseExtractor.addAdditionalTag(content);
            String urll = wikipedia.get("share").getAsJsonObject().get("url").getAsString();
            String style = "no-image";
            String host = "www.wikipedia.org";
            String time = new Timestamp(System.currentTimeMillis()).toString();


            if(width >= 300)
                style = "large-image";

            int wordCount = BaseExtractor.contentWordCount(content);

            int uniqueWordCount = BaseExtractor.getUniqueCount(content);

            int updates = getJdbcTemplate().update("insert ignore into parser_page (title, type, label, level, style" +
                    ", host, url, time, description, content, wordCount, uniqueWordCount, version, mainimage) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    title, dictWikiType, "", "", style, host, urll, time, "", content, wordCount, uniqueWordCount, "1", mainImage);
            if (updates == 1) {
                System.out.println("mysql插入成功");
            }else{
                System.out.println("插入失败：updates：" + updates);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void dictBK(){
        List<String> urls = getUrls();
        for(String url: urls){
            int start = url.indexOf("q=");
            int end = url.indexOf("&more=");
            String key = url.substring(start + 2, end);
//            url = exmpDetailUrl.replace("apple", key);
            fetch(key);
        }
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
//            System.out.println("execute job at " + new Date() + " by trigger " + context.getTrigger().getJobKey());

        dictBK();


    }

    public static void main(String[] args){

        new DictBKWikiCrawler().dictBK();

    }

}
