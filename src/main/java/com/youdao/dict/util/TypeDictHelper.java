package com.youdao.dict.util;

import com.youdao.dict.souplang.SoupLang;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Administrator on 2015/11/6.
 */
public class TypeDictHelper {
    private static Map<String, String> typeDict = new HashMap<String, String>();
    static {
        Properties dictProp=new Properties();
        try
        {
            dictProp.load(TypeDictHelper.class.getClassLoader().getResourceAsStream("conf/typesDict.properties"));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        Iterator<Map.Entry<Object, Object>> ppsi = dictProp.entrySet().iterator();
        while(ppsi.hasNext())
        {
            Map.Entry<Object,Object> e = ppsi.next();
            String strKey = (String)e.getKey();
            String strValue = (String)e.getValue();
            if(strKey == null || strValue == null || strValue.equals("")) continue;
            String[] words = strValue.split(",");
            for(String s: words){
                typeDict.put(s, strKey);
            }
        }
    }

    public static String getType(String key, String defult){
        String value = typeDict.get(key);
        if(value == null || value.equals("")) return defult;
        return value;
    }
}
