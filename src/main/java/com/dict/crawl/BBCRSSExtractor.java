package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.souplang.SoupLang;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.RSSReaderHelper;
import com.dict.util.TypeDictHelper;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.pojava.datetime.DateTime;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class BBCRSSExtractor extends BaseExtractor {

    public BBCRSSExtractor(Page page) {
        super(page);
    }

    public BBCRSSExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            if(url.matches(".*bbc.com/(japan|urdu|vietnamese|persian|arabic|zhongwen|indone" +
            "|kyrgyz|portuguese|mundo|ukrainian|azeri|afrique|nepali|russian|swahili|bengali|hausa|gahuza|pashto|sinhala|tamil" +
                    "|uzbek|turkce|somali|gahuza|hindi|burmese).*")) {
                log.error("non-English article, skipped");
                return false;
            }
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("BBCRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
//            Element article = (Element) context.output.get("isarticle");
            if(content == null) {
                log.error("extrate content failed, skipped");
                return false;
            }
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(10));
//            if(article == null || article.toString().contains("article") || url.contains("story")){
                for(Element svg: content.select("svg")){
                    if(svg != null) svg.remove();
                }

                AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(50));
//                content = content.child(0).select(".content-body").first();
                content.select(".gelicon").remove();
                content.select(".icon-wrapper").remove();
                content.select(".elastislide-wrapper").remove();
                content.select("blockquote").remove();
                content.select(".inline-horizontal-partner-module").remove();//广告
                content.select(".story-footer").remove();
                content.select(".component").remove();
                content.select(".component--default").remove();
                content.select(".story-body__link").remove();//READ MORE
                content.select(".story-body__crosshead").remove();
//                content.select(".media-landscape").remove();
                content.select(".story-body__unordered-list").remove();//magazine 推荐文章


                content.select(".content-meta").remove();//fenxiang
//                content.select(".small-thing").remove();//同上 一般一起出现
                content.select(".icon").remove();//每篇文章最后都有的洋葱图标
                content.select(".on-overlay").remove();//CLOSE button
                content.select(".below-article-tools").remove();//文章下面分享图标


                content.select(".rounded-icon").remove();//分享图标
                content.select(".inline-icon").remove();//分享图标
                content.select(".inline-share-facebook").remove();//分享图标
                content.select(".js-sport-tabs").remove();
                content.select(".content__meta-container").remove();//作者与分享图标
                content.select(".submeta").remove();
    //            content.select(".inline-share-facebook").remove();
    //            content.select(".inline-icon").remove();

                content.removeClass("content__article-body from-content-api js-article__body");
                content.removeClass("meta__social");


    //            String isarticle = context.output.get("isarticle").toString();
    //            if(isarticle.contains("article")){
                    log.debug("*****init  success*****");
                    return true;
//            }
//            log.error("*****init  failed，isn't an article***** url:" + url);
//            return false;
        } catch (Exception e) {
            log.error("*****init  failed***** url:" + url);
            e.printStackTrace();
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        String title;
        if (elementTitle == null){
            log.error("extracte title failed skipped");
            return false;
        }
        title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            title = elementTitle.text();
            if (title == null || "".equals(title.trim())) {
                log.error("*****extractorTitle  failed***** url:" + url);
                return false;
            }
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

        String type = RSSReaderHelper.getType(url);
        if(type != null && !type.equals("")) {
            p.setType(type);
        }else{
            log.error("cant get type, false");
            return false;
//            return true;
        }

        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null) {
            log.debug("no keywords continue, url:" + url);
            return true;
        }
        String label = elementLabel.attr("content");
//        String label = (String) context.output.get("label");
        if (label == null || "".equals(label.trim())) {
            log.info("*****extractorLabel  failed continue***** url:" + url);
            return true;
        }
        label = label.replaceAll(" ", "");
//        label = label.contains("China")?"China":label.contains("news")? "World": label;//news belong to World
        String[] keywords = label.split(",");
        for(String k: keywords){
            if(TypeDictHelper.rightTheType(k)) {
                p.setType(k.trim());
                break;
            }

        }
        PriorityQueue<String> pq = new PriorityQueue<String>(10, new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split(" ").length, length2 = o2.split(" ").length;
                return length1 - length2;
            }
        });

//        String typeFromLabel = keywords[0];
//        typeFromLabel = TypeDictHelper.getType(typeFromLabel, type);
////        if(TypeDictHelper.rightTheType(type))
//        p.setType(typeFromLabel);

        for(String keyword: keywords){
            int wordCount = keyword.split(" ").length;
            if(wordCount <= 3 && !pq.contains(keyword) && keyword.length() > 1) pq.add(keyword);
        }
        StringBuilder sb = new StringBuilder();
        Set<String> antiMuti = new HashSet<String>();
        while(!pq.isEmpty()){
            String key = pq.poll();
            if(!antiMuti.contains(key.toLowerCase())) {
                antiMuti.add(key.toLowerCase());
                sb.append(key);
                if (!pq.isEmpty()) sb.append(',');
            }
        }
        p.setLabel(sb.toString());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {

        log.debug("*****extractorTime*****");
        SyndEntry entry = RSSReaderHelper.getSyndEntry(url);
        if(entry != null && entry.getPublishedDate() != null){
            p.setTime(new Timestamp(entry.getPublishedDate().getTime()).toString());
            return true;
        }

        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null) {
            log.error("elementTime null, false");
            return false;
        }
        elementTime = elementTime.select(".date").first();
        if (elementTime == null ) {
            elementTime = (Element) context.output.get("time");
            String time = elementTime.text();
            String[] splitedDate = time.split(" ");
            String day = splitedDate[0], mounth = splitedDate[1];
            day = day.replaceAll(".", "d");
            mounth = mounth.replaceAll(".", "M");
            SimpleDateFormat format = new SimpleDateFormat(day + " " + mounth + " yyyy", Locale.US);
            Date date;
            try {
                date = format.parse(time.trim());
            } catch (ParseException e) {
                log.error("parse time exception,false");
                DateTime dt = new DateTime(time);
                p.setTime(dt.toString());
                log.debug("*****extractorTime  success*****");
                return true;
            }
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
//            if (System.currentTimeMillis() - date.getTime() > (long)Integer.MAX_VALUE * 10) {
                log.error("*****extractorTime  out of date*****");
                return false;
            }

            p.setTime(new Timestamp(date.getTime()).toString());
            log.debug("*****extractorTime  success*****");

            return true;
        }

        String time = elementTime.attr("data-seconds");
//2015-09-12 08:25

        long t = Long.valueOf(time);
        if (System.currentTimeMillis() - t * 1000 > 7 * 24 * 60 * 60 * 1000) {
            log.error("*****extractorTime  out of date*****");
            return false;
        }
        p.setTime(new Timestamp(t * 1000).toString());//-6 2 cst北京时间
        log.debug("*****extractorTime  success*****");
        return true;
    }


    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract desc, continue");
            return true;
        }
        String description = elementTime.attr("content");
        if (description == null || "".equals(description.trim())) {
            log.info("*****extractor Desc  failed***** url:" + url);
            return true;
        }
        description = StringEscapeUtils.unescapeHtml(description);

        p.setDescription(description);

        return true;
    }

    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
            log.error("content or p null, or no paging and too short, false");
            return false;
        }
        Elements hypLinks = content.select("a");
        for(Element a: hypLinks){
            a.unwrap();
//            System.out.println(a);
        }

        Elements socialLink = content.select("p");
        for(Element a: socialLink){
            String text = a.html();
            if(text.contains("Follow") && text.contains("@") && (text.contains("Twitter") || text.contains("Facebook"))){
                a.remove();
            }
            if(text.contains("Next story: ")) a.remove();
            if(text.contains("Subscribe to the")) a.remove();
//            System.out.println(a);
        }
//        hypLinks = content.select("noscript");
//        for(Element a: hypLinks){
//            a.unwrap();
////            System.out.println(a);
//        }

        String contentHtml = content.html();

        contentHtml = StringEscapeUtils.unescapeHtml(contentHtml);//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果


        if(contentHtml.length() < 384) {
            log.error("content after extracted too short, false");
            return false;//太短
        }

        p.setContent(contentHtml);
        if (!paging && isPaging()) {
            mergePage(p);
        }
        log.debug("*****extractorContent  success*****");
        return true;
    }

    public boolean isPaging() {
        Elements div = doc.select("div[id=div_currpage]");
        if (div == null) {
            return false;
        }
        Elements a = div.select("a");
        if (a == null || a.size() == 0) {
            return false;
        }
/*        if (url.equals(a.get(0).attr("href"))) {
            return false;
        }*/
        return true;
    }


}
