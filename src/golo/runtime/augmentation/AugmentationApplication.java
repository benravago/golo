package golo.runtime.augmentation;

import static golo.runtime.augmentation.DefiningModule.Scope;

import java.util.stream.Stream;

import golo.runtime.MethodInvocation;

/**
 * Encapsulate runtime information for an augmentation application resolution.
 */
public final class AugmentationApplication {

  enum Kind {
    SIMPLE, NAMED
  }

  private final Class<?> augmentation;
  private final Class<?> target;
  private final Scope scope;
  private final Kind kind;

  AugmentationApplication(Class<?> augmentation, Class<?> target, Scope scope, Kind kind) {
    this.scope = scope;
    this.target = target;
    this.augmentation = augmentation;
    this.kind = kind;
  }

  @Override
  public String toString() {
    return String.format("AugmentationApplication<%s,%s,%s,%s>", augmentation, target, scope, kind);
  }

  public Stream<AugmentationMethod> methodsMaching(MethodInvocation invocation) {
    if (augmentation == null) {
      return Stream.empty();
    }
    return Stream.of(augmentation.getMethods()).filter(invocation::match)
        .map(method -> new AugmentationMethod(kind, scope, target, method));
  }
}
