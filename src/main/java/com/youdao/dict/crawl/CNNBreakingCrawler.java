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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.util.JDBCHelper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
public class CNNBreakingCrawler extends DeepCrawler {

    RegexRule regexRule = new RegexRule();

    public static JdbcTemplate jdbcTemplate = null;





    public CNNBreakingCrawler(String crawlPath) {
        super(crawlPath);

        regexRule.addRule(".*cnn.com.*");
        regexRule.addRule("-http://us.cnn.com/video.*");
        regexRule.addRule("-http://arabic.cnn.com.*");
        regexRule.addRule("-http://ac360.blogs.cnn.com.*");
        regexRule.addRule("-http://archives.cnn.com/TRANSCRIPTS.*");
        regexRule.addRule("-http://cnnmoney.trulia.com.*");


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

            BaseExtractor extractor = new CNNExtractor(page);
            if (extractor.extractor() && jdbcTemplate != null) {
                ParserPage p = extractor.getParserPage();
                int updates = jdbcTemplate.update("insert ignore into parser_page (title, type, label, level, style, host, url, time, description, content, version, mainimage) values (?,?,?,?,?,?,?,?,?,?,?,?)",
                        p.getTitle(),"Breaking News",p.getLabel(),p.getLevel(),p.getStyle(),p.getHost(),p.getUrl(),p.getTime(),p.getDescription(),p.getContent(),p.getVersion(),p.getMainimage());
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





        try {
            //1.从StdSchedulerFactory工厂中获取一个任务调度器
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //2. 启动调度器
            scheduler.start();
            System.out.println("scheduler is start...");
            //3. 添加定时任务
            //  3.1 定义job
            JobDetail job = newJob(CNNBreakingNewsJob.class)
                    .withIdentity("jobCNN", "groupCNN")
                    .build();

            //  3.2 定义Trigger，使得job现在就运行，并每隔3s中运行一次，重复运行5次, withRepeatCount(number)设定job运行次数为number+1
            Trigger trigger = newTrigger()
                    .withIdentity("triggerCNN", "groupCNN")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInMinutes(30).repeatForever())//withIntervalInSeconds(3)
//                            .withRepeatCount(4))
                    .build();

            //  3.3 交给scheduler去调度
            scheduler.scheduleJob(job, trigger);

            JobDetail jobJpost = newJob(JpostBreakingNewsJob.class)
                    .withIdentity("jobJpost", "groupJpost")
                    .build();

            //  3.2 定义Trigger，使得job现在就运行，并每隔3s中运行一次，重复运行5次, withRepeatCount(number)设定job运行次数为number+1
            Trigger triggerJpost = newTrigger()
                    .withIdentity("triggerJpost", "groupJpost")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInMinutes(30).repeatForever())//withIntervalInSeconds(3)
//                            .withRepeatCount(4))
                    .build();

            //  3.3 交给scheduler去调度
            scheduler.scheduleJob(jobJpost, triggerJpost);

            JobDetail dictWikiJob = newJob(DictBKWikiCrawler.class)
                    .withIdentity("jobDictWiki", "groupDictWiki")
                    .build();

            //  3.2 定义Trigger，使得job现在就运行，并每隔3s中运行一次，重复运行5次, withRepeatCount(number)设定job运行次数为number+1
            Trigger triggerdictWiki = newTrigger()
                    .withIdentity("triggerDictWiki", "groupDictWiki")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInMinutes(60).repeatForever())//withIntervalInSeconds(3)
//                            .withRepeatCount(4))
                    .build();

            //  3.3 交给scheduler去调度
            scheduler.scheduleJob(dictWikiJob, triggerdictWiki);

            //4. 关闭调度器
            //scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }


    }



    public static class CNNBreakingNewsJob implements Job{

        public void execute(JobExecutionContext context) throws JobExecutionException {
//            System.out.println("execute job at " + new Date() + " by trigger " + context.getTrigger().getJobKey());

             /*构造函数中的string,是爬虫的crawlPath，爬虫的爬取信息都存在crawlPath文件夹中,
          不同的爬虫请使用不同的crawlPath
        */
            CNNBreakingCrawler crawler = new CNNBreakingCrawler("data/cnnBreaking");
            crawler.setThreads(1);

            HttpClient client = new HttpClient();
            HttpMethod method=new GetMethod("http://data.cnn.com/jsonp/breaking_news/domestic.json");
            try {
                client.executeMethod(method);
                String response = method.getResponseBodyAsString();
                if(response != null && !response.equals("")){
                    response = response.substring(response.indexOf("{"));
                    JSONObject ob = new JSONObject(response);
                    String url = ob.getString("linkURL");
                    if(url != null && !url.equals("")){
                        crawler.addSeed(url);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


//        crawler.addSeed("http://us.cnn.com/2015/08/07/us/death-row-stories-ruben-cantu/index.html");

            //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
            HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
            requester.setUserAgent("Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0");
//        requester.setProxy("proxy.corp.youdao.com", 3456, Proxy.Type.SOCKS);
            //单代理 Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0
        /*
blended-wrapper esc-wrapper
        //多代理随机
        RandomProxyGenerator proxyGenerator=new RandomProxyGenerator();
        proxyGenerator.addProxy("127.0.0.1",8080,Proxy.Type.SOCKS);
        requester.setProxyGenerator(proxyGenerator);
        */

        /*设置是否断点爬取*/
            crawler.setResumable(false);

            try {
                crawler.start(1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class JpostBreakingNewsJob implements Job{

        public void execute(JobExecutionContext context) throws JobExecutionException {
//            System.out.println("execute job at " + new Date() + " by trigger " + context.getTrigger().getJobKey());

             /*构造函数中的string,是爬虫的crawlPath，爬虫的爬取信息都存在crawlPath文件夹中,
          不同的爬虫请使用不同的crawlPath
        */
            JpostCrawler crawler = new JpostCrawler("data/JpostBreaking");
            crawler.setThreads(1);
//        crawler.addSeed("http://www.jpost.com/Business-and-Innovation/Sears-to-launch-online-commercial-operations-in-Israel-431751");
            crawler.addSeed("http://www.jpost.com/Headlines");

            //requester是负责发送http请求的插件，可以通过requester中的方法来指定http/socks代理
            HttpRequesterImpl requester = (HttpRequesterImpl) crawler.getHttpRequester();
            requester.setUserAgent("Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0");

            crawler.setResumable(false);

            try {
                crawler.start(2);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
