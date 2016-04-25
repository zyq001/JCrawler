package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.google.gson.Gson;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.*;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class PropublicaExtractor extends BaseExtractor {

    public PropublicaExtractor(Page page) {
        super(page);
    }

    public PropublicaExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("PropublicaRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            if(content == null){
//                Element art = doc.select("article").first();
//                if(art != null)
                content = doc.select(".content").first();
                if(content == null){
                    content = doc.select(".article-full").first();
                }
            }

            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(10));

            Element article = (Element) context.output.get("isarticle");
            if(article == null || article.toString().contains("rticle")){
//            String isarticle = context.output.get("isarticle").toString();
//            if(isarticle.contains("article")){

//                content.select("div[id=sidebar-second]").remove();
//                content.select("div[id=content-bottom]").remove();
//                Elements socailLinks = content.select("div[class=social-links]");
//                if(socailLinks != null)socailLinks.remove();
//                content.select("div[class=authoring full-date]").remove();
//                content.select("div[class=authoring]").remove();
                //去除分享

//                Elements ems = content.select("tagged as a stub");
                content.select(".wNarrow").remove();
                content.select(".byline-block").remove();
                content.select(".content-inset").remove();
                content.select(".sidebar-inject").remove();
                content.select(".callout-ad").remove();
                content.select(".ad").remove();
                content.select(".foot-tools").remove();
                content.select("div[id=ob_holder]").remove();
                content.select(".OUTBRAIN").remove();
                content.select("div[id=comments]").remove();
//                content.select(".").remove();
//                content.select(".").remove();
                content.select(".article-title").remove();//重复题目
                content.select("aside").remove();
                content.select("share-bar").remove();
                content.select("blockquote").unwrap();
                content.select(".published").remove();//日期
                content.select(".external-link").remove();//continue to read
                content.select(".more-from-issue").remove();//
//                content.select("http://yiduserver.youdao.com/dc.s?id=3260469")
//                content.select(".shareaholic-canvas").remove();//分享图标
//                content.select(".wpptopdfenh").remove();//pdf版下载
//                content.select(".entry-html-box").remove();//html版
//                for(Element e: content.select(".article")){
//                    if(e.text().contains("To open/download image"))
//                        e.remove();
//                }


//                if(content.select())
//                content.select("ul").select("li").wrap("<p></p>");
//                content.select(".mw-headline").wrap("<i></i>");
                log.debug("*****init  success*****");
                return true;
            }
            log.info("*****init  failed，isn't an article***** url:" + url);
            return false;
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
//        Elements hypLinks = content.select("a");
//        for(Element a: hypLinks){
//            a.unwrap();
////            System.out.println(a);
//        }

        removeComments(content);

        Elements videos = content.select("iframe");
        for(Element e: videos){
            String videoUrl = e.attr("src");
            if(GFWHelper.isBlocked(videoUrl))
                continue;
            String className = e.className();
            Tag imgTag = Tag.valueOf("p");
//                img.appendChild(imgTag);
            Element newImg = new Element(imgTag, "");
            newImg.attr("class", "iframe");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%; heigh:100%");
            newImg.attr("heigh","200");
            e.appendChild(newImg);
        }

        String contentHtml = content.html();

        contentHtml = StringEscapeUtils.unescapeHtml(contentHtml);//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|li|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果

        if(contentHtml.length() < 512) return false;//太短

        Document extractedContent = Jsoup.parse(contentHtml);
//        for(String className: classNames){
        Elements videoClassNames = extractedContent.select(".iframe");
        for(Element e: videoClassNames){
            String videoUrl = e.attr("src");
            Tag imgTag = Tag.valueOf("iframe");
//                img.appendChild(imgTag);
            Element newImg = new Element(imgTag, "");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%");
//                newImg.a
            e.appendChild(newImg);
            e.unwrap();
        }

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
        if (elementType == null){
            p.setType("Us");
            log.debug("*****extractorType  null  continue:" + url);
            return true;

        }
        String type = elementType.attr("content");
        if(!TypeDictHelper.rightTheType(type)){
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", type);
            String moreinfo = new Gson().toJson(map);
            p.setMoreinfo(moreinfo);
        }

        type = TypeDictHelper.getType(type, "Us");


        p.setType(type.trim());
//        p.setType(type.trim());

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
        String[] keywords = label.split(",");
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
            if(wordCount <= 3) pq.add(keyword.trim());
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX", Locale.US);
        Date date = null;
        try {
            date = format.parse(time.trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        if (System.currentTimeMillis() - date.getTime() > Long.MAX_VALUE >> 25) {
//            log.debug("*****extractorTime  out of date*****");
//            return false;
//        }
        p.setTime(new Timestamp(date.getTime()).toString());//utc 2 cst北京时间
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

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            return false;
        }

        //文章题目重复出现，去除之
//        content.select("div[id=block-onix-highlight-onix-highlight-article-header]").remove();
//        content.select("div[id=block-views-article-title-block]").remove();
//        //
//        content.select("div[id=157_ArticleControl_divShareButton]").remove();
//        if(isPaging()) return true;
       /* if (host.equals(port)) return true;*/

        Elements imgs = content.select("img");
        String mainImage = null;
        int width = 0;
        for (Element img : imgs) {
            String imageUrl = null;
            try {
                imageUrl = img.attr("data-src");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    imageUrl = img.attr("src");
                    if ("".equals(imageUrl)) {
                        img.remove();
                        continue;
                    }
                }
                if(imageUrl.startsWith("/"))
                    imageUrl = "http://" + host + imageUrl;
//                imageUrl = imageUrl.replaceAll(".jpg", "H.jpg");
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
                //                img.removeAttr("srcset");
//                img.attr("style", "width:100%;");
                OImageUploader uploader = new OImageUploader();
                if (!"".equals(host) && !"".equals(port))
                    uploader.setProxy(host, port);
                long id = uploader.deal(imageUrl);
                //                long id = 0;
                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                int twidth = uploader.getWidth();
                if(twidth >= 300)
                    img.attr("style", "width:100%;");
                img.attr("src", newUrl.toString());
                if (mainImage == null) {
                    width = uploader.getWidth();
                    mainImage = newUrl.toString();
                }
            } catch (Exception e) {
                System.out.println(imageUrl);
                e.printStackTrace();
                img.attr("style", "width:100%;");//抛异常则图片没有传到Oimage，即使用原地址，很有可能是因为图片太大，很有可能没有width：100%，
            }
        }
        Element elementImg = (Element) context.output.get("mainimage");
        if (elementImg != null){
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
                        e1.printStackTrace();

            }
        }
//        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width >= 300) {

            p.setMainimage(mainImage);
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
//        CNBCExtractor extractor = new CNBCExtractor(url + "?singlepage=true");
//        extractor.init();
//        extractor.extractorAndUploadImg();
//        extractor.extractorContent(true);
//        p.setContent(extractor.getParserPage().getContent());

        log.info("*****mergePage end*****");
    }

}
