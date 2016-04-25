package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.SoupLang;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class YahooNewsExtractor extends BaseExtractor {

    public YahooNewsExtractor(Page page) {
        super(page);
    }

    public YahooNewsExtractor(String url) {
        super(url);
    }

    public boolean extractor() {
        if (init())
            return extractorTime() && extractorTitle() && extractorType() && extractorDescription()  && extractorTags(keywords, p.getLabel());
        else
            return false;
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("YahooNewsRule.xml"));
            context = soupLang.extract(doc);
            content = doc;//全文即content

            Element article = (Element) context.output.get("isarticle");
            if(article != null && article.toString().contains("rticle")){
                log.debug("*****init  success*****");
                return true;
            }
            log.info("*****init  failed，isn't an article***** url:" + url);
            return false;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }


    public boolean extractorContent() {
        return extractorContent(false);
    }

    public boolean extractorContent(boolean paging) {
        return true;
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        if (elementTitle == null){
            log.error("extractorTitle failed skippppp");
            return false;
        }
        String title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            title = elementTitle.text();
            p.setTitle(title.trim());
//            log.info("*****extractorTitle  failed***** url:" + url);
            return true;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
//        if (title.contains("-"))
//            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
//        else
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
        String type = YahooNewsCrawler.url2Type.get(url);
        if(type == null || type.equals(""))
            type = "News";
        p.setType(type);
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        p.setTime(new Timestamp(new Date().getTime()).toString());//utc 2 cst北京时间
        log.debug("*****extractorTime  success*****");
        return true;
    }

    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract desc, continue + url : " + url);
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

    public boolean isPaging() {
        return  false;
    }

    public boolean extractorAndUploadImg(String host, String port) {
//        log.debug("*****extractorAndUploadImg*****");
        return true;

    }

    public void mergePage(ParserPage p) {
        log.debug("*****mergePage*****");
        log.info("*****mergePage end*****");
    }

}
