package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.util.Configuration;

/**
 * Created by zangyq on 2016/4/27.
 */
public class CNNPicsCrawler extends BaseCrawler {
//    ;

    public CNNPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public CNNPicsCrawler(Configuration conf) {
        super(conf);
    }

    public CNNPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new CNNPicsExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
        return seed.matches(".*index\\.html");
//        return true;
    }

    public static void main(String[]  args) throws Exception {
//        System.out.println("http://edition.cnn.com/specials/impact-your-world.indexrhtml".matches(".*index\\.html"));
        CNNPicsCrawler crawler = new CNNPicsCrawler("data/CNNPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();
        crawler.regexRule.addRule(".*index\\.html");


//        Document doc = Jsoup.parse(new URL("http://edition.cnn.com/specials/photos"), timeoutMillis);//parse("<p>dd</p><img></img>");
//        Element body = doc.body();
//        Elements groups = body.select("article");
//        for(Element e: groups.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//         doc = Jsoup.parse(new URL("http://edition.cnn.com/specials/cnn-photos-archive"), timeoutMillis);//parse("<p>dd</p><img></img>");
//         body = doc.body();
//         groups = body.select("article");
//        for(Element e: groups.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//         doc = Jsoup.parse(new URL("http://edition.cnn.com/specials/photos/throwback-thursday"), timeoutMillis);//parse("<p>dd</p><img></img>");
//         body = doc.body();
//         groups = body.select("article");
//        for(Element e: groups.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }

//        crawler.addSeed("http://edition.cnn.com/interactive/2016/04/world/nepal-one-year-later/");
        crawler.addIndexSeed("http://www.cnn.com/");
        crawler.addIndexSeed("http://www.cnn.com/travel");
        crawler.addIndexSeed("http://edition.cnn.com/specials/photos");
        crawler.addIndexSeed("http://www.cnn.com/specials/travel/best-of-travel");
        crawler.addIndexSeed("http://www.cnn.com/tech");
        crawler.addIndexSeed("http://www.cnn.com/entertainment");
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
