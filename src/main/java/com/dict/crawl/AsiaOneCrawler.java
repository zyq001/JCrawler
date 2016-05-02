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
import com.dict.util.Configuration;
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
public class AsiaOneCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public AsiaOneCrawler(String crawlPath) {
        super(crawlPath);

        regexRule.addRule("http://www.todayonline.com/.*");
        regexRule.addRule("-.*jpg.*");

        /*创建一个JdbcTemplate对象,"mysql1"是用户自定义的名称，以后可以通过
         JDBCHelper.getJdbcTemplate("mysql1")来获取这个对象。
         参数分别是：名称、连接URL、用户名、密码、初始化连接数、最大连接数
        
         这里的JdbcTemplate对象自己可以处理连接池，所以爬虫在多线程中，可以共用
         一个JdbcTemplate对象(每个线程中通过JDBCHelper.getJdbcTemplate("名称")
         获取同一个JdbcTemplate对象)             
         */

        try {

            Configuration conf = new Configuration();
            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql1",
                    conf.get(Configuration.MYSQL_URL),
                    conf.get(Configuration.MYSQL_USER), conf.get(Configuration.MYSQL_PASSWORD), 5, 30);
        } catch (Exception ex) {
            jdbcTemplate = null;
            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
        }
    }

    @Override
    public Links visitAndGetNextLinks(Page page) {
        try {
            BaseExtractor extractor = new AsiaOneExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, content, version, mainimage) values (?,?,?,?,?,?,?,?,?,?,?)",
                        p.getTitle(),p.getType(),p.getLabel(),p.getLevel(),p.getStyle(),p.getHost(),p.getUrl(),p.getTime(),p.getContent(),p.getVersion(),p.getMainimage());
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

        AsiaOneCrawler crawler = new AsiaOneCrawler("../data/AsiaOne");
        crawler.setThreads(1);
//        crawler.addSeed("http://www.theguardian.com/environment/2015/oct/12/new-ipcc-chief-calls-for-fresh-focus-on-climate-solutions-not-problems");
//        crawler.addSeed("http://www.theguardian.com/australia-news/2015/oct/10/pro-diversity-and-anti-mosque-protesters-in-standoff-in-bendigo-park");
//        crawler.addSeed("http://www.todayonline.com/world/americas/peru-military-fails-act-narco-planes-fly-freely");
        crawler.addSeed("http://news.asiaone.com/lifestyle");
        crawler.addSeed("http://business.asiaone.com/");
        crawler.addSeed("http://news.asiaone.com/news/asia");
//        crawler.addSeed("http://www.theguardian.com/us/culture");
//        crawler.addSeed("http://www.theguardian.com/uk/commentisfree");//opinion
//        crawler.addSeed("http://www.theguardian.com/us/commentisfree");
//        crawler.addSeed("http://www.theguardian.com/uk/business");
//        crawler.addSeed("http://www.theguardian.com/uk/environment");
//        crawler.addSeed("http://www.theguardian.com/uk/technology");//us == uk
//        crawler.addSeed("http://www.theguardian.com/us/business");
//        crawler.addSeed("http://www.theguardian.com/us/environment");
//////
//        crawler.addSeed("http://www.theguardian.com/travel");
//        crawler.addSeed("http://www.theguardian.com/lifeandstyle");
//        crawler.addSeed("http://www.theguardian.com/politics");



        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        AntiAntiSpiderHelper.defaultUserAgent(requester);

        crawler.setResumable(false);

        crawler.start(2);
    }

}
