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
import cn.edu.hfut.dmic.webcollector.net.RequestConfig;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.JDBCHelper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

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
public class HackerNewsCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();
    SoupLang soupLang;

    JdbcTemplate jdbcTemplate = null;

    public HackerNewsCrawler(String crawlPath) throws IOException, SAXException, ParserConfigurationException {
        super(crawlPath);

        soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("HackerNewsRule.xml"));

        regexRule.addRule("https://news.ycombinator.com/news.*");
        regexRule.addRule("-.*jpg.*");


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
            Context context = soupLang.extract(page.getDoc());
            Element table = (Element) context.output.get("table");
            if (table != null) {
                Elements elements = table.select("td[class=title]");
                for (int i = 1; i < elements.size(); i += 2) {
                    Elements hostElement = elements.get(i).select("span[class=sitebit comhead]");
                    Elements titleElement = elements.get(i).select("a");
                    if (hostElement == null || titleElement == null) {
                        continue;
                    }
                    String title = titleElement.text();
                    String url = titleElement.attr("href");
                    String host = hostElement.text();
                    host = host.replace("(", "").replace(")", "");
                    System.out.println(title + "," + host + "," + url);
                    ParserPage p = new ParserPage();
                    p.setUrl(url);
                    p.setHost(host);
                    p.setTitle(title);
                    p.setType("Technology");
                    int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, content, version, mainimage) values (?,?,?,?,?,?,?,?,?,?,?)",
                            p.getTitle(), p.getType(), p.getLabel(), p.getLevel(), p.getStyle(), p.getHost(), p.getUrl(), p.getTime(), p.getContent(), p.getVersion(), p.getMainimage());
                    if (updates == 1) {
                        System.out.println("mysql插入成功");
                    }
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
        HackerNewsCrawler crawler = new HackerNewsCrawler("../data/hn");
        crawler.setThreads(50);
        crawler.addSeed("https://news.ycombinator.com/news");
        crawler.setResumable(false);


        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        requester.setUserAgent("Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0");
        RequestConfig config = requester.getRequestConfig();
        config.setTimeoutForConnect(60000);
        config.setTimeoutForRead(60000);
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
//        requester.setProxy("proxy.corp.youdao.com", 3456, Proxy.Type.SOCKS);
        /*

        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
//        crawler.setResumable(true);
        crawler.setResumable(false);

        crawler.start(7);
    }

}
