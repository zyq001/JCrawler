package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class WashingtonExtractor extends BaseExtractor {
    private Context context;

    public WashingtonExtractor(Page page) {
        super(page);
    }

    public boolean init() {
        log.info("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("WashingtonPostRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            log.info("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.info("*****init  failed*****");
            return false;
        }
    }

    public boolean extractorTitle() {
        log.info("*****extractorTitle*****");

        String title = (String) context.output.get("title");
        if (title == null || "".equals(title.trim())) {
            log.info("*****extractorTitle  failed*****");
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        p.setTitle(title.trim());
        log.info("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        log.info("*****extractorType*****");
        String type = (String) context.output.get("type");
        String type0 = url.substring(url.indexOf(".com/") + 5);
        type0 = type0.substring(0, url.indexOf("/"));
        if (type0 != null && !"".equals(type0.trim())) {
            type0 = type0.replaceAll("/", "");
            type = type + "," + type0.trim();
        }
        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorTitle  failed*****");
            return false;
        }
        p.setType(type.trim());//TODO
        log.info("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.info("*****extractorTime*****");
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null)
            return false;
        String time = elementTime.attr("content");
        if (time == null) {
            log.info("*****extractorTime  failed*****");
            return false;
        }
        p.setTime(time.trim());
        log.info("*****extractorTime  success*****");
        return true;
    }

}
