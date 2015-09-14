package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("WashingtonPostRule.xml"));
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
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        log.debug("*****extractorType*****");
        String type = (String) context.output.get("type");
        String type0 = url.substring(url.indexOf(".com/") + 5);
        type0 = type0.substring(0, type0.indexOf("/"));
        if (type0 != null && !"".equals(type0.trim())) {
            type0 = type0.replaceAll("/", "");
            type = type + "," + type0.trim();
        }
        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        p.setType(type.trim());//TODO
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null)
            return false;
        String time = elementTime.attr("content");
        if (time == null || "".equals(time.trim())) {
            log.info("*****extractorTime  failed***** url:" + url);
            return false;
        }
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = format.parse(time);
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
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

}
