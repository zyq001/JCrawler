package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import com.youdao.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class TheStarExtractor extends BaseExtractor {

    public TheStarExtractor(Page page) {
        super(page);
    }

    public TheStarExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("TheStarRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");



//            String isarticle = context.output.get("isarticle").toString();
//            if(isarticle.contains("article")){
//                log.debug("*****init  success*****");
////                content.select("div[id=sidebar-second]").remove();
////                content.select("div[id=content-bottom]").remove();
//                Elements socailLinks = content.select("div[class=social-links]");
//                if(socailLinks != null)socailLinks.remove();
//                content.select("div[class=authoring full-date]").remove();
//                content.select("div[class=authoring]").remove();
//                return true;
//            }
            log.info("*****init  success***** url:" + url);
            return true;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        if (elementTitle == null)
            return false;
        String title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
//        if (title.contains("-"))
//            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
//        else
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        Element elementType = (Element) context.output.get("type");
        if (elementType == null)
            return false;
        Elements types = elementType.select("p[class=breadcrumbs]").select("a");
        Element typeE = types.get(1);
        String type = typeE.text();
//        child(0).tagName();
//        String type = elementType.attr("content");
        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        if (type.contains("/")) {
            type = type.substring(0, type.indexOf("/"));
            type = type.replace("/", "");
        }
//        type = type
        type = TypeDictHelper.getType(type, type);
        p.setType(type.trim());

        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null)
            return true;
        Elements labelE = elementLabel.select("a");
        if(labelE == null || labelE.size() < 1){
            log.debug("no keywords, continue");
            return true;
        }
        String[] keywords = new String[labelE.size()];
        for(int i = 0; i < labelE.size(); i++){
            keywords[i] = labelE.get(i).text();
        }

        //按关键字由短到长排序
        PriorityQueue<String> pq = new PriorityQueue<String>(10, new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split(" ").length, length2 = o2.split(" ").length;
                return length1 - length2;
            }
        });

        for(String keyword: keywords){
            int wordCount = keyword.split(" ").length;
            if(wordCount <= 3) pq.add(keyword);
        }
        StringBuilder sb = new StringBuilder();
        while(!pq.isEmpty()){
            sb.append(pq.poll());
            if(!pq.isEmpty()) sb.append(',');
        }

        p.setLabel(sb.toString());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract Time, skip");
            return false;

        }
        Elements timeE = elementTime.select("p[class=date]");

        if (timeE == null) {
            log.info("*****extractorTime  failed***** url:" + url);
            return false;
        }
        String time = timeE.text();
        Date date = new Date();
        if(!time.contains("Updated")){
            String month = time.split(" ")[2];
            if (month == null) {
                log.error("con't get month, skip");
                return false;
            }
            int length = month.length();
            StringBuilder M = new StringBuilder();
            while (length-- > 0) M.append('M');
            SimpleDateFormat format = new SimpleDateFormat("E, dd " + M.toString() + " yyyy ", Locale.US);

            try {
                date = format.parse(time.trim());
            } catch (ParseException e) {
//            return false;
                e.printStackTrace();
            }
        }else {
            time = time.substring(time.indexOf("Updated") + 9);

            //获取月份的长度
            String month = time.split(" ")[1];
            if (month == null) {
                log.error("con't get month, skip");
                return false;
            }
            int length = month.length();
            StringBuilder M = new StringBuilder();
            while (length-- > 0) M.append('M');
            SimpleDateFormat format = new SimpleDateFormat("E " + M.toString() + " dd, yyyy zzz h:mm:ss a", Locale.US);

            try {
                date = format.parse(time.trim());
            } catch (ParseException e) {
//            return false;
                e.printStackTrace();
            }
        }
        if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
            log.debug("*****extractorTime  out of date*****");
            return false;
        }

        p.setTime(new Timestamp(date.getTime()).toString());//utc 2 cst北京时间
        log.debug("*****extractorTime  success*****");
        return true;
    }

    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){
            log.error("can't extract desc, continue");
            return true;
        }
        String description = elementTime.attr("content");
        if (description == null || "".equals(description.trim())) {
            log.info("*****extractor Desc  failed***** url:" + url);
            return true;
        }

        p.setDescription(description);

        return true;
    }

    public boolean isPaging() {
//        Elements div2 = doc.select("div[id=\"content-main\"]");
        Elements div2 = content.select("div[id=content-main]");
        Elements sociallinks = div2.select("div[class=social-links]");
        if(sociallinks != null) sociallinks.remove();//去除社交网络分享栏目框
        Elements div = div2.select("div[class=item-list");
        if (div == null) {
            return false;
        }
        Elements a = div.select("li");
        if (a == null || a.size() == 0) {
            return false;
        }
/*        if (url.equals(a.get(0).attr("href"))) {
            return false;
        }*/
        return true;
    }

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            return false;
        }

        //文章题目重复出现，去除之
        content.select("div[id=block-onix-highlight-onix-highlight-article-header]").remove();
        content.select("div[id=block-views-article-title-block]").remove();

        if(isPaging()) return true;
       /* if (host.equals(port)) return true;*/

        Elements imgs = content.select("img");
        String mainImage = null;
        int width = 0;
        for (Element img : imgs) {
            try {
                String imageUrl = img.attr("src");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    img.remove();
                    continue;
                }
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
                //                img.removeAttr("srcset");
                img.attr("style", "width:100%;");
                OImageUploader uploader = new OImageUploader();
                if (!"".equals(host) && !"".equals(port))
                    uploader.setProxy(host, port);
                long id = uploader.deal(imageUrl);
                //                long id = 0;
                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                img.attr("src", newUrl.toString());
                if (mainImage == null) {
                    width = uploader.getWidth();
                    mainImage = newUrl.toString();
                }
            } catch (Exception e) {
                img.remove();
                e.printStackTrace();
            }
        }
         if(mainImage == null) {
            Element elementImg = (Element) context.output.get("mainimage");
            if (elementImg == null){
                log.error("img craw failed, continue");
//                return true;
            }else {
                String tmpMainImage = elementImg.attr("content");
                OImageUploader uploader = new OImageUploader();
                if (!"".equals(host) && !"".equals(port))
                    uploader.setProxy(host, port);
                long id = 0;
                try {
                    id = uploader.deal(tmpMainImage);

//                long id = 0;
                    URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                    width = uploader.getWidth();
                    mainImage = newUrl.toString();
                } catch (Exception e1) {
//                        e1.printStackTrace();

                }
            }
        }
        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width > 300) {
            p.setStyle("large-image");
        } else {
            p.setStyle("no-image");
        }

//        } catch (Exception e) {
//            p.setStyle("no-image");
//        }
        return true;

    }

    public void mergePage(ParserPage p) {
        log.debug("*****mergePage*****");
//        Elements div = doc.select("div[class=\"item-list\"]").select("li[class=\"pager-next\"]").select("a");
//        LinkedList<String> list = new LinkedList<String>();
//        for (Element a : div) {
//            String url = a.attr("href");
//            if (!list.contains(url)) {
//                list.add(url);
//            }
//        }
//        Elements div2 = content.select("div[id=content-main]");
//        Elements sociallinks = div2.select("div[class=social-links]");
//        if(sociallinks != null) sociallinks.remove();//去除社交网络分享栏目框
//        Elements div = div2.select("div[class=item-list");
//        if (div == null) {
//            return;
//        }
//        Elements a = div.select("li");

//        String nextPage = div.attr("href");
//        if(nextPage.equals("")) return;
        TheStarExtractor extractor = new TheStarExtractor(url + "?singlepage=true");
        extractor.init();
        extractor.extractorAndUploadImg();
        extractor.extractorContent(true);
        p.setContent(extractor.getParserPage().getContent());

        log.info("*****mergePage end*****");
    }

}
