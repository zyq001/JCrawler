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
package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.util.Config;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.util.AntiAntiSpiderHelper;
import com.youdao.dict.util.JDBCHelper;
import org.springframework.jdbc.core.JdbcTemplate;

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
public class BBCCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public BBCCrawler(String crawlPath) {
        super(crawlPath);

        regexRule.addRule("http://www.bbc.co.*");
        regexRule.addRule("-.*jpg.*");

        /*创建一个JdbcTemplate对象,"mysql1"是用户自定义的名称，以后可以通过
         JDBCHelper.getJdbcTemplate("mysql1")来获取这个对象。
         参数分别是：名称、连接URL、用户名、密码、初始化连接数、最大连接数
        
         这里的JdbcTemplate对象自己可以处理连接池，所以爬虫在多线程中，可以共用
         一个JdbcTemplate对象(每个线程中通过JDBCHelper.getJdbcTemplate("名称")
         获取同一个JdbcTemplate对象)             
         */

        try {
//            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
//                    "jdbc:mysql://localhost/readease?useUnicode=true&characterEncoding=utf8",
//                    "root", "", 5, 30);
//            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
//                    "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8",
//                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://pxc-mysql.inner.youdao.com/readease?useUnicode=true&characterEncoding=utf8",
                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }
    }
static int conter = 1;
    @Override
    public Links visitAndGetNextLinks(Page page) {
        try {
            BaseExtractor extractor = new BBCExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style" +
                        ", host, url, time, description, content, wordCount, uniqueWordCount, version, mainimage" +
                        ", moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        p.getTitle(), p.getType(), p.getLabel(), p.getLevel(), p.getStyle()
                        , p.getHost(), p.getUrl(), p.getTime(), p.getDescription(), p.getContent(), p.getWordCount()
                        , p.getUniqueWordCount(), p.getVersion(), p.getMainimage(), p.getMoreinfo());
//                int updates = jdbcTemplate.update("update parser_page set content = ?, time = ? where url = ?", p.getContent(), p.getTime(), p.getUrl());

                if (updates == 1) {
//                    System.out.println(conter++);
                    System.out.println("parser_page插入成功" + p.getUrl());
                    int id = jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", p.getUrl());

                    updates = jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
                            id, extractor.doc.html());
                    System.out.println("org_content插入成功" + p.getUrl());
                }else{
                    System.out.println("失败插入mysql" + p.getUrl());
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

        BBCCrawler crawler = new BBCCrawler("data/BBC");
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


        crawler.addSeed("http://www.bbc.com/");

        if(BaseExtractor.isNormalTime()) {
            crawler.addSeed("http://www.bbc.com/autos/");

            crawler.addSeed("http://www.bbc.com/news/science_and_environment");
            crawler.addSeed("http://www.bbc.com/news");
            crawler.addSeed("http://www.bbc.com/travel/");
            crawler.addSeed("http://www.bbc.com/culture/");
            crawler.addSeed("http://www.bbc.com/news/business");//opinion
            crawler.addSeed("http://www.bbc.com/news/world");

            crawler.addSeed("http://www.bbc.com/news/technology");
            crawler.addSeed("http://www.bbc.com/news/entertainment_and_arts");
            crawler.addSeed("http://www.bbc.com/news/health");
            crawler.addSeed("http://www.bbc.com/earth/world");
            crawler.addSeed("http://www.bbc.co.uk/arts");

////////
            crawler.addSeed("http://www.bbc.co.uk/newsround/news");
            crawler.addSeed("http://www.bbc.co.uk/newsround/sport");
            crawler.addSeed("http://www.bbc.co.uk/newsround/entertainment");

        }

        Config.WAIT_THREAD_END_TIME = 1000*60*5;//等待队列超时后，等待线程自动结束的时间，之后就强制kill
//        Config.TIMEOUT_CONNECT = 1000*10;
//        Config.TIMEOUT_READ = 1000*30;
        Config.requestMaxInterval = 1000*60*20;//线程池可用最长等待时间，当前时间-上一任务启动时间>此时间就会认为hung


        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);
//        requester.setCookie("CNZZDATA1950488=cnzz_eid%3D739324831-1432460954-null%26ntime%3D1432460954; wdcid=44349d3f2aa96e51; vjuids=-53d395da8.14eca7eed44.0.f17be67e; CNZZDATA3473518=cnzz_eid%3D1882396923-1437965756-%26ntime%3D1440635510; pt_37a49e8b=uid=FuI4KYEfVz5xq7L4nzPd1w&nid=1&vid=r4AhSBmxisCiyeolr3V2Ow&vn=1&pvn=1&sact=1440639037916&to_flag=0&pl=t4NrgYqSK5M357L2nGEQCw*pt*1440639015734; _ga=GA1.3.1121158748.1437970841; __auc=c00a6ac114d85945f01d9c30128; CNZZDATA1975683=cnzz_eid%3D250014133-1432460541-null%26ntime%3D1440733997; CNZZDATA1254041250=2000695407-1442220871-%7C1442306691; pt_7f0a67e8=uid=6lmgYeZ3/jSObRMeK-t27A&nid=0&vid=lEKvEtZyZdd0UC264UyZnQ&vn=2&pvn=1&sact=1442306703728&to_flag=0&pl=7GB3sYS/PJDo1mY0qeu2cA*pt*1442306703728; 7NSx_98ef_saltkey=P05gN8zn; 7NSx_98ef_lastvisit=1444281282; IframeBodyHeight=256; NTVq_98ef_saltkey=j5PydYru; NTVq_98ef_lastvisit=1444282735; NTVq_98ef_atarget=1; NTVq_98ef_lastact=1444286377%09api.php%09js; 7NSx_98ef_sid=hZyDwc; __utmt=1; __utma=155578217.1121158748.1437970841.1443159326.1444285109.23; __utmb=155578217.57.10.1444285109; __utmc=155578217; __utmz=155578217.1439345650.3.2.utmcsr=travel.chinadaily.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/; CNZZDATA3089622=cnzz_eid%3D1722311508-1437912344-%26ntime%3D1444286009; wdlast=1444287704; vjlast=1437916393.1444285111.11; 7NSx_98ef_lastact=1444287477%09api.php%09chinadaily; pt_s_3bfec6ad=vt=1444287704638&cad=; pt_3bfec6ad=uid=bo87MAT/HC3hy12HDkBg1A&nid=0&vid=erwHQyFKxvwHXYc4-r6n-w&vn=28&pvn=2&sact=1444287708079&to_flag=0&pl=kkgvLoEHXsCD2gs4VJaWQg*pt*1444287704638; pt_t_3bfec6ad=?id=3bfec6ad.bo87MAT/HC3hy12HDkBg1A.erwHQyFKxvwHXYc4-r6n-w.kkgvLoEHXsCD2gs4VJaWQg.nZJ9Aj/bgfNDIKBXI5TwRQ&stat=167.132.1050.1076.1body%20div%3Aeq%288%29%20ul%3Aeq%280%29%20a%3Aeq%282%29.0.0.1595.3441.146.118&ptif=4");
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
       //c requester.setProxy("proxy.corp.youdao.com", 3456, Proxy.Type.SOCKS);
        /*

        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
//        crawler.setResumable(true);
        crawler.setResumable(false);

        crawler.start(2);
    }

}
