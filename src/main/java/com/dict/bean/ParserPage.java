package com.dict.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by liuhl on 15-7-31.
 */
@ToString
public class ParserPage implements  Serializable {

    @Getter
    @Setter
    private long id = 0;

    @Getter
    private String title = "";

    @Getter
    @Setter
    private String type = "";

    @Getter
    @Setter
    private String label = "";

    @Getter
    @Setter
    private String level = "";

    @Getter
    @Setter
    private String style = "no-image";

    @Getter
    @Setter
    private String host = "";

    @Getter
    @Setter
    private String url = "";

    @Getter
    @Setter
    private String description = "";

    @Getter
    @Setter
    private String content = "";

    @Getter
    @Setter
    private int wordCount = 0;

    @Getter
    @Setter
    private int uniqueWordCount = 0;

    @Getter
    @Setter
    private String time = "";

    @Getter
    @Setter
    private String version = "1";

    @Getter
    @Setter
    private String mainimage = "";

    @Getter
    @Setter
    private int endtime = 100;

    @Getter
    @Setter
    private int page_type = 0;

    @Getter
    @Setter
    private String moreinfo = "";
    @Getter
    @Setter
    private Double avgWordLength = 0.0;

    @Getter
    @Setter
    private Double avgSentLength = 0.0;

    public void setTitle(String title) {
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        title = title.replaceAll("<.*?.>", "");//去除所有标签
        title = title.replaceAll("\\[[^\\]]+\\]", "");//去除[asd]
        this.title = title;
    }



    public static ParserPage getFromMap(Map<String, String> map) {
        ParserPage p = new ParserPage();
        if (map.get("id") != null)
            p.setId(Long.parseLong(map.get("id")));
        if (map.get("title") != null)
            p.setTitle(map.get("title"));
        if (map.get("type") != null)
            p.setType(map.get("type"));
        if (map.get("label") != null)
            p.setLabel(map.get("label"));
        if (map.get("level") != null)
            p.setLevel(map.get("level"));
        if (map.get("host") != null)
            p.setHost(map.get("host"));
        if (map.get("url") != null)
            p.setUrl(map.get("url"));
        if (map.get("content") != null)
            p.setContent(map.get("content"));
        if (map.get("time") != null)
            p.setTime(map.get("time"));
        if (map.get("mainimage") != null)
            p.setMainimage(map.get("mainimage"));
        return p;
    }

    public void insert(){

    }

    public boolean valid(Map<String, String> map) {
        return map.get("title") != null && map.get("content") != null && map.get("mainimage") != null;
    }

    public boolean valid() {
        return StringUtils.isEmpty(title);
    }
}
