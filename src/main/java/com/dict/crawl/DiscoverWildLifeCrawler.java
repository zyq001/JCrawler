package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.util.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zangyq on 2016/4/27.
 */
public class DiscoverWildLifeCrawler extends BaseCrawler {
//    ;

    public DiscoverWildLifeCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public DiscoverWildLifeCrawler(Configuration conf) {
        super(conf);
    }

    public DiscoverWildLifeCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new DiscoverWildLifeExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
//        return seed.matches(".*index\\.html");
//        return true;
        return regexRule.satisfy(seed);
    }

    public void addIndexSeed(String url){
        Document doc = null;//parse("<p>dd</p><img></img>");
        try {
            doc = Jsoup.parse(new URL(url), timeoutMillis);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element body = doc.body().select("#content").first();
//        Elements groups = body.select("article");
        for(Element e: body.select("a")){
            String seed = e.attr("abs:href");
            if(isMatches(seed))
                addSeed(seed);
        }
    }

    public static void main(String[]  args) throws Exception {
//        System.out.println("http://edition.cnn.com/specials/impact-your-world.indexrhtml".matches(".*index\\.html"));
        DiscoverWildLifeCrawler crawler = new DiscoverWildLifeCrawler("data/CNNPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();
        crawler.regexRule.addRule("+.*gallery.*");

        String baseUrl = "http://www.discoverwildlife.com/wildlife-nature-photography/pro-photo-galleries";
        String offset = "?page=";
        crawler.addIndexSeed(baseUrl);
        for(int i = 1; i < 32; i++){
            crawler.addIndexSeed(baseUrl + offset + i);
        }
//        crawler.addIndexSeed("http://www.cnn.com/travel");
//        crawler.addIndexSeed("http://edition.cnn.com/specials/photos");
//        crawler.addIndexSeed("http://www.cnn.com/specials/travel/best-of-travel");
//        crawler.addIndexSeed("http://www.cnn.com/tech");
//        crawler.addIndexSeed("http://www.cnn.com/entertainment");
//        crawler.addSeed("");
//        crawler.addSeed("");

//        group = body.select(".container-in-pictures-hawk").get(0);
//        for(Element e: group.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }

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
