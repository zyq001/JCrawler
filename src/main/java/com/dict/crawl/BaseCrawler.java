package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.util.Config;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.dict.util.JDBCHelper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.Configuration;
import com.dict.util.RSSReaderHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zyq on 2016/4/27.
 */
@CommonsLog
public abstract class BaseCrawler extends DeepCrawler {

    static int timeoutMillis = 180 * 1000;
    RegexRule regexRule = new RegexRule();
    Configuration conf;
    JdbcTemplate jdbcTemplate = null;
    Map<String, SyndEntry> url2SyndEntry = new ConcurrentHashMap<String, SyndEntry>();
    Map<String, String> url2Type = new ConcurrentHashMap<String, String>();

    private String DEAULT_CONFIG_FILE_NAME = "conf/remote.properties";

    public BaseCrawler(String propertiesFileName, String crawlPath){
        super(crawlPath);
        conf = new Configuration(propertiesFileName, crawlPath);
        init();//后续再考虑构造器之间的调用
    }

    public BaseCrawler(Configuration conf){
        super(conf.get(Configuration.CRAWL_PATH));
        this.conf = conf;
        init();
    }

    public BaseCrawler(String crawlPath) {
        super(crawlPath);
        conf = new Configuration(DEAULT_CONFIG_FILE_NAME, crawlPath);
        init();
    }

    public static boolean isNormalTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String hour = dateFormat.format(date);
        return BaseExtractor.normalHour.contains(hour);
    }

    public void PCAgentMode(){
        HttpRequesterImpl requester = (HttpRequesterImpl) getHttpRequester();
        AntiAntiSpiderHelper.PCUserAgent(requester);
    }
    public void MobileAgentMode(){
        HttpRequesterImpl requester = (HttpRequesterImpl) getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);
    }

    private void init(){

//        regexRule.addRule("-.*jpg.*");

        Config.WAIT_THREAD_END_TIME = 1000*60*3;//等待队列超时后，等待线程自动结束的时间，之后就强制kill
//        Config.TIMEOUT_CONNECT = 1000*10;
//        Config.TIMEOUT_READ = 1000*30;
        Config.requestMaxInterval = 1000*60*20;//线程池可用最长等待时间，当前时间-上一新任务启动时间>此时间就会认为hung

        MobileAgentMode();

        setResumable(false);

        /*创建一个JdbcTemplate对象,"mysql1"是用户自定义的名称，以后可以通过
         JDBCHelper.getJdbcTemplate("mysql1")来获取这个对象。
         参数分别是：名称、连接URL、用户名、密码、初始化连接数、最大连接数

         这里的JdbcTemplate对象自己可以处理连接池，所以爬虫在多线程中，可以共用
         一个JdbcTemplate对象(每个线程中通过JDBCHelper.getJdbcTemplate("名称")
         获取同一个JdbcTemplate对象)
         */
            try {

                jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                        conf.get(Configuration.MYSQL_URL),
                        conf.get(Configuration.MYSQL_USER), conf.get(Configuration.MYSQL_PASSWORD), 5, 30);
            } catch (Exception ex) {
                jdbcTemplate = null;
                System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
            }
    }

    public void addRSSSeeds(String rssAddr, String type) {
        URL url = null;
        try {
            url = new URL(rssAddr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpcon = null;
        try {
            httpcon = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Reading the feed
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        try {
            feed = input.build(new XmlReader(httpcon));
        } catch (FeedException e) {
            System.out.println(e + "feed: " + rssAddr);
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(e + "feed: " + rssAddr);

        }

        if(feed == null || feed.getEntries().size() < 1){
            log.info("get entries failed, feed: " + rssAddr);
            return;
        }

        List<SyndEntry> entries = feed.getEntries();
        for (SyndEntry entry : entries) {
            url2SyndEntry.put(entry.getLink(), entry);
            url2Type.put(entry.getLink(), type);

            addSeed(entry.getLink());
        }

    }

    static int conter = 1;
    @Override
    public Links visitAndGetNextLinks(Page page) {
        try {
            BaseExtractor extractor = getBbcrssExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                extractor.p.setPage_type(1);
                extractor.insertWith(jdbcTemplate);
//
//                ParserPage p = extractor.getParserPage();
//                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, wordCount, version, mainimage, moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                        p.getTitle(), p.getType(), p.getLabel(), p.getLevel(), p.getStyle(), p.getHost(), p.getUrl(), p.getTime(), p.getDescription(), p.getContent(), p.getWordCount(), p.getVersion(), p.getMainimage(), p.getMoreinfo());
////                int updates = jdbcTemplate.update("update parser_page set content = ?, time = ? where url = ?", p.getContent(), p.getTime(), p.getUrl());
//
//                if (updates == 1) {
////                    System.out.println(conter++);
//                    System.out.println("parser_page插入成功" + p.getUrl());
//                    int id = jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", p.getUrl());
//
//                    updates = jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
//                            id, extractor.doc.html());
//                    System.out.println("org_content插入成功" + p.getUrl());
//                }else{
//                    System.out.println("失败插入mysql" + p.getUrl());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*下面是2.0版本新加入的内容*/
        /*抽取page中的链接返回，这些链接会在下一轮爬取时被爬取。
         不用担心URL去重，爬虫会自动过滤重复URL。*/
        Links nextLinks = new Links();

        /*我们只希望抽取满足正则约束的URL，
         Links.addAllFromDocument为我们提供了相应的功能*/
        nextLinks.addAllFromDocument(page.getDoc(), regexRule);

        /*Links类继承ArrayList<String>,可以使用add、addAll等方法自己添加URL
         如果当前页面的链接中，没有需要爬取的，可以return null
         例如如果你的爬取任务只是爬取seed列表中的所有链接，这种情况应该return null
         */
        return nextLinks;
    }

    public abstract BaseExtractor getBbcrssExtractor(Page page);

    public void addIndexSeed(String url){
        Document doc = null;//parse("<p>dd</p><img></img>");
        try {
            doc = Jsoup.parse(new URL(url), timeoutMillis);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element body = doc.body();
        Elements groups = getGroups(body);
        for(Element e: groups.select("a")){
            String seed = e.attr("abs:href");
            if(isMatches(seed))
                addSeed(seed);
        }
    }

    public Elements getGroups(Element body) {
        return body.select("a");
    }

    public boolean isMatches(String seed){
        return regexRule.satisfy(seed);
    }

    public static void main(String[] args) throws Exception {
        /*构造函数中的string,是爬虫的crawlPath，爬虫的爬取信息都存在crawlPath文件夹中,
          不同的爬虫请使用不同的crawlPath
        */

        Document doc = Jsoup.parse("<p>dd</p><img></img>");
        Element body = doc.body();
        System.out.println(body.html());
        System.out.println(body.outerHtml());



        BBCRSSCrawler crawler = new BBCRSSCrawler("data/BBCRss");
        crawler.setThreads(5);
//
//        int id = crawler.jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", "http://cgi.money.cnn.com/2015/10/06/technology/twitter-moments/index.html");

//        byte[] b1 = {-30,-102,-67}; //ios5 //0xE2 0x9A 0xBD
//        byte[] b2 = {-18,-128,-104}; //ios4 //"E018"
//
//        //-------------------------------------
////
//        byte[] b3 = {-16,-97,-113,-128};    //0xF0 0x9F 0x8F 0x80
////        byte[] b4 = "\\xF0\\x9F\\x98\\x81".getBytes();
////        Byte b = new Byte("0xF0");
//
////        byte[] testbytes = {105,111,115,-30,-102,-67,32,36,-18,-128,-104,32,36,-16,-97,-113,-128,32,36,-18,-112,-86};
//        String tmpstr = new String(b3,"utf-8");
//        byte[] tt = tmpstr.getBytes();
//        System.out.println(tmpstr);
//        int updates = crawler.jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
//                0, tmpstr );
////        String s = crawler.jdbcTemplate.queryForObject("SELECT id FROM parser_page WHERE id = 0");
//
////        String[] ios5emoji = new String[]{new String(b1,"utf-8"),new String(b3,"utf-8")};
////        String[] ios4emoji = new String[]{new String(b2,"utf-8"),new String(b4,"utf-8")};
//
//        List<Map<String, Object>> urls = crawler.jdbcTemplate.queryForList("SELECT content FROM org_content WHERE id = 0");
////        crawler.addSeed("http://www.theguardian.com/environment/2015/oct/12/new-ipcc-chief-calls-for-fresh-focus-on-climate-solutions-not-problems");
////        crawler.addSeed("http://www.theguardian.com/australia-news/2015/oct/10/pro-diversity-and-anti-mosque-protesters-in-standoff-in-bendigo-park");
////        crawler.addSeed("http://www.todayonline.com/world/americas/peru-military-fails-act-narco-planes-fly-freely");
//        for(int i = 0; i < urls.size(); i++){
//            String url = (String)urls.get(i).get("content");
//            byte[] bb = url.getBytes();
//            System.out.println(url);
////            crawler.addSeed(url);
//        }
//        crawler.addSeed("http://www.bbc.com/culture/story/20160112-what-i-learned-when-i-lived-as-david-bowie");


        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/world/rss.xml", "World");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/business/rss.xml", "Business");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/politics/rss.xml", "Politics");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/uk/rss.xml", "World");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/technology/rss.xml", "Technology");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/video_and_audio/news_front_page/rss.xml", "News");

        if(isNormalTime()) {

            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/health/rss.xml", "Health");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml", "Entertainment");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/world/asia/rss.xml", "Asia Pacific");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/world/europe/rss.xml", "World");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml", "World");
//            RSSReaderHelper.addRSSSeeds(crawler, "", "");
        }
//        crawler.addSeed("http://www.bbc.com/");
//
//        if(BaseExtractor.isNormalTime()) {
//            crawler.addSeed("http://www.bbc.com/autos/");
//
//            crawler.addSeed("http://www.bbc.com/news/science_and_environment");
//            crawler.addSeed("http://www.bbc.com/news");
//            crawler.addSeed("http://www.bbc.com/travel/");
//            crawler.addSeed("http://www.bbc.com/culture/");
//            crawler.addSeed("http://www.bbc.com/news/business");//opinion
//            crawler.addSeed("http://www.bbc.com/news/world");
//
//            crawler.addSeed("http://www.bbc.com/news/technology");
//            crawler.addSeed("http://www.bbc.com/news/entertainment_and_arts");
//            crawler.addSeed("http://www.bbc.com/news/health");
//            crawler.addSeed("http://www.bbc.com/earth/world");
//            crawler.addSeed("http://www.bbc.co.uk/arts");
//
//////////
//            crawler.addSeed("http://www.bbc.co.uk/newsround/news");
//            crawler.addSeed("http://www.bbc.co.uk/newsround/sport");
//            crawler.addSeed("http://www.bbc.co.uk/newsround/entertainment");
//
//        }

        Config.WAIT_THREAD_END_TIME = 1000*60*5;//等待队列超时后，等待线程自动结束的时间，之后就强制kill
//        Config.TIMEOUT_CONNECT = 1000*10;
//        Config.TIMEOUT_READ = 1000*30;
        Config.requestMaxInterval = 1000*60*20;//线程池可用最长等待时间，当前时间-上一任务启动时间>此时间就会认为hung


        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);



        /*设置是否断点爬取*/
//        crawler.setResumable(true);
        crawler.setResumable(false);

        crawler.start(1);
    }

}
