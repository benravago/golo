package petit.parser.utils;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import petit.parser.PetitParser;

/**
 * Profiles the run-time of parsers.
 */
public class Profiler {
  private Profiler() {}

  /**
   * Returns a parser that calls the provided consumer with a {@link Profile} of every parser.
   */
  public static PetitParser on(PetitParser source, Consumer<Profile> consumer) {
    var builders = new LinkedHashMap<PetitParser, ProfileBuilder>();
    return Mirror.of(source).transform(parser -> {
      var builder = new ProfileBuilder(parser);
      builders.put(parser, builder);
      return parser.callCC((continuation, context) -> {
        builder.start();
        var result = continuation.apply(context);
        builder.stop();
        return result;
      });
    }).callCC((continuation, context) -> {
      builders.values().forEach(ProfileBuilder::reset);
      var result = continuation.apply(context);
      builders.values().stream().map(ProfileBuilder::build).forEach(consumer);
      return result;
    });
  }

  /**
   * The profile information about a parser.
   */
  public static class Profile {

    /**
     * The parser being profiled.
     */
    public final PetitParser parser;

    /**
     * The number of times the parser got activated on the top-level.
     */
    public final int activationCount;

    /**
     * The number of times the parser got activated in total (including nested calls).
     */
    public final int totalActivationCount;

    /**
     * The total time this parser or any of its children has been active.
     */
    public final long elapsedNanoseconds;

    private Profile(PetitParser parser, int activationCount, int totalActivationCount, long elapsedNanoseconds) {
      this.parser = parser;
      this.activationCount = activationCount;
      this.totalActivationCount = totalActivationCount;
      this.elapsedNanoseconds = elapsedNanoseconds;
    }

    @Override
    public String toString() {
      return activationCount + "\t" + totalActivationCount + "\t" + elapsedNanoseconds + "\t" + parser;
    }
  }

  /**
   * Internal builder for profile information.
   */
  private static class ProfileBuilder {

    private final PetitParser parser;

    private int activationCount;
    private int totalActivationCount;
    private long elapsedNanoseconds;
    private int nestingLevel;
    private long startTime;

    private ProfileBuilder(PetitParser parser) {
      this.parser = parser;
    }

    private void reset() {
      activationCount = 0;
      totalActivationCount = 0;
      elapsedNanoseconds = 0;
      nestingLevel = 0;
      startTime = 0;
    }

    private void start() {
      nestingLevel++;
      totalActivationCount++;
      if (nestingLevel == 1) {
        activationCount++;
        startTime = System.nanoTime();
      }
    }

    private void stop() {
      if (nestingLevel == 1) {
        elapsedNanoseconds += System.nanoTime() - startTime;
      }
      nestingLevel--;
    }

    private Profile build() {
      return new Profile(parser, activationCount, totalActivationCount, elapsedNanoseconds);
    }
  }

}
