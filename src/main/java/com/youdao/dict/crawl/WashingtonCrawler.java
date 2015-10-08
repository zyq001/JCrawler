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
import com.youdao.dict.util.JDBCHelper;
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
public class WashingtonCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    JdbcTemplate jdbcTemplate = null;

    public WashingtonCrawler(String crawlPath) {
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
        regexRule.addRule("-https://www.washingtonpost.com/politics/.*");
        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");
        regexRule.addRule("-https://www.washingtonpost.com/posttv/.*");
        regexRule.addRule("-https://www.washingtonpost.com/cars/.*");
        regexRule.addRule("-https://www.washingtonpost.com/opinions/.*");

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
            BaseExtractor extractor = new WashingtonExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page_ws (title, type, label, level, style, host, url, time, content, version, mainimage) values (?,?,?,?,?,?,?,?,?,?,?)",
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
        WashingtonCrawler crawler = new WashingtonCrawler("../data/washington");
        crawler.setThreads(10);
        crawler.addSeed("https://www.washingtonpost.com/regional/");
//        crawler.addSeed("https://www.washingtonpost.com/world/us-servicemen-become-french-knights/2015/08/24/c4654613-f872-48ad-ba81-bb4bf414dd88_story.html?tid=pm_pop_b");

        //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
        HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
        requester.setUserAgent("Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0");
        requester.setCookie("JSESSIONID=259E636B04FDAD2701378E3C1CE8BFC8; de=; drawbridge_o=6; drawbridge_test=drawbridge-adb|; client_region=0; rpld1=0:netease.com|20:chn|21:11|22:beijing|23:39.912498|24:116.388802|; rpld0=1:10|2:-|; wapo_login_id=2086968DDD18F77BE050007F01004A10; wapo_secure_login_id=ADOnmPmQWbu4leve8J/12XwOtZbIHHImok+AMGmG3ctgUeeyB0CCsD7yW3pCJ1mNUVh1HJxZXg/iYkM0C9BtfgOPKNYOYK1WmE2vVsNfqoL2z4C4UQdWvjuDjslLJbVxY03dJXrJlCmXDNR0O06OmBtVMp64mejKbXS0NYkLSIdaU685AGi3jdlZt9iE54/TlEaBjrl0R//apUvWw9AIlYdF8Rdmd6bu; wapo_groups=\"games_newsletters-short-default-post_registration-long-jobs_registration-legacy attributes-personal_post_reg-onestep\"; wapo_display=aqia358|https%3A%2F%2Fwpidentity.s3.amazonaws.com%2Fassets%2Fimages%2Favatar-default.png; wpniuser=aqia358@gmail.com; wpnisecure=aee42cb86e6ee028269d59f32657355f; washingtonpost_avatar=https%3A//wpidentity.s3.amazonaws.com/assets/images/avatar-default.png; WPPREF=HP=2:VS=1; wapo_provider=\"Washington Post\"; rplsb=0; wapo_actmgmt=egem:0|v:1|edlt:1|; s_vi=[CS]v1|2AED5CAD8501193F-6000013160007AD5[CE]; s_pers=%20s_vmonthnum%3D1446307200621%2526vn%253D1%7C1446307200621%3B%20s_nr%3D1444268706844-Repeat%7C1446860706844%3B%20s_lv%3D1444268706846%7C1538876706846%3B%20s_lv_s%3DMore%2520than%25207%2520days%7C1444270506846%3B%20s_monthinvisit%3Dtrue%7C1444270506853%3B%20gvp_p5%3Dwp%2520-%2520homepage%7C1444270506857%3B%20gvp_p51%3Dwp%2520-%2520homepage%7C1444270506859%3B; s_sess=%20s_wp_ep%3Dhomepage%3B%20s._ref%3DDirect-Load%3B%20s_cc%3Dtrue%3B%20s_ppvl%3Dwp%252520-%252520homepage%252C13%252C13%252C938%252C1680%252C938%252C1680%252C1050%252C1%252CP%3B%20s_dslv%3DMore%2520than%25207%2520days%3B%20s_sq%3Dwpniwashpostcom%253D%252526pid%25253Dwp%25252520-%25252520homepage%252526pidt%25253D1%252526oid%25253Dhttps%2525253A%2525252F%2525252Fwww.washingtonpost.com%2525252Fnews%2525252Fdc-sports-bog%2525252Fwp%2525252F2015%2525252F10%2525252F07%2525252Fi-play-with-guns-caron-butlers-insid%252526ot%25253DA%3B%20s_ppv%3Dwp%252520-%252520homepage%252C15%252C15%252C938%252C1680%252C938%252C1680%252C1050%252C1%252CP%3B; WPATC=A=1:D=2:C=2:E=BAAIH:P=2:B=1:B=26:B=60:B=101:VS=3; devicetype=0; osfam=0; rplmct=1");

//        requester.setProxy("proxy.corp.youdao.com", 3456, Proxy.Type.SOCKS);
        //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
        /*

        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
        crawler.setResumable(true);

        crawler.start(5000);
    }

}
