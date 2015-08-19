package com.youdao.dict.util;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author yinhang 
 */
public class OImageUploader {
    public String product = "dict-consult";
    public int timeout = 500;    

    public OImageUploader() {
    }
    public OImageUploader(String proxyHost, String proxyPort, String p) {
        product = p;
        setProxy(proxyHost, proxyPort);
    }
    public void setProxy (String proxyHost, String proxyPort) {
        System.getProperties().setProperty("http.proxyHost", proxyHost);
        System.getProperties().setProperty("http.proxyPort", proxyPort);
        System.getProperties().put("socksProxySet", true);  
        System.getProperties().setProperty("socksProxyHost", proxyHost);
        System.getProperties().setProperty("socksProxyPort", proxyPort);
    }
    
    protected byte [] data = null;
    protected String img = null;
    public void setUrl (String url) {
        img = url;
    }
    public void setImg (String url) {
        img = url;
    }
    public byte []  getCrawledArray() {
        return data;
    }
    public boolean crawl () throws Exception {
        if (img == null)
            return false;
        URL url = new URL(img);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
        conn.setRequestMethod("GET"); 
        conn.setConnectTimeout(timeout);
        InputStream inStream = conn.getInputStream();
        int shouldlen = conn.getContentLength();
        System.err.println("url = "+img+", shouldlen="+shouldlen);
        data = new byte[shouldlen];
        int readlen = 0;
        while (true) {
            int l1 = inStream.read(data, readlen, shouldlen-readlen);
            if (l1 <= 0)
                break;
            readlen += l1;
        }
        //System.err.println("DEBUG: conn.getContentLength() = "+conn.getContentLength()+", readlen = "+readlen);
        inStream.close();
        return (readlen == shouldlen); //true;

    }
    protected long upload () throws Exception {
        long imgId = 0l;
        if (data == null || img == null)
            return imgId;
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        //String domain="http://oimagea1.ydstatic.com";
        String domain="http://oimagea1.youdao.com";
        config.setServerURL(new URL(domain+"/xmlrpc"));
        config.setEnabledForExtensions(true);//据说没有这句会报错  
        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);
        Object[] params = new Object[] {
            img, data, product
        };
        imgId = (Long) client.execute("oimage.upload", params);
        //System.out.println("img : "+img+", id = "+imgId+", data.length="+data.length);
        return imgId;
    }
    public long deal (String imgurl) throws Exception {
        setImg(imgurl);
        boolean res = crawl();
        if (res == false) {
            System.err.println("OImageUploader::crawl failed");
            return 0l;
        }
        return upload();
    }

    public static void main (String [] args) {
        System.out.println("args.length = "+args.length);
        OImageUploader tool = new OImageUploader(
            args.length>=2?args[1]:"", args.length>=3?args[2]:"", args.length>=1?args[0]:"");
            //"proxy.corp.youdao.com", "8080", args.length>1?args[1]:"");
        InputStream fs = System.in;
        BufferedReader br = new BufferedReader(new InputStreamReader(fs));
        String line = null;
        while(true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                long r = tool.deal(line);
                System.out.println(line+" : "+r);
            } catch(Exception e) {
                System.out.println(line+" : "+e);
            }
        }
 
    }
}
