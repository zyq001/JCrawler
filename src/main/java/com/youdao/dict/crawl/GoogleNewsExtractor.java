package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.Parser;
import com.youdao.dict.souplang.SoupLang;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class GoogleNewsExtractor extends BaseExtractor {

    public GoogleNewsExtractor(Page page) {
        super(page);
    }

    public GoogleNewsExtractor(String url) {
        super(url);
    }


    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("GoogleNewsRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("body");
            log.debug("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractor() {
        return init() && extractorType() && extractorContent();
    }


    public boolean extractorType() {
        String type = (String) context.output.get("type");
        if (type == null || "".equals(type.trim())) {
            log.info("*****extractorType  failed***** url:" + url);
            return false;
        }
        if (type.contains("/")) {
            type = type.substring(0, type.indexOf("/"));
            type = type.replace("/", "");
        }
        p.setType(type.trim());

        log.debug("*****extractorType  success*****");
        return true;
    }

    public boolean extractorContent() {
        Elements elements = content.select("div[class=blended-wrapper esc-wrapper]");
        for (Element element : elements) {
            ParserPage parserPage = extractorElement(element);
            if (parserPage != null) {
                parserPages.add(parserPage);
            }
        }
        return true;
    }

    public ParserPage extractorElement(Element element) {
        ParserPage page = new ParserPage();
        Elements imgEle = element.select("img[class=esc-thumbnail-image]");
        if (imgEle != null && imgEle.size() != 0) {
            page.setMainimage(imgEle.attr("src"));
        }
        Elements titleEle = element.select("span[class=titletext]");
        if (titleEle != null && titleEle.size() != 0) {
            page.setTitle(titleEle.text());
        } else {
            return null;
        }
        Elements hostEle = element.select("h2[class=esc-lead-article-title]");
        if (hostEle != null && hostEle.size() != 0) {
            Elements urlEle = hostEle.select("a");
            if (urlEle != null && urlEle.size() != 0) {
                String url = urlEle.attr("url");
                page.setUrl(url);
                String host = getHost(url);
                page.setHost(host);
                try {
/*                    HttpRequest request = new HttpRequest(url);
                    request.getRequestConfig().setTimeoutForConnect(1000);
                    request.getRequestConfig().setTimeoutForRead(1000);
                    String html = request.getResponse().getHtmlByCharsetDetect();
                    page.setContent(html);*/
                } catch (Exception e) {
                    log.info("Fail to get the target page." + url);
                    return null;
                }
            }
        }
        page.setTime(new Timestamp(System.currentTimeMillis()).toString());
        page.setType(p.getType());
        page.setStyle("html");
        log.info("---success parser:" + page.getTitle() + page.getUrl());
        return page;
    }
}
