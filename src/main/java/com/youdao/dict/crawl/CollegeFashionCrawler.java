package com.youdao.dict.crawl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.youdao.dict.util.JDBCHelper;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import com.youdao.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zangyq on 2015/12/7.
 */
@CommonsLog
public class CollegeFashionCrawler implements Job{

    public static JdbcTemplate jdbcTemplate = null;

    public static String baseUrl = "http://www.collegefashion.net";

    public static String host = "www.collegefashion.net";

    public static String type = "Lifestyle";

    public  static String baseImageUrl = "http://a5.files.collegefashion.net/image/upload/c_fit,cs_srgb,dpr_1.0,q_80,w_620/";

    //FASHION TIPS    INSPIRATION    CAMPUS STYLE    TRENDS    BEAUTY & HAIR    COLLEGE LIFE    DORM            NEWS    SHOPPING
    public static String collegeLifetoken = "/.api/stream/college-life/?moreResultsToken=";

//    public static String collegeLifeJson = "MDoxMDA6YTcwYjVlMDJiMmE0MmFmMWMwMDAyMjZmOTJmOTNlOTc%3D";//size = 100
    public static String collegeLifeJson = "MDoxMDphNzBiNWUwMmIyYTQyYWYxYzAwMDIyNmY5MmY5M2U5Nw%3D";//size = 10

//    public static String CampusStyleJson = "MDoxMDA6YmQ4ZGNmZTFlOWY4ODY3ZGJkNTVlMGZjMDQ1MmZmNTA%3D";//size = 100
    public static String CampusStyleJson = "MDoxMDpiZDhkY2ZlMWU5Zjg4NjdkYmQ1NWUwZmMwNDUyZmY1MA%3D";//size = 10

//    public static String newsJson = "MDoxMDA6MGI3MmE0YzRiNmE3MjJiM2MzOTU2MDc1ZmIzYjk2NTQ%3D";//size = 100
    public static String newsJson = "MDoxMDowYjcyYTRjNGI2YTcyMmIzYzM5NTYwNzVmYjNiOTY1NA%3D";//size = 10

//    public static String shoppingJson = "MDoxMDA6NzU2ODBlMGEzOTEzYzQ4NzVlNmQxZGI5Yzc2YWI0YzU%3D";//size = 100
    public static String shoppingJson = "MDoxMDo3NTY4MGUwYTM5MTNjNDg3NWU2ZDFkYjljNzZhYjRjNQ%3D";//size = 10

//    public static String beautyHearJson = "MDoxMDA6ZTEyZDkwZWI1ZWI0ZDY5ODQ4N2ZkZTM1NmY0ZGVmOTk%3D";//size = 100
    public static String beautyHearJson = "MDoxMDplMTJkOTBlYjVlYjRkNjk4NDg3ZmRlMzU2ZjRkZWY5OQ%3D";//size = 10

//    public static String fashTipsJson = "MDoxMDA6MDQ4M2NiNzQwMjk0MzUxNWNhMDEwODY5YzUxMjRkYzQ%3D";//size = 100
    public static String fashTipsJson = "MDoxMDowNDgzY2I3NDAyOTQzNTE1Y2EwMTA4NjljNTEyNGRjNA%3D";//size = 10

//    public static String dormJson = "MDoxMDA6OTM5MmE3MWNhZGJlYjM5MjA2NWQyZDc2ZjNkZDFjNDk%3D";//size = 100
    public static String dormJson = "MDoxMDo5MzkyYTcxY2FkYmViMzkyMDY1ZDJkNzZmM2RkMWM0OQ%3D";//size = 10

//    public static String trendsJson = "MDoxMDA6YzVlYmFlYWEzMGYyZjY2NTc1ZmQ3Y2Q3ZTMwYTgyZDg%3D";//size = 100
    public static String trendsJson = "MDoxMDpjNWViYWVhYTMwZjJmNjY1NzVmZDdjZDdlMzBhODJkOA%3D";//size = 10

    public static String js_baike_words = "http://xue.youdao.com/special/js_baike_words_v2";

    public static String urlTail = "eng";

    public static  String exmpDetailUrl = "http://dict.youdao.com/search?le=eng&q=bk:apple&keyfrom=dataserver&doctype=json&jsonversion=2";

//    public static String dictWikiType = "Wiki";

    private  static CollegeFashionCrawler instance = null;

    public static Map<String, String> resMap = new HashMap<String, String>();

    public static CollegeFashionCrawler getInstance(){
        if(instance == null){
            instance = new CollegeFashionCrawler();
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

    public CollegeFashionCrawler(){
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

    public static String extractTime(String time){
//2015-12-06T15:00:00Z
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        Date date;
        try {
            date = format.parse(time.trim());
        } catch (ParseException e) {
            log.info("*****extractorTime format.parse  failed***** ");
            return "";
        }
        return new Timestamp(date.getTime()).toString();
    }




    public static String extractContent(Document content){


//        String text = je.getAsJsonObject().get("text").getAsString();
//        Document doc = null;
//        try {
//            doc = Jsoup.connect(url).get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//                if(mainImage == null && text.contains("img")){

        if(content == null){
            log.error("get url content faile " );
            return "";
        }
//            return "";

//        Elements content = doc.select("section[itemprop=itemprop]");

//        content.select("ng-binding").remove();
//        doc.select(".icon").remove();
//        doc.select(".navbox").remove();
//        doc.select(".mw-editsection").remove();
//        doc.select(".mw-headline").select("span[id=See_also]").remove();
//        doc.select(".mw-headline").select("span[id=External_links]").remove();
//        doc.select(".reference").remove();
//        for(Element e: doc.select("span")){
//            if(e.text().equals("Edit"))
//                e.remove();
//        }

        Elements hypLinks = content.select("a");
        for(Element a: hypLinks){
            a.unwrap();
        }



        String contentHtml = content.html();
        contentHtml = contentHtml.replaceAll("&gt;", ">").replaceAll("&lt;", "<");//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|li|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n

        return  contentHtml;
    }

    public static Map<String , String> getBodyImgMap(JsonArray tmlEmbeds){
//        Map<String, String> resMap = new HashMap<String, String>();
        for(JsonElement je: tmlEmbeds){
            JsonObject jo = je.getAsJsonObject();
            JsonElement id = jo.get("id");
            JsonElement publicId = jo.get("publicId");
            if(id == null || publicId == null) continue;
            resMap.put(id.getAsString(), publicId.getAsString());
        }
        return resMap;
    }

    public static boolean fetchFullPage(String url){

        return  false;
    }

    public static boolean fetch(String chanel){
        String jsonUrl = baseUrl + chanel;
        if(jsonUrl == null || jsonUrl.equals("") || jsonUrl.indexOf("http") < 0) return false;
        URL urls = null;
        try {
            urls = new URL(jsonUrl);
            HttpURLConnection urlConnection = (HttpURLConnection)urls.openConnection();
            InputStream is = urlConnection.getInputStream();
            Reader rd = new InputStreamReader(is, "utf-8");
            JsonObject json = new JsonParser().parse(rd).getAsJsonObject();

//            String mainImage = null;
//            int width = 0;

            JsonArray items = json.get("items").getAsJsonArray();
            for(JsonElement je: items){
                long start = System.currentTimeMillis();
                JsonObject jo = je.getAsJsonObject();
                String title = jo.get("title").getAsString();



                String time = jo.get("publicationTimestamp").getAsString();
                time = extractTime(time);
                String url = baseUrl + jo.get("path").getAsString();

                //过滤部分文章
                if(title == null || title.contains("autelinks")){
                    System.out.println("skip article:" + title + "---url:" + url);
                    continue;
                }

                String description ="";
                        JsonElement descJE = jo.get("metaDescription");
                if(descJE != null) description = descJE.getAsString();

                System.out.println("desc之后：" + (System.currentTimeMillis() - start));
                String label = "";
                StringBuilder labelSB = new StringBuilder();
                for(JsonElement le: jo.get("associatedRichTerms").getAsJsonArray()){
                    JsonElement displayJE = le.getAsJsonObject().get("displayName");
                    if(displayJE == null) continue;
                    labelSB.append(displayJE.getAsString());
                    labelSB.append(",");
                }
                if(labelSB.length() > 1)label = labelSB.substring(0, labelSB.length() - 1);
//                label = labelSB.toString();

                System.out.println("label之后：" + (System.currentTimeMillis() - start));

                String moreinfo = "";
                JsonElement orgTypeJE = jo.get("sectionKey");
                String orgType = "";
                if(orgTypeJE != null) orgType = orgTypeJE.getAsString();
                moreinfo = TypeDictHelper.getMoreInfo(orgType);
                System.out.println("moreInfo之后：" + (System.currentTimeMillis() - start));
                type = TypeDictHelper.getType(orgType,"Lifestyle");
                System.out.println("type之后：" + (System.currentTimeMillis() - start));

                Map<String, String> imgmap = getBodyImgMap(jo.get("tmlEmbeds").getAsJsonArray());//获取正文中所有图片的id与publicId映射
                String mainImage = null;
                int width = 0;
                JsonObject imgO = jo.get("primaryImage").getAsJsonObject();
                if(imgO != null){
                    JsonElement idJE = imgO.get("id");
                    JsonElement publicidJE = imgO.get("publicId");
                    if(idJE == null || publicidJE == null){
                        JsonElement featuredImg =  jo.get("featuredImage");//有些文章不在primaryImage里 而在featuredImage里
                        if(featuredImg != null){
                            idJE = featuredImg.getAsJsonObject().get("id");
                            publicidJE = featuredImg.getAsJsonObject().get("publicId");
                        }
                    }

                    if(idJE != null || publicidJE != null) {
                        imgmap.put(idJE.getAsString(), publicidJE.getAsString());//主图放入map

                        mainImage = baseImageUrl + publicidJE.getAsString() + ".jpg";
                        OImageUploader uploader = new OImageUploader();
                        long id = 0;
                        try {
                            id = uploader.deal(mainImage);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        //                long id = 0;
                        URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");

                        if (!mainImage.equals(newUrl)) {
                            mainImage = newUrl.toString();
                            width = uploader.getWidth();
                        }
                    }
                }
                System.out.println("mainImage之后：" + (System.currentTimeMillis() - start));

                Document doc = null;
                if(jo.get("bodyTml") == null) continue;
                doc = Jsoup.parse(jo.get("bodyTml").getAsString());
//                try {
//                    doc = Jsoup.connect(url).get();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                String label = doc.select("meta[name=keywords]").attr("content");
//                String content = extractContent(doc);

                doc.select("a").unwrap();
                System.out.println("aUnwrap之后：" + (System.currentTimeMillis() - start));
//                String mainImage = null;
//                int width = 0;
                Elements imgDivs = doc.select("div");
                for(Element e: imgDivs){
                    String tmlImage = e.attr("tml-image");
                    if(tmlImage == null || tmlImage.equals("")){
                        e.remove();
                    }else{
                        String imgPublicId = imgmap.get(tmlImage);
                        if(imgPublicId == null){
                            e.remove();
                            continue;
                        }
                        String imageUrl = baseImageUrl + imgPublicId + ".jpg";
                        OImageUploader uploader = new OImageUploader();
                        long id = 0;
                        try {
                            id = uploader.deal(imageUrl);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        //                long id = 0;
                        URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");

                        Tag imgTag = Tag.valueOf("img");
//                img.appendChild(imgTag);
                        Element newImg = new Element(imgTag, "");
                        newImg.attr("src", newUrl.toString());
                        newImg.attr("style", "width:100%;");
                        if (mainImage == null) {
                            width = uploader.getWidth();
                            mainImage = newUrl.toString();
                        }
                        e.replaceWith(newImg);
                    }
                }
                System.out.println("div清除之后：" + (System.currentTimeMillis() - start));

                String style = "large-image";
                if(mainImage == null || mainImage.equals("") || width < 300) style = "no-image";

                if(doc.body().html().length() < 384) return false;//太短


                int wordCount = BaseExtractor.contentWordCount(doc.body());

                long bef = System.currentTimeMillis();
                int updates = getJdbcTemplate().update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, wordCount, version, mainimage, moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        title, type, label, "", style, host, url, time, description, doc.body().html(), wordCount, "1", mainImage, moreinfo);
                //add wordcount

                long aft = System.currentTimeMillis();
//                System.out.println("插入耗时（毫秒）：" + (aft - bef));
                if (updates == 1) {
                    System.out.println("parser_page插入成功");
                    int id = jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", url);

                    updates = jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
                            id, jo.get("bodyTml").getAsString());
                    System.out.println("org_content插入成功");
                }else{
                    System.out.println("插入失败：updates：" + updates);
                }


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
        String collegeLife = collegeLifetoken + collegeLifeJson;
        fetch(collegeLife);


        String campus = collegeLifetoken.replace("college-life", "campus-style") + CampusStyleJson;
        fetch(campus);

        String shopping = collegeLifetoken.replace("college-life", "shopping") + shoppingJson;
        fetch(shopping);

        String beautyHair = collegeLifetoken.replace("college-life", "beauty-and-hair") + beautyHearJson;
        fetch(beautyHair);

        String News = collegeLifetoken.replace("college-life", "news") + newsJson;
        fetch(News);

        String Trends = collegeLifetoken.replace("college-life", "trends") + trendsJson;
        fetch(Trends);

        String Dorm = collegeLifetoken.replace("college-life", "dorm") + dormJson;
        fetch(Dorm);

        String Fashion = collegeLifetoken.replace("college-life", "fashion-tips") + fashTipsJson;
        fetch(Fashion);
    }


    public void execute(JobExecutionContext context) throws JobExecutionException {
//            System.out.println("execute job at " + new Date() + " by trigger " + context.getTrigger().getJobKey());

        dictBK();


    }

    public static void main(String[] args){

        new CollegeFashionCrawler().dictBK();

    }

}
