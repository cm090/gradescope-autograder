package rhit;

import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length > 0) {
      new FileToolCli(args);
    } else {
      new FileToolGui();
    }
  }
}