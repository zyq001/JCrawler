package com.dict.util;

import com.google.gson.Gson;

import java.util.*;

/**
 * Created by Administrator on 2015/11/6.
 */
public class TypeDictHelper {
    private static Map<String, String> typeDict = new HashMap<String, String>();
    private static Map<String, String> typeRegDict = new HashMap<String, String>();
    private static Set<String> typeSet = new HashSet<String>();

    private static void loadProperties(String filePath, Map<String, String> map) {
        Properties dictProp = new Properties();
        try {
            dictProp.load(TypeDictHelper.class.getClassLoader().getResourceAsStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Iterator<Map.Entry<Object, Object>> ppsi = dictProp.entrySet().iterator();
        while (ppsi.hasNext()) {
            Map.Entry<Object, Object> e = ppsi.next();
            String strKey = (String) e.getKey();
            typeSet.add(strKey);
            String strValue = (String) e.getValue();
            if (strKey == null || strValue == null || strValue.equals("")) continue;
            String[] words = strValue.split(",");
            for (String s : words) {
                map.put(s.toLowerCase(), strKey);
            }
        }
    }

    static {

        loadProperties("conf/typesDict.properties", typeDict);
        loadProperties("conf/typesDictReg.properties", typeRegDict);

    }

    public static boolean rightTheType(String orgType) {
        if (orgType == null || orgType.equals("")) return false;
        return typeSet.contains(orgType);
    }

    public static String getType(String key, String defult) {
        if (typeDict.containsValue(key)) return key;//正好是app频道之一。返回

        String lkey = key.toLowerCase();

        String value = typeDict.get(lkey);
        if (value != null && !value.equals(key)) return value;

        for(String type: typeRegDict.keySet()){
            if(lkey.matches(type)){
                return typeRegDict.get(type);
            }
        }

        //拆开来匹配
        for (String s : lkey.split(" ")) {
            value = typeDict.get(s);
            if (value != null && !value.equals(s)) return value;
        }
        return defult;
    }

    public static String getMoreInfo(String key) {
        if (!rightTheType(key)) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("orgType", key);
            String moreinfo = new Gson().toJson(map);
            return moreinfo;
        }
        return "";
    }

    public static void updateType() {

    }

    public static void main(String[] args) {

        System.out.println("arts".matches(".*[(art)|(fff)]+.*"));
        for(String underCheck: typeDict.keySet()){
            String type = typeDict.get(underCheck);
            boolean succ = false;
            for(String reg: typeRegDict.keySet()){
                if(underCheck.matches(reg)){
                    succ = true;
                    System.out.println(succ + "underCheck:" + underCheck + "  type:" + type + "  reg:" + reg);

                }
            }
            if(!succ)
                System.out.println(succ + "underCheck:" + underCheck + "  type:" + type);
//            if(!underCheck.matches(typeRegDict.get(type))){
//                System.out.println("underCheck:" + underCheck + "  type:" + type);
//            }
//            if(typeDict.get(underCheck).equals())
        }
    }
}
