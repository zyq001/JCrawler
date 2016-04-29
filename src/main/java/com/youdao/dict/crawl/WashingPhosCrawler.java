package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.youdao.dict.util.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zangyq on 2016/4/27.
 */
public class WashingPhosCrawler extends BaseCrawler {
//    ;

    public WashingPhosCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public WashingPhosCrawler(Configuration conf) {
        super(conf);
    }

    public WashingPhosCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new WashingPhosExtractor(page);
    }

    @Override
    public boolean isMatches(String seed) {
//        return seed.matches(".*index\\.html");
//        return true;
        return regexRule.satisfy(seed);
    }

    public void addIndexSeed(String url) {
        Document doc = null;//parse("<p>dd</p><img></img>");
        URL urls = null;
        JsonObject json = null;
        try {
            urls = new URL(url);

            HttpURLConnection urlConnection = (HttpURLConnection) urls.openConnection();
            InputStream is = urlConnection.getInputStream();
            Reader rd = new InputStreamReader(is, "utf-8");
            json = new JsonParser().parse(rd).getAsJsonObject();
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String render = json.get("rendering").getAsString();

            doc = Jsoup.parse(render);

        Element body = doc.body();
//        Elements groups = body.select("article");
        for (Element e : body.select("a[itemprop=url]")) {
            String seed = e.attr("abs:href");
//            if (isMatches(seed))
                addSeed(seed);
            counter ++;
        }
    }

    static int counter = 0;
    public static void main(String[] args) throws Exception {
//        System.out.println("http://edition.cnn.com/specials/impact-your-world.indexrhtml".matches(".*index\\.html"));
        WashingPhosCrawler crawler = new WashingPhosCrawler("data/WashingPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();
//        crawler.regexRule.addRule("+.*html");

        String baseUrl = "https://www.washingtonpost.com/pb/api/v2/render/feature?id=f02kpsQl5PEcCp&contentUri=" +
                "%2Fphotography%2F%3Flimit%3D15%26offset%3D";
        String tail =  "%26itemDisplayTemplateName%3Ditem&uri=%2Fpb%2Fphotography%2F";
        for(int i = 0; i < 50; i= i + 30) {
            crawler.addIndexSeed(baseUrl + i + tail);
        }
        System.out.println("&&&&&&&&&&& " + counter);
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
