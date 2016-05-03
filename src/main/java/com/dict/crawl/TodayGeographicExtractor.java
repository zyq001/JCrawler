package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.bean.ParserPage;
import com.dict.souplang.SoupLang;
import com.dict.util.AntiAntiSpiderHelper;
import com.dict.util.TypeDictHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

//import com.dict.util.OImageUploader;
//import com.dict.util.OImageConfig;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class TodayGeographicExtractor extends BaseExtractor {

    public TodayGeographicExtractor(Page page) {
        super(page);
    }

    public TodayGeographicExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            AntiAntiSpiderHelper.crawlinterval(new Random().nextInt(20));
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("TodayGeographicRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");

//            if(url.contains("photo-of-the-day")){
//                System.out.println("photo of the day skip");
//                return  false;
//            }
            Element article = (Element) context.output.get("isarticle");
            if(article == null || article.toString().contains("article")){

                content.select(".hidden").remove();//
                content.select(".content_mainB").remove();
                content.select(".header").remove();
                content.select(".clearfix").remove();

                content.select(".ys-collection-splash").remove();//your shot gall
                content.select(".ys-carousel-header").remove();//1/14索引
                content.select(".js-carousel-toggle-caption").remove();//more less
                content.select(".ys-carousel-control").remove();//左右下一张图 图标
                content.select(".aside").remove();//左边分享图标

                content.select(".social_buttons").remove();//文章末尾分享图标

                content.select(".primary").remove();//related
                content.select(".secondary").remove();//secondary

                content.select(".box-share").remove();//
                content.select(".footer").remove();

                content.select(".popup").remove();//网站链接

                content.select(".loading-container").remove();//loading photos

                content.select(".interstitial").remove();//

                content.select(".wallpaper_link").remove();//

                content.select(".p1").remove();//buy the print

                content.select(".download_link").remove();//
                content.select(".nav").remove();//
                content.select(".buy_link").remove();

                content.select(".social_buttons").remove();//
                content.select("div[id=livefyre]").remove();//
                content.select(".powered_by_livefyre").remove();

                Elements imgli = content.select(".thumb");
                if(imgli != null)
                    for(Element e: imgli){
                        e.select("img").remove();
                        Tag imgTag = Tag.valueOf("img");
    //                img.appendChild(imgTag);
                        Element newImg = new Element(imgTag, "");
                        newImg.attr("src", e.attr("href"));
                        e.appendChild(newImg);
                    }

                Elements youphotos = content.select(".ys-collection-iframe");
                if(youphotos != null && youphotos.size() > 0){
                    System.out.println("+++++++++++++++++++++++++++++has youshot dont crawl temply ");
                    return false;
                }

                log.debug("*****init  success*****");
                return true;
            }
            log.info("*****init  failed，isn't an article***** url:" + url);
            return false;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
            return false;
        }
    }


    public boolean extractorContent() {
        return extractorContent(false);
    }

    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
            log.debug("*****extractorContent failed,return false*****");
            return false;
        }



        Elements hypLinks = content.select("a");
        for(Element a: hypLinks){
            a.unwrap();
//            System.out.println(a);
        }
        Elements extnlinks = content.select("p");
        for(Element e: extnlinks){
            Elements a = e.select("a");
            for(Element aa: a){
                if(a.html().contains("Buy the book") || a.html().contains("Watch a video of") || a.html().contains("See more pictures in") || a.html().contains(">>")){
                    e.remove();
                    break;
                }
            }
        }

        content.select("img").wrap("<p></p>");

        String contentHtml = content.html();

        contentHtml = StringEscapeUtils.unescapeHtml(contentHtml);//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|br|li|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果

        if(contentHtml.length() < 384) return false;//太短

        p.setContent(contentHtml);
        if (!paging && isPaging()) {
            mergePage(p);
        }
        log.debug("*****extractorContent  success*****");
        return true;
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        if (elementTitle == null){
            log.error("extractorTitle failed skippppp");
            return false;
        }
        String title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            log.info("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
//        if (title.contains("-"))
//            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
//        else
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
//        Element elementType = (Element) context.output.get("type");
//        if (elementType == null){
//            log.debug("*****extractorType  null  skipp url:" + url);
//            return false;
//
//        }
//        String type = elementType.attr("content");
//        if(!TypeDictHelper.rightTheType(type)){
//            Map<String, String> map = new HashMap<String, String>();
//            map.put("orgType", type);
//            String moreinfo = new Gson().toJson(map);
//            p.setMoreinfo(moreinfo);
//        }
//http://animals.nationalgeographic.com/animals/mammals/aardvark/?source=A-to-Z

        String type = url.substring(url.indexOf("http") + 7, url.indexOf("nationalgeo") - 1);
        type = TypeDictHelper.getType(type, "Travel");


        p.setType(type);

        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null) {
            log.info("*****extractorLabel  failed, continue***** url:" + url);
            return true;
        }
        String label = elementLabel.attr("content");
//        String label = (String) context.output.get("label");
        if (label == null || "".equals(label.trim())) {
            log.info("*****extractorLabel  failed, continue***** url:" + url);
            return true;
        }
//        label = label.contains("China")?"China":label.contains("news")? "World": label;//news belong to World
        String[] keywords = label.split(", ");
        PriorityQueue<String> pq = new PriorityQueue<String>(10, new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split(" ").length, length2 = o2.split(" ").length;
                return length1 - length2;
            }
        });

        for(String keyword: keywords){
            keyword = keyword.replaceAll(",", "");
            int wordCount = keyword.split(" ").length;
            if(wordCount <= 3) pq.add(keyword);
        }
        StringBuilder sb = new StringBuilder();
        while(!pq.isEmpty()){
            sb.append(pq.poll());
            if(!pq.isEmpty()) sb.append(", ");
        }
        p.setLabel(sb.toString());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
//        log.debug("*****extractorTime*****");
//        Element elementTime = (Element) context.output.get("time");
//        if (elementTime == null){//business版head meta里没有时间
//            log.error("can't extract Time, skip url:" + url);
//            return false;
//
//        }
//        String time = elementTime.attr("content");
//        if (time == null || "".equals(time.trim())) {
//            log.info("*****extractorTime  failed***** url:" + url);
//            return false;
//        }
////2015-09-12 08:25
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX", Locale.US);
//        Date date;
//        try {
//            date = format.parse(time.trim());
//        } catch (ParseException e) {
//
//            log.info("*****extractorTime format.parse  failed***** url:" + url);
//            return false;
//        }
//        if (System.currentTimeMillis() - date.getTime() > Long.MAX_VALUE >> 25) {
//            log.debug("*****extractorTime  out of date*****");
//            return false;
//        }
//        p.setTime(new Timestamp(date.getTime() + 13 * 60 * 60 * 1000).toString());//-0500纽约时间 +13h是 cst北京时间
        p.setTime(new Timestamp(System.currentTimeMillis()).toString());
        log.debug("*****extractorTime  success*****");
        return true;
    }

    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){//business版head meta里没有时间
            log.error("can't extract desc, continue + url : " + url);
            return true;
        }
        String description = elementTime.attr("content");
        if (description == null || "".equals(description.trim())) {
            log.info("*****extractor Desc  failed***** url:" + url);
            return true;
        }

        description = StringEscapeUtils.unescapeHtml(description);
        p.setDescription(description);

        return true;
    }

    public boolean isPaging() {
////        Elements div2 = doc.select("div[id=\"content-main\"]");
//        Elements div2 = content.select(".nav-article-pages");
//        if(div2 == null){
//            log.info("one page, continue");
//            return  false;
//        }
//
//        Elements div = div2.select("a");
//        if (div == null || div.size() < 1) {
//            log.info("one page 2, continue");
//            return false;
//        }
///*        if (url.equals(a.get(0).attr("href"))) {
//            return false;
//        }*/
//        return true;
        return  false;
    }

    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            return false;
        }

        //文章题目重复出现，去除之
//        content.select("div[id=block-onix-highlight-onix-highlight-article-header]").remove();
//        content.select("div[id=block-views-article-title-block]").remove();
//        //
//        content.select("div[id=157_ArticleControl_divShareButton]").remove();
//        if(isPaging()) return true;
       /* if (host.equals(port)) return true;*/


        Elements imgs = content.select("img");
        if(imgs == null || imgs.size() < 1)
                imgs = content.select(".delayed-image-load");

        String mainImage = null;
        int width = 0;
        for (Element img : imgs) {
            try {
                String imageUrl = img.attr("data-src");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    imageUrl = img.attr("src");
                    if ("".equals(imageUrl)) {
                        img.remove();
                        continue;
                    }
                }
//                Tag imgTag = Tag.valueOf("img");
////                img.appendChild(imgTag);
//                Element newImg = new Element(imgTag, "");
//                img.appendChild(newImg);
//                img = newImg;
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
//                img.attr("style", "width:100%;");

//                int idx = imageUrl.indexOf("width");
//                if(idx > 0){
//                    imageUrl = imageUrl.substring(0, idx - 2);
//                }else{
//                    imageUrl.substring(0, imageUrl.indexOf("jpg") + 3);
//                }
//                imageUrl = imageUrl.replace("jpg", "adapt.1190.1.jpg");
//                OImageUploader uploader = new OImageUploader();
//                if (!"".equals(host) && !"".equals(port))
//                    uploader.setProxy(host, port);
//                long id = uploader.deal(imageUrl);
//                //                long id = 0;
//                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
//                int twidth = uploader.getWidth();
//                if(twidth > 300)
//                    img.attr("style", "width:100%;");
//                img.attr("src", newUrl.toString());
//
//                if (mainImage == null) {
//                    width = uploader.getWidth();
//                    mainImage = newUrl.toString();
//                }
            } catch (Exception e) {
                System.out.println(img.attr("src"));
                e.printStackTrace();
            }
        }
        Element elementImg = (Element) context.output.get("mainimage");
        if (elementImg != null){
            String tmpMainImage = elementImg.attr("content");
//            OImageUploader uploader = new OImageUploader();
//            if (!"".equals(host) && !"".equals(port))
//                uploader.setProxy(host, port);
//            long id = 0;
//            try {
//                id = uploader.deal(tmpMainImage);
//
////                long id = 0;
//                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
//                width = uploader.getWidth();
//                mainImage = newUrl.toString();
//            } catch (Exception e1) {
////                        e1.printStackTrace();
//
//            }
        }
//        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width >= 300) {

            p.setMainimage(mainImage);
            p.setStyle("large-image");

        } else {
            p.setStyle("no-image");
        }

//        } catch (Exception e) {
//            p.setStyle("no-image");
//        }
        return true;

    }

    public void mergePage(ParserPage p) {
//        log.debug("*****mergePage*****");
//        Elements div = doc.select("div[class=\"item-list\"]").select("li[class=\"pager-next\"]").select("a");
//        LinkedList<String> list = new LinkedList<String>();
//        for (Element a : div) {
//            String url = a.attr("href");
//            if (!list.contains(url)) {
//                list.add(url);
//            }
//        }
//        Elements div2 = content.select("div[id=content-main]");
//        Elements sociallinks = div2.select("div[class=social-links]");
//        if(sociallinks != null) sociallinks.remove();//去除社交网络分享栏目框
//        Elements div = div2.select("div[class=item-list");
//        if (div == null) {
//            return;
//        }
//        Elements a = div.select("li");
//
//        String nextPage = div.attr("href");
//        if(nextPage.equals("")) return;
//        CNBCExtractor extractor = new CNBCExtractor(url + "?singlepage=true");
//        extractor.init();
//        extractor.extractorAndUploadImg();
//        extractor.extractorContent(true);
//        p.setContent(extractor.getParserPage().getContent());
//
//        log.info("*****mergePage end*****");
    }

}
