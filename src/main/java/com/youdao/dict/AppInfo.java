package com.youdao.dict;

import com.google.gson.Gson;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by liuhl on 15-9-8.
 */
@Data
public class AppInfo {
    String appName;
    String os;
    String pkgName;
    String IDS;
    String auid;
    String appVersion;
    String imei;
    String slotId;
    String udid;
    String variantId;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        UserContext userContext = new UserContext();
        userContext.setTime("time");
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName("xxx");
        userContext.setAttrs(appInfo);
        System.out.println(new Gson().toJson(userContext));
        String str = new Gson().toJson(userContext);
        UserContext u = new Gson().fromJson(str, UserContext.class);
        u.getAttrs().getAppName();
        System.out.println(u);
    }
}
@Data
@ToString
class UserContext{
    String time;
    AppInfo attrs;
}
