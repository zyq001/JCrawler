package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.JsoupUtils;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.score.LeveDis;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuhl on 15-8-17.
 */
public class ChinaDailyExtractor {
    public static long MINSIZE = 512;

    public static ParserPage extractor(Page page) throws Exception {
        String url = page.getUrl();
        Document doc = page.getDoc();
        JsoupUtils.makeAbs(doc, url);
        SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("DictRule.xml"));

//        SoupLang soupLang = new SoupLang(ClassLoader.getSystemResourceAsStream("DictRulePhone.xml"));
        Context context = soupLang.extract(doc);
        ParserPage p = new ParserPage();
        String type = (String) context.output.get("type");
        String type0 = (String) context.output.get("type0");
        if (type0 != null && !"".equals(type0.trim())) {
            type0 = type0.replaceAll("/", "");
            type = type + "," + type0.trim();
        }
        if (type == null || "".equals(type.trim())) return null;
        p.setType(type.trim());//TODO
        String title = (String) context.output.get("title");
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        if (title == null || "".equals(title.trim())) return null;
        if (title.contains("-"))
            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
        else
            p.setTitle(title.trim());
        Element contentElements = (Element) context.output.get("content");
        if (contentElements == null)
            return null;
        String contentStr = contentElements.text();
        if (contentStr.length() < MINSIZE)
            return null;
//        String phoneUrl = convUrl(page.getUrl());
//        if (phoneUrl == null) {
//            System.out.println("can't conv to phone url:" + url);
//            return null;
//        }
//        contentElements = getPhoneContent(url);
//        if (contentElements == null) {
//            System.out.println("source url is :" + url);
//            System.out.println("can't fetch phone url:" + phoneUrl);
//            return null;
//        }
//        contentStr = contentElements.text();
        LeveDis leveDis = LeveDis.getInstance(LeveDis.p);
        String tags = leveDis.tag(contentStr, 5);
        p.setLabel(tags);
        int level = leveDis.compFileLevel(leveDis.compLevel(contentStr));
        p.setLevel(String.valueOf(level));
        p.setHost(getHost(url));
        p.setUrl(url);
        p.setTime((String) context.output.get("time"));

        Elements imgs = contentElements.select("img");
        String mainImage = null;
        for (Element img : imgs) {
            String imageUrl = img.attr("src");
            img.removeAttr("width");
            img.removeAttr("WIDTH");
            img.removeAttr("height");
            img.removeAttr("HEIGHT");
            img.attr("style", "width:100%;");
            long id = new OImageUploader().deal(imageUrl);
            URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
            img.attr("src", newUrl.toString());
            if (mainImage == null) {
                mainImage = newUrl.toString();
            }
        }
        if (mainImage == null) {
            return null;
        }
        p.setMainimage(mainImage);
        String str = contentElements.html();
        str = str.replaceAll("<(?!img|br|p|/p).*?>", "");//去除所有标签，只剩img,br,p
        str = str.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        p.setContent(str);
        return p;
    }

    public static String getHost(String url) {
        if (url == null || url.trim().equals("")) {
            return "";
        }
        String host = "";
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            host = matcher.group();
        }
        return host;
    }

}
