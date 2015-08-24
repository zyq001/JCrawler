package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.JsoupUtils;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.score.LeveDis;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuhl on 15-8-17.
 */
public class ChinaDailyExtractor extends BaseExtractor {
    private Context context;

    public ChinaDailyExtractor(Page page) {
        super(page);
    }

    public boolean init() {
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("DictRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean extractorTitle() {
        String title = (String) context.output.get("title");
        if (title == null || "".equals(title.trim())) return false;
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        if (title.contains("-"))
            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
        else
            p.setTitle(title.trim());
        return true;
    }

    public boolean extractorType() {
        String type = (String) context.output.get("type");
        String type0 = (String) context.output.get("type0");
        if (type0 != null && !"".equals(type0.trim())) {
            type0 = type0.replaceAll("/", "");
            type = type + "," + type0.trim();
        }
        if (type == null || "".equals(type.trim()))
            return false;
        p.setType(type.trim());//TODO
        return true;
    }

    public boolean extractorTime() {
        String time = (String) context.output.get("time");
        if (time == null || "".equals(time.trim()))
            return false;
        p.setTime(time.trim());
        return true;
    }

}
