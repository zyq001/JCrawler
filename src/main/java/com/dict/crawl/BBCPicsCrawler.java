package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.util.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * Created by zangyq on 2016/4/27.
 */
public class BBCPicsCrawler extends BaseCrawler {
    public BBCPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public BBCPicsCrawler(Configuration conf) {
        super(conf);
    }

    public BBCPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new BBCPicsExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
//        return true;
        if(seed.matches(".*\\.jpg")){
            return false;
        }
        return true;
    }

    @Override
    public Elements getGroups(Element body) {

        Elements hyLinks = body.select("a");
        Element res = body.appendElement("div");
        for(int i = 0; i < hyLinks.size(); i++){
            Element link = hyLinks.get(i);
            if(link.getAllElements().hasClass("icon-wrapper-image") ){
//                link.remove();//从dom树种remove
//                hyLinks.remove(i);//从hylinks结果集中remove
//            }else {
                res.appendChild(link);
//                continue;
            }
        }

        return res.children();
    }

    public static void main(String[]  args) throws Exception {

        BBCPicsCrawler crawler = new BBCPicsCrawler("data/BBCPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();

//        Document doc = Jsoup.parse(new URL("http://www.bbc.com/news/10768686"), 180* 1000);//parse("<p>dd</p><img></img>");
//        Element body = doc.body();
//        Element group = body.select(".group").get(2);
//        for(Element e: group.select(".unit__link-wrapper")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
        Document doc = Jsoup.parse(new URL("http://www.bbc.com/news/in_pictures"), 180* 1000);//parse("<p>dd</p><img></img>");
        Element body = doc.body();
        Element group = body.select(".container-sparrow").get(0);
        for(Element e: group.select("a")){
            crawler.addSeed(e.attr("abs:href"));
        }
        group = body.select(".container-in-pictures-hawk").get(0);
        for(Element e: group.select("a")){
            crawler.addSeed(e.attr("abs:href"));
        }

        crawler.addIndexSeed("http://www.bbc.com/news");
        crawler.addIndexSeed("http://www.bbc.com/news/business");
        crawler.addIndexSeed("http://www.bbc.com/news/technology");
        crawler.addIndexSeed("http://www.bbc.com/news/science_and_environment");
        crawler.addIndexSeed("http://www.bbc.com/news/entertainment_and_arts");
        crawler.addIndexSeed("http://www.bbc.com/travel");
        crawler.addIndexSeed("http://www.bbc.com/culture/");
        crawler.addIndexSeed("http://www.bbc.com/autos/");
//        crawler.addIndexSeed("");

//        crawler.addSeed("http://www.bbc.com/news/magazine-36074328");
//        group = body.select(".container-parakeet").get(0);
//        for(Element e: group.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//        System.out.println(body.html());
//        System.out.println(body.outerHtml());

        crawler.start(1);
    }


}
