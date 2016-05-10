package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.souplang.SoupLang;
import com.dict.util.AntiAntiSpiderHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pojava.datetime.DateTime;

import java.util.Random;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class BBCPicsExtractor extends BaseExtractor {

    public BBCPicsExtractor(Page page) {
        super(page);
    }

    public BBCPicsExtractor(String url) {
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

            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(30));

            content = doc.body();

            if(content.getAllElements().hasClass("gallery-images__list")){


                content = content.select(".gallery-images__list").first();
//                content.select(":not(.d-photo)").remove();
//                content = content.select(".d-photo");
            }else if(content.getAllElements().hasClass("inline-image")){
                Element newContent = doc.createElement("div");
                newContent.insertChildren(0, content.select(".inline-image"));

                content = newContent;
                for(Element aItem: content.select("a")){
                    String imgSrc = aItem.attr("abs:href");

                    Element img = content.appendElement("img");
                    img.attr("src", imgSrc);


                    String desc = aItem.attr("data-caption");
                    Element descP = content.appendElement("p");
                    descP.text(desc);
                    aItem.remove();
                }

//                content.select(":not(.d-photo)").remove();
//                content = content.select(".d-photo");
            }else{
                log.error("no phtoto, skipped url:" + url);
                return false;
            }
//            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(10));
//            if(article == null || article.toString().contains("article") || url.contains("story")){
                for(Element svg: content.select("svg")){
                    if(svg != null) svg.remove();
                }

//                content = content.child(0).select(".content-body").first();


//            content.select(".off-screen").remove();//copyright
//
//                content.select(".gelicon").remove();
//                content.select(".icon-wrapper").remove();
//                content.select(".elastislide-wrapper").remove();
//                content.select("blockquote").remove();
//                content.select(".inline-horizontal-partner-module").remove();//广告
//                content.select(".story-footer").remove();
//                content.select(".component").remove();
//                content.select(".component--default").remove();
//                content.select(".story-body__link").remove();//READ MORE
//                content.select(".story-body__crosshead").remove();
////                content.select(".media-landscape").remove();
//                content.select(".story-body__unordered-list").remove();//magazine 推荐文章
//
//
//                content.select(".content-meta").remove();//fenxiang
////                content.select(".small-thing").remove();//同上 一般一起出现
//                content.select(".icon").remove();//每篇文章最后都有的洋葱图标
//                content.select(".on-overlay").remove();//CLOSE button
//                content.select(".below-article-tools").remove();//文章下面分享图标
//
//
//                content.select(".rounded-icon").remove();//分享图标
//                content.select(".inline-icon").remove();//分享图标
//                content.select(".inline-share-facebook").remove();//分享图标
//                content.select(".js-sport-tabs").remove();
//                content.select(".content__meta-container").remove();//作者与分享图标
//                content.select(".submeta").remove();
//                content.select(".mpu-ad").remove();
//    //            content.select(".inline-share-facebook").remove();
//    //            content.select(".inline-icon").remove();
//
////                content.removeClass("content__article-body from-content-api js-article__body");
//                content.removeClass("meta__social");
//
//            Elements uselessTag = content.select(":not(p,img,br,div)");
//            uselessTag.unwrap();




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
        p.setType("Gallery");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null) {
            log.error("elementTime null, false");
            return false;
        }

        String time = elementTime.text();
        DateTime dt = new DateTime(time);
        p.setTime(dt.toString());
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
        return true;
    }

    public void dealImg(Element img){
        String imageUrl = img.attr("src");
        //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
        if ("".equals(imageUrl)) {
            img.remove();
            return;
        }
        img.removeAttr("width");
        img.removeAttr("WIDTH");
        img.removeAttr("height");
        img.removeAttr("HEIGHT");
        img.removeAttr("srcset");

        img.attr("style", "width:100%;");
//        if(imageUrl.contains("news/320/cpsprodpb")){
            img.attr("src", imageUrl.replaceFirst("news/.*/cpsprodpb", "news/904/cpsprodpb"));//小图换大图
//        }


    }


    public static void main(String[] args){
        String url = "http://ichef.bbci.co.uk/news/320/cpsprodpb/174DA/production/_89305459_5a5e1a23-b856-4d52-a89d-995f6b35087b.jpg";
        System.out.println(url.replaceFirst("news/.*/cpsprodpb", "news/904/cpsprodpb"));
    }
}
