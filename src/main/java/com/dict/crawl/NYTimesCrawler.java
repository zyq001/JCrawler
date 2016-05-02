/*
 * Copyright (C) 2014 hu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.dict.bean.ParserPage;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.JDBCHelper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.Proxy;

/**
 * WebCollector 2.x版本的tutorial
 * 2.x版本特性：
 * 1）自定义遍历策略，可完成更为复杂的遍历业务，例如分页、AJAX
 * 2）内置Berkeley DB管理URL，可以处理更大量级的网页
 * 3）集成selenium，可以对javascript生成信息进行抽取
 * 4）直接支持多代理随机切换
 * 5）集成spring jdbc和mysql connection，方便数据持久化
 * 6）集成json解析器
 * 7）使用slf4j作为日志门面
 * 8）修改http请求接口，用户自定义http请求更加方便
 * <p/>
 * 可在cn.edu.hfut.dmic.webcollector.example包中找到例子(Demo)
 *
 * @author hu
 */
public class NYTimesCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public NYTimesCrawler(String crawlPath) {
        super(crawlPath);

/*        regexRule.addRule("https://www.washingtonpost.com/entertainment/.*");
        regexRule.addRule("https://www.washingtonpost.com/lifestyle/.*");
        regexRule.addRule("https://www.washingtonpost.com/technology/.*");
        regexRule.addRule("https://www.washingtonpost.com/business/.*");
        regexRule.addRule("https://www.washingtonpost.com/world/.*");
        regexRule.addRule("https://www.washingtonpost.com/national/.*");
        regexRule.addRule("https://www.washingtonpost.com/sports/.*");
        regexRule.addRule("https://www.washingtonpost.com/local/.*");*/
        regexRule.addRule(".*.nytimes.com/.*");
/*        regexRule.addRule("-https://www.washingtonpost.com/politics/.*");
        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");
        regexRule.addRule("-https://www.washingtonpost.com/posttv/.*");
        regexRule.addRule("-https://www.washingtonpost.com/cars/.*");
        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");*/

        /*创建一个JdbcTemplate对象,"mysql1"是用户自定义的名称，以后可以通过
         JDBCHelper.getJdbcTemplate("mysql1")来获取这个对象。
         参数分别是：名称、连接URL、用户名、密码、初始化连接数、最大连接数
        
         这里的JdbcTemplate对象自己可以处理连接池，所以爬虫在多线程中，可以共用
         一个JdbcTemplate对象(每个线程中通过JDBCHelper.getJdbcTemplate("名称")
         获取同一个JdbcTemplate对象)             
         */

        try {
//            jdbcTemplate = JDBCHelper.createMysqlTemplate("nytimes",
//                    "jdbc:mysql://localhost/readease?useUnicode=true&characterEncoding=utf8",
//                    "root", "", 5, 30);
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://pxc-mysql.inner. /readease?useUnicode=true&characterEncoding=utf8",
                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }
    }

    @Override
    public Links visitAndGetNextLinks(Page page) {
        try {
            BaseExtractor extractor = new NYTimesExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, version, mainimage, moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        p.getTitle(),p.getType(),p.getLabel(),p.getLevel(),p.getStyle(),p.getHost(),p.getUrl(),p.getTime(),p.getDescription(),p.getContent(),p.getVersion(),p.getMainimage(),p.getMoreinfo());
                if (updates == 1) {
                    System.out.println("mysql插入成功");
                }
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

    public static void main(String[] args) throws Exception {
        /*构造函数中的string,是爬虫的crawlPath，爬虫的爬取信息都存在crawlPath文件夹中,
          不同的爬虫请使用不同的crawlPath
        */
        NYTimesCrawler crawler = new NYTimesCrawler("data/nytimes");
        crawler.setThreads(1);
//        String seeds = "http://www.nytimes.com,http://international.nytimes.com/,http://www.nytimes.com/pages/world/index.html,http://www.nytimes.com/pages/national/index.html,http://www.nytimes.com/pages/politics/index.html,http://www.nytimes.com/pages/nyregion/index.html,http://www.nytimes.com/pages/business/index.html,http://www.nytimes.com/pages/business/international/index.html,http://www.nytimes.com/pages/opinion/index.html,http://www.nytimes.com/pages/opinion/international/index.html,http://www.nytimes.com/pages/technology/index.html,http://www.nytimes.com/pages/science/index.html,http://www.nytimes.com/pages/health/index.html,http://www.nytimes.com/pages/sports/index.html,http://www.nytimes.com/pages/sports/international/index.html,http://www.nytimes.com/pages/arts/index.html,http://www.nytimes.com/pages/arts/international/index.html,http://www.nytimes.com/pages/fashion/index.html,http://www.nytimes.com/pages/style/international/index.html,http://www.nytimes.com/pages/dining/index.html,http://www.nytimes.com/pages/dining/international/index.html,http://www.nytimes.com/pages/travel/index.html,http://www.nytimes.com/pages/magazine/index.html,http://www.nytimes.com/section/t-magazine,http://www.nytimes.com/pages/realestate/index.html,http://www.nytimes.com/pages/obituaries/index.html,http://www.nytimes.com/video/,http://www.nytimes.com/upshot/,http://www.nytimes.com/crosswords/,http://www.nytimes.com/times-insider,http://www.nytimes.com/pages/multimedia/index.html,http://lens.blogs.nytimes.com/,http://www.nytwineclub.com/,http://nytedu.com,http://www.nytimes.com/times-journeys/,http://www.nytimes.com/seeallnav,http://www.nytimes.com/membercenter,http://www.nytimes.com/pages/todayspaper/index.html,http://www.nytimes.com/interactive/blogs/directory.html,http://www.nytimes.com/pages/topics/,http://www.nytimes.com/marketing/tools-and-services/,http://www.nytimes.com/section/jobs,http://www.nytimes.com/ref/classifieds/,http://www.nytimes.com/pages/corrections/index.html";
//        for (String seed : seeds.split(",")) {
//            crawler.addSeed(seed);
//        }
        crawler.addSeed("http://www.nytimes.com/2015/08/26/business/dealbook/daily-stock-market-activity.html");

        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);
        requester.setProxy("
//        requester.setCookie("RMID=007f010079b255dbe5880008; optimizelyEndUserId=oeu2440474544317r0.4275816313456745; Akamai_AnalyticsMetrics_clientId=ADB55E335AA1532B095DDB4EFFCAC060685A33E9; HTML_ViewerId=30065e76-e7ea-bc88-723d-f81430054466; __cfduid=d3fbb4f5cf69b2677b0c215e20ac5feca1440558795; WT_FPC=id=7a6d0f44-ebfc-4d33-80af-c9d78760de0f:lv=1442199775778:ss=1442199718478; _dycst=l.c.ms.frv3.tos.; _dy_geo=SG.AS.SG_00.SG_00_Singapore; _dy_toffset=0; __CT_Data=gpv=4&apv_330_www06=2&apv_202_www06=1; WRUID=0; _sp_id.ddc6=1f273d32fe3caf3a.1440474510.5.1442267557.1442246591; _dyus_8765260=285%7C10%7C1%7C23%7C0%7C0.0.1440474511746.1442246575880.1772064.0%7C257%7C38%7C8%7C115%7C9%7C0%7C0%7C0%7C0%7C0%7C0%7C9%7C0%7C0%7C22%7C0%7C0%7C9%7C22%7C0%7C0%7C0%7C0; walley=GA1.2.1254418564.1440474510; nyt-a=8b5c8a696a9c6a9867f60e1c2ea71018; _chartbeat2=q8uTRCvbyhS1RXQG.1440474512623.1442290238053.0000000000000001; NYT_W2=New%20YorkNYUS|ChicagoILUS|London--UK|Los%20AngelesCAUS|San%20FranciscoCAUS|Tokyo--JP; nyt-d=101.000000000NAI00000YAIWo0%2CPN5%2F0zB0aL1w8JKF0MOq54%4012f8961d%2Ff669ad92; NYT-S=1Mfd8zJmglmlc3/0nGADovICYDlM3LyoS443pEvD3aR.9cs5etVqYPH3974PUKaDd8MKjijxuNEuPEdFXNMFLLFGcrUw4eLZoy8bwIDCr3j3yyKW321p0tleG.KGMJXOL8V6Csd.rZn5Jn.kpbFgxmLw00; _ga=GA1.2.915152680.1440474546; __utma=69104142.915152680.1440474546.1442288772.1442900984.5; __utmz=69104142.1440474546.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); nyt-m=8ACE9E83A96D9CD2E20283DA200BCF48&e=i.1443672000&t=i.10&v=i.25&l=l.15.591932996.4169611015.1394222789.2059812240.2895612839.937988420.3058835947.2279965908.1890252540.1387882654.1586304971.3136038376.2160970785.1172130517.893392079&n=i.2&g=i.175&rc=i.0&er=i.1440474509&vr=l.4.40.0.0.0&pr=l.4.217.0.0.0&vp=i.0&gf=l.10.2350676162.3324830587.4126281465.3171192173.3435150701.3951810194.2363370322.2953791066.1453560303.2279965908&ft=i.0&fv=i.0&gl=l.2.1890252540.1172130517&rl=l.1.-1&cav=i.25&imu=i.1&igu=i.1&prt=i.5&kid=i.1&ica=i.1&iue=i.0&ier=i.0&iub=i.0&ifv=i.0&igd=i.1&iga=i.1&imv=i.0&igf=i.0&iru=i.0&ird=i.0&ira=i.1&iir=i.1; optimizelySegments=%7B%223007620980%22%3A%22direct%22%2C%223013750536%22%3A%22false%22%2C%223028090192%22%3A%22gc%22%2C%223032570147%22%3A%22none%22%2C%223315571554%22%3A%22search%22%2C%223321851195%22%3A%22false%22%2C%223334171090%22%3A%22none%22%2C%223336921036%22%3A%22gc%22%7D; optimizelyBuckets=%7B%7D; NYT-wpAB=0012|1&0033|0&0036|-2&0051|0&0052|5&0061|-2&0063|0&0064|1&0066|1&0067|-2&0069|1; optimizelyPendingLogEvents=%5B%5D");
        requester.setCookie("RMID=007f010168fc562775b8000f; optimizelyEndUserId=oeu1445426618120r0.750250875717029; NYT-Edition=edition|GLOBAL; adxcs=-; walley=GA1.2.1519620208.1445426666; optimizelySegments=%7B%223007620980%22%3A%22referral%22%2C%223013750536%22%3A%22false%22%2C%223028090192%22%3A%22gc%22%2C%223032570147%22%3A%22none%22%2C%223315571554%22%3A%22direct%22%2C%223321851195%22%3A%22false%22%2C%223334171090%22%3A%22none%22%2C%223336921036%22%3A%22gc%22%7D; optimizelyBuckets=%7B%7D; NYT-S=0MK.wmwb./QInDXrmvxADeHLS/ffJlIHzbdeFz9JchiAImJkOx2rgxzsV.Ynx4rkFI; _ga=GA1.2.1564663541.1445426667; mt.v=2.884258513.1445430678560; nyt-a=82338d57d269c4e9e4844149678f5076; NYT-mab=%7B%222%22%3A%22A3%22%7D; NYT-wpAB=0012|1&0033|3&0036|-2&0051|1&0052|5&0061|-2&0063|1&0064|1&0066|1&0067|-2&0069|1&0070|1; __utma=69104142.1564663541.1445426667.1445426667.1445430805.2; __utmb=69104142.4.10.1445430805; __utmc=69104142; __utmz=69104142.1445426667.1.1.utmcsr=international.nytimes.com|utmccn=(referral)|utmcmd=referral|utmcct=/; nyt-m=80F885DA20B91D054007916EAF9B3286&e=i.1446350400&t=i.10&v=i.8&l=l.15.992675626.1654906570.1111667441.1548630622.4254452610.3851345550.3632327984.1339763924.-1.-1.-1.-1.-1.-1.-1&n=i.2&g=i.0&rc=i.0&er=i.1445426659&vr=l.4.8.0.0.0&pr=l.4.10.0.0.0&vp=i.0&gf=l.10.992675626.1654906570.1111667441.1548630622.4254452610.3851345550.3632327984.1339763924.-1.-1&ft=i.0&fv=i.0&gl=l.2.-1.-1&rl=l.1.-1&cav=i.8&imu=i.1&igu=i.1&prt=i.5&kid=i.1&ica=i.1&iue=i.0&ier=i.0&iub=i.0&ifv=i.0&igd=i.0&iga=i.0&imv=i.1&igf=i.0&iru=i.0&ird=i.0&ira=i.0&iir=i.1");
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
        /*

        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
        crawler.setResumable(false);

        crawler.start(2);
    }

}
