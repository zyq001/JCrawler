package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.util.Configuration;

/**
 * Created by zangyq on 2016/4/27.
 */
public class BBCPicsCrawler extends BaseCrawler {
    public BBCPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public BBCPicsCrawler(Configuration conf) {
        super(conf);
    }

    public BBCPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new BBCPicsExtractor(page);
    }


}
