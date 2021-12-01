import com.ctrip.apollo.client.ApolloEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloConfigDemo {
  private ApolloEnvironment env;

  public ApolloConfigDemo() {
    env = ApolloEnvironment.getInstance();
    env.init();
  }

  private String getConfig(String key) {
    String result = env.getProperty(key, "undefined");
    System.out.println(String.format("Loading key: %s with value: %s", key, result));
    return result;
  }

  public static void main(String[] args) throws IOException {
    ApolloConfigDemo apolloConfigDemo = new ApolloConfigDemo();
    System.out.println(
        "Apollo Config Demo. Please input key to get the value.");
    while (true) {
      System.out.print("> ");
      String input = (new BufferedReader(new InputStreamReader(System.in)).readLine()).trim();
      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
      apolloConfigDemo.getConfig(input);
    }
  }
}
