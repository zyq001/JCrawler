package com.dict.util;

import lombok.extern.apachecommons.CommonsLog;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zyq on 2016/4/26.
 */
@CommonsLog
public class Configuration {

    public static String CRAWL_PATH = "crawPath";
    public static String MYSQL_URL = "jdbcUrl";
    public static String MYSQL_USER = "jdbcUser";
    public static String MYSQL_PASSWORD = "jdbcPasswd";
    private Map<String, String> map = new ConcurrentHashMap<String, String>();

    public Configuration(String propertiesFileName, String crawlPath){
        map.put(CRAWL_PATH, crawlPath);
        loadProperties(propertiesFileName);
    }

    public Configuration(){
        loadProperties("conf/remote.properties");
    }

    public Configuration(String propertiesFileName){
        loadProperties(propertiesFileName);
    }

    public String get(String key){
        if(map.containsKey(key)){
            return map.get(key);
        }
        log.error("get conf value error, don't contains key : " + key);
        return "";
    }


    public void loadProperties(String propFile){
        Properties prop = new Properties();

        try {
            prop.load(Configuration.class.getClassLoader().getResourceAsStream(propFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry entry: prop.entrySet()){
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            map.put(key, value);
        }
    }

}
