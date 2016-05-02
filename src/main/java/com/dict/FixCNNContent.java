package com.dict;

import com.dict.crawl.CNNExtractor;
import com.dict.util.DBClient;
import com.dict.bean.ParserPage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuhl on 15-8-21.
 */
public class FixCNNContent {

    public static List<ParserPage> list;
    public static AtomicInteger index = new AtomicInteger(0);
    public static int size = 0;

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

    public static void main(String[] args) {
//        FixCNNContent.fix("http://us.cnn.com/2015/08/07/us/death-row-stories-ruben-cantu/index.html");
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
        for (int i = FixCNNContent.index.getAndIncrement(); i < FixCNNContent.size; i = FixCNNContent.index.getAndIncrement()) {
            ParserPage p = FixCNNContent.list.get(i);
            String url = p.getUrl();
            try {
                if (fix(p)) {
                    System.out.println(i + "fix success: id:" + p.getId() + " url:" + url);
                } else
                    System.out.println(i + "fix failed:" + url);
            } catch (Exception e) {
                System.out.println(i + "fix failed:" + url);
            }
        }
    }

    public static boolean fix(ParserPage p) {
        String url = p.getUrl();
        CNNExtractor extractor = new CNNExtractor(url);
        if (extractor.extractor()) {
            ParserPage parserPage = extractor.getParserPage();
            parserPage.setId(p.getId());
            DBClient.update(parserPage);
            return true;
        } else {
            return false;
        }
    }
}