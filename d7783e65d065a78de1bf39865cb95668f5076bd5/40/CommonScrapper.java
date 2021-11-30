package com.asynch.crawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.asynch.common.Article;
import com.asynch.common.Result;
import com.asynch.common.Tuple;
import com.asynch.error.ScrapperException;
import com.asynch.interfaces.IScrapper;

public abstract class CommonScrapper implements IScrapper {

	public Tuple getPageSource(final String url) {
		final Tuple tuple = new Tuple();
		try {
			final String html = Jsoup.connect(url).timeout(70000).get().html();
			tuple.setUrl(url);
			tuple.setPageSource(html);
		} catch (Exception e) {
			tuple.setThrowable(new ScrapperException("Problem While Fetching URL : "+url, e));
		}
		return tuple;
	}

	@Override
	public Article fetchArticle(final Tuple tuple) {
		final Article article = new Article();
		if(tuple.hasError()){
			article.setThrowable(tuple.getThrowable());
		}else{
			final Document document = Jsoup.parse(tuple.getPageSource());
			article.setUrl(tuple.getUrl());
			article.setRawHtml(tuple.getPageSource());
			article.setCleanContent(document.text());
			article.setTitle(document.title());
		}		
		return article;
				
	}

	@Override
	public Result getResult(final Article article) {
		final Result result = new Result(article);
		if(article.hasError()){
			result.setThrowable(article.getThrowable());
		}else{
			//TODO Add Logger			
		}
		return result;
	}

	@Override
	public abstract void process();

}
