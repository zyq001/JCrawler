package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.JsoupUtils;
import com.rometools.rome.feed.synd.SyndEntry;
import com.sun.jna.platform.win32.Sspi;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.score.LeveDis;
import com.youdao.dict.souplang.Context;
import com.youdao.dict.util.GFWHelper;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class BaseExtractor {
    Context context;
    String keywords;
    public static long MINSIZE = 384;
    public ParserPage p = new ParserPage();
    private static String contentChatset = "utf-8";
//    private String
    String url;
    Document doc;
    Element content;
    SyndEntry _rssEntry;
    Page _page;
    List<ParserPage> parserPages = new ArrayList<ParserPage>();

    public String CUENTTIME = new Sspi.TimeStamp().toString();

    static Set<String> normalHour = new HashSet<String>();

    static {
        normalHour.add("06");
        normalHour.add("11");
        normalHour.add("15");
        normalHour.add("20");
    }

    public ParserPage getParserPage() {
        return p;
    }

    public BaseExtractor(){

    }

    public BaseExtractor(Page page) {
        url = page.getUrl();
        this._page = page;

        if (!getDoc(page)) {
            doc = page.getDoc();//瞎猜字符编码，有时候会猜错
        }
        JsoupUtils.makeAbs(doc, url);
        p.setHost(getHost(url));
        p.setUrl(url);
    }

    /**
     * @param LazyLoad 是否加载js
     *
     * */
    public BaseExtractor(Page page, boolean LazyLoad) {
        url = page.getUrl();

        boolean getDocSucc = LazyLoad?getJsLoadedDoc():getDoc(page);

        if (!getDocSucc) {
            doc = page.getDoc();//瞎猜字符编码，有时候会猜错
        }
        JsoupUtils.makeAbs(doc, url);
        p.setHost(getHost(url));
        p.setUrl(url);
    }

    public boolean getJsLoadedDoc(){

        Capabilities caps = new DesiredCapabilities();
        ((DesiredCapabilities) caps).setJavascriptEnabled(true);
        ((DesiredCapabilities) caps).setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19" +
                        " (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
//        ((DesiredCapabilities) caps).setCapability("takesScreenshot", true);
        ((DesiredCapabilities) caps).setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
//                "D:\\Crawl\\phantomjs-2.1.1-windows\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe"
                "/global/exec/zangyq/phantomjs-2.1.1-linux-x86_64/bin/phantomjs"
        );
        WebDriver   driver = new PhantomJSDriver(caps);

//        WebDriver driver = new ChromeDriver();
//        driver.setJavascriptEnabled(true);
        driver.manage().timeouts().pageLoadTimeout(2, TimeUnit.MINUTES);
        driver.get(_page.getUrl());

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String html = driver.getPageSource();
        driver.quit();
//        if (doc == null)
        this.doc = Jsoup.parse(html, url);
        _page.setDoc(this.doc);
        return true;
    }

    public boolean getDoc(Page page) {

        //获取reponse中的charset
        byte[] contentByte = page.getContent();
        String contentType = page.getResponse().getContentType();
        if (contentType != null && contentType.toLowerCase().contains("charset")) {
            String contentTypeLow = contentType.toLowerCase();
            if (!contentTypeLow.contains("utf-8")) {//不是utf8 需要取出来
                int index = contentTypeLow.indexOf("charset=");
                if (index >= 0)
                    contentChatset = contentTypeLow.substring(index + 8);
            }
            try {
                String html = new String(contentByte, contentChatset);
                if (doc == null) this.doc = Jsoup.parse(html, url);
                page.setDoc(this.doc);
                return true;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        log.error("getDoc(page) faile, false");
        return false;
    }

    public BaseExtractor(String url) {
        try {
            Connection conn = Jsoup.connect(url);
            conn.timeout(1000 * 60 * 3);
            this.doc = conn.get();

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.url = url;
        JsoupUtils.makeAbs(doc, url);
        p.setHost(getHost(url));
        p.setUrl(url);
    }

    public static boolean isNormalTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String hour = dateFormat.format(date);
        return normalHour.contains(hour);
    }


    public boolean extractor() {
        if (init())
            return extractorTime() && extractorTitle() && extractorType()
                    && extractorAndUploadImg() && extractorDescription()
                    && extractorContent() && extractorKeywords() && extractorTags(keywords, p.getLabel());
        else {
            log.error("init failed");
            return false;
        }
    }

    public boolean init() {
        return false;
    }

    public boolean extractorTitle() {
        return false;
    }

    public boolean extractorType() {
        return false;
    }

    public boolean extractorTime() {
        return false;
    }

    public boolean extractorKeywords() {
        log.debug("*****extractorKeywords*****");
        Element keywordsElement = (Element) context.output.get("keywords");
        if (keywordsElement == null)
            return true;
        keywords = keywordsElement.attr("content");
        if (keywords == null || "".equals(keywords)) {
            return true;
        }
        if (!keywords.contains(",")) {
            keywords = "".equals(keywords) ? "" : keywords.replaceAll(" ", ",");
        }
        return true;
    }

    public boolean isPaging() {
        return false;
    }

    public boolean extractorAndUploadImg() {
        return extractorAndUploadImg("", "");
    }

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            log.error("content or p null, return false");
            return false;
        }
       /* if (host.equals(port)) return true;*/
        String mainImage = null;
        int width = 0;
        try {
            Elements imgs = content.select("img");
            for (Element img : imgs) {
                String imageUrl = img.attr("src");
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
//                img.attr("style", "width:100%;");
                OImageUploader uploader = new OImageUploader();
                if (!"".equals(host) && !"".equals(port))
                    uploader.setProxy(host, port);
                long id = 0;
                try {
                    id = uploader.deal(imageUrl);
                }catch (Exception e){
                    img.attr("src", imageUrl);
                    log.error("use org img url, continue");
                    continue;
                }                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                int twidth = uploader.getWidth();
                if (twidth >= 300)
                    img.attr("style", "width:100%;");
                img.attr("src", newUrl.toString());
                if (mainImage == null) {
                    width = uploader.getWidth();
                    mainImage = newUrl.toString();
                }
            }


        } catch (Exception e) {
            p.setStyle("no-image");
        }
        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width > 300) {
            p.setStyle("large-image");
        } else {
            p.setStyle("no-image");
        }
        return true;
    }

    public boolean extractorDescription() {
        return true;
    }

    public boolean extractorContent() {
        return extractorContent(false);
    }


    public void HideVideo(String videoSelector) {
        Elements videos = content.select("videoSelector").select("iframe");
        for(Element e: videos){
            String videoUrl = e.attr("src");
            String className = e.className();
            Tag imgTag = Tag.valueOf("p");
//                img.appendChild(imgTag);
            Element newImg = new Element(imgTag, "");
            newImg.attr("class", "iframe");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%; heigh:100%");
            e.appendChild(newImg);
        }
    }

    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
            log.error("extractorContent failed return false");
            return false;
        }
        Elements hypLinks = content.select("a");
        for (Element a : hypLinks) {
            a.unwrap();
//            System.out.println(a);
        }

        removeComments(content);

        String contentHtml = content.html();

        contentHtml = contentHtml.replaceAll("&gt;", ">").replaceAll("&lt;", "<");//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");
// 多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果


        if (contentHtml.length() < 384) {
            log.error("content after extracted too short, false");
            return false;//太短
        }

        p.setContent(contentHtml);
        if (!paging && isPaging()) {
            mergePage(p);
        }
        log.debug("*****extractorContent  success*****");
        return true;
    }

    public void replaceFrame() {
        Elements videos = content.select("iframe");
        for (Element e : videos) {
            String videoUrl = e.attr("src");
            if (GFWHelper.isBlocked(videoUrl))
                continue;
            String className = e.className();
            Tag imgTag = Tag.valueOf("p");
//                img.appendChild(imgTag);
            Element newImg = new Element(imgTag, "");
            newImg.attr("class", "iframe");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%; heigh:100%");
            newImg.attr("heigh", "200");
            e.appendChild(newImg);
        }
    }

    public static void main(String[] args){
        String testContent = "                             <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/7/16/enhanced/webdr11/enhanced-13833-1460059586-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/7/16/enhanced/webdr11/enhanced-13833-1460059586-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"\" />                               Andrew Richard / BuzzFeed                  1. Cracked Out Rice Krispie Treats                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr01/enhanced-6953-1459786194-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr01/enhanced-6953-1459786194-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Cracked Out Rice Krispie Treats\" />                               Teri Lyn Fisher / Spoon Fork Bacon / Via spoonforkbacon.com       <p class=\"sub_buzz_desc_w_attr\">Take your standard puffed rice cereal dessert to the next level. Recipe <a href=\"http://www.spoonforkbacon.com/2011/12/cracked-out-rice-krispies-treats/\">here.</p>               2. Salted Caramel Chocolate Peanut Oat Cookies                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr13/enhanced-13587-1459787481-16.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr13/enhanced-13587-1459787481-16.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Salted Caramel Chocolate Peanut Oat Cookies\" />                               Sarah Coates / The Sugar Hit / Via thesugarhit.com       <p class=\"sub_buzz_desc_w_attr\">Ridiculously decadent, and ridiculously worth it. Recipe <a href=\"http://www.thesugarhit.com/2015/06/worlds-greatest-cookies-aka-salted-caramel-chocolate-peanut-oat-cookies.html\">here.</p>               3. Thin and Crispy Chocolate Chip Cookies                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-18960-1459869163-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-18960-1459869163-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Thin and Crispy Chocolate Chip Cookies\" />                               Tessa Arias / Handle The Heat / Via handletheheat.com       <p class=\"sub_buzz_desc_w_attr\">Melted butter is the key to thin, crispy cookies. Come to think of it, it’s kinda the key to everything. Recipe <a href=\"http://www.handletheheat.com/thin-crispy-chocolate-chip-cookies/\">here.</p>               4. Crunchy Caramel-Covered Pretzels                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-24012-1459871703-11.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-24012-1459871703-11.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Crunchy Caramel-Covered Pretzels\" />                               Stacey / Stop Cook and Listen / Via stopcookandlisten.com       <p class=\"sub_buzz_desc_w_attr\">You will eat every last sweet, salty morsel. Recipe <a href=\"http://www.stopcookandlisten.com/crunchy-caramel-covered-pretzels/\">here.</p>               5. <a href=\"http://www.thesugarhit.com/2015/06/no-bake-smores-chocolate-crackle-cake.html\">No-Bake S’mores Chocolate Crackle Cake                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr05/enhanced-6222-1459786486-2.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr05/enhanced-6222-1459786486-2.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"No-Bake S'mores Chocolate Crackle Cake\" />                               Sarah Coates / The Sugar Hit / Via thesugarhit.com       <p class=\"sub_buzz_desc_w_attr\">No baking? Marshmallow topping? Is this real life?! Recipe <a href=\"http://www.thesugarhit.com/2015/06/no-bake-smores-chocolate-crackle-cake.html\">here.</p>               6. Mini Choco Tacos                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr12/enhanced-24641-1459786001-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr12/enhanced-24641-1459786001-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Mini Choco Tacos\" />                               Jenny Park / Spoon Fork Bacon / Via spoonforkbacon.com       <p class=\"sub_buzz_desc_w_attr\">Treat yourself to this ice cream truck OG. Recipe <a href=\"http://www.spoonforkbacon.com/2015/08/mini-choco-tacos/\">here.</p>               7. Chocolate Shortbread Hearts with Toffee Popcorn                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr01/enhanced-22215-1459893742-1.png\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr01/enhanced-22215-1459893742-1.png\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Chocolate Shortbread Hearts with Toffee Popcorn\" />                               Emily Walton / The Sticky Spatula / Via stickyspatula.com       <p class=\"sub_buzz_desc_w_attr\">Two crunchy delights for the price of one. Recipe <a href=\"http://stickyspatula.com/chocolate-shortbread-hearts-with-toffee-popcorn/\">here.</p>               8. Deerfield Apple Crisp                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr01/enhanced-27669-1459872841-13.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr01/enhanced-27669-1459872841-13.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Deerfield Apple Crisp\" />                               Lu Xia / Super Nummy / Via supernummy.com       <p class=\"sub_buzz_desc_w_attr\">Recipe <a href=\"http://www.supernummy.com/deerfield-apple-crisp/\">here.</p>               9. Double Chocolate Copycat Kit Kat Bars                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/16/enhanced/webdr02/enhanced-5489-1459886647-2.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/16/enhanced/webdr02/enhanced-5489-1459886647-2.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Double Chocolate Copycat Kit Kat Bars\" />                               Toni Dash / Boulder Locavore / Via boulderlocavore.com       <p class=\"sub_buzz_desc_w_attr\">You melt some chocolate, you coat some wafer cookies, and boom!—you’re in heaven. Recipe <a href=\"http://boulderlocavore.com/double-chocolate-copycat-kit-kat-bars-gluten-free-recipe/\">here.</p>               10. Galaktoboureko Crumble                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr10/enhanced-5842-1459872975-7.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr10/enhanced-5842-1459872975-7.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Galaktoboureko Crumble\" />                               Katerina Delidimou / Culinary Flavors / Via culinaryflavors.gr       <p class=\"sub_buzz_desc_w_attr\">A Greek treat with depth, history, and, so. much. crunch. Recipe <a href=\"http://culinaryflavors.gr/2016/02/galaktoboureko-crumble/\">here.</p>               11. Marshmallow Cereal Treats                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr04/enhanced-10372-1459786423-8.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr04/enhanced-10372-1459786423-8.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Marshmallow Cereal Treats\" />                               Jenny Park / Spoon Fork Bacon / Via spoonforkbacon.com       <p class=\"sub_buzz_desc_w_attr\">Your inner kid has got to eat too. Recipe <a href=\"http://www.spoonforkbacon.com/2015/02/marshmallow-cereal-treats/\">here.</p>               12. Easy Salted Almond Brittle                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr09/enhanced-19062-1459787892-4.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/4/12/enhanced/webdr09/enhanced-19062-1459787892-4.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Easy Salted Almond Brittle\" />                               Christina Lane / Dessert For Two / Via dessertfortwo.com       <p class=\"sub_buzz_desc_w_attr\">Basically gourmet candy you can make in your dang microwave. Recipe <a href=\"http://www.dessertfortwo.com/salted-almond-brittle-easy/\">here.</p>               13. Mocha Chip Biscotti                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-18804-1459868599-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr02/enhanced-18804-1459868599-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Mocha Chip Biscotti\" />                               Sally / Sally’s Baking Addiction / Via sallysbakingaddiction.com       <p class=\"sub_buzz_desc_w_attr\">The perfectly crispy choice for dessert + coffee. Recipe <a href=\"http://sallysbakingaddiction.com/2015/02/27/mocha-chip-biscotti/\">here.</p>               14. Strawberry Crumble                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr10/enhanced-26635-1459868845-9.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr10/enhanced-26635-1459868845-9.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Strawberry Crumble\" />                               Katerina Petrovska / Diethood / Via diethood.com       <p class=\"sub_buzz_desc_w_attr\">You got oats, you got fruit; yeah, this is relatively healthy, right? Recipe <a href=\"http://diethood.com/strawberry-crumble/\">here.</p>               15. Napoleons                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr14/enhanced-16787-1459872495-6.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr14/enhanced-16787-1459872495-6.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Napoleons\" />                               Katie Mazur / Baking My Way / Via bakingmywaythroughgermany.com       <p class=\"sub_buzz_desc_w_attr\">Actually way easier than they look, promise. Recipe <a href=\"http://www.bakingmywaythroughgermany.com/2016/01/napoleons-mille-feuille/\">here.</p>               16. Snickers Cheesecake Bars                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr01/enhanced-17660-1459869314-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr01/enhanced-17660-1459869314-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Snickers Cheesecake Bars\" />                               Samantha / Five Heart Home / Via fivehearthome.com       <p class=\"sub_buzz_desc_w_attr\">Nbd, you just make your own Snickers now. Recipe <a href=\"http://www.fivehearthome.com/2015/08/02/snickers-cheesecake-bars/\">here.</p>               17. Yogurt and Apricot Pie with Crunchy Oatmeal Crust                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr07/enhanced-26910-1459871854-8.png\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr07/enhanced-26910-1459871854-8.png\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Yogurt and Apricot Pie with Crunchy Oatmeal Crust\" />                               Yunhee Kim / Food And Wine / Via foodandwine.com       <p class=\"sub_buzz_desc_w_attr\">Like even-more-deconstructed parfait, and it’s awesome. Recipe <a href=\"http://www.foodandwine.com/recipes/yogurt-and-apricot-pie-with-crunchy-granola-crust\">here.</p>               18. Sylvanas                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/15/enhanced/webdr15/enhanced-11178-1459884149-2.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/15/enhanced/webdr15/enhanced-11178-1459884149-2.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Sylvanas\" />                               Bebs / Foxy Folksy / Via foxyfolksy.com       <p class=\"sub_buzz_desc_w_attr\">A Filipino recipe for cookie sandwiches made of cashew meringue-filled wafers covered with buttercream and dusted with cake crumbs. Oh, I’m sorry, was I drooling? Recipe <a href=\"http://www.foxyfolksy.com/sylvanas-recipe/\">here.</p>               19. Old-Fashioned Oatmeal Cupcakes                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr10/enhanced-5663-1459872241-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr10/enhanced-5663-1459872241-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Old-Fashioned Oatmeal Cupcakes\" />                               Brittany Mullins / Eating Bird Food / Via eatingbirdfood.com       <p class=\"sub_buzz_desc_w_attr\">Oatmeal cupcakes with a crunchy coconut, pecan, and brown sugar topping; basically Samoa cupcakes. Recipe <a href=\"http://www.eatingbirdfood.com/old-fashioned-oatmeal-cupcakes/\">here.</p>               20. Fried Ice Cream                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr12/enhanced-30477-1459868992-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr12/enhanced-30477-1459868992-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Fried Ice Cream\" />                               Miriam Pascal / Overtime Cook / Via overtimecook.com       <p class=\"sub_buzz_desc_w_attr\">Honestly one of the coolest things you will ever cook. Recipe <a href=\"http://overtimecook.com/2013/11/25/fried-ice-cream/\">here.</p>               21. Thin Mint Puppy Chow                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr01/enhanced-26873-1459872164-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr01/enhanced-26873-1459872164-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Thin Mint Puppy Chow\" />                               Lizzy Mae Early / Your Cup Of Cake / Via yourcupofcake.com       <p class=\"sub_buzz_desc_w_attr\">So incredibly easy to make, and perfect for parties. Recipe <a href=\"http://www.yourcupofcake.com/2013/02/thin-mint-puppy-chow.html\">here.</p>               22. Superfood Dark Chocolate Quinoa Bark                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr04/enhanced-20787-1459872038-1.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/12/enhanced/webdr04/enhanced-20787-1459872038-1.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Superfood Dark Chocolate Quinoa Bark\" />                               Bailey Sissom / Simply Sissom / Via simplysissom.com       <p class=\"sub_buzz_desc_w_attr\">Dessert that will make you feel so rad. <img class=\"twitter-emoji\" src=\"https://twemoji.maxcdn.com/36x36/1f44d.png\" alt=\"&amp;#x1f44d;\" title=\"thumbs up sign\" aria-label=\"Emoji: thumbs up sign\" /> Recipe <a href=\"http://www.simplysissom.com/superfooddarkchocolatequinoabark/\">here.</p>               23. Red Velvet Cr&egrave;me Brul&eacute;e Brownies                      <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr15/grid-cell-8736-1459895476-5.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr15/grid-cell-8736-1459895476-5.jpg\" />                                           <p class=\"sub_buzz_grid_source_via\">Katalina / Peas &amp; Peonies / Via peasandpeonies.com</p>                             <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr15/grid-cell-8736-1459895478-10.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/18/enhanced/webdr15/grid-cell-8736-1459895478-10.jpg\" />                                           <p class=\"sub_buzz_grid_source_via\">Katalina / Peas &amp; Peonies / Via peasandpeonies.com</p>                                                  &nbsp;            <p class=\"sub_buzz_desc_w_attr\">Because you deserve only the fanciest in life. Recipe <a href=\"http://peasandpeonies.com/2016/01/red-velvet-creme-brulee-brownies/\">here.</p>                   24. Chocolate Hazelnut Cannoli                        <img src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr01/enhanced-16794-1459868773-2.jpg\" rel:bf_image_src=\"https://img.buzzfeed.com/buzzfeed-static/static/2016-04/5/11/enhanced/webdr01/enhanced-16794-1459868773-2.jpg\" class=\"bf_dom\" rel:bf_bucket=\"progload\" alt=\"Chocolate Hazelnut Cannoli\" />                               Sabrina / Cooking At Sabrina’s / Via cookingatsabrinas.com       <p class=\"sub_buzz_desc_w_attr\">Bellissimo! <img class=\"twitter-emoji\" src=\"https://twemoji.maxcdn.com/36x36/1f48b.png\" alt=\"&amp;#x1f48b;\" title=\"kiss mark\" aria-label=\"Emoji: kiss mark\" /><img class=\"twitter-emoji\" src=\"https://twemoji.maxcdn.com/36x36/1f44c.png\" alt=\"&amp;#x1f44c;\" title=\"ok hand sign\" aria-label=\"Emoji: ok hand sign\" /> Recipe <a href=\"https://cookingatsabrinas.com/2015/11/25/chocolate-hazelnut-cannoli/\">here.</p>       ";

        new BaseExtractor().resumeFrame(testContent);
    }

    public void HideSomeHypLink(Element _content) {
        Elements hypLinks = _content.select("a");
        for (Element a : hypLinks) {
//            if(a.text().toLowerCase().matches("(.*[^\\\\w])?(more|here)+(s|ment|\\\\'s|ies|es|ing|ship|ion|e)?([^\\\\w].*)?"))
            if(a.text().toLowerCase().contains("more") || a.text().toLowerCase().contains("here")) {
                continue;
            }
            a.unwrap();
//            System.out.println(a);
        }
    }

    public String resumeFrame(String orgContent) {
//        Document extractedContent = Jsoup.parseBodyFragment(orgContent);
        Document extractedContent = Jsoup.parse(orgContent);
//        for(String className: classNames){
        Elements videoClassNames = extractedContent.select(".iframe");
        for (Element e : videoClassNames) {
            String videoUrl = e.attr("src");
            Tag imgTag = Tag.valueOf("iframe");
//                img.appendChild(imgTag);
            Element newImg = new Element(imgTag, "");
            newImg.attr("src", videoUrl);
            newImg.attr("style", "width:100%");
//                newImg.a
            e.appendChild(newImg);
            e.unwrap();
        }
        HideSomeHypLink(extractedContent);//逻辑有点乱，先这样吧，有机会再重构
        return extractedContent.body().html();
    }

    public static void removeComments(Node node) {
        for (int i = 0; i < node.childNodes().size(); ) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }

    public void mergePage(ParserPage p) {
    }

    public boolean extractorTags(String... keywords) {
        log.debug("*****extractorTags*****");
        if (content == null) {
            log.error("*****extractorTags  failed***** url:" + url);
            return false;
        }
        try {
            String contentStr = content.text();
            LeveDis leveDis = LeveDis.getInstance("");
            String tags = leveDis.tag(contentStr, 5);
            if (keywords != null) {
                for (String key : keywords) {
                    if ("".equals(key) || key == null) {
                        continue;
                    }
                    if (!"".equals(tags) && !tags.contains(key)) {
                        tags = key + "," + tags;
                    } else {
                        tags = key;
                    }
                }
            }
            p.setLabel(tags);
            int level = leveDis.compFileLevel(leveDis.compLevel(contentStr));
            p.setLevel(String.valueOf(level));
            log.debug("*****extractorTags  success*****");
            return true;
        } catch (Exception e) {
            log.error("*****extractorTags  failed***** url:" + url);
            return false;
        }
    }

    public static String getHost(String url) {
        if (url == null || url.trim().equals("")) {
            return "";
        }
        String host = "";
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            host = matcher.group();
        }
        return host;
    }

    public List<ParserPage> getParserPageList() {
        return parserPages;
    }
}
