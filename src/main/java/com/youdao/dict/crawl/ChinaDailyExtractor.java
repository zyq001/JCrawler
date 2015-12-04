package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.google.gson.Gson;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class ChinaDailyExtractor extends BaseExtractor {

    public ChinaDailyExtractor(Page page) {
        super(page);
    }

    public ChinaDailyExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("ChinaDailyRule.xml"));
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
//            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
            p.setTitle(title.substring(0, title.indexOf(" - ")).trim());
        else
            p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        String type = (String) context.output.get("type");
        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
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
        String time = (String) context.output.get("time");
        if (time == null || "".equals(time.trim())) {
            log.info("*****extractorTime  failed***** url:" + url);
            return false;
        }
//2015-09-12 08:25
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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
        p.setTime(time.trim());
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

    public void mergePage(ParserPage p) {
        log.debug("*****mergePage*****");
        Elements div = doc.select("div[id=div_currpage]").select("a");
        LinkedList<String> list = new LinkedList<String>();
        for (Element a : div) {
            String url = a.attr("href");
            if (!list.contains(url)) {
                list.add(url);
            }
        }
        for (String url : list) {
            ChinaDailyExtractor extractor = new ChinaDailyExtractor(url);
            extractor.init();
            extractor.extractorAndUploadImg();
            extractor.extractorContent(true);
            p.setContent(p.getContent() + extractor.getParserPage().getContent());
            //TODO parser
        }
        log.info("*****mergePage end*****");
    }
}
