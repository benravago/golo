package golo.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>@WithCaller</code> is used to define a function that takes the caller class as first argument.
 *
 * <p>The caller will be injected by the runtime, and will not be present in the code using the function. The annotated
 * function must have a class as first parameter.
 *
 * <p>Mainly used for internal stuff.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface WithCaller {
}
