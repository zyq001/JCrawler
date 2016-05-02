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
import cn.edu.hfut.dmic.webcollector.util.Config;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.dict.util.JDBCHelper;
import com.dict.util.AntiAntiSpiderHelper;
import lombok.extern.apachecommons.CommonsLog;
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
@CommonsLog
public class MediumCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public MediumCrawler(String crawlPath) {
        super(crawlPath);

        regexRule.addRule("https://medium.com/.*");
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
//                    "root", "123456", 5, 30);
//            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
//                    "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8",
//                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
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
//            System.out.println(page.getResponse().getHeader("accept-language"));
            if (page.getResponse().getContentType().toLowerCase().contains("utf-8")) {
                page.setHtml(new String(page.getContent(), "UTF-8"));
                BaseExtractor extractor = new MediumExtractor(page);
                if (extractor.extractor() && jdbcTemplate != null) {
                    extractor.insertWith(jdbcTemplate);
//
//                    ParserPage p = extractor.getParserPage();
//                    String url = p.getUrl();
//                    int idx = url.indexOf("?");
//                    if(idx > 0) p.setUrl(url.substring(0, idx));
//                    int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, wordCount, version, mainimage, moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                            p.getTitle(), p.getType(), p.getLabel(), p.getLevel(), p.getStyle(), p.getHost(), p.getUrl(), p.getTime(), p.getDescription(), p.getContent(), p.getWordCount(), p.getVersion(), p.getMainimage(), p.getMoreinfo());
//                    if (updates == 1) {
//                        System.out.println("parser_page插入成功");
//                        int id = jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", p.getUrl());
//
//                        updates = jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
//                                id, extractor.doc.html());
//                        System.out.println("org_content插入成功");
//                    } else {
//                        System.out.println("mysql插入不成功，updates：" + updates);
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println(page.getResponse().getContentType());
//        System.out.println(page.getResponse().getCode());
//        log.info("after insert");
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
//        log.info("Before return nextLinks");
        return nextLinks;
    }

    public static void main(String[] args) throws Exception {
        /*构造函数中的string,是爬虫的crawlPath，爬虫的爬取信息都存在crawlPath文件夹中,
          不同的爬虫请使用不同的crawlPath
        */

//System.out.println(Long.MAX_VALUE);


        MediumCrawler crawler = new MediumCrawler("data/Medium");
        crawler.setThreads(3);

        crawler.addSeed("https://medium.com/");
        if(BaseCrawler.isNormalTime()) {
            crawler.addSeed("https://medium.com/top-stories");
//        crawler.addSeed("https://medium.com/top-100/january-2015");
            crawler.addSeed("https://medium.com/@MediumStaff/has-recommended");

        }
//        crawler.addSeed("https://medium.com/top-100/december-2015");
//        crawler.addSeed("https://medium.com/top-100/november-2015");
//        crawler.addSeed("https://medium.com/top-100/october-2015");
//        crawler.addSeed("https://medium.com/top-100/september-2015");
//        crawler.addSeed("https://medium.com/top-100/august-2015");
//        crawler.addSeed("https://medium.com/top-100/july-2015");
//        crawler.addSeed("https://medium.com/top-100/june-2015");
//        crawler.addSeed("https://medium.com/top-100/may-2015");
//        crawler.addSeed("https://medium.com/top-100/april-2015");
//        crawler.addSeed("https://medium.com/top-100/march-2015");
//        crawler.addSeed("https://medium.com/top-100/february-2015");
//        crawler.addSeed("https://medium.com/tag/politics");
//        crawler.addSeed("https://medium.com/tag/teaching");
////////
//        crawler.addSeed("https://medium.com/tag/tech");
//        crawler.addSeed("https://medium.com/tag/travel");
//        crawler.addSeed("https://medium.com/tag/lifestyle");
//        crawler.addSeed("https://medium.com/tag/economics");
//        crawler.addSeed("https://medium.com/tag/sports/latest");
//        crawler.addSeed("https://medium.com/tag/food/latest");
//        crawler.addSeed("https://medium.com/tag/business/latest");
//        crawler.addSeed("https://medium.com/tag/art/latest");
//        crawler.addSeed("https://medium.com/tag/education/latest");
//        crawler.addSeed("https://medium.com/tag/future-of-food/latest");
//        crawler.addSeed("https://medium.com/tag/government/latest");
//        crawler.addSeed("https://medium.com/tag/health/latest");
//        crawler.addSeed("https://medium.com/tag/life/latest");
//        crawler.addSeed("https://medium.com/tag/media/latest");
//        crawler.addSeed("https://medium.com/tag/music/latest");
//        crawler.addSeed("https://medium.com/tag/news/latest");
//        crawler.addSeed("https://medium.com/tag/politics/latest");
//        crawler.addSeed("https://medium.com/tag/teaching/latest");
////////
//        crawler.addSeed("https://medium.com/tag/tech/latest");
//        crawler.addSeed("https://medium.com/tag/travel/latest");
//        crawler.addSeed("https://medium.com/tag/lifestyle/latest");
//        crawler.addSeed("https://medium.com/tag/economics/latest");


//        Config
        Config.WAIT_THREAD_END_TIME = 1000*60*3;//等待队列超时后，等待线程自动结束的时间，之后就强制kill
//        Config.TIMEOUT_CONNECT = 1000*10;
//        Config.TIMEOUT_READ = 1000*30;
        Config.requestMaxInterval = 1000*60*20;//线程池可用最长等待时间，当前时间-上一新任务启动时间>此时间就会认为hung

        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);
        requester.addHeader("Accept-Language", "en-US,en;q=0.8,en-ca;q=0.6");
//        setHeader("Accept-Language", "iso-8859-1");
//        requester.setHeader();
//        requester.setCookie("CNZZDATA1950488=cnzz_eid%3D739324831-1432460954-null%26ntime%3D1432460954; wdcid=44349d3f2aa96e51; vjuids=-53d395da8.14eca7eed44.0.f17be67e; CNZZDATA3473518=cnzz_eid%3D1882396923-1437965756-%26ntime%3D1440635510; pt_37a49e8b=uid=FuI4KYEfVz5xq7L4nzPd1w&nid=1&vid=r4AhSBmxisCiyeolr3V2Ow&vn=1&pvn=1&sact=1440639037916&to_flag=0&pl=t4NrgYqSK5M357L2nGEQCw*pt*1440639015734; _ga=GA1.3.1121158748.1437970841; __auc=c00a6ac114d85945f01d9c30128; CNZZDATA1975683=cnzz_eid%3D250014133-1432460541-null%26ntime%3D1440733997; CNZZDATA1254041250=2000695407-1442220871-%7C1442306691; pt_7f0a67e8=uid=6lmgYeZ3/jSObRMeK-t27A&nid=0&vid=lEKvEtZyZdd0UC264UyZnQ&vn=2&pvn=1&sact=1442306703728&to_flag=0&pl=7GB3sYS/PJDo1mY0qeu2cA*pt*1442306703728; 7NSx_98ef_saltkey=P05gN8zn; 7NSx_98ef_lastvisit=1444281282; IframeBodyHeight=256; NTVq_98ef_saltkey=j5PydYru; NTVq_98ef_lastvisit=1444282735; NTVq_98ef_atarget=1; NTVq_98ef_lastact=1444286377%09api.php%09js; 7NSx_98ef_sid=hZyDwc; __utmt=1; __utma=155578217.1121158748.1437970841.1443159326.1444285109.23; __utmb=155578217.57.10.1444285109; __utmc=155578217; __utmz=155578217.1439345650.3.2.utmcsr=travel.chinadaily.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/; CNZZDATA3089622=cnzz_eid%3D1722311508-1437912344-%26ntime%3D1444286009; wdlast=1444287704; vjlast=1437916393.1444285111.11; 7NSx_98ef_lastact=1444287477%09api.php%09chinadaily; pt_s_3bfec6ad=vt=1444287704638&cad=; pt_3bfec6ad=uid=bo87MAT/HC3hy12HDkBg1A&nid=0&vid=erwHQyFKxvwHXYc4-r6n-w&vn=28&pvn=2&sact=1444287708079&to_flag=0&pl=kkgvLoEHXsCD2gs4VJaWQg*pt*1444287704638; pt_t_3bfec6ad=?id=3bfec6ad.bo87MAT/HC3hy12HDkBg1A.erwHQyFKxvwHXYc4-r6n-w.kkgvLoEHXsCD2gs4VJaWQg.nZJ9Aj/bgfNDIKBXI5TwRQ&stat=167.132.1050.1076.1body%20div%3Aeq%288%29%20ul%3Aeq%280%29%20a%3Aeq%282%29.0.0.1595.3441.146.118&ptif=4");
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
       //c requester.setProxy("
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
