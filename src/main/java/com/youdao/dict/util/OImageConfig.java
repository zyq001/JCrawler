package com.youdao.dict.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuhl on 15-8-5.
 */
public class OImageConfig {


    private static final String URL_YODAO = ".youdao.com";  //oimage域名

    private static final String URL_CDN = ".ydstatic.com";  //cdn 域名

    /**
     * 只允许包内部访问<br>
     * <a href="https://dev.corp.youdao.com/outfoxwiki/OImage">OImage WIKI</a>
     */
    public OImageConfig() {
        /*
         * init server URLs, currently a1-a8, b1-b8 and c1-c8
         */
        List<URL> urls = new LinkedList<URL>();
        final String urlFormat = "http://oimage%s%d.youdao.com/xmlrpc";
        for (int i = 1; i <= 8; i++) {
            addServerUrl(urls, String.format(urlFormat, "a", i));
            addServerUrl(urls, String.format(urlFormat, "b", i));
            addServerUrl(urls, String.format(urlFormat, "c", i));
        }
        serverUrls = urls.toArray(new URL[urls.size()]);
    }

    /**
     * 取得所有可用的服务URL
     * <p/>
     * 以后可以考虑扩展成定时更新
     *
     * @return 所有可用的服务URL
     */
    public URL[] getServerUrls() {
        return serverUrls;
    }

    /**
     * 根据图片ID取得其对应的OImage URL
     *
     * @param imageId     图片ID
     * @param productName 产品名称
     * @return 图片ID对应的OImage URL
     */
    public URL getImageSrc(long imageId, String productName) {
//        Validate.notNull(productName);
        URL randomUrl = serverUrls[RandomUtils.nextInt(0, serverUrls.length)];
        // randomUrl.get
        // e.g. http://oimagea1.youdao.com/image?id=-3239098648288039279
        String imageSrc = String.format("%s://%s/image?id=%d&product=%s",
                randomUrl.getProtocol(), randomUrl.getHost().replaceAll(URL_YODAO, URL_CDN), imageId,
                productName);

        try {
            return new URL(imageSrc);
        } catch (MalformedURLException e) {
            LOG.error("Failed to construct image source: " + imageSrc, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "OImageConfig [serverUrls=" + Arrays.toString(serverUrls) + "]";
    }

    private void addServerUrl(List<URL> urls, String url) {
        try {
            urls.add(new URL(url));
            LOG.debug("Add OImage server URL: " + url);
        } catch (Exception e) {
            LOG.warn("Failed to add OImage server URL: " + url);
        }
    }

    private URL[] serverUrls;

    private static final Log LOG = LogFactory.getLog(OImageConfig.class);
}
