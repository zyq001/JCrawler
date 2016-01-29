package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.google.gson.Gson;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.AntiAntiSpiderHelper;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import com.youdao.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
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
public class BBCExtractor extends BaseExtractor {

    public BBCExtractor(Page page) {
        super(page);
    }

    public BBCExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            if(url.matches(".*bbc.com/[japan|urdu|vietnamese|persian|arabic|zhongwen|indone" +
            "|kyrgyz|portuguese|mundo|ukrainian|azeri|afrique|nepali|russian|swahili|bengali|hausa|gahuza|pashto|sinhala|tamil" +
                    "|uzbek|turkce|somali|gahuza|hindi|burmese].*")) {
                log.debug("non-English article, skipped");
                return false;
            }
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("BBCRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            Element article = (Element) context.output.get("isarticle");
            if(content == null) {
                log.debug("extrate content failed, skipped");
                return false;
            }
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(10));
            if(article == null || article.toString().contains("article")){
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
            }
            log.error("*****init  failed，isn't an article***** url:" + url);
            return false;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
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
            log.info("extracte title failed skipped");
            return false;
        }
        title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            title = elementTitle.text();
            if (title == null || "".equals(title.trim())) {
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
        if (elementType == null) {
            int idx = url.indexOf("bbc.com");
            String fromUrl = url.substring(idx + 8);
            if(idx < 0) {
                Element selectedType = doc.select(".navigation-arrow--open").first();
                if(selectedType != null) {
                    Element span = selectedType.select("span").first();
                    if(span != null)
                        fromUrl = span.text();
                }
            }else {

                fromUrl = fromUrl.substring(0, fromUrl.indexOf("/"));
            }
            if (!TypeDictHelper.rightTheType(fromUrl)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("orgType", fromUrl);
                String moreinfo = new Gson().toJson(map);
                p.setMoreinfo(moreinfo);
            }
            fromUrl = TypeDictHelper.getType(fromUrl, fromUrl);
            p.setType(fromUrl.trim());

//            return true;
        }else {
            String type = elementType.attr("content");
            if (type == null || "".equals(type.trim())) {
                log.info("*****extractorTitle  failed***** url:" + url);
                return false;
            }
            if (type.contains("/")) {
                type = type.substring(0, type.indexOf("/"));
                type = type.replace("/", "");
            }
//        type = type

//        if(type.contains("horoscope")){
//            log.info("horoscope, skipped");
//            return  false;
//        }
            if (!TypeDictHelper.rightTheType(type)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("orgType", type);
                String moreinfo = new Gson().toJson(map);
                p.setMoreinfo(moreinfo);
            }
            type = TypeDictHelper.getType(type, type);
            p.setType(type.trim());
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
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null)
            return false;
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
                return false;
            }
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
                log.debug("*****extractorTime  out of date*****");
                return false;
            }

            p.setTime(new Timestamp(date.getTime()).toString());
            log.debug("*****extractorTime  success*****");

            return true;
        }

        String time = elementTime.attr("data-seconds");
//2015-09-12 08:25

        long t = Long.valueOf(time);
//        if (System.currentTimeMillis() - t * 1000 > 7 * 24 * 60 * 60 * 1000) {
//            log.info("*****extractorTime  out of date*****");
//            return false;
//        }
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

        p.setDescription(description);

        return true;
    }

    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
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

        contentHtml = contentHtml.replaceAll("&gt;", ">").replaceAll("&lt;", "<");//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果


        if(contentHtml.length() < 384) return false;//太短

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

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            return false;
        }
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
                img.removeAttr("srcset");
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
                e.printStackTrace();
            }
        }

        imgs = content.select(".inline-image");
        for (Element img : imgs) {
            try {
                String imageUrl = img.select("a").attr("href");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    img.remove();
                    continue;
                }
                Tag imgTag = Tag.valueOf("img");
//                img.appendChild(imgTag);
                Element newImg = new Element(imgTag, "");
                img.before(newImg);
                img.remove();
                img = newImg;
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
                e.printStackTrace();
            }
        }

        imgs = content.select(".image-and-copyright-container");
        for (Element img : imgs) {
            try {
                Element e = img.select(".js-delayed-image-load").first();
                if(e == null) continue;
                String imageUrl = img.select(".js-delayed-image-load").first().attr("data-src");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    img.remove();
                    continue;
                }
                Tag imgTag = Tag.valueOf("img");
//                img.appendChild(imgTag);
                Element newImg = new Element(imgTag, "");
                img.before(newImg);
                img.remove();
                img = newImg;
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
                e.printStackTrace();
            }
        }

//         if(mainImage == null) {
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
                if(mainImage == null){
                    //正文中无图片 将meta中图片加入到正文中
                    Tag imgTag = Tag.valueOf("img");
//                img.appendChild(imgTag);
                    Element newImg = new Element(imgTag, "");
                    newImg.attr("src", newUrl.toString());
                    if(width >= 300)
                        newImg.attr("style", "width:100%;");
                    content.prependChild(newImg);
                }
                mainImage = newUrl.toString();

            } catch (Exception e1) {
//                        e1.printStackTrace();

            }
        }
        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width >= 300) {
            p.setStyle("large-image");
        } else {
            p.setStyle("no-image");
        }

//        } catch (Exception e) {
//            p.setStyle("no-image");
//        }
        return true;

    }
}
