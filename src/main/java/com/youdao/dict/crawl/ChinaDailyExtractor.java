package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by liuhl on 15-8-17.
 */
public class ChinaDailyExtractor extends BaseExtractor {
    private Context context;

    public ChinaDailyExtractor(Page page) {
        super(page);
    }

    public ChinaDailyExtractor(String url) {
        super(url);
    }

    public boolean init() {
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("ChinaDailyRule.xml"));
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

    }
}
