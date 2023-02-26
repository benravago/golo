package golo.runtime.spi;

import java.util.ServiceLoader;

public class Main {

  // TODO: make a new service loader system

  public static void main(String... args) throws Throwable {
    ServiceLoader<CliCommand> commands = ServiceLoader.load(CliCommand.class);
    for (CliCommand command : commands) {
      // cmd.addCommand(command);
    }
  }
}
