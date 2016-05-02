package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.util.Configuration;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zangyq on 2016/4/27.
 */
public class TheGuardianPicsCrawler extends BaseCrawler {
//    ;

    public TheGuardianPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public TheGuardianPicsCrawler(Configuration conf) {
        super(conf);
    }

    public TheGuardianPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new TheguardianPicsExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
//        return seed.matches(".*index\\.html");
        return true;
    }

    @Override
    public Elements getGroups(Element body) {

        Elements hyLinks = body.select("a");
        Element res = body.appendElement("div");
        for (int i = 0; i < hyLinks.size(); i++) {
            Element link = hyLinks.get(i);
            if (link.getAllElements().hasClass("inline-camera")) {
//                link.remove();//从dom树种remove
//                hyLinks.remove(i);//从hylinks结果集中remove
//            }else {
                res.appendChild(link);
//                System.out.println("no exec?");
//                continue;
            }
        }

        return res.children();
    }

    public static void main(String[] args) throws Exception {
//        System.out.println("http://edition.cnn.com/specials/impact-your-world.indexrhtml".matches(".*index\\.html"));
        TheGuardianPicsCrawler crawler = new TheGuardianPicsCrawler("data/CNNPics");
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
        crawler.addIndexSeed("http://www.theguardian.com/inpictures/all");

//        for(int i = 2; i < 50; i++){
//            crawler.addIndexSeed("http://www.theguardian.com/inpictures?page=" + i);
//        }

        crawler.addIndexSeed("http://www.theguardian.com/inpictures");
        crawler.addIndexSeed("http://www.theguardian.com/international");
        crawler.addIndexSeed("http://www.theguardian.com/us/sport");
        crawler.addIndexSeed("http://www.theguardian.com/uk/business");
        crawler.addIndexSeed("http://www.theguardian.com/us/business");
        crawler.addIndexSeed("http://www.theguardian.com/travel");
        crawler.addIndexSeed("http://www.theguardian.com/lifeandstyle");
        crawler.addIndexSeed("http://www.theguardian.com/uk/technology");
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
