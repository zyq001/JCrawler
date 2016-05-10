package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.souplang.SoupLang;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.TypeDictHelper;
import com.google.gson.Gson;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pojava.datetime.DateTime;

import java.util.*;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class TheguardianPicsExtractor extends BaseExtractor {

    private Elements galleryContent;
    public TheguardianPicsExtractor(Page page) {
        super(page);
    }

    public TheguardianPicsExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("TheguardianRule.xml"));
            context = soupLang.extract(doc);
            content = doc.body();
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(30));
            if(content.getAllElements().hasClass("gallery2")){

                content.select(".share-container").remove();
                content.select(".d-body-copy").remove();
                content.select(".block-share").remove();
                content.select(".block-share--gallery-mobile").remove();
                content.select("button").remove();

                content = content.select(".gallery2").first();
//                content.select(":not(.d-photo)").remove();
//                content = content.select(".d-photo");
            }else if(content.getAllElements().hasClass("gv-rows")){
                content.select(".cloned").remove();
                content.select(".el__storyelement__title").remove();
                content.select(".js__gallery-showhide").remove();
                content.select(".share-container").remove();
                content.select(".d-body-copy").remove();
                content.select(".block-share").remove();
                content.select(".block-share--gallery-mobile").remove();
                content.select("button").remove();
                content.select(".lead-text").remove();

                content = content.select(".gv-rows").first();
//                content.select(":not(.js-owl-carousel)").remove();
            }else{
                log.error("no phtoto, skipped url:" + url);
                return false;
            }
            content.select("#share-modal-img-1").remove();
            content.select(".share-modal").remove();

            log.debug("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.error("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        String title = elementTitle.attr("content");
        if (title == null){
            log.info("extracte title failed continue");
            p.setTitle(doc.title());
            return true;
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

        if(!TypeDictHelper.rightTheType(type)){
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", type);
            String moreinfo = new Gson().toJson(map);
            p.setMoreinfo(moreinfo);
        }
        type = TypeDictHelper.getType(type, type);
        p.setType(type.trim());

        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null)
            return false;
        String label = elementLabel.attr("content");
//        String label = (String) context.output.get("label");
        if (label == null || "".equals(label.trim())) {
            log.info("*****extractorLabel  failed***** url:" + url);
            return false;
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
        if (elementTime == null) {
            log.error("elementTime null, use CURRENT url:" + url);
            return true;
        }
        String time = elementTime.attr("content");
        if (time == null || "".equals(time.trim())) {
            log.info("*****extractorTime  failed, use CURRENT***** url:" + url);
            return true;
        }
        DateTime dt = new DateTime(time);
        p.setTime(dt.toString());
        log.debug("*****extractorTime  success*****");
        return true;
    }


    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){//business版head meta里没有时间
            log.info("can't extract desc, continue");
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
            log.error("content after extracted too short, false, url: " + url);
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
        if(img.hasAttr("data-src-large")){
            imageUrl = img.attr("data-src-large");
        }
        if ("".equals(imageUrl)) {
            img.remove();
            return;
        }
        img.removeAttr("width");
        img.removeAttr("WIDTH");
        img.removeAttr("height");
        img.removeAttr("HEIGHT");
        String srcSet = img.attr("srcset");
        if(!srcSet.equals("") && srcSet.split(" ").length > 0){
            imageUrl = srcSet.split(" ")[0];
            img.attr("src", imageUrl);
        }

        img.attr("style", "width:100%;");
//        if(imageUrl.contains("news/320/cpsprodpb")){
//            img.attr("src", imageUrl.replaceFirst("news/.*/cpsprodpb", "news/904/cpsprodpb"));//小图换大图
//        }


    }

//    public boolean extractorAndUploadImg(String host, String port) {
//        log.debug("*****extractorAndUploadImg*****");
//        Elements imgs = content.select("img");
//        String mainImage = null;
//        int width = 0;
//        for (Element img : imgs) {
//            dealImg(img);
//            if (mainImage == null) {
//                mainImage = img.attr("src");
//            }
//        }
//
//        imgs = content.select(".inline-image");
//        for (Element img : imgs) {
//            dealImg(img);
//            if (mainImage == null) {
//                mainImage = img.attr("src");
//            }
//        }
//
//        imgs = content.select(".image-and-copyright-container");
//        for (Element img : imgs) {
//            dealImg(img);
//            if (mainImage == null) {
//                mainImage = img.attr("src");
//            }
//        }
//
////         if(mainImage == null) {
//        Element elementImg = (Element) context.output.get("mainimage");
//        if (elementImg != null){
//            String tmpMainImage = elementImg.attr("content");
//            if (mainImage == null) {
//                mainImage = tmpMainImage;
//            }
//        }
//        p.setMainimage(mainImage);
//
//        p.setStyle("no-image");
//
//        return true;
//
//    }

    public static void main(String[] args){
        String url = "http://ichef.bbci.co.uk/news/320/cpsprodpb/174DA/production/_89305459_5a5e1a23-b856-4d52-a89d-995f6b35087b.jpg";
        System.out.println(url.replaceFirst("news/.*/cpsprodpb", "news/904/cpsprodpb"));
    }
}
