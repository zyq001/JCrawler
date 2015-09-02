package com.youdao.dict;

import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.crawl.CNNExtractor;
import com.youdao.dict.util.DBClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by liuhl on 15-8-21.
 */
public class FixCNNContent {

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

        List<ParserPage> list = DBClient.getList();
        int count = 0;
        for (ParserPage p : list) {
            String url = p.getUrl();
            try {
                if (fix(url)) {
                    System.out.println("fix success.........." + url);
                } else
                    System.out.println("fix failed:" + url);
            } catch (Exception e) {
                System.out.println("fix failed:" + url);
            }

        }
    }
}
