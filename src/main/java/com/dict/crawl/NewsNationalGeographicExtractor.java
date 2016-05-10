package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.bean.ParserPage;
import com.dict.souplang.SoupLang;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.GFWHelper;
import com.dict.util.TypeDictHelper;
import com.google.gson.Gson;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class NewsNationalGeographicExtractor extends BaseExtractor {

    public NewsNationalGeographicExtractor(Page page) {
        super(page);
    }

    public NewsNationalGeographicExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(20));
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("NewsNationalGeographicRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");

            Element article = (Element) context.output.get("isarticle");
//            if(article == null || article.toString().contains("article")){

            content.select(".article_title").remove();
            content.select(".title").remove();
            content.select(".article_credits_author").remove();
            content.select(".article_credits_photographer").remove();
            content.select(".nextpage_continue").remove();//
            content.select(".subscribe").remove();

            content.select(".relatedContentList").remove();
            content.select(".byline").remove();//作者-时间等
                content.select(".Kicker").remove();
                content.select(".titleAndDek").remove();
                content.select(".col-md-1").remove();
                content.select(".icon-menu").remove();
                content.select(".info-button").remove();
                content.select("div[id=bottom-caption-button]").remove();
                content.select(".current-item-display").remove();
                content.select(".hidden").remove();
                content.select(".photo-gallery-initial--button").remove();
                content.select(".photo-gallery-initial__header").remove();
                content.select(".gallery-modal--next-gallery__body").remove();

                content.select(".media__caption--text").select(".mobile").remove();

                content.select(".gallery-modal__header").remove();

                content.select(".carousel-control").remove();

                content.select(".gallery-modal__footer").remove();

                content.select(".gallery-modal__right-rail").remove();

                content.select(".rightRailSlot").remove();
                content.select(".OneColumn").remove();//推荐链接
//                content.select(".UniversalVideo").remove();//删除视频

                for(Element e: content.select("i")){//删除follow smBody on Twitter
                    String iText = e.text();
                    if(iText.contains("Follow "))
                        e.parent().remove();
                }


//                content.select(".Interactive").select(".section").select(".media--small").select(".left").remove();

                content.select(".instagram-media").remove();

//                content.select(".media--small").select(".right").remove();
                content.select(".pull-quote__author").select(".hidden-md").remove();

                content.select(".author").remove();//ngm
                content.select(".published").remove();

                content.select(".share-buttons").remove();
                content.select(".editors-note").remove();
                content.select(".tpPlayer").remove();
                content.select(".bio").remove();
                content.select(".promo-thumbs").remove();
                content.select(".livefyre").remove();

                content.select(".arrower").remove();
                content.select("script").remove();//promo-tile box explorer
//                content.select(".keyel").remove();//删除图例 太小了 效果不好

                content.select(".promo-tile").remove();//作者连接
//                content.select(".explorer").remove();//作者连接
                content.select(".promo-previous").remove();//上一篇文章
                content.select(".promo-next").remove();//下一篇文章
                content.select(".detail-image").remove();//图片上有文字 显示乱

            content.select(".inline-gallery__on-image-control").remove();//图片集的左右图标

//            content.select(".share-buttons").remove();//fb 分享图标

//                content.
                //加p标签
//                content.select(".mw-headline").wrap("<p></p>");
//
//                content.select(".step").wrap("<p></p>");
//
//                content.select(".step_num").wrap("<p></p>");
//
//                Elements lis = content.select("li");
//                for(Element e: lis){
//                    if(!e.parent().tagName().equals("ul"))
//                        e.unwrap();
//                }
//                if(content.select())
//                content.select("ul").select("li").wrap("<p></p>");
//                content.select(".mw-headline").wrap("<i></i>");
                log.debug("*****init  success*****");
            AntiAntiSpiderHelper.crawlinterval(10);
                return true;
//            }
//            log.info("*****init  failed，isn't an article***** url:" + url);
//            return false;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }


    public boolean extractorContent() {
        return extractorContent(false);
    }


    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
            log.debug("*****extractorContent failed,return false*****");
            return false;
        }
        Elements hypLinks = content.select("a");
        for(Element a: hypLinks){
            a.unwrap();
//            System.out.println(a);
        }
        content.select("img").wrap("<p></p>");
        HideVideo(".media--small");
//        content.select("#comment").remove();
        removeComments(content);


        String contentHtml = content.html();

        contentHtml = StringEscapeUtils.unescapeHtml(contentHtml);//替换转义字符


        contentHtml = contentHtml.replaceAll("<!--.*?-->", "");//去除换行符制表符/r,/n,/t /n
        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|li|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果



        if(contentHtml.length() < 512) {
            log.info("too short < 512 skip " + contentHtml.length());
            return false;//太短
        }

        Document extractedContent = Jsoup.parse(contentHtml);
//        for(String className: classNames){
        Elements videoClassNames = extractedContent.select(".iframe");
        for(Element e: videoClassNames){
            String videoUrl = e.attr("src");
            if(GFWHelper.isBlocked(videoUrl))
                continue;
            Tag imgTag = Tag.valueOf("iframe");
//                img.appendChild(imgTag);

            Element newImg = new Element(imgTag, "");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%; heigh:100%");
//                newImg.a
            e.appendChild(newImg);
            e.unwrap();
        }
//        }


        p.setContent(extractedContent.html());
        if (!paging && isPaging()) {
            mergePage(p);
        }
        log.debug("*****extractorContent  success*****");
        return true;
    }



    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        if (elementTitle == null){
            log.error("extractorTitle failed skippppp");
            return false;
        }
        String title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            title = elementTitle.text();
            if (title == null || "".equals(title.trim())) {
//                title = elementTitle.text();
                log.info("*****extractorTitle  failed***** url:" + url);
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
        Element elementType = (Element) context.output.get("type");
        if (elementType == null){
            log.debug("*****extractorType  null  skipp url:" + url);
            return false;

        }
        String type = elementType.attr("content");
        if(!TypeDictHelper.rightTheType(type)){
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", type);
            String moreinfo = new Gson().toJson(map);
            p.setMoreinfo(moreinfo);
        }

        type = TypeDictHelper.getType(type, type);


        p.setType(type.trim());

        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null) {
            log.info("*****extractorLabel  failed, continue***** url:" + url);
            return true;
        }
        String label = elementLabel.attr("content");
//        String label = (String) context.output.get("label");
        if (label == null || "".equals(label.trim())) {
            log.info("*****extractorLabel  failed, continue***** url:" + url);
            return true;
        }
//        label = label.contains("China")?"China":label.contains("news")? "World": label;//news belong to World
        String[] keywords = label.split(", ");
        PriorityQueue<String> pq = new PriorityQueue<String>(10, new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split(" ").length, length2 = o2.split(" ").length;
                return length1 - length2;
            }
        });

        for(String keyword: keywords){
            keyword = keyword.replaceAll(",", "");
            int wordCount = keyword.split(" ").length;
            if(wordCount <= 3) pq.add(keyword);
        }
        StringBuilder sb = new StringBuilder();
        while(!pq.isEmpty()){
            sb.append(pq.poll());
            if(!pq.isEmpty()) sb.append(", ");
        }
        p.setLabel(sb.toString());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {

        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract Time, skip url:" + url);
            return false;

        }
        String time = elementTime.attr("content");
        if(time == null || "".equals(time.trim())) time = elementTime.text();
        if (time == null || "".equals(time.trim())) {
            log.info("*****extractorTime  failed***** url:" + url);
            return false;
        }
//2015-09-12 08:25
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.US);
        Date date;
        try {
            date = format.parse(time.trim());
        } catch (ParseException e) {
            if(time.startsWith("Publish")){
                int idx = time.indexOf(":");
                if(idx >= 0) time = time.substring(idx + 2);
            }
            int length = time.split(" ")[0].length();
            StringBuilder M = new StringBuilder();
            while (length-- > 0) M.append('M');
            format = new SimpleDateFormat(M.toString() +" yyyy", Locale.US);
            try {
                date = format.parse(time.trim());
            } catch (ParseException e1) {

                format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.US);
                try {
                    date = format.parse(time.trim());
                } catch (ParseException e2) {
                    e2.printStackTrace();
                    log.info("*****extractorTime format.parse  failed***** url:" + url);
                    return false;
                }
//                e1.printStackTrace();

            }

        }
//        if (System.currentTimeMillis() - date.getTime() > Long.MAX_VALUE >> 25) {
//            log.debug("*****extractorTime  out of date*****");
//            return false;
//        }
        p.setTime(new Timestamp(date.getTime() + 13 * 60 * 60 * 1000).toString());//-0500纽约时间 +13h是 cst北京时间
        log.debug("*****extractorTime  success*****");
        return true;
    }

    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract desc, continue + url : " + url);
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
//
//    public boolean extractorAndUploadImg(String host, String port) {
//        log.debug("*****extractorAndUploadImg*****");
//        if (content == null || p == null) {
//            return false;
//        }
//
//        //文章题目重复出现，去除之
////        content.select("div[id=block-onix-highlight-onix-highlight-article-header]").remove();
////        content.select("div[id=block-views-article-title-block]").remove();
////        //
////        content.select("div[id=157_ArticleControl_divShareButton]").remove();
////        if(isPaging()) return true;
//       /* if (host.equals(port)) return true;*/
//
//
//        Elements imgs = content.select(".delayed-image-load--photogallery-modal");
//        if(imgs == null || imgs.size() < 1) {
//            imgs = content.select(".delayed-image-load");
//            if (imgs == null || imgs.size() < 1) {
////                imgs = content.select("img");
////            "data-platform-component" -> ""data-platform-component" -> "PictureFill""
//                imgs = content.select("div[data-platform-component=PictureFill]");
//                if (imgs == null || imgs.size() < 1)
//                    imgs = content.select("img");
//            }
//        }
//        String mainImage = null;
//        int width = 0;
//        for (Element img : imgs) {
//            try {
//                String imageUrl = img.attr("data-src");
//                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
//                if ("".equals(imageUrl)) {
//                    imageUrl = img.attr("src");
//                    if ("".equals(imageUrl)) {
//                        imageUrl = img.attr("data-srcset");
//                        if ("".equals(imageUrl)) {
//                            imageUrl = img.attr("data-platform-src");
//                            if ("".equals(imageUrl)) {
//                                img.remove();
//                                continue;
//                            }
//                        }
//                    }
//                }
//                if(!img.tagName().equals("img")) {
//                    Tag imgTag = Tag.valueOf("img");
////                img.appendChild(imgTag);
//                    Element newImg = new Element(imgTag, "");
//                    img.appendChild(newImg);
//                    img = newImg;
//                    int idx = imageUrl.indexOf("width");
//                    if(idx > 0){
//                        imageUrl = imageUrl.substring(0, idx - 2);
//                    }else{
//                        imageUrl.substring(0, imageUrl.indexOf("jpg") + 3);
//                    }
//                    imageUrl = imageUrl.replace("jpg", "adapt.1190.1.jpg");
//                }
//
//                if(imageUrl.startsWith("/")){
//                    imageUrl = "http://" + p.getHost()+imageUrl;
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        Element elementImg = (Element) context.output.get("mainimage");
//        if (elementImg != null){
//            String tmpMainImage = elementImg.attr("content");
//        }
////        p.setMainimage(mainImage);
//        if (width == 0) {
//            p.setStyle("no-image");
//        } else if (width >= 300) {
//
//            p.setMainimage(mainImage);
//            p.setStyle("large-image");
//
//        } else {
//            p.setStyle("no-image");
//        }
//
////        } catch (Exception e) {
////            p.setStyle("no-image");
////        }
//        return true;
//
//    }

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
//        CNBCExtractor extractor = new CNBCExtractor(url + "?singlepage=true");
//        extractor.init();
//        extractor.extractorAndUploadImg();
//        extractor.extractorContent(true);
//        p.setContent(extractor.getParserPage().getContent());

        log.info("*****mergePage end*****");
    }

}
