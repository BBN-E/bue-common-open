package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import java.lang.annotation.Annotation;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A module which has the properties of {@link AbstractParameterizedModule}, except it also
 * includes an annotation class (which is also used in determinsing its identity for hashCode and
 * equals).
 *
 * You might wish to use this if you are providing a generic capability (e.g. a string normalizer)
 * which you expect may be bound to multiple binding sites by multiple instances of the same module.
 *
 * @author Ryan Gabbard
 */
public abstract class AbstractAnnotatedParameterizedModule extends AbstractParameterizedModule {

  private final Class<? extends Annotation> annotation;

  protected AbstractAnnotatedParameterizedModule(final Parameters parameters,
      Class<? extends Annotation> annotation) {
    super(parameters);
    this.annotation = checkNotNull(annotation);
  }


  protected final Class<? extends Annotation> annotation() {
    return annotation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), annotation);
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj)
        && annotation.equals(((AbstractAnnotatedParameterizedModule) obj).annotation());
  }
}
