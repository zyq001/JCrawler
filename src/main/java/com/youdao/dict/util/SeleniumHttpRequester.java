package com.youdao.dict.util;

import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.ProxyGenerator;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by zangyq on 2016/1/11.
 */
public class SeleniumHttpRequester extends HttpRequesterImpl {

    public HttpResponse getResponse(String url) throws Exception {

        HttpRequest request = new HttpRequest(url, requestConfig){
            @Override
            public HttpResponse getResponse() throws Exception {

                HtmlUnitDriver driver = new HtmlUnitDriver();

                driver.setJavascriptEnabled(true);

//                try {


                HttpResponse response = new HttpResponse(url);
                int code = -1;
                int maxRedirect = Math.max(0, requestConfig.getMAX_REDIRECT());
                Proxy proxy;
                ProxyGenerator proxyGenerator = requestConfig.getProxyGenerator();
                if (proxyGenerator == null) {
                    proxy = null;
                } else {
                    proxy = proxyGenerator.next(url.toString());
                }

                try {
                    HttpURLConnection con = null;
                    for (int redirect = 0; redirect <= maxRedirect; redirect++) {
                        if (proxy == null) {
                            con = (HttpURLConnection) url.openConnection();
                        } else {
                            con = (HttpURLConnection) url.openConnection(proxy);
                        }

                        requestConfig.config(con);

                        code = con.getResponseCode();
                /*只记录第一次返回的code*/
                        if (redirect == 0) {
                            response.setCode(code);
                        }

                        boolean needBreak = false;
                        switch (code) {
                            case HttpURLConnection.HTTP_MOVED_PERM:
                            case HttpURLConnection.HTTP_MOVED_TEMP:
                                response.setRedirect(true);
                                if (redirect == requestConfig.getMAX_REDIRECT()) {
                                    throw new Exception("redirect to much time");
                                }
                                String location = con.getHeaderField("Location");
                                if (location == null) {
                                    throw new Exception("redirect with no location");
                                }
                                String originUrl = url.toString();
                                url = new URL(url, location);
                                response.setRealUrl(url);
                                LOG.info("redirect from " + originUrl + " to " + url.toString());
                                continue;
                            default:
                                needBreak = true;
                                break;
                        }
                        if (needBreak) {
                            break;
                        }

                    }


                    driver.get(url.toString());

                    InputStream is;

                    is = con.getInputStream();
                    String contentEncoding = con.getContentEncoding();
                    if (contentEncoding != null && contentEncoding.equals("gzip")) {
                        is = new GZIPInputStream(is);
                    }

                    byte[] buf = new byte[2048];
                    int read;
                    int sum = 0;
                    int maxsize = requestConfig.getMAX_RECEIVE_SIZE();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while ((read = is.read(buf)) != -1) {
                        if (maxsize > 0) {
                            sum = sum + read;

                            if (maxsize > 0 && sum > maxsize) {
                                read = maxsize - (sum - read);
                                bos.write(buf, 0, read);
                                break;
                            }
                        }
                        bos.write(buf, 0, read);
                    }

                    is.close();

                    response.setContent(bos.toByteArray());
                    response.setHeaders(con.getHeaderFields());
                    bos.close();
                    if (proxy != null) {
                        proxyGenerator.markGood(proxy, url.toString());
                    }

                    String html = driver.getPageSource();
                    response.setContent(html.getBytes());

                    return response;
                } catch (Exception ex) {
                    if (proxy != null) {
                        proxyGenerator.markBad(proxy, url.toString());
                    }
                    throw ex;
                }
            }
        };
        return request.getResponse();


    }

}
