package lib.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)

@ArgumentsSource(PathArgumentsProvider.class)

public @interface PathSource {
  String value() default "."; // see java.nio.file.Files::walk(Path)
  String match() default "glob:**"; // see java.nio.file.FileSystem::getPathMatcher(String)
}
