package eu.excitementproject.eop.redis;


/**
 * Runs a Redis similarity resources on local host, given Redis file and port
 * 
 * Assumption: There's a redis dir in the running directory, contain redis-server binary file and redis.conf template file
 * 
 * @author Meni Adler
 * @since Feb 11, 2014
 *
 */
public class RunRedisResource {
	public static void main(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.out.println("Usage: java eu.excitementproject.eop.redis.RunRedisResource <redis file name> <port>");
			System.exit(0);
		}
		String redisFile = args[0];
		int port = Integer.parseInt(args[1].trim());
		String redisConfFile = BasicRedisRunner.generateConfigurationFile(redisFile, port);
		Runtime.getRuntime().exec(new String[]{BasicRedisRunner.DEFAULT_REDIS_BIN_DIR + "/" + BasicRedisRunner.getRedisServerCmd(),redisConfFile});		
	}
}
