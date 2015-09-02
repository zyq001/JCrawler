package com.youdao.dict;

import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.crawl.CNNExtractor;
import com.youdao.dict.util.DBClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuhl on 15-8-21.
 */
public class FixCNNContent {

    public static List<ParserPage> list;
    public static AtomicInteger index = new AtomicInteger(0);
    public static int size = 0;



    public static void main(String[] args) {

        list = DBClient.getList();
        size = list.size();
        Runnable r = new ThreadTest();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
        new Thread(r).start();
    }
}

class ThreadTest implements Runnable {

    @Override
    public synchronized void run() {
        for (int i = FixCNNContent.index.getAndIncrement(); i < 100; FixCNNContent.index.getAndIncrement()) {
            ParserPage p = FixCNNContent.list.get(i);
            String url = p.getUrl();
            try {
                if (fix(url)) {
                    System.out.println(FixCNNContent.index.get() + "fix success: id:" + p.getId() + " url:" + url);
                } else
                    System.out.println(FixCNNContent.index.get() + "fix failed:" + url);
            } catch (Exception e) {
                System.out.println(FixCNNContent.index.get() + "fix failed:" + url);
            }
        }
    }

    public static boolean fix(String url) {
        CNNExtractor extractor = new CNNExtractor(url);
        if (extractor.extractor()) {
            ParserPage parserPage = extractor.getParserPage();
            DBClient.update(parserPage);
            return true;
        } else {
            return false;
        }
    }
}