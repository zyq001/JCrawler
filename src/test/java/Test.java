import cn.edu.hfut.dmic.webcollector.util.JsoupUtils;
import com.dict.souplang.Context;
import com.dict.souplang.SoupLang;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by liuhl on 15-8-16.
 */
public class Test {
    public static void main(String[] args) throws Exception{

//        News news = ContentExtractor.getNewsByUrl("http://www.huxiu.com/article/121959/1.html");
        String url = "http://www.chinadaily.com.cn/world/2015victoryanniv/2015-08/14/content_21611446.htm";
        Document doc= Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0")
                .get();
        JsoupUtils.makeAbs(doc,url);
        SoupLang soupLang=new SoupLang(ClassLoader.getSystemResourceAsStream("DictRule.xml"));
        Context context=soupLang.extract(doc);
        System.out.println(context);
    }
}
