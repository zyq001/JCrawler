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

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class NYTimesExtractor extends BaseExtractor {

    public NYTimesExtractor(Page page) {
        super(page);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("NYTimesRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            log.debug("*****init  success*****");
            AntiAntiSpiderHelper.crawlinterval(20);
            return true;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
        Element titleElement = (Element) context.output.get("title");
        if (titleElement == null) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        String title = titleElement.attr("content");
        if (title == null || "".equals(title.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        log.debug("*****extractorType*****");
        Element typeElement = (Element) context.output.get("type");
        if (typeElement == null) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        String type = typeElement.attr("content");
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
        if(!TypeDictHelper.rightTheType(type)){
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", type);
            String moreinfo = new Gson().toJson(map);
            p.setMoreinfo(moreinfo);
        }
        type = TypeDictHelper.getType(type, type);
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
            DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = format.parse(time);
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
            log.error("can't extract desc, continue");
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

    public boolean extractorAndUploadImg() {
        return extractorAndUploadImg("", "");
    }
}
