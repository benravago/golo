package golo.lang.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>@DecoratedBy</code> is used to define the reference to the decorator on a decorated function.
 *
 * Mainly used for internal stuff, this annotation can be useful to create decorated function in Java.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DecoratedBy {
  /**
   * This is the reference to the decorator function.
   *
   * @return the reference to the decorator function.
   */
  String value();
}
