package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.rometools.rome.feed.synd.SyndEntry;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.AntiAntiSpiderHelper;
import com.youdao.dict.util.RSSReaderHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class WashingtonRSSExtractor extends BaseExtractor {

    public WashingtonRSSExtractor(Page page) {
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
        String type = RSSReaderHelper.getType(url);
        if(type != null && !type.equals("")) {
            p.setType(type);
        }else{
            log.error("cant get type, false");
            return false;
//            return true;
        }

        String label = (String) context.output.get("label");
        p.setLabel(label);
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");

        log.debug("*****extractorTime*****");
        SyndEntry entry = RSSReaderHelper.getSyndEntry(url);
        if(entry != null && entry.getPublishedDate() != null){
            p.setTime(new Timestamp(entry.getPublishedDate().getTime()).toString());
            return true;
        }

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

        p.setDescription(description);

        return true;
    }

}
