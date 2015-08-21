package com.youdao.dict;

import com.sun.jna.platform.win32.Sspi;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.util.DBClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by liuhl on 15-8-21.
 */
public class FixTime {
    public static void main(String[] args) {
        List<ParserPage> list = DBClient.getList();
        int count = 0;
        for (ParserPage p : list) {
            String url = p.getUrl();
            String[] array = url.split("/");
            if (array.length >= 7) {
                try {
                    String time = array[4] + "-" + array[5];
                    DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = format1.parse(time);
                    p.setTime(String.valueOf(time));
                    DBClient.updateTime(p);
                } catch (ParseException e) {
                    count++;
                    System.out.println(count + ": not update:" + p.getId());
                }
            }
        }
    }
}
