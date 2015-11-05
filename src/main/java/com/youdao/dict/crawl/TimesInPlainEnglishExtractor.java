package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class TimesInPlainEnglishExtractor extends BaseExtractor {

    public TimesInPlainEnglishExtractor(Page page) {
        super(page);
    }

    public TimesInPlainEnglishExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("TimesInPlainEnglishRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            log.debug("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
        String title = (String) context.output.get("title");
        if (title == null || "".equals(title.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        if (title.contains("-"))
            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
        else
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
            log.info("*****extractorType  failed***** url:" + url);
//            return false;
            return true;
        }
        if (type.contains("/")) {
            type = type.substring(0, type.indexOf("/"));
            type = type.replace("/", "");
        }
        p.setType(type.trim());

        Element labelElement = (Element) context.output.get("label");
        String label = "";
        if (labelElement != null) {
            label = labelElement.attr("content");
        }
        p.setLabel(label);
        log.debug("*****extractorType  success*****");
        return true;

    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null)
            return false;
        String time = elementTime.text();
        if (time == null || "".equals(time.trim())) {
            log.info("*****extractorTime  failed***** url:" + url);
            return false;
        }
        int length = time.split(" ")[0].length();
        StringBuilder M = new StringBuilder();
        while (length-- > 0) M.append('M');

        int lengthDay = time.split(" ")[1].length() - 1;
        StringBuilder d = new StringBuilder();
        while (lengthDay-- > 0) d.append('d');
//        SimpleDateFormat format = new SimpleDateFormat(M.toString() + " " +d.toString() + ", yyyy");
        try {

            DateFormat format = new SimpleDateFormat("MMMMMMMM dd, yyyy");
            Date date = format.parse(time);
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
                return false;
            }
            p.setTime(new Timestamp(date.getTime()).toString());
            log.debug("*****extractorTime  success*****");
        } catch (Exception e) {
            log.info("*****extractorTime  failed***** url:" + url);
        }
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
//    public boolean extractorAndUploadImg() {
//        return extractorAndUploadImg("proxy.corp.youdao.com", "7890");
//    }

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            return false;
        }
       /* if (host.equals(port)) return true;*/
        try {
            Elements imgs = content.select("img");
            String mainImage = null;
            int width = 0;
            for (Element img : imgs) {
                String imageUrl = img.attr("src");
                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                    img.remove();
                    continue;
                }
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
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
            }

            p.setMainimage(mainImage);
            if (width == 0) {
                p.setStyle("no-image");
            } else if (width > 300) {
                p.setStyle("large-image");
            } else {
                p.setStyle("no-image");
            }

        } catch (Exception e) {
            p.setStyle("no-image");
        }
        return true;

    }

}
