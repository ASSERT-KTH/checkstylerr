package com.asynch.crawl;

import java.io.IOException;
import java.util.List;

import com.asynch.util.CommonUtils;

public class SimpleScrapper extends CommonScrapper{
	
	private final List<String> urlList;
	
	public SimpleScrapper(final String urlFile) throws IOException {
		this.urlList = CommonUtils.getLinks(urlFile);
	}

	@Override
	public void process() {
		urlList.stream()
			   .map(url -> getPageSource(url))
			   .map(pageSource -> fetchArticle(pageSource))
			   .map(article -> getResult(article))
			   .forEach(System.out::println);		
	}

}
