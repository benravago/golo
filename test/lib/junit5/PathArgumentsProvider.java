package lib.junit5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class PathArgumentsProvider implements AnnotationConsumer<PathSource>, ArgumentsProvider {

  private String directory;
  private String syntaxAndPattern;

  @Override // for AnnotationConsumer
  public void accept(PathSource annotation) {
    directory = annotation.value();
    syntaxAndPattern = annotation.match();

    if (!syntaxAndPattern.startsWith("glob:") && !syntaxAndPattern.startsWith("regex:")) {
      syntaxAndPattern = "glob:"+syntaxAndPattern;
    }
  }

  @Override // ArgumentsProvider
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return paths().map(Arguments::of);
  }

  Stream<Path> paths() {
    try {
      var matcher = FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
      return Files.walk(Paths.get(directory)).filter(Files::isRegularFile).filter(matcher::matches);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
