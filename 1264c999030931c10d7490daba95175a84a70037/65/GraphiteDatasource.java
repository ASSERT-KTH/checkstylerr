package com.intuit.tank.persistence.databases;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.intuit.tank.reporting.databases.IDatabase;
import com.intuit.tank.reporting.databases.Item;
import com.intuit.tank.reporting.databases.PagedDatabaseResult;
import com.intuit.tank.reporting.databases.TankDatabaseType;
import com.intuit.tank.results.TankResult;
import com.intuit.tank.vm.settings.TankConfig;

/**
 * GraphiteDatasource
 * 
 * @author Kevin McGoldrick
 * 
 */
public class GraphiteDatasource implements IDatabase {
    private static final Logger LOG = LogManager.getLogger(GraphiteDatasource.class);
	
	private String enviornemnt = "qa";
	private String graphiteHost = "doubleshot.internal.perf.a.intuit.com";
	private int graphitePort = 2003;
    private int interval = 15; // SECONDS
    
    private TankConfig config = new TankConfig();
	private HierarchicalConfiguration resultsProviderConfig = config.getVmManagerConfig().getResultsProviderConfig();

	@Override
	public void createTable(String tableName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteTable(String tableName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteForJob(String tableName, String jobId, boolean asynch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasTable(String tableName) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasJobData(String tableName, String jobId) {
		// TODO Auto-generated method stub
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTimingResults(String tableName, List<TankResult> results, boolean asynch) {
        if (resultsProviderConfig != null) {
            try {
            	enviornemnt = config.getInstanceName();
            	graphiteHost = resultsProviderConfig.getString("graphiteHost");
            	String s = resultsProviderConfig.getString("graphitePort");
                if (NumberUtils.isDigits(s)) {
                	graphitePort = Integer.parseInt(s);
                }
            } catch (Exception e) {
                LOG.error("Failed to get Graphite parameters " + e.toString());
            }
        }
		Collections.sort(results);
		try {
			Socket socket = new Socket(graphiteHost, graphitePort);
			OutputStream s = socket.getOutputStream();
			PrintWriter out = new PrintWriter(s, true);
			String requestName = "";
			String jobId = "";
			long sum = 0, max = 0, l = 0;
			int count = 0;
			for (TankResult metric: results) {
				if (metric.getRequestName().equalsIgnoreCase(requestName)) {
					max = Math.max(max, metric.getResponseTime());
					sum += metric.getResponseTime();
					count++;
				} else if (count != 0) {
					long average = sum / count;
					l = metric.getTimeStamp().getTime() / 1000;
					int tps = count / interval;
					out.printf("tank.%s.%s.%s.ResponseTime.AVG %d %d%n", enviornemnt, jobId, requestName, average, l );
					out.printf("tank.%s.%s.%s.ResponseTime.MAX %d %d%n", enviornemnt, jobId, requestName, max, l );
					out.printf("tank.%s.%s.%s.TPS %d %d%n", enviornemnt, jobId, requestName, tps, l );
					requestName = metric.getRequestName();
					jobId = metric.getJobId();
					sum = metric.getResponseTime();
					count = 1;
				} else { // Handles the first time through //
					requestName = metric.getRequestName();
					jobId = metric.getJobId();
					sum = metric.getResponseTime();
					count = 1;
				}
			}
			long average = sum / count;
			int tps = count / interval;
			out.printf("tank.%s.%s.%s.ResponseTime.AVG %d %d%n", enviornemnt, jobId, requestName, average, l );
			out.printf("tank.%s.%s.%s.ResponseTime.MAX %d %d%n", enviornemnt, jobId, requestName, max, l );
			out.printf("tank.%s.%s.%s.TPS %d %d%n", enviornemnt, jobId, requestName, tps, l );
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			LOG.error("Unknown host: " + graphiteHost);
		} catch (IOException e) {
			LOG.error("Error while writing data to graphite: " + e.getMessage(), e);
		} catch (Exception e) {
			LOG.error("Error: " + e.getMessage(), e);
		}
	}

	@Override
	public Set<String> getTables(String regex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addItems(String tableName, List<Item> items, boolean asynch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Item> getItems(String tableName, String minRange, String maxRange, String instanceId, String... jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PagedDatabaseResult getPagedItems(String tableName,
			Object nextToken, String minRange, String maxRange,
			String instanceId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDatabaseName(TankDatabaseType type, String jobId) {
		// TODO Auto-generated method stub
		return graphiteHost;
	}

}
