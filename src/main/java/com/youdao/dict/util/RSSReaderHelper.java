package com.youdao.dict.util;

import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.apachecommons.CommonsLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zangyq on 2016/4/15.
 */
@CommonsLog
public class RSSReaderHelper {
    private static Map<String, SyndEntry> url2SyndEntry = new ConcurrentHashMap<String, SyndEntry>();
    private static Map<String, String> url2Type = new ConcurrentHashMap<String, String>();

    public static SyndEntry getSyndEntry(String url){
        return url2SyndEntry.get(url);
    }

    public static String getType(String url){
        return url2Type.get(url);
    }

    public static void addRSSSeeds(Crawler crawler, String rssAddr, String type) {

        List<String> links = new ArrayList<String>(32);

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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(feed == null || feed.getEntries().size() < 1){
            log.info("get entries failed, feed: " + rssAddr);
            return;
        }

        List<SyndEntry> entries = feed.getEntries();
        for (SyndEntry entry : entries) {
            url2SyndEntry.put(entry.getLink(), entry);
            url2Type.put(entry.getLink(), type);

            crawler.addSeed(entry.getLink());
        }
//        return links;
    }
}
