package com.youdao.dict.util;

import lombok.Getter;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author yinhang
 */
public class OImageUploader {
    public String product = "dict-consult";
    public int timeout = 2 * 6000;
    public int readTimeout = 4 * 60 * 1000;
    @Getter
    private int width;
    @Getter
    private int height;

    public OImageUploader() {
    }

    public OImageUploader(String proxyHost, String proxyPort, String p) {
        product = p;
        setProxy(proxyHost, proxyPort);
    }

    public void setProxy(String proxyHost, String proxyPort) {
        System.getProperties().setProperty("http.proxyHost", proxyHost);
        System.getProperties().setProperty("http.proxyPort", proxyPort);
        System.getProperties().put("socksProxySet", true);
        System.getProperties().setProperty("socksProxyHost", proxyHost);
        System.getProperties().setProperty("socksProxyPort", proxyPort);
    }

    protected byte[] data = null;
    protected String img = null;

    public void setUrl(String url) {
        img = url;
    }

    public void setImg(String url) {
        img = url;
    }

    public byte[] getCrawledArray() {
        return data;
    }

    public boolean crawl() throws Exception {
        if (img == null)
            return false;
        URL url = new URL(img);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Encoding", "identity");
        conn.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0");
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(readTimeout);
        conn.connect();
        InputStream inStream = conn.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        data = outStream.toByteArray();//图片的二进制数据
        inStream.close();
        BufferedImage sourceImg = ImageIO.read(new ByteArrayInputStream(data));
//        BufferedImage sourceImg = ImageIO.read(inStream);
        width = sourceImg.getWidth();
        height = sourceImg.getHeight();
//        return (readlen == shouldlen); //true;
        return true;
    }

    protected long upload() throws Exception {
        long imgId = 0l;
        if (data == null || img == null)
            return imgId;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        //String domain="http://oimagea1.ydstatic.com";
        String domain = "http://oimagea1.youdao.com";
        config.setServerURL(new URL(domain + "/xmlrpc"));
        config.setEnabledForExtensions(true);//据说没有这句会报错  
        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);
        Object[] params = new Object[]{
                img, data, product
        };
        imgId = (Long) client.execute("oimage.upload", params);
        //System.out.println("img : "+img+", id = "+imgId+", data.length="+data.length);
        return imgId;
    }

    public long deal(String imgurl) throws Exception {
        setImg(imgurl);
        boolean res = crawl();
        if (res == false) {
            System.err.println("OImageUploader::crawl failed");
            return 0l;
        }
        return upload();
    }

    public static void main(String[] args) {
        System.out.println("args.length = " + args.length);
        OImageUploader tool = new OImageUploader(
                args.length >= 2 ? args[1] : "", args.length >= 3 ? args[2] : "", args.length >= 1 ? args[0] : "");
        //"proxy.corp.youdao.com", "8080", args.length>1?args[1]:"");
        InputStream fs = System.in;
        BufferedReader br = new BufferedReader(new InputStreamReader(fs));
        String line = null;
        while (true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                long r = tool.deal(line);
                System.out.println(line + " : " + r);
            } catch (Exception e) {
                System.out.println(line + " : " + e);
            }
        }

    }
}
