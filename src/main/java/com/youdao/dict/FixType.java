package com.youdao.dict;

import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.util.DBClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liuhl on 15-8-21.
 */
public class FixType {
    public static void fixType() {
        Map<String, String> map = DBClient.getChannelMapping();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            DBClient.updateType(entry.getKey(), entry.getValue());
        }

    }

    public static void main(String[] args) {
        FixType.fixType();
    }
}
