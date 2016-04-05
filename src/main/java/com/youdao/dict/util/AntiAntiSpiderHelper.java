package com.youdao.dict.util;

import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import org.eclipse.jetty.util.log.Log;

/**
 * Created by zangyq on 2016/1/25.
 */
public class AntiAntiSpiderHelper {

    public static void crawlinterval(int seconds){
        try {
            System.out.println("sleep " + seconds + "seconds for anti-spider");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void defaultUserAgent(HttpRequesterImpl requester) {
        //Android 4.1 chrome默认UserAgent
        requester.setUserAgent("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19" +
                " (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
    }
}
