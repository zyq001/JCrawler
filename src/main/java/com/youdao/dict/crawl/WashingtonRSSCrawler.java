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
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.util.AntiAntiSpiderHelper;
import com.youdao.dict.util.JDBCHelper;
import com.youdao.dict.util.RSSReaderHelper;
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
public class WashingtonRSSCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public WashingtonRSSCrawler(String crawlPath) {
        super(crawlPath);

/*        regexRule.addRule("https://www.washingtonpost.com/entertainment/.*");
        regexRule.addRule("https://www.washingtonpost.com/lifestyle/.*");
        regexRule.addRule("https://www.washingtonpost.com/technology/.*");
        regexRule.addRule("https://www.washingtonpost.com/business/.*");
        regexRule.addRule("https://www.washingtonpost.com/world/.*");
        regexRule.addRule("https://www.washingtonpost.com/national/.*");
        regexRule.addRule("https://www.washingtonpost.com/sports/.*");
        regexRule.addRule("https://www.washingtonpost.com/local/.*");*/
        regexRule.addRule("https://www.washingtonpost.com/.*");
//        regexRule.addRule("-https://www.washingtonpost.com/politics/.*");
//        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");
//        regexRule.addRule("-https://www.washingtonpost.com/posttv/.*");
//        regexRule.addRule("-https://www.washingtonpost.com/cars/.*");
//        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");

        /*创建一个JdbcTemplate对象,"mysql1"是用户自定义的名称，以后可以通过
         JDBCHelper.getJdbcTemplate("mysql1")来获取这个对象。
         参数分别是：名称、连接URL、用户名、密码、初始化连接数、最大连接数
        
         这里的JdbcTemplate对象自己可以处理连接池，所以爬虫在多线程中，可以共用
         一个JdbcTemplate对象(每个线程中通过JDBCHelper.getJdbcTemplate("名称")
         获取同一个JdbcTemplate对象)             
         */

        try {
/*
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://localhost/readease?useUnicode=true&characterEncoding=utf8",
                    "root", "tiger", 5, 30);
*/
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    "jdbc:mysql://pxc-mysql.inner.youdao.com/readease?useUnicode=true&characterEncoding=utf8",
                    "eadonline4nb", "new1ife4Th1sAugust", 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }
    }

    @Override
    public Links visitAndGetNextLinks(Page page) {
        try {
            BaseExtractor extractor = new WashingtonRSSExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, wordCount, version, mainimage, moreinfo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        p.getTitle(), p.getType(), p.getLabel(), p.getLevel(), p.getStyle(), p.getHost(), p.getUrl(), p.getTime(), p.getDescription(), p.getContent(), p.getWordCount(), p.getVersion(), p.getMainimage(), p.getMoreinfo());
                if (updates == 1) {
                    System.out.println("parser_page插入成功");
                    int id = jdbcTemplate.queryForInt("SELECT id FROM parser_page WHERE url = ?", p.getUrl());

                    updates = jdbcTemplate.update("insert ignore into org_content (id, content) values (?,?)",
                            id, extractor.doc.html());
                    System.out.println("org_content插入成功");
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
        WashingtonRSSCrawler crawler = new WashingtonRSSCrawler("data/washingtonRss");
        crawler.setThreads(2);
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_election-2012", "Politics");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/politics", "Politics");

        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/sports", "Sports");
        RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/world", "Sports");

        if(BaseExtractor.isNormalTime()) {

            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_powerpost", "Politics");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_fact-checker", "Politics");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_the-fix", "Politics");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_monkey-cage", "Politics");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_act-four", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_ask-the-post", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_all-opinions-are-local", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_compost", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_book-party", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_erik-wemple", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_plum-line", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_post-partisan", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_post-everything", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_rampage", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_right-turn", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_in-theory", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_tom-toles", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_the-watch", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_volokh-conspiracy", "Opinion");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/sports/blogs-columns", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_capitals-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_dc-sports-bog", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_fancy-stats", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_early-lead", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_football-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_nationals-journal", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_recruiting-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_soccer-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_terrapins-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "http://feeds.washingtonpost.com/rss/rss_wizards-insider", "Sports");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");
            RSSReaderHelper.addRSSSeeds(crawler, "", "");

        }
//        crawler.addSeed("https://www.washingtonpost.com/world/us-servicemen-become-french-knights/2015/08/24/c4654613
// -f872-48ad-ba81-bb4bf414dd88_story.html?tid=pm_pop_b");

        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);
        requester.setCookie("obuid=d3793881-c05e-4a8c-9a12-c7f4da3f785d; _lvs2=PuyX4roKXd4k97qtPrYtbSYvfhh0MvKEIQw" +
                "+AtC1gqd7j4U5hAyxjxwalj7ctRrHc78XCdnEsS1PsA3mJQMQAEUxTkCNgfOGj0l1hQBjMTQ=; _lvd2=2V7qwNcCmcpY5NjJLU4mq36IdK0HdHNIDZwiM25+UNewKmmdEUIes2ecx34pVsFylkr6GviMeRtrKO24zEhB3MUhDn1ambwFbN8oMTQQlhmZU1GQo64FG47nDaN8UppYlMt+2w7QFqScsUdsMT9M1kYAV0k5DgkHYiPL71enCod6Bo2O9pWiiMcw11NViXIOA1F/A7CmGk2jiLOMso5Q1ToaK72THyYNnIeudr5bY0pwWWAFzDGrKcp5t999GLBdahcSKrjKJ7DPuMaZgZ3N4CLK62FM/XHlIZZg1dugSvH18Q2YkshsJvZAfYv42XJSu9Q2PAPiwJXYNkiybOx8TnQPOPncLvbNHqNWPuCMOp5LZsWmKr26M1+LmDmU1WNC4Q3Uem/ZUE2FxOCBqOBCbaDIHLQeUbaqY2RiBv+B2hlFzV1x7eX7Ig==; _rcc2=1zlrdoE389nkHcrbS7sT3WL8bm7Y1FNotuzTqHMM+orYbfEVbZw+opZoa/YFv1n1pfjr+qK3X5Q=; _fcap_CAM4=AHAAZgBjAGEAcAABABkAAAAAC2I7m1sQAEQ7m85UAKYAAQUOAFEAAELRAFE7m8XKAGQ7m7zIAEI7mwbPADsAAPkYAGQ7m8HBAEI7m8LGAGQAALYiAGQAAXMiAD0AAXcoAFo7m8U3AV07m8O1AFoAAM4vAFEAAR4zADs7m8XoAFoAAOB2AAsAAA62AGQAAR42AAs7m87lAEQ7m8qkAFo7m7FkAGQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=; _ofcap_DOC1=AG8AZgBjAGEAcAABAGQ85JzsAGQ9VmBcAHBDCHsCAGFDCw0AAHlCwj7SAGVCYhNxALhC/G3vAGRDB/AcAGlDEcgNAIRDRaBaAUFDFTAKALRDO0QYAH9C/PvbAGM/a/1DAHo/+LbQAK9ARM5rAKhDKQQZAGpDGYQpAHxAMC0FAGk2UINmAGRBdIJPALdCxgr6AJBDTOhwAGJDOzUFAX9Cymz1AGVCxWWEAGRBjjLMAO5DE7BQAMBAU6MQAQpB062aAGJC4aarAGBDSVsCAGFCi/PGAHlDQWIQAGBDOwdsANtC+bWgAN1DMntpAGBDNXZTAGBC93GQAGFCyLKgAK1C6zmBAGFCqMTEAGBDPSdMATo/5hmXALNDEfRgAF9CpmXUAGBDQQoyALw+Fl9sAGRCoIvbAGA+fz8CAOtDMwdNAI5DM4+6AI5DNpG9AIpDCiuFAIpDARiTAGlCxi1VAGtDJLKyAH4/SpLSAHFDBNydAJBDUADJAI45mFcDAJdDImy8AMFDBK+hAGdDCxqtAF9DIK6HAIpDPzaWAGFC1n59AGRDKOqFAIpDM0aBAGFDI9WXAIpDDUC5AGBDSKz9ARhBueAPAF8+4tNaALA4fzHGAO1BFPLUALdCazqqAH1DI7nnAGlDN1bwAK1CABXMAGlDOdz0AI5CLGf9AGgVs51hAGRC81MgAGVC+D0jAGhDIXPAAGNByEoqAGIxgc5lAGNDM0DZALdC1ls9ALBCy3wgAGBDE6n8AIo8DsnhAF9C2kkoAGNCEgnhAF9DFG/nAGJA3+kqAGRDOH/OALRDTwi0AGNB4p4dAGgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=; _utastes_1=AFUAVABBAFMAVABFAFP/////FwZEAAAGpgAABeYAAAanAA8F5wAEBqoAJQarAAAETAAYBqwABAatAAAHDQAAB28ABgdzAAgH0wAACDQAAAS1ABYFFwAABLcAAAd4AAAHeQAABLoAAAd6AAAFewAAFwAAAAAACJHMAAAAAAAAACjpjwAAAAAAAABTWLoAFgAAAAAAUzw6AAAAAAAAABuicgAAAAAAAAAIlmMAAAAAAAAACJZlAAoAAAAAAAVgKwBHAAAAAABKUSUAAAAAAAAAAADRAAAAAAAAAEn52AAEAAAAAAAC11EAAAAAAAAAUaLCAAAAAAAAAEsgvgAAAAAAAAAZc24AAAAAAAAASfnRACsAAAAAAAivMgAAAAAAAABJ+dMAKQAAAAAASfnSABYAAAAAAAtN1wAAAAAAAABJ+dUAAAAAAAAASfnUABIAAAAAAEn51wAA; recs-8a739a2a99b2b06e269bfae84175f90d=\"0:1088416042,1021615340,1121353341,911246182,1123839471,364092769,-|1\"");

//        requester.setProxy("proxy.corp.youdao.com", 3456, Proxy.Type.SOCKS);
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
        /*

        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
        crawler.setResumable(false);

        crawler.start(1);
    }

}
