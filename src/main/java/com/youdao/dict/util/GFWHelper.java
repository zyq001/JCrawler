package com.youdao.dict.util;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zangyq on 2016/2/2.
 */
public class GFWHelper {
    private static BufferedReader gfwBr = null;
    private static BufferedReader cnBr = null;
    private static Set<String> gfw_list = new HashSet<String>();
    private static Set<String> cn_list = new HashSet<String>();

    static {
        try {

//            gfwBr = new BufferedReader(new FileReader(new File("gfw.url_regex.lst")));
            gfwBr = new BufferedReader(new InputStreamReader(GFWHelper.class.getResourceAsStream("/gfw.url_regex.lst")));
            cnBr = new BufferedReader(new InputStreamReader(GFWHelper.class.getResourceAsStream("/cn.url_regex.lst")));
//            cnBr = new BufferedReader(new FileReader(new File("cn.url_regex.lst")));
            String readline = null;
            while((readline = gfwBr.readLine()) != null){
                gfw_list.add(readline);
            }
            while((readline = cnBr.readLine()) != null){
                cn_list.add(readline);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBlocked(String url){
        for(String patten: cn_list){
            if(url.matches(patten))
                return false;
        }
        for(String patten: gfw_list){
            if(url.matches(patten))
                return true;
        }
        return false;
    }

}
