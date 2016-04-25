package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.google.gson.Gson;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.AntiAntiSpiderHelper;
import com.youdao.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class WashingtonExtractor extends BaseExtractor {

    public WashingtonExtractor(Page page) {
        super(page);
    }

    public boolean init() {
        log.debug("*****init*****");

        AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(20));


        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("WashingtonPostRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            content.select(".external-game-embed").remove();
            content.select(".pinnochio").remove();
            content.select(".post-body-sig-line").remove();
            content.select(".pb-f-page-newsletter-inLine").remove();
            content.select(".pb-f-games-userPolls").remove();
//            content.select(".moat-trackable").remove();external-game-embed

            log.debug("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.error("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");

        String title = (String) context.output.get("title");
        if (title == null || "".equals(title.trim())) {
            log.error("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        log.debug("*****extractorType*****");

        String type = url.substring(url.indexOf(".com/") + 5);
        type = type.substring(0, type.indexOf("/"));
        if (type == null || "".equals(type.trim())) {
            log.error("*****extractorTitle  failed***** url:" + url);
            return false;
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

        String label = (String) context.output.get("label");
        p.setLabel(label);
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null) {
            log.error("element time null, false, url: " + url);
            return false;
        }
        String time = elementTime.attr("content");
        if (time == null || "".equals(time.trim())) {
            log.error("*****extractorTime  failed***** url:" + url);
            return false;
        }
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = format.parse(time);
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
                log.error("out of date, false, url: " + url);
                return false;
            }
            p.setTime(new Timestamp(date.getTime()).toString());
            log.debug("*****extractorTime  success*****");
        } catch (Exception e) {
            log.info("*****extractorTime  failed***** url:" + url);
            e.printStackTrace();
        }
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

}
