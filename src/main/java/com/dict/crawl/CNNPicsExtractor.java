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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class CNNPicsExtractor extends BaseExtractor {

    private Elements galleryContent;
    public CNNPicsExtractor(Page page) {
        super(page);
    }

    public CNNPicsExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("CNNRule.xml"));
            context = soupLang.extract(doc);
            content = doc.body();
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(30));
            if(content.getAllElements().hasClass("d-photo")){

                content.select(".share-container").remove();
                content.select(".d-body-copy").remove();

                content = content.select(".interactive-container").first();
//                content.select(":not(.d-photo)").remove();
//                content = content.select(".d-photo");
            }else if(content.getAllElements().hasClass("js-owl-carousel")){
                content.select(".cloned").remove();
                content.select(".el__storyelement__title").remove();
                content.select(".js__gallery-showhide").remove();

                content = content.select(".js-owl-carousel").first();
//                content.select(":not(.js-owl-carousel)").remove();
            }else{
                log.error("no phtoto, skipped url:" + url);
                return false;
            }
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
//        Element elementTitle = (Element) context.output.get("title");
        String title = (String) context.output.get("title");;
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
        log.debug("*****extractorType*****");
        Element typeElement = (Element) context.output.get("type");
        String type = "";
        if (typeElement != null) {
            type = typeElement.attr("content");
        }

        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorTitle  continue***** url:" + url);
            return true;
        }
        if (type.contains("/")) {
            type = type.substring(0, type.indexOf("/"));
            type = type.replace("/", "");
        }

        if(!TypeDictHelper.rightTheType(type)){
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", type);
            String moreinfo = new Gson().toJson(map);
            p.setMoreinfo(moreinfo);
        }
        type = TypeDictHelper.getType(type, type);
        p.setType(type.trim());

        Element labelElement = (Element) context.output.get("label");
        String label = "";
        if (labelElement != null) {
            label = labelElement.attr("content");
        }
        p.setLabel(label);
        log.debug("*****extractorTitle  success*****");
        return true;
//        p.setType("Gallery");
//        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null || elementTime.text().equals("")) {
            log.info("elementTime null, useCurrentTime");
            p.setTime(CUENTTIME);
            return true;
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


    public static void main(String[] args){
        String url = "http://ichef.bbci.co.uk/news/320/cpsprodpb/174DA/production/_89305459_5a5e1a23-b856-4d52-a89d-995f6b35087b.jpg";
        System.out.println(url.replaceFirst("news/.*/cpsprodpb", "news/904/cpsprodpb"));
    }
}
