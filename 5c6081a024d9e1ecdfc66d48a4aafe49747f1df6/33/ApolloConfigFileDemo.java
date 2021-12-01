import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigFileDemo {
  private static final Logger logger = LoggerFactory.getLogger(ApolloConfigDemo.class);
  private ConfigFile configFile;
  private String namespacePrefix = "application";

  public ApolloConfigFileDemo() {
    configFile = ConfigService.getConfigFile(namespacePrefix, ConfigFileFormat.XML);
  }

  private void print() {
    if (!configFile.hasContent()) {
      System.out.println("No config file content found for " + namespacePrefix);
      return;
    }
    System.out.println("=== Config File Content for " + namespacePrefix + " is as follows: ");
    System.out.println(configFile.getContent());
  }

  public static void main(String[] args) throws IOException {
    ApolloConfigFileDemo apolloConfigFileDemo = new ApolloConfigFileDemo();
    System.out.println(
        "Apollo Config File Demo. Please input print to get the config file content.");
    while (true) {
      System.out.print("> ");
      String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
      if (input == null || input.length() == 0) {
        continue;
      }
      input = input.trim();
      if (input.equalsIgnoreCase("print")) {
        apolloConfigFileDemo.print();
      }
      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
    }
  }
}
