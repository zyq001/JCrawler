package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.crawler.MultiExtractorCrawler;
import cn.edu.hfut.dmic.webcollector.extract.Extractor;
import cn.edu.hfut.dmic.webcollector.extract.ExtractorParams;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.JsoupUtils;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.score.LeveDis;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.DBClient;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import odis.serialize.lib.Url;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * Created by liuhl on 15-8-16.
 */
public class ChinaDailyCrawl {

    public static class ChinaDailyExtractor extends Extractor {

        private ParserPage p;
        Elements contentElements;
        String contentStr;

        public ChinaDailyExtractor(Page page, ExtractorParams params) {
            super(page, params);
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public void extract() throws Exception {
            contentElements = null;
            contentStr = "";
            Document doc = getDoc();
            JsoupUtils.makeAbs(doc, getUrl());
            SoupLang soupLang = new SoupLang(ClassLoader.getSystemResourceAsStream("DictRule.xml"));
            Context context = soupLang.extract(doc);
            p = new ParserPage();
            p.setTitle((String) context.output.get("title"));
            if ("".equals(p.getTitle())) return;
            p.setType((String) context.output.get("type"));//TODO
            if ("".equals(p.getTitle())) return;
            contentElements = (Elements) context.output.get("content");
            if (contentElements == null) return;
            contentStr = contentElements.text();
            LeveDis leveDis = LeveDis.getInstance(LeveDis.p);
            String tags = leveDis.tag(contentStr, 10);
            p.setLabel(tags);
            int level = leveDis.compFileLevel(leveDis.compLevel(contentStr));
            p.setLevel(String.valueOf(level));
            Url url = new Url(getUrl());
            p.setHost(url.getHostname());
            p.setUrl(getUrl());
            p.setTime((String) context.output.get("time"));

            Elements imgs = contentElements.select("img");
            String mainImage = null;
            for (Element node : imgs) {
                String imageUrl = node.attr("src");
                long id = new OImageUploader().deal(imageUrl);
                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                node.attr("src", newUrl.toString() + "&w=300");
                if (mainImage == null) {
                    mainImage = newUrl.toString() + "&w=300";
                }
            }
            p.setMainimage(mainImage);
            p.setContent((String) context.output.get("content"));

        }

        @Override
        public void output() throws Exception {
            if (contentElements != null) {
                p.setContent(contentElements.html());
                DBClient.insert(p);
            }
        }
    }

    public static void main(String[] args) throws Exception {


        MultiExtractorCrawler crawler = new MultiExtractorCrawler("crawl", true);
        crawler.addSeed("http://www.chinadaily.com.cn");
//        crawler.addRegex("http://www.zhihu.com/people/[^/]*");
//        crawler.addRegex("http://www.zhihu.com/question/.*");
//
//        不希望爬取包含#?的链接，同时不希望爬取jpg或者png文件
//        crawler.addRegex("-.*#.*");
//        crawler.addRegex("-.*\\?.*");
//        crawler.addRegex("-.*\\.jpg.*");
//        crawler.addRegex("-.*\\.png.*");

        /*加载抽取器时需要给出适用的url正则，在遇到满足url正则的网页时，抽取器就会在网页上执行*/
        crawler.addExtractor("http://www.chinadaily.com.cn/world/.*", ChinaDailyExtractor.class);

        ChinaDailyExtractor dailyExtractor = new ChinaDailyExtractor(new Page(),null);
        dailyExtractor.extract();

        crawler.setThreads(100);
        crawler.start(1000);
    }
}
