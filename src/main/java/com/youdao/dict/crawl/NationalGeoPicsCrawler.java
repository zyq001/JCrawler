package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.util.Configuration;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zangyq on 2016/4/27.
 */
public class NationalGeoPicsCrawler extends BaseCrawler {
//    ;

    public NationalGeoPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public NationalGeoPicsCrawler(Configuration conf) {
        super(conf);
    }

    public NationalGeoPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new NationalGeoPicsExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
//        return seed.matches(".*index\\.html");
        return true;
    }

    @Override
    public Elements getGroups(Element body) {

        Elements res = body.select("#content_mainA");

        if(res.size()> 0 ){
            Elements recommandeds = body.select(".jcarousel-container-horizontal");
            if(recommandeds.size() > 0)
                res.addAll(recommandeds);
            return res;
        }

        return body.select("a");
    }

    public static void main(String[] args) throws Exception {
//        System.out.println("http://edition.cnn.com/specials/impact-your-world.indexrhtml".matches(".*index\\.html"));
        NationalGeoPicsCrawler crawler = new NationalGeoPicsCrawler("data/NationalGeoPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();
//        crawler.regexRule.addRule(".*index\\.html");


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
        crawler.addIndexSeed("http://photography.nationalgeographic.com/photography/photogalleries/");

//        for(int i = 2; i < 50; i++){
//            crawler.addIndexSeed("http://www.theguardian.com/inpictures?page=" + i);
//        }

        crawler.addIndexSeed("http://photography.nationalgeographic.com/photography/photos/books-photo-galleries/");
        crawler.addIndexSeed("http://photography.nationalgeographic.com/photography/photos/prints-photo-galleries/");
//        crawler.addIndexSeed("http://www.theguardian.com/us/sport");
//        crawler.addIndexSeed("http://www.theguardian.com/uk/business");
//        crawler.addIndexSeed("http://www.theguardian.com/us/business");
//        crawler.addIndexSeed("http://www.theguardian.com/travel");
//        crawler.addIndexSeed("http://www.theguardian.com/lifeandstyle");
//        crawler.addIndexSeed("http://www.theguardian.com/uk/technology");
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
