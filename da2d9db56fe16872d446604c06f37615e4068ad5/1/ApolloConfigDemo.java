import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigDemo {
  private static final Logger logger = LoggerFactory.getLogger(ApolloConfigDemo.class);
  private Config config;

  public ApolloConfigDemo() {
    config = ConfigService.getConfig("BUS.test0711");
    config.addChangeListener(new ConfigChangeListener() {
      @Override
      public void onChange(ConfigChangeEvent changeEvent) {
        logger.info("Changes for namespace {}", changeEvent.getNamespace());
        for (String key : changeEvent.changedKeys()) {
          ConfigChange change = changeEvent.getChange(key);
          logger.info("Change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
              change.getPropertyName(), change.getOldValue(), change.getNewValue(),
              change.getChangeType());
        }
      }
    });
  }

  private String getConfig(String key) {
    String result = config.getProperty(key, "undefined");
    logger.info(String.format("Loading key: %s with value: %s", key, result));
    return result;
  }

  public static void main(String[] args) throws IOException {
    ApolloConfigDemo apolloConfigDemo = new ApolloConfigDemo();
    System.out.println(
        "Apollo Config Demo. Please input key to get the value.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
      if (input == null || input.length() == 0) {
        continue;
      }
      input = input.trim();
      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
      apolloConfigDemo.getConfig(input);
    }
  }
}
