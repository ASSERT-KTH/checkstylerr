package com.asynch.interfaces;

import com.asynch.common.Article;
import com.asynch.common.Result;
import com.asynch.common.Tuple;

public interface IScrapper {
	
	public void process();
	
	public Tuple getPageSource(final String url) throws Exception;
	
	public Article fetchArticle(final Tuple tuple);
	
	public Result getResult(final Article article);

}
