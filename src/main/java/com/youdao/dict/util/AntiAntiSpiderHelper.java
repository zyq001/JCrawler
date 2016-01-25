package com.youdao.dict.util;

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
}
