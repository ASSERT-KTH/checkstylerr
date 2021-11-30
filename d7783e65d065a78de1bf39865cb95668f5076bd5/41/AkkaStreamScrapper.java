package com.asynch.crawl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import com.asynch.common.Result;
import com.asynch.util.CommonUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


public class AkkaStreamScrapper extends CommonScrapper {

	private final List<String> urlList;
	private final ExecutorService executor;
	private final ActorSystem actorSystem;
	private final ActorMaterializer actorMaterializer;

	public AkkaStreamScrapper(final String file, final ExecutorService executor) throws IOException {
		this.urlList = CommonUtils.getLinks(file);
		this.executor = executor;
		final Config config = ConfigFactory.parseFile(new File("src/main/resources/application.conf"));
		actorSystem = ActorSystem.create("AkkaStreamScrapper", config.getConfig("test"));
		actorMaterializer = ActorMaterializer.create(actorSystem);
	}

	public void process() {
		Source<String, NotUsed> source = Source.from(urlList);		
		final int parallelism = 32;
		Flow<String, Result, NotUsed> stageAsync = Flow.of(String.class)
			.mapAsyncUnordered(parallelism, (value) -> CompletableFuture.supplyAsync(() -> getPageSource(value), executor))
			.mapAsyncUnordered(parallelism, (tuple) -> CompletableFuture.supplyAsync(() -> fetchArticle(tuple), executor))
			.mapAsyncUnordered(parallelism, (article) -> CompletableFuture.supplyAsync(() -> getResult(article), executor));
		CompletionStage<Done> graph = source.via(stageAsync).runWith(Sink.foreach(System.out::println),
				actorMaterializer);
		
		graph.whenComplete((result, error) -> {
			System.out.println(result);
			invokeDone();
		});
	}
	
	private void invokeDone(){
        actorSystem.terminate();
		executor.shutdown();
	}
}
