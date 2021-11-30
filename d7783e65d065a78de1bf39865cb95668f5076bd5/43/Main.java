package com.asynch.app;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.asynch.crawl.AsynchScrapper;
import com.asynch.crawl.FutureScrapper;
import com.asynch.crawl.ParallelScrapper;

/**
 * Created by kkishore on 8/4/16.
 */
public class Main {

	public static void main(String[] ags) throws InterruptedException,
			IOException {
		final String file = "Links.txt";

		System.out.println("Starting Asynch CompletableFutures : \n\n\n");
		ExecutorService executor = Executors.newFixedThreadPool(60);
		final AsynchScrapper asyncScrapper = new AsynchScrapper(file, executor);
		asyncScrapper.process();

		System.out.println("\n\n\n");
		System.out.println("Starting Future Scrapper : \n\n\n");
		final FutureScrapper futureScrapper = new FutureScrapper(file, executor);
		futureScrapper.process();

		System.out.println("\n\n\n");
		System.out.println("Starting Parallel Streams : \n\n\n");
		final ParallelScrapper parallelScrapper = new ParallelScrapper(file);
		parallelScrapper.process();
		executor.shutdown();

	}
}
